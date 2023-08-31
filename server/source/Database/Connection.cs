using System;
using System.Collections.Generic;
using System.ComponentModel.Design;
using System.Data.Common;
using System.Data.SqlClient;
using System.Data.SQLite;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.Database
{
    public class Connection
    {
        internal string DatabaseName;
        internal string Username;
        internal string Password;

        internal string Host;


        public DbConnection connection;


        public enum DatabaseType : byte
        {
            SQLite,
            Registry,
            MySQL
        }


        internal DatabaseType DbType;


        internal Connection()
        {

        }



        public void Close()
        {
            connection.Close();
        }
    }


    public class ConnectionBuilder
    {
        public ConnectionBuilder()
        {
            connection = new Connection();
        }

        private Connection connection;

        public ConnectionBuilder withDatabase(string file)
        {
            connection.DatabaseName = file;
            return this;
        }

        public ConnectionBuilder withUserName(string userName)
        {
            connection.Username = userName;
            return this;
        }

        public ConnectionBuilder withPassword(string password)
        {
            connection.Password = password;
            return this;
        }

        public ConnectionBuilder withHost(string host)
        {
            connection.Host = host;
            return this;
        }

        public ConnectionBuilder withDbType(Connection.DatabaseType databaseType)
        {
            connection.DbType = databaseType;
            return this;
        }

        public Connection build()
        {
            if(connection.DbType == Connection.DatabaseType.SQLite)
            {
                connection.connection = new SQLiteConnection($"Data Source={connection.DatabaseName}; Version=3;");
            } else if(connection.DbType == Connection.DatabaseType.MySQL)
            {
                connection.connection = new SqlConnection($"Server={connection.Host}; Uid={connection.Username}; Pwd={connection.Password}; Database={connection.DatabaseName}");
            } else if(connection.DbType == Connection.DatabaseType.Registry)
            {
                connection.connection = new RegistryConnection($"{connection.DatabaseName}");
            }
                

            connection.connection.Open();
            
            return connection;
        }
    }
}
