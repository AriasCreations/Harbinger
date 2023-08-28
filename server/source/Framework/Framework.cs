using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Harbinger.Framework
{
    public class Framework
    {
        public static CancellationTokenSource keepAlive = new CancellationTokenSource();
        /// <summary>
        /// This exists only to make sure dotnet loads this library... ugh
        /// </summary>
        public static void init()
        {

        }
    }
}
