#include "Common.lsl"

vector g_vCurrent = <0.5,0,0>; // Default is red

integer PLUGIN_LIGHTS = 1;
integer PLUGIN_LIGHTS_ON = 2;
integer PLUGIN_LIGHTS_OFF = 3;


glow(integer iOn)
{
    llSetLinkPrimitiveParams(LIGHTS_PRIM, [PRIM_GLOW, LIGHTS_FACE, fSetor(iOn, 0.25, 0)]);
}

color(vector vColor)
{
    llSetLinkColor(LIGHTS_PRIM, vColor, LIGHTS_FACE);
}

turn_off()
{
    glow(FALSE);
    color(g_vCurrent/2);
}
default
{
    state_entry()
    {
        turn_off();
    }
    link_message( integer iSender, integer iNum, string sStr, key kID )
    {
        if(iNum == PLUGIN_LIGHTS_ON)
        {
            turn_on();
        } else if(iNum == PLUGIN_LIGHTS_OFF)
        {
            turn_off();
        } else if(iNum == PLUGIN_LIGHTS)
        {
            g_vCurrent = (vector)sStr;
            color(g_vCurrent);
        }
    }
}