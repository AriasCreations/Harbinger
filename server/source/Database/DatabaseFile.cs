using Harbinger.Framework.Registry;
using SQLite;
using System.IO;

namespace Harbinger.Framework.Database
{
    public class DB
    {
        private static readonly object lck = new object();
        private static DB _con;
        
        public static DB Instance
        {
            get
            {
                lock (lck)
                {
                    if(_con == null)
                    {
                        _con = new DB();
                    }
                    return _con;
                }
            }
        }

        private SQLiteConnection connection;

        public DB connect(string DBName)
        {
            lock (lck)
            {
                connection = new SQLiteConnection(DBName);

                return this;
            }
        }

        public SQLiteConnection getConnection()
        {
            return connection;
        }
    }
}
