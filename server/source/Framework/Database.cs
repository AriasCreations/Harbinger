using Harbinger.Framework.Database;
using TP.CS.Registry;
using SQLite;
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
        public const int VERSION = 1;

        public DB con;

        
        public VInt32 CurVer;
        public Word DatabaseName;

        public Key MY_KEY;
        


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

            con = DB.Instance;
        }

        /// <summary>
        /// Initializes the registry key and migrates to the current version with default values
        /// </summary>
        public void Initialize()
        {
            ActivateV1();
        }

        public void ActivateV1()
        {
            CurVer = new VInt32("version", 1);
            DatabaseName = new Word("dbname", Consts.DatabaseName);

            MY_KEY.Add(CurVer);
            MY_KEY.Add(DatabaseName);
        }


        public void Load(int ver)
        {
            switch(ver)
            {
                case 1:
                    {
                        DatabaseName = MY_KEY.getNamed("dbname").Word();

                        break;
                    }
            }
        }
    }
}
