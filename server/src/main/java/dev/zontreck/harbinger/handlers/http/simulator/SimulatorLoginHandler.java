package dev.zontreck.harbinger.handlers.http.simulator;

import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.xmlrpc.MethodCall;
import dev.zontreck.ariaslib.xmlrpc.MethodResponse;
import dev.zontreck.ariaslib.xmlrpc.XmlRpcDeserializer;
import dev.zontreck.harbinger.HarbingerServer;
import dev.zontreck.harbinger.events.GenericRequestEvent;
import dev.zontreck.harbinger.simulator.types.Account;
import dev.zontreck.harbinger.simulator.types.LLoginResponse;
import dev.zontreck.harbinger.utils.DataUtils;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimulatorLoginHandler {
	@Subscribe
	public static void onSimLogin ( GenericRequestEvent GRE ) {
		if ( GRE.path.equals ( "/simulation/login" ) ) {
			GRE.setCancelled ( true );

			ByteArrayInputStream ARIS = new ByteArrayInputStream ( GRE.body );

			try {
				XmlRpcDeserializer deserial = new XmlRpcDeserializer ( ARIS );
				MethodCall call = MethodCall.fromDeserializer ( deserial );
				Object[] objoptions = ( Object[] ) call.parameters.get ( "options" );
				List<String> options = new ArrayList<> ( );
				for (
						Object obj :
						objoptions
				) {
					try {
						options.add ( ( String ) obj );
					} catch ( Exception e ) {

					}
				}

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


				DataUtils.WriteFileBytes( HarbingerServer.BASE_PATH.resolve ( "lastRequest.xml"), GRE.body);
				DataUtils.WriteFileText( HarbingerServer.BASE_PATH.resolve ( "lastResponse.xml" ), GRE.responseText);

			} catch ( Exception e ) {
				throw new RuntimeException ( e );
			}


		}
	}

	public static MethodResponse loginToSimulator ( int address_size , int agree_to_tos , String channel , int extended_errors , String first , String host_id , String id0 , String last , int last_exec_duration , int last_exec_event , String mac , String mfa_hash , String passwd , String platform , String platform_string , String platform_version , int read_critical , String start , String token , String version , String[] options ) {

		Path accounts = HarbingerServer.BASE_PATH.resolve ( "accounts" );
		if ( ! accounts.toFile ( ).exists ( ) ) {
			// Generate the folder
			accounts.toFile ( ).mkdir ( );
		}

		Path namedAccount = accounts.resolve ( first + "." + last + ".txt" );
		Path accountData = accounts.resolve ( "data" );

		Account userAccount = null;
		if ( namedAccount.toFile ( ).exists ( ) ) {

			try {
				String ID = DataUtils.StripNewLines ( DataUtils.ReadTextFile ( namedAccount.toFile ( ) ) );
				Path userData = accountData.resolve ( ID + ".json" );


				userAccount = Account.readFrom ( userData );
			} catch ( Exception e ) {
				throw new RuntimeException ( e );
			}
		}
		else {

			userAccount = new Account ( first , last , passwd );
			userAccount.commit ( );
		}

		LLoginResponse response = new LLoginResponse ( userAccount , passwd );
		response.setToSStatus ( agree_to_tos > 0 ? true : false );
		response.setReadPatch ( read_critical > 0 ? true : false );
		response.setOptionalQuery ( options );
		response.setLocationRequest ( last );


		return response.generateResponse ( );
	}
}
