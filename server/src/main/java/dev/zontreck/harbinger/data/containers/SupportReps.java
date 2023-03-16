package dev.zontreck.harbinger.data.containers;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import dev.zontreck.ariaslib.nbt.old.CompoundTag;
import dev.zontreck.ariaslib.nbt.old.ListTag;
import dev.zontreck.harbinger.data.types.Person;

public class SupportReps {
    public static List<Person> REPS = Lists.newArrayList();

    public SupportReps(){}


    public static void add(Person rep)
    {
        REPS.add(rep);
    }

    public static void remove(Person rep)
    {
        REPS.remove(rep);
    }

    public static boolean contains(Person rep)
    {
        return REPS.contains(rep);
    }

    public static boolean hasID(UUID id)
    {
        return REPS.stream().filter(m->m.ID.equals(id)).count()>0;

    }

    public static ListTag save()
    {
        ListTag tag = new ListTag();
        for (Person person : REPS) {
            tag.add(person.save());
        }

        return tag;
    }


    public static void load(ListTag tag)
    {
        for(int i=0;i<tag.size(); i++)
        {
            REPS.add(Person.deserialize((CompoundTag)tag.get(i)));
        }
    }
}
