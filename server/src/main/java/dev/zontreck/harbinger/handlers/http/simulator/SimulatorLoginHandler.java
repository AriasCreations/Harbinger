package dev.zontreck.harbinger.handlers.http.simulator;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeFactoryImpl;
import org.apache.xmlrpc.common.XmlRpcController;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.parser.StringParser;
import org.apache.xmlrpc.parser.XmlRpcRequestParser;

import java.io.IOException;

public class SimulatorLoginHandler
{
	@Subscribe
	public static void onSimLogin( GenericRequestEvent GRE )
	{
		if(GRE.path.equals ( "/simulator/login" ))
		{
			GRE.setCancelled ( true );

			

		}
	}
}
