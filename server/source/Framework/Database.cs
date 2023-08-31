using Harbinger.Framework.Database;
using Harbinger.Framework.Registry;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework
{


    public class DatabaseConnection
    {
        private static readonly object lck = new object();
        private static DatabaseConnection instance;
        public static DatabaseConnection Instance
        {
            get
            {
                lock (lck)
                {
                    if (instance != null) return instance;
                    instance = new DatabaseConnection();
                    return instance;
                }
            }
            set
            {
                instance = value;
            }
        }
        public const string KEY = "root/HKS/database";
        public const int VERSION = 2;

        public Connection conn;

        public VInt32 CurVer;
        public Word DatabaseName;
        public Word Username;
        public Word Password;
        public Word Host;

        public Key MY_KEY;

        public VByte DBType;
        


        public DatabaseConnection()
        {
            MY_KEY = Entry.getByPath(KEY)?.Key();

            if(MY_KEY == null)
            {
                Initialize();
            }
            else
            {
                CurVer = MY_KEY.getNamed("version").Int32();
                Load(CurVer.Value);
            }

            conn = new ConnectionBuilder().withPassword(Password.Value).withDatabase(DatabaseName.Value).withDbType((Connection.DatabaseType)DBType.Value).withHost(Host.Value).withUserName(Username.Value).build();
        }

        /// <summary>
        /// Initializes the registry key and migrates to the current version with default values
        /// </summary>
        public void Initialize()
        {
            ActivateV1();
            ActivateV2();
        }

        public void ActivateV1()
        {
            CurVer = new VInt32("version", 1);
            DatabaseName = new Word("dbname", "Harbinger");
            Username = new Word("user", "");
            Password = new Word("pass", "");
            Host = new Word("host", "");

            MY_KEY.Add(CurVer);
            MY_KEY.Add(DatabaseName);
            MY_KEY.Add(Username);
            MY_KEY.Add(Password);
            MY_KEY.Add(Host);
        }

        public void ActivateV2()
        {
            CurVer.setInt32(2);

            DBType = new VByte("dbtype", (byte)DatabaseType.Registry);

            MY_KEY.Add(DBType);
        }

        public void Load(int ver)
        {
            switch(ver)
            {
                case 1:
                    {
                        DatabaseName = MY_KEY.getNamed("dbname").Word();
                        Username = MY_KEY.getNamed("user").Word();
                        Password = MY_KEY.getNamed("pass").Word();
                        Host = MY_KEY.getNamed("host").Word();

                        break;
                    }
                case 2:
                    {

                        DatabaseName = MY_KEY.getNamed("dbname").Word();
                        Username = MY_KEY.getNamed("user").Word();
                        Password = MY_KEY.getNamed("pass").Word();
                        Host = MY_KEY.getNamed("host").Word();

                        DBType = MY_KEY.getNamed("dbtype").Byte();
                        break;
                    }
            }
        }
    }
}
