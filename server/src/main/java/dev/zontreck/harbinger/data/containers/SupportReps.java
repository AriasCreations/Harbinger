package dev.zontreck.harbinger.data.containers;

import com.google.common.collect.Lists;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SupportReps {
	public static final String TAG = "support_reps";
	public static List<Person> REPS = Lists.newArrayList ( );

	public SupportReps ( ) {
	}

	public static void load ( OSD entry ) {
		if ( entry instanceof OSDArray map ) {
			Iterator<OSD> it = map.iterator ( );
			while ( it.hasNext ( ) ) {
				REPS.add ( new Person ( it.next ( ) ) );
			}
		}
	}


	public static void add ( final Person rep ) {
		SupportReps.REPS.add ( rep );
	}

	public static void remove ( final Person rep ) {
		SupportReps.REPS.remove ( rep );
	}

	public static boolean contains ( final Person rep ) {
		return SupportReps.REPS.contains ( rep );
	}

	public static boolean hasID ( final UUID id ) {
		return 0 < REPS.stream ( ).filter ( m -> m.ID.equals ( id ) ).count ( );

	}

	public static Person get ( final UUID ID ) {
		if ( ! SupportReps.hasID ( ID ) ) return null;
		return SupportReps.REPS.stream ( ).filter ( m -> m.ID.equals ( ID ) ).toList ( ).get ( 0 );
	}

	public static OSD save ( ) {
		OSDArray arr = new OSDArray ( );
		for (
				Person p :
				SupportReps.REPS
		) {
			arr.add ( p.save ( ) );
		}
		return arr;
	}

	public static String dump ( ) {
		String s = "[\n";
		for ( int i = 0 ; i < SupportReps.REPS.size ( ) ; i++ ) {
			final Person p = SupportReps.REPS.get ( i );
			s += p.print ( 1 );
			if ( ( i + 1 ) == SupportReps.REPS.size ( ) ) {
				s += ",\n";
			}
			else s += "\n";
		}

		s += "]";


		return s;
	}
}
