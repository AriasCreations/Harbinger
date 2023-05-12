/*

    This is a collection of helper functions, some made by me, some taken from Second Life's wiki

    Where possible, if i remember the source, I have noted the origin of the helper function

*/
string API_ENDPOINT = "https://harbinger.zontreck.dev/api";
string VERSION_ENDPOINT = "https://harbinger.zontreck.dev/version";
string SUPPORT_ENDPOINT = "https://harbinger.zontreck.dev/get_support";

// VERSION NUMBER
// CHANGE ME WHEN UPDATING BUILDS
string VERSION = "0.0.0.1";
integer BUILD_NUM = 1;

#define PREALPHA "pa"
#define ALPHA "a"
#define DEV "dev"
#define BUILD "rv"
#define RC "rc"
#define BETA "b"

string compileVersion(list props)
{
    return llDumpList2String(props, "-");
}

#define RED_QUEEN "27a0ae67-30d9-4fbc-b9fb-fd388c98c202"
#define ARIA "5556d037-3990-4204-a949-73e56cd3cb06"


#define ONE_DAY ((60*60)*24)
string MakeTimeNotation(integer iSec)
{
    integer iDifference = iSec;

    integer Days = iDifference/ONE_DAY;
    iDifference  -=  (ONE_DAY*Days);
    integer Hours = iDifference/60/60;
    iDifference -= (Hours*60*60);
    integer Minutes = iDifference/60;
    iDifference -= (Minutes*60);

    list lItem = [Days,"d", Hours, "h", Minutes, "m", iDifference, "s"];
    return llDumpList2String(lItem, "");
}
string MakeTimeNotationFloated(float iSec)
{
    float iDifference = iSec;

    integer Days = llRound(iDifference/ONE_DAY);
    iDifference  -=  (ONE_DAY*Days);
    integer Hours = llRound(iDifference/60/60);
    iDifference -= (Hours*60*60);
    integer Minutes = llRound(iDifference/60);
    iDifference -= (Minutes*60);

    list lItem = [Days,"d", Hours, "h", Minutes, "m", llRound(iDifference), "s", llRound((iDifference - llRound(iDifference))*1000), "ms"];
    return llDumpList2String(lItem, "");
}

key extractIDFromSLURL(string sSLURL)
{
    list lTmp = llParseStringKeepNulls(sSLURL, ["/"],[]);
    // secondlife: / / / app / agent / id / about
    integer iLen = llGetListLength(lTmp);
    
    return llList2String(lTmp, iLen-2);
}

integer RNG()
{
    return llRound(llFrand(0xFFFFFF));
}

// Returns in the same format as Make: Days, Hours, Minutes, Seconds, TotalSeconds
list ParseTimeNotation(string sNotation)
{
    integer Days;
    integer Hours;
    integer Minutes;
    integer Seconds;
    integer TotalSec;

    list lTmp = llParseString2List(llToLower(sNotation), [], ["d", "h", "m", "s"]);
    integer i = 0;
    integer end = llGetListLength(lTmp);
    for(i=0;i<end;i+=2)
    {
        string sNote = llList2String(lTmp,i+1);
        integer iSeg = (integer)llList2String(lTmp,i);
        switch(sNote)
        {
            case "d":
            {
                Days = iSeg;
                break;
            }
            case "h":
            {
                Hours = iSeg;
                break;
            }
            case "m":
            {
                Minutes=iSeg;
                break;
            }
            case "s":
            {
                Seconds = iSeg;
                break;
            }
        }
    }


    TotalSec += Seconds;
    TotalSec += (Minutes*60);
    TotalSec += (Hours*(60*60));
    TotalSec += (Days*((60*60)*24));
    return [Days, Hours, Minutes, Seconds, TotalSec];
}


// Ranks taken from PermissionLevel.java of Harbinger Server

#define R_ADMIN 16
#define R_DEVELOPER 8
#define R_MOD 4
#define R_SUPPORT 2
#define R_CUSTOMER 1
#define R_NONE 0

string rank2Title(integer iNum)
{
    switch(iNum)
    {
        case R_ADMIN: return "Admin";
        case R_DEVELOPER: return "Developer";
        case R_MOD: return "Moderator";
        case R_SUPPORT: return "Support";
        case R_CUSTOMER: return "Customer";
        default: return "None";
    }
}

