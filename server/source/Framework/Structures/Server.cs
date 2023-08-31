using Harbinger.EventsBus;
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
        public static readonly int VERSION = 1;

        public string Name { get; set; }
        public string URL { get; set; }
        public UUID ID { get; set; }


    }
}
