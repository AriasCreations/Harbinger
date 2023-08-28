using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Registry
{
    public class Session
    {
        private Key vars { get; set; } = new Key("root", null);
        public static Session instance = new Session();

        public Session()
        {
            vars = RegistryIO.loadHive("env");

            assert();
        }

        public void Add(string key, string value)
        {
            Environment.SetEnvironmentVariable(key, value, EnvironmentVariableTarget.Process);

            vars.Add(new Word(key, null).setWord(value));

            update();
        }

        public void Set(string key, string? value)
        {
            if (value == null)
            {
                if (vars.HasNamedKey(key))
                    Remove(key);
            }
            else
            {
                if (vars.HasNamedKey(key))
                {
                    vars.getNamed(key).Word().setWord(value);
                    Environment.SetEnvironmentVariable(key, value, EnvironmentVariableTarget.Process);

                    update();
                }
                else
                {
                    Add(key, value);
                }
            }

        }

        public string? Get(string key)
        {
            if (vars.HasNamedKey(key))
                return vars.getNamed(key).Word().Value;
            else return null;
        }

        public void Remove(string key)
        {
            Environment.SetEnvironmentVariable(key, null, EnvironmentVariableTarget.Process);
            try
            {
                vars.Remove(vars.getNamed(key));
            }catch(Exception ex)
            {

            }

            update();

        }

        public void update()
        {
            RegistryIO.saveHive(vars, "env");
        }

        public void assert()
        {
            foreach(Entry e in vars)
            {
                Environment.SetEnvironmentVariable(e.Name, e.Word().Value);
            }
        }

    }
}
