#include "Common.lsl"


default
{
    state_entry()
    {
        llSay(0, "Checking Harbinger server version...");
        llHTTPRequest(VERSION_ENDPOINT, [], "");
    }
    touch_start(integer n)
    {
        llSay(0, "Checking Harbinger server version...");
        llHTTPRequest(VERSION_ENDPOINT, [], "");
    }

    http_response(key kID, integer iStat, list lMeta, string sBody)
    {
        if(iStat != 200)
        {
            llSay(0, "ERROR: Harbinger failed to respond");
            llSay(0, "Server error: "+(string)iStat+"\n \n"+sBody);
            return;
        }
        list lTmp = llParseString2List(sBody, [";;", ";"],[]);
        if(llList2String(lTmp,-1)=="null")
        {
            llSay(0, "Harbinger is still starting up, try again in a moment");
        }else if(llList2String(lTmp,-1)=="")
        {
            llSay(0, "Error in server response: "+(string)iStat+"\n\n"+sBody);
        }else
        
        {
            llSay(0, "Harbinger is version "+llList2String(lTmp,-1));
        }
    }
}