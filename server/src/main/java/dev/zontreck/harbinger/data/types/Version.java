package dev.zontreck.harbinger.data.types;

import com.google.common.collect.Lists;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Version {
	public static final Logger LOGGER = LoggerFactory.getLogger ( Version.class.getName ( ) );
	public List<Integer> versionDigits = Lists.newArrayList ( );

	public Version ( final String ver ) {
		final String[] str = ver.split ( "\\." );
		for ( final String string : str ) {
			this.versionDigits.add ( Integer.parseInt ( string ) );
		}
	}

	public Version ( OSD entry ) {
		if ( entry instanceof OSDArray map ) {
			versionDigits = new ArrayList<> ( );
			Iterator<OSD> it = map.iterator ( );
			while ( it.hasNext ( ) ) {
				OSD item = it.next ( );
				versionDigits.add ( item.AsInteger ( ) );
			}
		}
	}

	public OSD save ( ) {
		OSDArray arr = new OSDArray ( );
		for (
				int x :
				versionDigits
		) {
			arr.add ( OSD.FromInteger ( x ) );
		}

		return arr;
	}

	@Override
	public String toString ( ) {
		String ret = "";

		final Iterator<Integer> it = this.versionDigits.iterator ( );
		while ( it.hasNext ( ) ) {
			ret += it.next ( );

			if ( it.hasNext ( ) ) {
				ret += ".";
			}
		}

		return ret;
	}

	/**
	 * Increments a version number
	 *
	 * @param position
	 * @return True if the number was incremented
	 */
	public boolean increment ( final int position ) {
		if ( this.versionDigits.size ( ) <= position )
			return false;
		Integer it = this.versionDigits.get ( position );
		it++;
		this.versionDigits.set ( position , it );

		return true;
	}


	public boolean isNewer ( final Version other ) throws Exception {
		if ( other.versionDigits.size ( ) != this.versionDigits.size ( ) ) {
			Version.LOGGER.error ( "Cannot proceed, the versions do not have the same number of digits" );
			return false;

		}
		else {
			for ( int i = 0 ; i < this.versionDigits.size ( ) ; i++ ) {
				final int v1 = this.versionDigits.get ( i );
				final int v2 = other.versionDigits.get ( i );

				if ( v1 < v2 ) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean isSame ( final Version other ) {
		if ( other.versionDigits.size ( ) != this.versionDigits.size ( ) )
			return false;

		for ( int i = 0 ; i < this.versionDigits.size ( ) ; i++ ) {
			final int v1 = this.versionDigits.get ( i );
			final int v2 = other.versionDigits.get ( i );

			if ( v1 != v2 ) return false;
		}

		return true;
	}
}
