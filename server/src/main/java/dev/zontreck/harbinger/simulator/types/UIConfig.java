package dev.zontreck.harbinger.simulator.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UIConfig
{
	public boolean allowFirstLife = true;

	public UIConfig(boolean firstLife)
	{
		allowFirstLife=firstLife;
	}


	public List<Map<?,?>> save()
	{
		Map<String,Object> output = new HashMap<> (  );
		output.put ( "allow_first_life", allowFirstLife );
		List<Map<?,?>> result = new ArrayList<> (  );
		result.add ( output );
		return result;
	}
}
