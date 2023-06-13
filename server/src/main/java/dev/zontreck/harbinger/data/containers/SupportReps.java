package dev.zontreck.harbinger.data.containers;

import com.google.common.collect.Lists;
import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
import dev.zontreck.harbinger.data.types.Person;

import java.util.List;
import java.util.UUID;

public class SupportReps {
	public static List<Person> REPS = Lists.newArrayList ( );

	public SupportReps ( ) {
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

	public static Entry<List<Entry>> save ( ) {
		final Entry<List<Entry>> tag = Folder.getNew ( "support" );
		for ( final Person person : SupportReps.REPS ) {
			tag.value.add ( person.save ( ) );
		}

		return tag;
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


	public static void load ( final Entry<List<Entry>> tag ) {
		try {

			for ( int i = 0 ; i < tag.value.size ( ) ; i++ ) {
				SupportReps.REPS.add ( Person.deserialize ( ( Entry<List<Entry>> ) tag.value.get ( i ) ) );
			}
		} catch ( final Exception e ) {
			e.printStackTrace ( );
			//REPS = new ArrayList<>();
		}
	}
}
