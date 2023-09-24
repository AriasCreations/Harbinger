using TP.CS.EventsBus;
using TP.CS.EventsBus.Events;
using Harbinger.Framework.HTTP.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.HTTP
{
    public class HTTPServer
    {
        private static HTTPServer instance;

        public HttpListener http_server;

        public static Runner http_server_runner;

        public static void init()
        {
        }


        [Subscribe(Priority.Severe)]
        public static void onStart(StartupEvent evt)
        {
            EventBus.PRIMARY.Scan(typeof(HTTPServer));
            EventBus.PRIMARY.Scan(typeof(HTTPServerSettings));
        }

        [Subscribe(Priority.Medium)]
        public static void onServerSettingsLoaded(HTTPServerSettingsLoadedEvent evt)
        {
            
            instance = new HTTPServer();
            instance.http_server = new HttpListener();
            instance.http_server.Prefixes.Add($"http://127.0.0.1:{HTTPServerSettings.instance.codec.HTTPPort.Value}/");

            instance.http_server.Start();

            Console.WriteLine($"HTTP Server started on port {HTTPServerSettings.instance.codec.HTTPPort.Value}");


            http_server_runner = new Runner();
            http_server_runner.Name = "HTTP Server Runner";
            http_server_runner.start = () =>
            {

                var ctx = HTTPServer.instance.http_server.GetContext();
                HTTPRequestEvent evt = new HTTPRequestEvent(ctx);

                if (EventBus.PRIMARY.post(evt))
                {
                    // Dispatch the response
                    evt.response.StatusCode = 200;
                }else
                {
                    evt.response.StatusCode = 404;
                    evt.responseBody = "Not Found";
                    evt.response.ContentType = "text/plain";
                }

                var os = evt.response.OutputStream;
                var bytes = Encoding.UTF8.GetBytes(evt.responseBody);
                os.Write(bytes, 0, bytes.Length);
                os.Close();

                evt.response.Close();
                
            };
            http_server_runner.executeAfter = TimeSpan.FromSeconds(1);
            http_server_runner.resetTick();

            DelayedExecutorService.ScheduleRepeating(http_server_runner);
        }
    }
}
