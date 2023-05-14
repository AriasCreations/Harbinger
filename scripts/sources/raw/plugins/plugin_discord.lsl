#include "Common.lsl"


integer PLUGIN_DISCORD = 4;


default
{
    link_message(integer iSender, integer iNum, string sMsg, key kID)
    {
        if(iNum == PLUGIN_DISCORD)
        {
            llHTTPRequest(DISCORD_ENDPOINT, [HTTP_METHOD, "POST", HTTP_MIMETYPE, "application/json"], llJsonSetValue(sMsg, ["nick"], DEST_HOOK_NICK));
        }
    }
}