using Harbinger.EventsBus;
using Harbinger.Framework.Database;
using Harbinger.Framework.Registry;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Structures
{
    public class Server
    {
        public Server()
        {

        }
        public static readonly int VERSION = 1;

        public string Name { get; set; }
        public string URL { get; set; }
        public UUID ID { get; set; }

        /// <summary>
        /// Performs table migrations
        /// </summary>
        public static void Migrate()
        {
            Migration self = Migrations.migrations.Where(x => x.TableName == "Servers").First();
            if(self == null)
            {
                self = new Migration();
                self.TableName = "Servers";
                self.TableVersion = VERSION;

                self.update(0);


                Initialize(self);
            } else
            {
                /*
                 * No current migrations are needed. We are still V1
                switch (self.TableVersion)
                {
                    case 1:
                        {
                            DatabaseConnection.Instance.con.getConnection().CreateTable<Server>();
                            DatabaseConnection.Instance.con.getConnection().Execute("CREATE TABLE IF NOT EXISTS `Server` (text Name, text URL, text ID PRIMARY KEY UNIQUE);");
                            break;
                        }
                }*/
            }
        }

        public static void Initialize(Migration current)
        {
            ActivateV1(current);
        }

        public static void ActivateV1(Migration current)
        {
            current.update(1);

            DatabaseConnection.Instance.con.getConnection().Execute("CREATE TABLE IF NOT EXISTS `Servers` (TEXT Name Primary Key, TEXT URL, TEXT ID UNIQUE");
        }
    }
}
