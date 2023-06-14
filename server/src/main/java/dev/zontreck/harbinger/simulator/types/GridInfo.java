package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.ariaslib.events.EventBus;
import dev.zontreck.harbinger.simulator.events.GridInfoGatherEvent;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.LLSD.LLSDXml;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDMap;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Provides the grid_info.xml when requested from the Harbinger service.
 */
@Root(name = "gridinfo")
public class GridInfo implements Cloneable {

	private static final GridInfo BLANK_INFO = new GridInfo ( );
	@Element(name = "platform")
	public String ServiceType = "OpenSim";

	@Element(name = "gridname")
	public String GridName;

	@Element(name = "gridnick")
	public String GridNick;

	@Element(name = "login")
	public String LoginURI;

	@Element(name = "economy")
	public String Economy;

	@Element(name = "register")
	public String Register;

	private GridInfo ( ) {
		GridName = "Dark Space";
		GridNick = "space";
		LoginURI = "$SELF$/simulation/login";
		Economy = "$SELF$/simulation/economy";
		Register = "$SELF$/simulation/register";
	}

	public GridInfo( @Element(name = "gridname") String GridName, @Element(name = "gridnick") String GridNick, @Element(name = "login") String LoginURI, @Element(name = "economy") String Economy, @Element(name = "register") String Register)
	{
		this.GridNick = GridNick;
		this.GridName = GridName;
		this.LoginURI = LoginURI;
		this.Economy = Economy;
		this.Register = Register;
	}

	private GridInfo ( GridInfo base ) {
		GridName = base.GridName;
		GridNick = base.GridNick;
		LoginURI = base.LoginURI;
		Economy = base.Economy;
		Register = base.Register;
	}

	public static GridInfo consume ( ) {

		GridInfoGatherEvent gather = new GridInfoGatherEvent ( BLANK_INFO );
		EventBus.BUS.post ( gather );

		return gather.info;
	}

	@Override
	public String toString ( ) {
		try {
			Serializer serial = new Persister (  );
			ByteArrayOutputStream baos = new ByteArrayOutputStream (  );
			serial.write(this, baos);

			return new String(baos.toByteArray (), StandardCharsets.UTF_8);
		} catch ( Exception e ) {
			e.printStackTrace ();
			return "";
		}
	}

	@Override
	public GridInfo clone ( ) {
		return new GridInfo ( this );
	}
}
