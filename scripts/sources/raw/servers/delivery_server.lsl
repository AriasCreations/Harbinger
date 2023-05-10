#include "Common.lsl"

integer PLUGIN_LIGHTS = 1;
integer PLUGIN_LIGHTS_ON = 2;
integer PLUGIN_LIGHTS_OFF = 3;

integer PLUGIN_DISCORD = 4;

discord(string msg)
{
    llMessageLinked(LINK_SET, PLUGIN_DISCORD, msg, "");
}

lights_on()
{
    llMessageLinked(LINK_SET, PLUGIN_LIGHTS_ON, "","");
}
lights_off()
{
    llMessageLinked(LINK_SET, PLUGIN_LIGHTS_OFF, "","");
}

lights(vector vColor)
{
    llMessageLinked(LINK_SET, PLUGIN_LIGHTS, (string)vColor, "");
}

string g_sURL;

default
{
    state_entry()
    {

        lights_off();
        discord("Server "+CLIENT_NICK+" is now attempting to obtain a URL");
        llSleep(5);
        lights(<1,0,0>);
        lights_on();

        llSleep(2);
        UpdateDSRequest(NULL, llRequestURL(), SetDSMeta(["obtain_url"]));
    }
    http_request(key kID, string sMethod, string sBody)
    {
        if(~HasDSRequest(kID))
        {
            DeleteDSReq(kID); // Only one should be obtain_url

            if(sMethod == URL_REQUEST_GRANTED)
            {
                g_sURL = sBody;
                lights(<1,1,0>);
                discord("Server ["+CLIENT_NICK+"] has obtained a URL");
                discord("Registering ["+CLIENT_NICK+"] with Harbinger");

                UpdateDSRequest(NULL, llHTTPRequest(API_ENDPOINT, [HTTP_METHOD, "POST", HTTP_MIMETYPE, "application/json"], 
                    llList2Json(JSON_OBJECT, ["type", "servers", "sub_command", "register", "name", CLIENT_NICK, "url", g_sURL]);
                ), SetDSMeta(["register_server"]));
            }
        }
    }
}