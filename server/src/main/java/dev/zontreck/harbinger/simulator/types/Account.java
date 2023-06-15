package dev.zontreck.harbinger.simulator.types;

import dev.zontreck.harbinger.utils.DataUtils;
import dev.zontreck.harbinger.utils.DigestUtils;
import org.simpleframework.xml.Default;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@Default(required = false)
public class Account {


	public String First;
	public String Last;
	public int UserLevel;
	public String PasswordHash;
	public String PasswordSalt;
	public String UserID;
	public String UserTitle = "Resident";

	@org.simpleframework.xml.Path ("tos")
	public boolean HasAgreedToTermsOfService = false;

	@org.simpleframework.xml.Path ("tos")
	public long LastReadTOS = 0;

	@org.simpleframework.xml.Path ("critical")
	public boolean HasReadCriticalInfo = false;

	@Element (required = false)
	@org.simpleframework.xml.Path ("critical")
	public long LastReadCritical;
	public Location LastLocation = new Location (  );
	public Location HomeLocation = new Location (  );

	public Account ( ) {

	}

	public Account ( String First , String Last , String Pwd ) {
		this.First = First;
		this.Last = Last;
		UserID = UUID.randomUUID ( ).toString ( );
		UserLevel = 0;
		/*
		 * Level Information
		 * 0 - Brand new User
		 * 1 - Agreed to TOS
		 * 2 - Read Critical information
		 * 3 - Resident
		 * 4 - Resident + Estate God
		 * 50 - Premium
		 * 100 - Premium Plus
		 * 150 - Support Staff
		 * 200 - God
		 * 	- At 150 and above, Unlimited Groups is activated.
		 */

		// Generate a salt
		this.PasswordSalt = DigestUtils.md5hex ( DataUtils.GenerateGarbage ( 255 ) );
		this.PasswordHash = DigestUtils.md5hex ( ( Pwd + ":" + DigestUtils.md5hex ( PasswordSalt.getBytes ( ) ) ).getBytes ( ) );

	}

	public boolean ValidatePassword ( String pass ) {
		String hash = DigestUtils.md5hex ( ( pass + ":" + DigestUtils.md5hex ( PasswordSalt.getBytes ( ) ) ).getBytes ( ) );
		if ( PasswordHash.equals ( hash ) )
			return true;
		else return false;
	}


	public void commit ( ) {
		Path accounts = Path.of ( "accounts" );
		Path UserNameFile = accounts.resolve ( First + "." + Last + ".xml" );
		if ( UserNameFile.toFile ( ).exists ( ) )
			UserNameFile.toFile ( ).delete ( );
		try {
			FileWriter fw = new FileWriter ( UserNameFile.toFile ( ) );
			fw.write ( UserID );

			fw.close ( );
		} catch ( FileNotFoundException e ) {
			throw new RuntimeException ( e );
		} catch ( IOException e ) {
			throw new RuntimeException ( e );
		}


		Path accountData = accounts.resolve ( "data" );
		if ( ! accountData.toFile ( ).exists ( ) )
			accountData.toFile ( ).mkdir ( );

		Path UserDataFile = accountData.resolve ( UserID + ".xml" );
		Serializer serial = new Persister ( );
		try {
			serial.write ( this , UserDataFile.toFile ( ) );
		} catch ( Exception e ) {
			throw new RuntimeException ( e );
		}

	}
}
