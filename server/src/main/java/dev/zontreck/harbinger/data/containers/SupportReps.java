package dev.zontreck.harbinger.data.containers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import dev.zontreck.ariaslib.file.Entry;
import dev.zontreck.ariaslib.file.Folder;
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

    public static Entry<List<Entry>> save()
    {
        Entry<List<Entry>> tag = Folder.getNew("support");
        for (Person person : REPS)
        {
            tag.value.add(person.save());
        }

        return tag;
    }

    public static String dump()
    {
        String s = "[\n";
        for (int i = 0; i< REPS.size(); i++) {
            Person p = REPS.get(i);
            s += p.print(1);
            if((i+1) == REPS.size())
            {
                s += ",\n";
            }else s+= "\n";
        }

        s += "]";


        return s;
    }


    public static void load(Entry<List<Entry>> tag)
    {
        try{

            for(int i=0;i<tag.value.size(); i++)
            {
                REPS.add(Person.deserialize((Entry<List<Entry>>)tag.value.get(i)));
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            //REPS = new ArrayList<>();
        }
    }
}
