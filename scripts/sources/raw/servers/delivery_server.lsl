#include "Common.lsl"

integer PLUGIN_LIGHTS = 1;
integer PLUGIN_LIGHTS_ON = 2;
integer PLUGIN_LIGHTS_OFF = 3;

integer PLUGIN_DISCORD = 4;

discord(string title, string message, string color)
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
        discord(CLIENT_NICK, "Server is now attempting to obtain a URL", "yellow");
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
                discord(CLIENT_NICK, "URL obtained", "dark_green");
                discord(CLIENT_NICK, "Registering with Harbinger", "yellow");

                UpdateDSRequest(NULL, llHTTPRequest(API_ENDPOINT, [HTTP_METHOD, "POST", HTTP_MIMETYPE, "application/json"], 
                    llList2Json(JSON_OBJECT, ["type", "servers", "sub_command", "register", "name", CLIENT_NICK, "url", g_sURL]);
                ), SetDSMeta(["register_server"]));
            }
        }else {
            key kTarget = llJsonGetValue(sBody, ["id"]);
            string sItem = llJsonGetValue(sBody, ["item"]);
            UpdateDSRequest(NULL, llRequestUsername(kTarget), SetDSMeta(["get_name", kID, sItem, kTarget]));
        }
    }
    dataserver(key kID, string sData)
    {
        if(~HasDSRequest(kID))
        {
            list lMeta = GetMetaList(kID);
            DeleteDSReq(kID);
            if(llList2String(lMeta,0) == "get_name")
            {
                key kHTTP = llList2String(lMeta,1);
                string sItem = llList2String(lMeta,2);
                key kTarget = llList2String(lMeta,3);
                if(llGetInventoryType(sItem) == INVENTORY_NONE)
                {
                    discord(CLIENT_NICK, "Cannot send '"+sItem+"' to '"+sData+"' because it was not found", "dark_red");

                    llHTTPResponse(kHTTP, 404, "Item not found");
                }else {
                    llHTTPResponse(kHTTP, 200, "Sending");
                    discord(CLIENT_NICK, "Sending item '"+sItem+"' to '"+sData+"'", "dark_green");

                    llGiveInventory(kTarget, sItem);
                }
            }
        }
    }

    http_response(key kID, integer iStat, list lMeta, string sBody)
    {
        if(~HasDSRequest(kID))
        {
            list lMeta = GetMetaList(sBody);
            DeleteDSReq(kID);
            if(llList2String(lMeta,0) == "register_server")
            {
                if(llJsonGetValue(sBody, ["success"])=="true")
                {
                    lights(<0,0.5,0>);
                    discord(CLIENT_NICK, "Registered with Harbinger", "dark_green");
                }else {
                    lights(<0.5,0,0>);
                    discord(CLIENT_NICK, "Failed to register with Harbinger", "dark_red");
                }
            }
        }
    }
}