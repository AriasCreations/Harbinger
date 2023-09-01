using Harbinger.Framework.Database;
using SQLite;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Structures
{
    
    public class Migration
    {
        public string TableName;
        public int TableVersion;

        public void update(int newVer)
        {
            Console.Write($"Migrating {TableName} to {TableVersion}... ");
            TableVersion = newVer;

            DB.Instance.getConnection().InsertOrReplace(this);


            Console.WriteLine("\t[OK]");
        }
    }

    public class Migrations
    {
        public static List<Migration> migrations = new();

        public static Migration getTableMigrationStep<T>()
        {
            return DatabaseConnection.Instance.con.getConnection().Table<Migration>().Where(x => x.TableName == typeof(T).Name).First();
        }
    }
}
