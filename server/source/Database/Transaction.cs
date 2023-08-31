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
        private Connection conn;
        public XTransaction(Connection con)
        {
            conn = con;
        }

        public XTransaction(ConnectionBuilder builder)
        {
            conn = builder.build();

        }

        public DbTransaction GetTransaction()
        {
            return conn.connection.BeginTransaction();
        }
    }
}