list g_lSupport;
integer g_iLastSupportCheck;
initBasicSupport()
{
    g_lSupport = [RED_QUEEN, R_ADMIN,
                    ARIA, R_DEVELOPER
    ];
}

integer checkSupportUser(key kID, integer minAccessRights)
{
    integer iIndex=llListFindList(g_lSupport, [(string)kID]);
    if(iIndex!=-1)
    {
        integer iMask  =  (integer)llList2String(g_lSupport, iIndex+1);
        if(iMask & minAccessRights)
        {
            return TRUE;
        }else return FALSE;
    }
    return FALSE;
}
// Expected return format: GetSupport;;Name~Level~Name~Level
checkSupport()
{
    UpdateDSRequest(NULL, llHTTPRequest(SUPPORT_ENDPOINT, [], ""), SetDSMeta(["get_support"]));
}


// Version Number Comparison Function
/*
    Stops at the first difference and returns the result. This function is not capable of comparing development states stored in the extra params at this time. Perhaps in the future these can just be converted to a number
    This is TODO
    RETURNS
        0   -    if both are the same
        1   -    if sCompare is greater
        -1  -    if sCompare is less than sInput
*/
integer VersionNumberCompare(string sInput, string sCompare)
{
    list l1 = llParseString2List(sInput,["-"],[]);
    list l2 = llParseString2List(sCompare,["-"],[]);
    // TODO: Add here the conversion of the extra params like Prealpha, dev, and build num to extra decimals and as numbers. PREALPHA would be 0, ALPHA would be 1.  etc.  DEV can be perhaps -1 or maybe 0 and shift prealpha to 1.
    list lInput = llParseString2List(llList2String(l1,0), ["."],[]);
    list lCompare = llParseString2List(llList2String(l2,0), ["."],[]);

    integer i=0;
    integer end = llGetListLength(lInput);

    integer iBias = 0; // Same!
    for(i=0;i<end;i++)
    {
        integer iComp = (integer)llList2String(lCompare,i);
        integer iCur = (integer)llList2String(lInput,i);

        if(iComp < iCur)return -1;
        if(iComp > iCur)return 1;
    }

    return 0;
}
/*
END VERSION CODE
*/



string ToBinary(integer num)
{
    integer i = 0;
    integer end = 16;

    string sStr = "";

    for(i=0;i<end;i++)
    {
        if(num & 1)sStr = "1" + sStr;
        else sStr = "0" + sStr;

        num = num >> 1;
    }

    return sStr;
}

integer CALLBACK_REGISTER = 0x1A1A;
integer CALLBACK_BACK = 0x1A1B;
integer CALLBACK_CANCEL = 0x1A1C;

set_callback(string callbackID, list extra, string sReturnNotation)
{
    llMessageLinked(LINK_SET, CALLBACK_REGISTER, llList2Json(JSON_OBJECT, ["returns", sReturnNotation, "data", llList2Json(JSON_OBJECT, extra)]), callbackID);
}

cancel_callback(string callbackID)
{
    llMessageLinked(LINK_SET, CALLBACK_CANCEL, "", callbackID);
}

list g_lCallbacks; // callbackID, return_time, extraData
integer callback(string callbackID)
{
    integer i = llListFindList(g_lCallbacks, [callbackID]);
    if(i == -1)
    {
        return FALSE;
    }

    integer iRet = (integer)llList2String(g_lCallbacks, i+1);

    if(llGetUnixTime() > iRet)
    {
        string sExtra = llList2String(g_lCallbacks, i+2);
        llMessageLinked(LINK_SET, CALLBACK_BACK, sExtra, callbackID);

        g_lCallbacks = llDeleteSubList(g_lCallbacks, i, i+2);
        return TRUE;
    }

    return FALSE;
}

// insert into timer event
processCallbacks()
{

    integer index = 0;
    integer end = llGetListLength(g_lCallbacks);

    for(index=0;index<end;index += 3)
    {
        string sID = llList2String(g_lCallbacks, index);
        if(callback(sID)){
            processCallbacks();
            return;
        }
    }
}


