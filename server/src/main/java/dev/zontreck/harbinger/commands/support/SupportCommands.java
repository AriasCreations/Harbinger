package dev.zontreck.harbinger.commands.support;

import java.util.UUID;

import dev.zontreck.ariaslib.events.CommandEvent;
import dev.zontreck.ariaslib.events.annotations.Subscribe;
import dev.zontreck.ariaslib.terminal.ConsolePrompt;
import dev.zontreck.ariaslib.terminal.Terminal;
import dev.zontreck.harbinger.commands.CommandRegistry;
import dev.zontreck.harbinger.data.containers.SupportReps;
import dev.zontreck.harbinger.data.types.PermissionLevel;
import dev.zontreck.harbinger.data.types.Person;

public class SupportCommands{
    public static final String LIST_SUPPORT = "list";
    public static final String SUPPORT_ADD = "add"; // Starts a interactive prompt
    public static final String SUPPORT = "support";



    @Subscribe
    public static void onListSupport(CommandEvent ev)
    {
        if(ev.command.equals(SUPPORT))
        {
            if(ev.arguments.size()==0)
            {
                CommandRegistry.LOGGER.info("The following are the accepted subcommands\n \n"+
                
                        LIST_SUPPORT+"\t\t- Lists all support representatives\n" +
                        SUPPORT_ADD+"\t\t- Adds a new support rep\n"
                        );
                return;
            }else {
                if(ev.arguments.get(0).equals(LIST_SUPPORT))
                {
                    CommandRegistry.LOGGER.info("The following are the support reps: \n"+SupportReps.dump());
                } else if(ev.arguments.get(0).equals(SUPPORT_ADD))
                {
                    ev.setCancelled(true);

                    ConsolePrompt.console.printf("\nPlease enter the Rep's UUID > ");
                    String input = ConsolePrompt.console.readLine();
                    ConsolePrompt.console.printf("\nPlease enter the Rep's Second Life User Name (Not display) > ");
                    String name = ConsolePrompt.console.readLine();
                    ConsolePrompt.console.printf("\nWhat level is this user? \n");
                    for (PermissionLevel lPermissionLevel : PermissionLevel.values()) {
                        System.out.println(String.valueOf(lPermissionLevel.getFlag())+"\t\t-\t"+lPermissionLevel.name());
                        
                    }
                    ConsolePrompt.console.printf("\nChoose a level > ");
                    String lvl = ConsolePrompt.console.readLine();
                    PermissionLevel perm = PermissionLevel.of(Integer.parseInt(lvl));

                    Person p = new Person(UUID.fromString(input), name, perm);
                    SupportReps.add(p);

                    Terminal.startTerminal();
                    
                }
            }
        }
    }


}
