using TP.CS.EventsBus;
using TP.CS.Registry;
using System;
using System.Linq;

namespace Harbinger.Prompt
{
    public class Program
    {
        public static void Main(string[] args)
        {
            EventBus.debug = false;


            string envVar = args[0];
            string[] remArgs = args.Skip(1).ToArray();

            if(remArgs.Length == 0)
            {
                Console.Write($"{envVar} = {Environment.GetEnvironmentVariable(envVar)}");
                return;
            }

            string reply = "";
            
            Console.ForegroundColor = ConsoleColor.DarkGreen;
            Console.Write(String.Join(" ", remArgs) + " ");
            Console.ForegroundColor = ConsoleColor.White;

            reply = Console.ReadLine();

            Session.instance.Set(envVar, reply);
            
        }
    }
}
