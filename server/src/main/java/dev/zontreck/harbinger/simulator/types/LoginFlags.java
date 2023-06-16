package dev.zontreck.harbinger.simulator.types;

import java.time.Instant;
import java.util.*;

public class LoginFlags {
	public boolean StipendSinceLogin = false;
	public boolean EverLoggedIn = false;
	public long SecondsSinceEpoch;
	public boolean DaylightSavings = false;
	public boolean Gendered = false;

	public LoginFlags(Account user)
	{
		if(user.UserLevel == 2) EverLoggedIn = false;
		if(user.PendingStipend) StipendSinceLogin=true;
		SecondsSinceEpoch = Instant.now ( ).getEpochSecond ();

	}

	public List<Map<?,?>> save()
	{
		Map<String,Object> obj = new HashMap<> (  );
		obj.put ( "stipend_since_login", YN(StipendSinceLogin) );
		obj.put("ever_logged_in", YN(EverLoggedIn));
		obj.put("seconds_since_epoch", SecondsSinceEpoch);
		obj.put("daylight_savings", YN(DaylightSavings));
		obj.put("gendered", YN(Gendered));

		List<Map<?,?>> ret = new ArrayList<> (  );
		ret.add ( obj );

		return ret;
	}

	private String YN(boolean test)
	{
		if(test)return "Y";
		else return "N";
	}
}