integer IsLikelyUUID(string sID)
{
    if(sID == (string)NULL_KEY)return TRUE;
    if(llStringLength(sID)==32)return TRUE;
    key kID = (key)sID;
    if(kID)return TRUE;
    if(llStringLength(sID) >25){
        if(llGetSubString(sID,8,8)=="-" && llGetSubString(sID, 13,13) == "-" && llGetSubString(sID,18,18) == "-" && llGetSubString(sID,23,23)=="-") return TRUE;

    }
    return FALSE;
}

/*
    Bitmask Helper functions
*/
integer TOGGLE = 2;
integer SET=1;
integer UNSET=0;
/*
    Usage:  i = mask (SET, i, BITMASK)
            i = mask (FALSE, i, BITMASK)
*/
integer mask(integer states, integer source, integer mask)
{
    if(states==TOGGLE)
    {
        return source ^ mask;
    }
    if(states)
    {
        source = source | mask;
        return source;
    }else {
        source = source &~ mask;
        return source;
    }
}

integer IsLikelyAvatarID(key kID)
{
    if(!IsLikelyUUID(kID))return FALSE;
    // Avatar UUIDs always have the 15th digit set to a 4
    if(llGetSubString(kID,8,8) == "-" && llGetSubString(kID,14,14)=="4")return TRUE;

    return FALSE;
}

integer IsListOfIDs(list lIDs)
{
    integer i=0;
    integer end = llGetListLength(lIDs);
    for(i=0;i<end;i++){
        if(IsLikelyUUID(llList2String(lIDs,i)))return TRUE;
    }
    return FALSE;
}

list TestsuiteID2(key kID)
{
    // This testsuite uses a hardcoded ID
    list lOpts = [];
    lOpts += [kID, IsLikelyUUID(kID), IsLikelyAvatarID(kID)];
    return lOpts;
}

list TestsuiteID()
{
    list lOpts;
    // Testsuite 1 will generate a unique ID;
    key kID = llGenerateKey();
    lOpts += [kID, IsLikelyUUID(kID), IsLikelyAvatarID(kID)];
    return lOpts;
}

float getTimestamp()
{

    string sOrigin=llGetTimestamp();
    list TimeStamp = llParseString2List(sOrigin,["-",":", "."],["T", "Z"]); //Get timestamp and split into parts in a list
    //llSay(0, "Timestamp format: "+sOrigin);
    // YYYY-MM-DDThh:mm:ss.ff..fZ
    string t = (string)zniTimestamp(TimeStamp);
    t = t+"."+zniTimestampMilli(TimeStamp);
    float fStamp = zniTsNormalize(t);
    return fStamp;
}
string stripMetatags(string sInput)
{
    list lTmp = llParseString2List(sInput,[], ["<",">"]);
    integer iIndex = llListFindList(lTmp,[">"]);
    while(iIndex!=-1){
        lTmp = llDeleteSubList(lTmp, iIndex-2, iIndex);
        iIndex=llListFindList(lTmp,[">"]);
    }
    return llDumpList2String(lTmp, "");
}
integer timeAsSeconds(string sInput)
{
    // Converts a string like 02:00  into its seconds equivalent (ie. 120)

    list lTmp = llParseString2List(stripMetatags(sInput), [":"],[]);
    integer iMin = (integer)llList2String(lTmp,0);
    integer iSec = (integer)llList2String(lTmp,1);
    return (iSec+(iMin*60));
}

integer timestampMilli(list Tmp) {
    return llList2Integer(Tmp,7);
}
/*// Released to Public Domain without limitation. Created by Nexii Malthus. //*/
// SL Wiki
integer Timestamp( list lDate ){
    integer iRet;
    integer Days = llList2Integer(lDate,2);
    integer Hours = llList2Integer(lDate,4);
    integer Minutes = llList2Integer(lDate,5);
    integer Seconds = llList2Integer(lDate,6);

    iRet += Seconds;
    iRet += (Minutes * 60);
    iRet += (Hours * 60 * 60);

    return iRet;
}
float timestampNormalize(string t)
{
    integer iIndex = llSubStringIndex(t, ".");
    iIndex+=4;
    return (float)llGetSubString(t,0,iIndex);
}

integer bool(integer a){
    if(a)return TRUE;
    else return FALSE;
}
list g_lCheckboxes=["□","▣"];
string Checkbox(integer iValue, string sLabel) {
    return llList2String(g_lCheckboxes, bool(iValue))+" "+sLabel;
}

