package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.simulator.events.GridInfoGatherEvent;
import org.json.JSONObject;

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

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<gridinfo>");
		{
			sb.append("<platform>" + ServiceType + "</platform>");
		}

		{
			sb.append("<login>" + LoginURI + "</login>");
		}

		{
			sb.append("<economy>" + Economy + "</economy>");
		}

		{
			sb.append("<register>" + Register + "</register>");
		}

		{
			sb.append("<gridname>"+GridName+"</gridname>");
			sb.append("<gridnick>"+GridNick+"</gridnick>");
		}


		sb.append("</gridinfo>");

		return sb.toString();
	}

	@Override
	public GridInfo clone()
	{
		return new GridInfo(this);
	}
}
