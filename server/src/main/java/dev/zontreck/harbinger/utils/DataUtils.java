package dev.zontreck.harbinger.utils;

import dev.zontreck.harbinger.HarbingerServer;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;

public class DataUtils {
	public static byte[] GenerateGarbage ( int len ) {
		byte[] t = new byte[ len ];
		Random rng = new Random ( Instant.now ( ).getEpochSecond ( ) );
		for ( int i = 0 ; i < len ; i++ ) {
			t[ i ] = ( ( byte ) ( rng.nextInt ( 0 , 255 ) ) );
		}

		return t;
	}

	public static String StripNewLines ( String input ) {
		return input.replace ( "\n" , "" );
	}

	public static String ReadTextFile ( File fileToRead ) {
		StringBuilder sb = new StringBuilder ( );
		try {
			BufferedReader br = new BufferedReader ( new FileReader ( fileToRead ) );
			String line = br.readLine ( );
			while ( line != null ) {
				sb.append ( line );
				sb.append ( System.lineSeparator ( ) );
				line = br.readLine ( );
			}

			return sb.toString ( );

		} catch ( FileNotFoundException e ) {
			throw new RuntimeException ( e );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}
	}


	public static void WriteFileBytes ( Path path , byte[] bytes ) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream ( new FileOutputStream ( path.toFile ( ) ) );
		bos.write ( bytes );
		bos.flush ( );
		bos.close ( );
	}

	public static void WriteFileText ( Path path , String text ) {
		try {
			WriteFileBytes ( path , text.getBytes ( ) );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}
	}

	public static byte[] ReadAllBytes ( Path pth ) {
		try {
			BufferedInputStream bis = new BufferedInputStream ( new FileInputStream ( pth.toFile ( ) ) );

			return bis.readAllBytes ( );
		} catch ( FileNotFoundException e ) {
			throw new RuntimeException ( e );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}
	}

	public static byte[] ReadAllBytesFromResource ( String resourceName ) {
		try {
			InputStream is = HarbingerServer.class.getClassLoader ( ).getResourceAsStream ( resourceName );

			if ( is == null )
				return new byte[ 0 ];

			BufferedInputStream bis = new BufferedInputStream ( is );
			return bis.readAllBytes ( );

		} catch ( IOException e ) {
			return new byte[ 0 ];
		}
	}
}
