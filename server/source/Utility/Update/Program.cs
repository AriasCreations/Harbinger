using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Prebuild.Core.Utilities;
using LibAC;
using System.Reflection;
using System.Diagnostics;
using System.Threading;

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

                    HTTPReplyData HRD = HTTP.performRequest(manifest.URL, "");
                    
                    UpdaterManifest remote = JsonConvert.DeserializeObject<UpdaterManifest>(HRD.MessageAsString);

                    if(remote.particle.CurrentVersion != manifest.CurrentVersion)
                    {
                        string tempPath = Path.Combine(Path.GetTempPath(), "updater.temp");
                        Directory.CreateDirectory(tempPath);

                        remote.particle.InstallPath = Directory.GetParent(Assembly.GetExecutingAssembly().Location).FullName;
                        
                        Directory.SetCurrentDirectory(tempPath);
                        File.WriteAllText("update.json", JsonConvert.SerializeObject(remote));


                        // Download the updater
                        foreach(RemoteFile Rf in remote.updaterFiles)
                        {
                            string target = Path.Combine(Directory.GetCurrentDirectory(), Rf.localPath);
                            string directory = Directory.GetParent(target).FullName;
                            Directory.CreateDirectory(directory);

                            HRD = HTTP.performRequest(Rf.remotePath, "");
                            File.WriteAllBytes(Rf.localPath, HRD.MessageAsBytes);
                        }


                        Process.Start("dotnet", "Harbinger.Updater.dll -doUpdate");
                        return (int)ECODES.PREPARATIONS_COMPLETE;
                    } else
                    {
                        return (int)ECODES.NO_UPDATE_REQUIRED;
                    }
                } else if(cmd.WasPassed("doUpdate"))
                {
                    Console.WriteLine("Waiting 10 seconds for all processes to spin down before starting...");

                    int seconds = 10;
                    while (seconds >= 0)
                    {
                        Console.Write("Update Starting in " + seconds + " seconds\r  ");
                        Thread.Sleep(1000);

                        seconds--;
                    }

                    Console.Write("\nUpdate is starting...\n");

                    UpdaterManifest manifest = JsonConvert.DeserializeObject<UpdaterManifest>(File.ReadAllText("update.json"));

                    Console.WriteLine("Deleting old files...");
                    Directory.Delete(manifest.particle.InstallPath, true);

                    // Start download now
                    Console.WriteLine("Downloading files...");

                    // Begin
                    Directory.CreateDirectory(manifest.particle.InstallPath);
                    Directory.SetCurrentDirectory(manifest.particle.InstallPath);

                    // Loop through the remote files
                    foreach(RemoteFile rf in manifest.remoteFiles)
                    {
                        // Get the directory path name
                        string target = Path.Combine(Directory.GetCurrentDirectory(), rf.localPath);
                        string directory = Directory.GetParent(target).FullName;
                        Directory.CreateDirectory(directory);

                        HTTPReplyData hrd = HTTP.performRequest(rf.remotePath, "");
                        File.WriteAllBytes(rf.localPath, hrd.MessageAsBytes);

                    }

                    Console.WriteLine("Saving particle manifest...");
                    File.WriteAllText("update.current.json", JsonConvert.SerializeObject(manifest.particle, Formatting.Indented));

                    Console.WriteLine("Executing Clean Up Script");
                    Process.Start("dotnet", "Harbinger.Updater.dll -cleanTemp");

                    return (int)ECODES.UPDATED;
                } else if(cmd.WasPassed("cleanTemp"))
                {
                    Console.WriteLine("Cleaning up Temporary Directory");
                    Thread.Sleep(5000);
                    Directory.Delete(Path.Combine(Path.GetTempPath(), "updater.temp"), true);

                    return (int)ECODES.NOTHING;
                } else if(cmd.WasPassed("genManifest"))
                {
                    string host = cmd["host"];

                    if (cmd.WasPassed("plat")) host += "/" + cmd["plat"];


                    UpdaterManifest manifest = null;
                    string useManifest = cmd["manifest"];
                    if (File.Exists(useManifest))
                    {
                        manifest = JsonConvert.DeserializeObject<UpdaterManifest>(File.ReadAllText(useManifest));
                    }else
                    {
                        manifest = new UpdaterManifest();
                    }

                    if(cmd.WasPassed("updaterMode"))
                    {
                        manifest.updaterFiles.Clear();
                        manifest.updaterFiles.AddRange(Enumerate(Directory.GetCurrentDirectory(), host:host, currentLocal:Directory.GetCurrentDirectory()));
                    }else
                    {
                        manifest.remoteFiles.Clear();
                        manifest.remoteFiles.AddRange(Enumerate(Directory.GetCurrentDirectory(), host: host, currentLocal: Directory.GetCurrentDirectory()));
                    }

                    manifest.particle.CurrentVersion = GitVersion.FullVersion;
                    manifest.particle.InstallPath = "TBD";
                    
                    manifest.particle.URL = host + "/manifest.json";

                    File.WriteAllText(useManifest, JsonConvert.SerializeObject(manifest, Formatting.Indented));
                }
            }


            return 0;
        }

        public static RemoteFile[] Enumerate(string path, string currentLocal="", string host="")
        {
            List<RemoteFile> working = new();
            foreach(string X in Directory.EnumerateDirectories(path))
            {
                working.AddRange(Enumerate(X, currentLocal, host));
            }

            foreach(string F in Directory.EnumerateFiles(path))
            {
                RemoteFile rf = new RemoteFile();
                rf.localPath = Path.GetRelativePath(currentLocal, F);
                
                rf.remotePath = host + "/" + rf.localPath.Replace("\\", "/");

                working.Add(rf);
            }

            return working.ToArray();
        }
    }
}