string sSetor(integer a, string b, string c)
{
    if(a)return b;
    else return c;
}
integer iSetor(integer a, integer b, integer c)
{
    if(a)return b;
    else return c;
}
vector vSetor(integer a, vector b, vector c)
{
    if(a)return b;
    else return c;
}
list lSetor(integer a,list b, list c)
{
    if(a)return b;
    else return c;
}

float fSetor(integer a, float b, float c)
{
    if(a)return b;
    else return c;
}

// Partly from SL Wiki, modified by Aria
string List2Type(list input) { // converts a list to a string with type information prepended to each item
    integer     i;
    list        output;
    integer     len;

    len=llGetListLength(input); //this can shave seconds off long lists
    for (i = 0; i < len; i++) {
        output += [llGetListEntryType(input, i)] + [llStringToBase64(llList2String(input, i))];
    }

    return llDumpList2String(output,"&&_");
}
list Type2List(string inputstring) { // converts a CSV string created with List2TypeCSV back to a list with the correct type information
    integer     i;
    list        input;
    list        output;
    integer     len;

    input = llParseString2List(inputstring, ["&&_"],[]);

    len=llGetListLength(input);
    for (i = 0; i < len; i += 2) {
        if (llList2Integer(input, i) == TYPE_INTEGER) output += (integer)llBase64ToString(llList2String(input, i + 1));
        else if (llList2Integer(input, i) == TYPE_FLOAT) output += (float)llList2String(input, i + 1);
        else if (llList2Integer(input, i) == TYPE_STRING) output += llBase64ToString(llList2String(input, i + 1));
        else if (llList2Integer(input, i) == TYPE_KEY) output += (key)llBase64ToString(llList2String(input, i + 1));
        else if (llList2Integer(input, i) == TYPE_VECTOR) output += (vector)llBase64ToString(llList2String(input, i + 1));
        else if (llList2Integer(input, i) == TYPE_ROTATION) output += (rotation)llBase64ToString(llList2String(input, i + 1));
    }

    return output;
}

string Uncheckbox(string sLabel)
{
    integer iBoxLen = 1+llStringLength(llList2String(g_lCheckboxes,0));
    return llGetSubString(sLabel,iBoxLen,-1);
}


string SLURL(key kID){
    return "secondlife:///app/agent/"+(string)kID+"/about";
}
string OSLURL(key kID)
{
    return llKey2Name(kID); // TODO: Replace with a SLURL of some kind pointing to the object inspect.
}

// From SL Wiki
list StrideOfList(list src, integer stride, integer start, integer end)
{
    list l = [];
    integer ll = llGetListLength(src);
    if(start < 0)start += ll;
    if(end < 0)end += ll;
    if(end < start) return llList2List(src, start, start);
    while(start <= end)
    {
        l += llList2List(src, start, start);
        start += stride;
    }
    return l;
}

string tf(integer a){
    if(a)return "true";
    return "false";
}

list g_lDSRequests;
key NULL=NULL_KEY;
UpdateDSRequest(key orig, key new, string meta){
    if(orig == NULL){
        g_lDSRequests += [new,meta];
    }else {
        integer index = HasDSRequest(orig);
        if(index==-1)return;
        else{
            g_lDSRequests = llListReplaceList(g_lDSRequests, [new,meta], index,index+1);
        }
    }
}

string GetDSMeta(key id){
    integer index=llListFindList(g_lDSRequests,[id]);
    if(index==-1){
        return "N/A";
    }else{
        return llList2String(g_lDSRequests,index+1);
    }
}

integer HasDSRequest(key ID){
    return llListFindList(g_lDSRequests, [ID]);
}

DeleteDSReq(key ID){
    if(HasDSRequest(ID)!=-1)
        g_lDSRequests = llDeleteSubList(g_lDSRequests, HasDSRequest(ID), HasDSRequest(ID)+1);
    else return;
}

string MkMeta(list lTmp){
    return llDumpList2String(lTmp, ":");
}
string SetMetaList(list lTmp){
    return llDumpList2String(lTmp, ":");
}

string SetDSMeta(list lTmp){
    return llDumpList2String(lTmp, ":");
}

list GetMetaList(key kID){
    return llParseStringKeepNulls(GetDSMeta(kID), [":"],[]);
}


