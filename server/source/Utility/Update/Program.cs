using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Prebuild.Core.Utilities;

namespace Harbinger.Updater
{
    public class Program
    {
        public enum ECODES : int
        {
            ERROR = -1,
            NOTHING = 0,
            PREPARATIONS_COMPLETE = 2,
            NO_UPDATE_REQUIRED = 3,
            UPDATE_NOT_FOUND = 4, // Returned when the server manifest returns an error, or when downloading the update bundle errors.
            UPDATED = 5
        }
        public static int Main(string[] args)
        {
            CommandLineCollection cmd = new CommandLineCollection(args, true);

            if(args.Length == 0)
            {
                // Return a failure
                Console.WriteLine("You cannot run this program by itself. Please have the primary program call this executable.");
                return (int) ECODES.ERROR;
            } else
            {

                if (cmd.WasPassed("prep"))
                {
                    // Prepare for update. This is usually executed from within a working directory.
                    // Now, we need to retrieve the manifest file
                    // The current manifest should be in the directory, read the URL from it, and pull the current manifest.

                    // Manifest stored in local folder is of DataType ParticleManifest
                    // Manifest from server is of DataType UpdaterManifest

                    ParticleManifest manifest = JsonConvert.DeserializeObject<ParticleManifest>(File.ReadAllText("update.current.json"));

                    HttpClient client = new HttpClient();
                    var reply = client.GetAsync(manifest.URL);
                    reply.Wait();

                    var val = reply.Result.Content.ReadAsStringAsync();
                    val.Wait();
                    UpdaterManifest remote = JsonConvert.DeserializeObject<UpdaterManifest>(val.Result);

                    if(remote.particle.CurrentVersion != manifest.CurrentVersion)
                    {
                        Directory.CreateDirectory("updater.temp");
                        Directory.SetCurrentDirectory("updater.temp");

                        // Download the updater
                    } else
                    {
                        return (int)ECODES.NO_UPDATE_REQUIRED;
                    }
                }
            }


            return 0;
        }
    }
}
