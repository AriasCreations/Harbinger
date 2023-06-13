package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.simulator.events.GridInfoGatherEvent;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;

/**
 * Provides the grid_info.xml when requested from the Harbinger service.
 */
public class GridInfo implements Cloneable
{

	public String GridName;
	public String GridNick;
	public final String ServiceType = "Harbinger";
	public String LoginURI;
	public String Economy;
	public String Register;


	private static final GridInfo BLANK_INFO= new GridInfo();
	private GridInfo(){
		GridName = "Dark Space";
		GridNick = "space";
		LoginURI = "$SELF$/simulation/login";
		Economy = "$SELF$/simulation/economy";
		Register = "$SELF$/simulation/register";
	}

	private GridInfo(GridInfo base)
	{
		GridName = base.GridName;
		GridNick = base.GridNick;
		LoginURI=base.LoginURI;
		Economy=base.Economy;
		Register=base.Register;
	}
	public static GridInfo consume()
	{
		GridInfoGatherEvent gather = new GridInfoGatherEvent(BLANK_INFO);
		EventBus.BUS.post(gather);

		return gather.info;
	}

	public OSD buildOSD()
	{
		OSDMap gridinfo = new OSDMap();
		gridinfo.put("platform", OSD.FromString(ServiceType));
		gridinfo.put("login", OSD.FromString(LoginURI));
		gridinfo.put("economy", OSD.FromString(Economy));
		gridinfo.put("register", OSD.FromString(Register));
		gridinfo.put("gridname", OSD.FromString(GridName));
		gridinfo.put("gridnick", OSD.FromString(GridNick));

		return gridinfo;
	}

	@Override
	public String toString()
	{
		return buildOSD().toString();
	}

	@Override
	public GridInfo clone()
	{
		return new GridInfo(this);
	}
}
