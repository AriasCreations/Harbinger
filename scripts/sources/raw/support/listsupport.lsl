#include "Common.lsl"

default 
{
    state_entry()
    {

    }
    touch_start(integer n)
    {
        checkSupport();
    }

    http_response(key kID, integer iStat, list lMeta, string sBody)
    {
        if(~HasDSRequest(kID))
        {
            list lMeta = GetMetaList(kID);
            DeleteDSReq(kID);

            if(llList2String(lMeta,0)=="get_support")
            {
                list lTmp = llParseStringKeepNulls(sBody, [";;"],[]);
                list lSup = llParseString2List(llList2String(lTmp,1), ["~"],[]);

                integer i = 0;
                integer end = llGetListLength(lSup);
                for(i=0;i<end;i+=2)
                {
                    llOwnerSay("Rep: "+SLURL(llList2String(lSup,i)) + " [ Level : "+rank2Title((integer)llList2String(lSup,i+1))+" ]");
                }
            }
        }
    }
}