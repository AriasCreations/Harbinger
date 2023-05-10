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
    UpdateDSRequest(NULL, llHTTPRequest(API_ENDPOINT, [HTTP_METHOD, "POST", HTTP_MIMETYPE, "application/json"], llList2Json(JSON_OBJECT, ["type", "support", "sub_command", "add", "id", g_kID, "psk", PRESHAREDKEY, "name", g_sName])), SetDSMeta(["submit_api_call"]));
}
default
{
    state_entry()
    {
        llOwnerSay("I am ready. You are to be added as support with level: "+SUPPORT_LEVEL+"\n\n[ Click me to proceed and accept ]");
    }
    touch_start(integer n)
    {
        if(llDetectedKey(0)!=llGetOwner())return;

        g_kID = llGetOwner();
        g_sName = llGetUsername(llGetOwner());
        submit();
    }

    http_response(key kID, integer iStat, list lMeta, string sBody)
    {
        if(~HasDSRequest(kID))
        {
            list lMeta = GetMetaList(kID);
            if(llList2String(lMeta,0) == "submit_api_call")
            {
                if(iStat!=200)
                {
                    llOwnerSay("ERROR: Harbinger returned a 404 page. Endpoint not found\n \n"+sBody);
                    return;
                }
                // Call was submitted
                string result = llJsonGetValue(sBody, ["result"]);
                llOwnerSay("HARBINGER> "+result);

                llRemoveInventory(llGetScriptName());
            }
        }
    }
}