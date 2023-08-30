using System;
using System.Collections.Generic;
using System.Linq;
using System.Data.SQLite.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Transactions;
using System.Data.Common;

namespace Harbinger.Framework.Database
{
    public class XTransaction
    {
        private Connection connection;
        public XTransaction(Connection con)
        {
            connection = con;
        }

        public XTransaction(ConnectionBuilder builder)
        {
            connection = builder.build();

        }

        public DbTransaction GetTransaction()
        {
            return connection.connection.BeginTransaction();
        }
    }
}
