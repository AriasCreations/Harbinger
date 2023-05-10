#include "Common.lsl"

integer channel;
integer listener;

string g_sName;
key g_kID;


get_name()
{

    UpdateDSRequest(NULL, llRequestUsername(g_kID), SetDSMeta(["get_name"]));
}
get_id()
{

    UpdateDSRequest(NULL, llRequestUserKey(g_sName), SetDSMeta(["get_id"]));
}

submit()
{
    // Send API Request to Harbinger
    UpdateDSRequest(NULL, llHTTPRequest(API_ENDPOINT, [HTTP_METHOD, "POST", HTTP_MIMETYPE, "application/json"], llList2Json(JSON_OBJECT, ["type", "support", "sub_command", "delete", "id", g_kID, "psk", PRESHAREDKEY])), SetDSMeta(["submit_api_call"]));
}
default
{
    state_entry()
    {
        llOwnerSay("I am ready. Click me and enter the username when ready");
    }
    touch_start(integer n)
    {
        if(llDetectedKey(0)!=llGetOwner())return;

        channel = RNG();
        listener = llListen(channel, "", llGetOwner(), "");
        llTextBox(llGetOwner(), "Who do you want to remove from a support role?\n\n[ Accepts: Username, UUID, SLURL ]", channel);
    }

    http_response(key kID, integer iStat, list lMeta, string sBody)
    {
        if(~HasDSRequest(kID))
        {
            list lMeta = GetMetaList(kID);
            if(llList2String(lMeta,0) == "submit_api_call")
            {
                // Call was submitted
                if(iStat!=200)
                {
                    llOwnerSay("ERROR: Harbinger returned a 404 page. Endpoint not found\n \n"+sBody);
                    return;
                }
                string result = llJsonGetValue(sBody, ["result"]);
                llOwnerSay("HARBINGER> " +result);

                llResetScript();
            }
        }
    }

    listen(integer c,string n,key i,string m)
    {
        if(IsLikelyAvatarID(m))
        {
            // UUID Entered
            g_kID = m;
            get_name();
        }else if (~llSubStringIndex(m, "secondlife:///"))
        {
            // SLURL Entered
            g_kID = extractIDFromSLURL(m);
            get_name();
        }else {
            // Name Entered
            g_sName = m;
            get_id();
        }

        llListenRemove(listener);
    }

    dataserver( key queryid, string data )
    {
        if(~HasDSRequest(queryid))
        {
            list lMeta = GetMetaList(queryid);
            DeleteDSReq(queryid);

            if(llList2String(lMeta,0)=="get_id")
            {
                g_kID = data;
                submit();
            }else if(llList2String(lMeta,0) == "get_name")
            {
                g_sName = data;
                submit();
            }
        }
    }
}