string getperm_str(integer perm)
{
    integer fullPerms = PERM_COPY | PERM_MODIFY | PERM_TRANSFER;
    integer copyModPerms = PERM_COPY | PERM_MODIFY;
    integer copyTransPerms = PERM_COPY | PERM_TRANSFER;
    integer modTransPerms = PERM_MODIFY | PERM_TRANSFER;
    string output = "";
    if ((perm & fullPerms) == fullPerms)
        output += "full";
    else if ((perm & copyModPerms) == copyModPerms)
        output += "copy & modify";
    else if ((perm & copyTransPerms) == copyTransPerms)
        output += "copy & transfer";
    else if ((perm & modTransPerms) == modTransPerms)
        output += "modify & transfer";
    else if ((perm & PERM_COPY) == PERM_COPY)
        output += "copy";
    else if ((perm & PERM_TRANSFER) == PERM_TRANSFER)
        output += "transfer";
    else
        output += "none";
    return  output;
}
string getperms(string inventory)
{
    integer perm = llGetInventoryPermMask(inventory,MASK_NEXT);
    integer fullPerms = PERM_COPY | PERM_MODIFY | PERM_TRANSFER;
    integer copyModPerms = PERM_COPY | PERM_MODIFY;
    integer copyTransPerms = PERM_COPY | PERM_TRANSFER;
    integer modTransPerms = PERM_MODIFY | PERM_TRANSFER;
    string output = "";
    if ((perm & fullPerms) == fullPerms)
        output += "full";
    else if ((perm & copyModPerms) == copyModPerms)
        output += "copy & modify";
    else if ((perm & copyTransPerms) == copyTransPerms)
        output += "copy & transfer";
    else if ((perm & modTransPerms) == modTransPerms)
        output += "modify & transfer";
    else if ((perm & PERM_COPY) == PERM_COPY)
        output += "copy";
    else if ((perm & PERM_TRANSFER) == PERM_TRANSFER)
        output += "transfer";
    else
        output += "none";
    return  output;
}

string getperms_current(string inventory)
{
    integer perm = llGetInventoryPermMask(inventory,MASK_OWNER);
    integer fullPerms = PERM_COPY | PERM_MODIFY | PERM_TRANSFER;
    integer copyModPerms = PERM_COPY | PERM_MODIFY;
    integer copyTransPerms = PERM_COPY | PERM_TRANSFER;
    integer modTransPerms = PERM_MODIFY | PERM_TRANSFER;
    string output = "";
    if ((perm & fullPerms) == fullPerms)
        output += "full";
    else if ((perm & copyModPerms) == copyModPerms)
        output += "copy & modify";
    else if ((perm & copyTransPerms) == copyTransPerms)
        output += "copy & transfer";
    else if ((perm & modTransPerms) == modTransPerms)
        output += "modify & transfer";
    else if ((perm & PERM_COPY) == PERM_COPY)
        output += "copy";
    else if ((perm & PERM_TRANSFER) == PERM_TRANSFER)
        output += "transfer";
    else
        output += "none";
    return  output;
}

/////////////////////////////////////////////////////////////////////
// Script Library Contribution by Flennan Roffo
// Logic Scripted Products & Script Services
// Peacock Park (183,226,69)
// (c) 2007 - Flennan Roffo
//
// Distributed as GPL, donated to wiki.secondlife.com on 19 sep 2007
//
// SCRIPT:  Unix2DateTimev1.0.lsl
//
// FUNCTION:
// Perform conversion from return value of llGetUnixTime() to
// date and time strings and vice versa.
//
// USAGE:
// list timelist=Unix2DateTime(llGetUnixTime());
// llSay(PUBLIC_CHANNEL, "Date: " +  DateString(timelist); // displays date as DD-MON-YYYY
// llSay(PUBLIC_CHANNEL, "Time: " +  TimeString(timelist); // displays time as HH24:MI:SS
/////////////////////////////////////////////////////////////////////

///////////////////////////// Unix Time conversion //////////////////

integer DAYS_PER_YEAR        = 365;           // Non leap year
integer SECONDS_PER_YEAR     = 31536000;      // Non leap year
integer SECONDS_PER_DAY      = 86400;
integer SECONDS_PER_HOUR     = 3600;
integer SECONDS_PER_MINUTE   = 60;

list MonthNameList = [  "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" ];

