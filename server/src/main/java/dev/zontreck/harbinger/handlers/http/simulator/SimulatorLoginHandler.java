package dev.zontreck.harbinger.handlers.http.simulator;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.xmlrpc.MethodCall;
import dev.zontreck.ariaslib.xmlrpc.MethodResponse;
import dev.zontreck.ariaslib.xmlrpc.XmlRpcDeserializer;
import dev.zontreck.harbinger.events.GenericRequestEvent;

import java.io.ByteArrayInputStream;
import java.util.List;

public class SimulatorLoginHandler {
	@Subscribe
	public static void onSimLogin ( GenericRequestEvent GRE ) {
		if ( "/simulator/login".equals ( GRE.path ) ) {
			GRE.setCancelled ( true );

			ByteArrayInputStream ARIS = new ByteArrayInputStream ( GRE.body );

			try {
				XmlRpcDeserializer deserial = new XmlRpcDeserializer ( ARIS );
				MethodCall call = MethodCall.fromDeserializer ( deserial );
				List<String> options = ( List<String> ) call.parameters.get ( "options" );
				String channel = ( String ) call.parameters.get ( "channel" );
				int address_size = ( int ) call.parameters.get ( "address_size" );
				int agree_to_tos = ( int ) call.parameters.get ( "agree_to_tos" );
				int extended_errors = ( int ) call.parameters.get ( "extended_errors" );
				String first = ( String ) call.parameters.get ( "first" );
				String host_id = ( String ) call.parameters.get ( "host_id" );
				String id0 = ( String ) call.parameters.get ( "id0" );
				String last = ( String ) call.parameters.get ( "last" );
				int last_exec_duration = ( int ) call.parameters.get ( "last_exec_duration" );
				int last_exec_event = ( int ) call.parameters.get ( "last_exec_event" );
				String mac = ( String ) call.parameters.get ( "mac" );
				String mfa_hash = ( String ) call.parameters.get ( "mfa_hash" );
				String passwd = ( String ) call.parameters.get ( "passwd" );
				String platform = ( String ) call.parameters.get ( "platform" );
				String platform_string = ( String ) call.parameters.get ( "platform_string" );
				String platform_ver = ( String ) call.parameters.get ( "platform_version" );
				int read_critical = ( int ) call.parameters.get ( "read_critical" );
				String start = ( String ) call.parameters.get ( "start" );
				String token = ( String ) call.parameters.get ( "token" );
				String version = ( String ) call.parameters.get ( "version" );


				MethodResponse resp = loginToSimulator ( address_size , agree_to_tos , channel , extended_errors , first , host_id , id0 , last , last_exec_duration , last_exec_event , mac , mfa_hash , passwd , platform , platform_string , platform_ver , read_critical , start , token , version , options.toArray ( new String[ 0 ] ) );

				GRE.responseCode = 200;
				GRE.responseText = resp.toXml ( );
				GRE.responseIsBinary = false;
				GRE.contentType = "application/xml";

			} catch ( Exception e ) {
				throw new RuntimeException ( e );
			}


		}
	}

	public static MethodResponse loginToSimulator ( int address_size , int agree_to_tos , String channel , int extended_errors , String first , String host_id , String id0 , String last , int last_exec_duration , int last_exec_event , String mac , String mfa_hash , String passwd , String platform , String platform_string , String platform_version , int read_critical , String start , String token , String version , String[] options ) {
		MethodResponse resp = new MethodResponse ( );
		resp.parameters.put ( "message" , "This function is not yet implemented" );
		resp.parameters.put ( "login" , "false" );
		resp.parameters.put ( "reason" , "Not yet implemented" );
		resp.parameters.put ( "first_name" , first );
		resp.parameters.put ( "last_name" , last );


		return resp;
	}
}
