package dev.zontreck.harbinger.simulator.network.packets;

import java.io.*;
import java.net.DatagramPacket;
import java.util.UUID;

public class UseCircuitCode
{
	public int CircuitCode=1;
	public UUID SessionID = new UUID ( 0,0 );
	public UUID AgentID = new UUID ( 0,0 );

	public static UseCircuitCode Decode( InputStream IS )
	{
		DataInputStream dis = new DataInputStream ( IS );
		UseCircuitCode code = new UseCircuitCode ();
		try {
			code.CircuitCode = dis.readInt ();
			code.SessionID = UUID.fromString ( dis.readUTF () );
			code.AgentID = UUID.fromString ( dis.readUTF () );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}

		return code;

	}

	public void Serialize( OutputStream os )
	{
		DataOutputStream dos = new DataOutputStream ( os );
		try {
			dos.writeInt(CircuitCode);
			dos.writeUTF ( SessionID.toString () );
			dos.writeUTF ( AgentID.toString () );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}

	}
}