// This leap year test works for all years from 1901 to 2099 (yes, including 2000)
// Which is more than enough for UnixTime computations, which only operate over the range [1970, 2038].  (Omei Qunhua)
integer LeapYear( integer year)
{
    return !(year & 3);
}

integer DaysPerMonth(integer year, integer month)
{
    // Compact Days-Per-Month algorithm. Omei Qunhua.
    if (month == 2)  	return 28 + LeapYear(year);
    return 30 + ( (month + (month > 7) ) & 1);           // Odd months up to July, and even months after July, have 31 days
}

integer DaysPerYear(integer year)
{
    return 365 + LeapYear(year);
}

///////////////////////////////////////////////////////////////////////////////////////
// Convert Unix time (integer) to a Date and Time string
///////////////////////////////////////////////////////////////////////////////////////
// SL Wiki
/////////////////////////////// Unix2DataTime() ///////////////////////////////////////

list Unix2DateTime(integer unixtime)
{
    integer days_since_1_1_1970     = unixtime / SECONDS_PER_DAY;
    integer day = days_since_1_1_1970 + 1;
    integer year  = 1970;
    integer days_per_year = DaysPerYear(year);

    while (day > days_per_year)
    {
        day -= days_per_year;
        ++year;
        days_per_year = DaysPerYear(year);
    }

    integer month = 1;
    integer days_per_month = DaysPerMonth(year, month);

    while (day > days_per_month)
    {
        day -= days_per_month;

        if (++month > 12)
        {
            ++year;
            month = 1;
        }

        days_per_month = DaysPerMonth(year, month);
    }

    integer seconds_since_midnight  = unixtime % SECONDS_PER_DAY;
    integer hour        = seconds_since_midnight / SECONDS_PER_HOUR;
    integer second      = seconds_since_midnight % SECONDS_PER_HOUR;
    integer minute      = second / SECONDS_PER_MINUTE;
    second              = second % SECONDS_PER_MINUTE;

    return [ month, day, year, hour, minute, second ];
}

///////////////////////////////// MonthName() ////////////////////////////
// SL Wiki
string MonthName(integer month)
{
    if (month >= 0 && month < 12)
        return llList2String(MonthNameList, month);
    else
        return "";
}

///////////////////////////////// DateString() ///////////////////////////
//SL Wiki
string DateString(list timelist)
{
    integer year       = llList2Integer(timelist,0);
    integer month      = llList2Integer(timelist,1);
    integer day        = llList2Integer(timelist,2);

    return (string)day + "-" + MonthName(month - 1) + "-" + (string)year;
}

///////////////////////////////// TimeString() ////////////////////////////
// SLWiki
string TimeString(list timelist)
{
    string  hourstr     = llGetSubString ( (string) (100 + llList2Integer(timelist, 3) ), -2, -1);
    string  minutestr   = llGetSubString ( (string) (100 + llList2Integer(timelist, 4) ), -2, -1);
    string  secondstr   = llGetSubString ( (string) (100 + llList2Integer(timelist, 5) ), -2, -1);
    return  hourstr + ":" + minutestr + ":" + secondstr;
}

///////////////////////////////////////////////////////////////////////////////
// Convert a date and time to a Unix time integer
///////////////////////////////////////////////////////////////////////////////
// SL WIKI

////////////////////////// DateTime2Unix() ////////////////////////////////////

integer DateTime2Unix(integer month, integer day, integer year, integer hour, integer minute, integer second)
{
	integer time = 0;
	integer yr = 1970;
	integer mt = 1;
	integer days;

	while(yr < year)
	{
		days = DaysPerYear(yr++);
		time += days * SECONDS_PER_DAY;
	}

	while (mt < month)
	{
		days = DaysPerMonth(year, mt++);
		time += days * SECONDS_PER_DAY;
	}

	days = day - 1;
	time += days * SECONDS_PER_DAY;
	time += hour * SECONDS_PER_HOUR;
	time += minute * SECONDS_PER_MINUTE;
	time += second;

	return time;
}
//SL WIKI
integer DateList2Unix(list lTime)
{
    return DateTime2Unix(llList2Integer(lTime,0), llList2Integer(lTime,1), llList2Integer(lTime,2), llList2Integer(lTime,3), llList2Integer(lTime,4), llList2Integer(lTime,5));
}