package dev.zontreck.harbinger.commands;

import org.json.JSONObject;

public enum CommandResponse
{
	NOARG("E-NOARG", "Not enough arguments supplied"),
	OK("E-OK", "success"),
	FAIL("E-FAIL", "fail"),
	DENY("E-DENY","rejected");


	public String code;
	public String msg;

	CommandResponse(String code, String msg)
	{
		this.code=code;
		this.msg=msg;
	}


	public void addToResponse( JSONObject obj, String custom )
	{
		obj.put("reason", msg);
		obj.put("code", code);
		obj.put("output", custom);
	}

}
