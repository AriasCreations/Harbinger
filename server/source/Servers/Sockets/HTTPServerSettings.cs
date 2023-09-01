using Harbinger.EventsBus;
using Harbinger.Framework.HTTP.Events;
using Harbinger.Framework.Registry;
using LibreMetaverse.LslTools;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace Harbinger.Framework.HTTP
{
    public class HTTPServerSettings
    {
        public const string KEY = "root/HKS/sockets";

        public Key MY_KEY;
        public static HTTPServerSettings instance = new ();
        public SocketServerSettingsCodec codec;


        [Subscribe(Priority.High)]
        public static void onRegistryLoaded(RegistryLoadedEvent evt)
        {
            instance.MY_KEY = Entry.getByPath(KEY)?.Key();
            if(instance.MY_KEY == null)
            {
                instance.MY_KEY = new Key("sockets", null);
                Entry.ROOT.placeAtPath(KEY.Substring(0, KEY.LastIndexOf('/')), instance.MY_KEY);

                instance.codec = new SocketServerSettingsCodec(instance.MY_KEY);
            }
            else
            {
                instance.codec = new SocketServerSettingsCodec(instance.MY_KEY);
            }

            EventBus.PRIMARY.post(new HTTPServerSettingsLoadedEvent());
        }
    }

    public class SocketServerSettingsCodec
    {
        public const int VERSION = 2;
        public const int HTTPPORT = 7790; // Default port number
        public const int TCPPORT = 7791; // TCP Socket
        public const int UDPPORT = 7792; // UDP Port
        public Key key;

        public VInt32 CurVer;
        public VInt32 HTTPPort;
        public VInt32 TCPPort;
        public VInt32 UDPPort;

        public SocketServerSettingsCodec(Key key)
        {
            this.key = key;
            if(!key.HasNamedKey("version"))
            {
                Initialize();
            }else
            {
                CurVer = key.getNamed("version").Int32();

                Load(CurVer.Value);
            }
        }

        public void Initialize()
        {
            ActivateV1();
            ActivateV2();
        }

        public void ActivateV1()
        {
            CurVer = new VInt32("version", 1);
            HTTPPort = new VInt32("port", HTTPPORT);

            if (!key.HasNamedKey("version"))
                key.Add(CurVer);
            else CurVer = key.getNamed("version").Int32().setInt32(1);


            key.Add(HTTPPort);
        }

        public void ActivateV2()
        {
            CurVer.setInt32(2);
            TCPPort = new VInt32("tcp", TCPPORT);
            HTTPPort.Name = "http";
            UDPPort = new VInt32("udp", UDPPORT);

            key.Add(TCPPort);
            key.Add(UDPPort);
        }

        public void Load(int value)
        {
            switch (value)
            {
                case 1:
                    {
                        HTTPPort = key.getNamed("port").Int32();


                        ActivateV2();

                        break;
                    }
                case 2:
                    {
                        HTTPPort = key.getNamed("http").Int32();
                        UDPPort = key.getNamed("udp").Int32();
                        TCPPort = key.getNamed("tcp").Int32();


                        break;
                    }
            }
        }
    }
}
