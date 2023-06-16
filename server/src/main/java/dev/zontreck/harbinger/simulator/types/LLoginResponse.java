package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.ariaslib.xmlrpc.MethodResponse;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.events.GridFeatureQueryEvent;
import dev.zontreck.harbinger.simulator.services.grid.PresenceService;
import dev.zontreck.harbinger.utils.DataUtils;

import javax.xml.crypto.Data;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

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

	public LLoginResponseCodes code = LLoginResponseCodes.False;
	public String Reason;
	public String Message;

	public String agent_id;
	public String First_Name;
	public String Last_Name;

	public Boolean UnlimitedGroups=false;
	public int MaximumGroups=0;


	public Account cached;

	public Location StartLocation;
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
		code = LLoginResponseCodes.OK;
		Reason = code.reason;
		Message = "Welcome";


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
			cached.UserLevel++;
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

	public void setLocationRequest(String last)
	{
		if(cached.UserLevel == 2)
		{
			StartLocation = cached.LastLocation;
		}else {
			StartLocation = new Location (last);
		}
	}

	public Map<String,Object> Optionals = new HashMap<> (  );
	public void setOptionalQuery(String[] options)
	{
		GridFeatureQueryEvent GFQE = new GridFeatureQueryEvent ( List.of ( options), this );
		if( EventBus.BUS.post ( GFQE) )
		{
			// We have some optional parameters to copy out of the event
			Optionals = GFQE.reply;
		}
	}

	public MethodResponse generateResponse ( ) {
		MethodResponse resp = new MethodResponse ( );
		resp.parameters.put ( "first_name", First_Name );
		resp.parameters.put ( "agent_id", agent_id);
		resp.parameters.put ( "region_size_x", 256 );
		resp.parameters.put ( "region_size_y", 256 );
		resp.parameters.put ( "look_at", "[r0,r0,r0]" ); // TODO: Set this to the actual avatar's lookat
		resp.parameters.put ( "agent_access_max", "A" ); // For this one, we aren't SL, but this can be derived from having more account levels. It can be set to G, M, or A, depending on the user's age. Harbinger isn't intended to be accessed by non-adults, so this is set to A, for now.
		resp.parameters.put ( "map-server-url", Persist.simulatorSettings.BASE_URL+"/simulation/map" );


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
			} else {
				// Login should be good to proceed, lets make a session ID

				Presence pres = new Presence ( cached );
				PresenceService.registerPresence ( pres );

				resp.parameters.put ( "secure_session_id", pres.SessionID.toString () );

				// The proper X and Y will be calculated inside the Presence constructor.
				resp.parameters.put ( "region_x", pres.GlobalX );
				resp.parameters.put ( "region_y", pres.GlobalY );
				resp.parameters.put ( "circuit_code", pres.CircuitCode );
				for (
						Map.Entry<String, Object> reply :
						Optionals.entrySet()
				) {
					resp.parameters.put ( reply.getKey (), reply.getValue () );
				}
				String FQDN = Persist.simulatorSettings.BASE_URL;
				if(FQDN.contains ( "://" )){
					FQDN = FQDN.substring ( FQDN.indexOf ( "://" )+3 );
				}
				resp.parameters.put ( "inventory_host", FQDN );


				resp.parameters.put ( "start_location", StartLocation.interpret(cached) );
				resp.parameters.put ( "seed_capability", Persist.simulatorSettings.BASE_URL + "/simulation/CAP/" + pres.SessionID.toString () );

				resp.parameters.put ( "sim_ip", Persist.HARBINGER_EXTERNAL_IP );
				if(Persist.serverSettings.ExternalPortNumberSet)
					resp.parameters.put ( "sim_port", Persist.serverSettings.ExternalPortNumber );
				else resp.parameters.put ( "sim_port", Persist.serverSettings.port );
			}

		}

		resp.parameters.put ( "login", code == LLoginResponseCodes.OK ? Login : false );


		if(!Login)
			resp.parameters.put ( "reason", Reason );
		resp.parameters.put("message", Message);

		resp.parameters.put ( "seconds_since_epoch", Instant.EPOCH.getEpochSecond () );
		resp.parameters.put ( "agent_access", "M" );
		resp.parameters.putAll ( Optionals );



		return resp;
	}
}
