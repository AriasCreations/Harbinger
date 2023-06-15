package dev.zontreck.harbinger.simulator.services.simulator;

import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Task;
import dev.zontreck.ariaslib.util.DelayedExecutorService;
import dev.zontreck.harbinger.data.Persist;
import dev.zontreck.harbinger.simulator.services.ServiceRegistry;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class SimulatorUDPService extends Task
{
	private static DatagramSocket socket;
	private static boolean running;

	public SimulatorUDPService ( String name ) throws SocketException {
		super ( name, true );
		socket = new DatagramSocket ( Persist.serverSettings.udp_settings.UDPPort );

	}

	public static void startService()
	{
		try {
			DelayedExecutorService.scheduleRepeatingTask ( new SimulatorUDPService ("UDP Server"), 1 );
		} catch ( SocketException e ) {
			throw new RuntimeException ( e );
		}
	}

	@Override
	public void run ( ) {
		byte[] buffer = new byte[4096];

		DatagramPacket packet = new DatagramPacket ( buffer, buffer.length );
		try {
			socket.receive ( packet );



			InetAddress ip = packet.getAddress ();
			int port = packet.getPort ();
			packet = new DatagramPacket ( buffer, buffer.length, ip, port );
			String received = new String ( packet.getData (), 0, packet.getLength () );

			ServiceRegistry.LOGGER.debug ( "UDP Packet data: " + received );

			buffer = new byte[4096];
			packet = new DatagramPacket ( buffer, buffer.length, ip, port );

			socket.send ( packet );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}
	}
}
