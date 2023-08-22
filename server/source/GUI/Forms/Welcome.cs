using Modern.Forms;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.GUI.Forms
{
    public partial class Welcome : Form
    {
        public Welcome()
        {
            InitializeComponent();
        }


        public static void StartMain()
        {
            Application.Run(new Welcome());
        }
    }
}
