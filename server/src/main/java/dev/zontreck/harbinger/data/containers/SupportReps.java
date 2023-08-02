package dev.zontreck.harbinger.data.containers;

import com.google.common.collect.Lists;
import dev.zontreck.harbinger.data.mongo.DBSession;
import dev.zontreck.harbinger.data.mongo.MongoDriver;
import dev.zontreck.harbinger.data.types.GenericClass;
import dev.zontreck.harbinger.data.types.Person;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSD;
import dev.zontreck.harbinger.thirdparty.libomv.StructuredData.OSDArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SupportReps {
	public static final String TAG = "support_reps";
	public static List<Person> REPS = new ArrayList<> ( );

	public SupportReps ( ) {
	}

	public static void loadSupportReps ( ) {
		DBSession sess = MongoDriver.makeSession();
		var table = sess.getTableFor(TAG, new GenericClass<>(Person.class));

		REPS = new ArrayList<>();
		for (Person person :
				table.find()) {
			REPS.add(person);
		}
	}


	public static void add ( final Person rep ) {
		SupportReps.REPS.add ( rep );
		rep.commit();
	}

	public static void remove ( final Person rep ) {
		SupportReps.REPS.remove ( rep );
		rep.delete();
	}

	public static boolean contains ( final Person rep ) {
		return SupportReps.REPS.contains ( rep );
	}

	public static boolean hasID ( final UUID id ) {
		return REPS.stream().anyMatch(x->x.ID.equals(id));
	}

	public static Person get ( final UUID ID ) {
		if ( ! SupportReps.hasID ( ID ) ) return null;
		return SupportReps.REPS.stream ( ).filter ( m -> m.ID.equals ( ID ) ).toList ( ).get ( 0 );
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
