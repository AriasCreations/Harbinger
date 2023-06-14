package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.ariaslib.xmlrpc.MethodResponse;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.utils.DataUtils;

import javax.xml.crypto.Data;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

public class LLoginResponse {

	public enum LLoginResponseCodes
	{
		PasswordIncorrect("key"),
		InternalError("Internal Error"),
		Critical("critical"),
		ToSNeedsSent("tos"),
		Update("update"),
		OptionalUpdate("optional"),
		PresenceIssue("presence"),
		OK("true"),
		Indeterminate("indeterminate"),
		Unknown("Unknown"),
		False("false"),
		MFAChallenge("mfa_challenge");           // This is used to display the prompt to the End-User



		public String reason;

		LLoginResponseCodes(String reason)
		{
			this.reason=reason;
		}
	}

	public boolean Login = false;

	public LLoginResponseCodes code;
	public String Reason;
	public String Message;

	public String agent_id;
	public String First_Name;
	public String Last_Name;

	public Boolean UnlimitedGroups=false;
	public int MaximumGroups=0;


	private Account cached;
	/**
	 * Pre-fills some essential information from the User Account object.
	 *
	 * @param user A user.
	 */
	public LLoginResponse ( Account user , String passwd ) {
		ApplyUserParams(user);
		if ( user.ValidatePassword ( passwd ) ) {
			setLoginSuccess ( );
		}
		else {
			setLoginFail ( "Invalid password" );
			code = LLoginResponseCodes.PasswordIncorrect;
			Reason = code.reason;

		}
	}

	public void ApplyUserParams(Account user)
	{
		agent_id = user.UserID.toString ();
		First_Name = user.First;
		Last_Name = user.Last;
		if(user.UserLevel>=150)
		{
			UnlimitedGroups=true;
		}else {
			if(user.UserLevel>=100){
				MaximumGroups = 240;
			}else if(user.UserLevel >= 50)
			{
				MaximumGroups = 180;
			}else MaximumGroups = 14;
		}

		cached=user;
	}

	public void setLoginSuccess()
	{
		Login=true;
		Reason = "";
		Message = "";


	}

	public void setToSStatus(boolean tos)
	{
		cached.HasAgreedToTermsOfService=tos;
		if(tos && cached.UserLevel == 0)
		{
			cached.UserLevel++;
		}
		if(tos)
		{
			cached.LastReadTOS = Instant.now ( ).getEpochSecond ();
		}
		cached.commit ();
	}

	public void setReadPatch(boolean patch)
	{
		cached.HasReadCriticalInfo = patch;
		if(patch && cached.UserLevel == 1)
		{
			cached.UserLevel=3;
		}
		if(patch)
		{
			cached.LastReadCritical = Instant.now ( ).getEpochSecond ();
		}
		cached.commit ();

	}

	public void setLoginFail(String reason)
	{
		Login=false;
		Reason=reason;
		Message = reason;

		code = LLoginResponseCodes.False;
	}

	public MethodResponse generateResponse ( ) {
		MethodResponse resp = new MethodResponse ( );
		if(!UnlimitedGroups)resp.parameters.put ( "max-agent-groups", MaximumGroups );
		resp.parameters.put ( "first_name", First_Name );
		resp.parameters.put ( "agent_id", agent_id);
		resp.parameters.put ( "region_size_x", 256 );
		resp.parameters.put ( "region_size_y", 256 );

		if(Login){
			// DISALLOW UNAUTHENTICATED USERS FROM ANSWERING THESE
			if(cached.UserLevel == 0 || cached.LastReadTOS < Persist.simulatorSettings.LAST_TOS_UPDATE.getEpochSecond () )
			{
				code = LLoginResponseCodes.ToSNeedsSent;
				Reason = code.reason;
				Message = DataUtils.ReadTextFile ( Path.of("tos.html").toFile ());
			} else if(cached.UserLevel == 1 || cached.LastReadCritical < Persist.simulatorSettings.LAST_PATCHNOTES_UPDATE.getEpochSecond ())
			{
				code = LLoginResponseCodes.Critical;
				Reason = code.reason;
				Message = DataUtils.ReadTextFile ( Path.of("latest.html").toFile () );
			}

		}

		resp.parameters.put ( "login", code == LLoginResponseCodes.OK ? Login : false );


		resp.parameters.put ( "reason", Reason );
		resp.parameters.put("message", Message);


		return resp;
	}
}
