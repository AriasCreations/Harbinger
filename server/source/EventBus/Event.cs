using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventBus
{
    public class Event
    {
        public Event() { }
        

        public bool isCancellable()
        {
            var cnl = GetType().GetCustomAttribute<CancellableAttribute>();
            if (cnl == null)
            {
                return false;
            }
            else return true;
        }
        private bool cancel { get; set; } = false;

        public void setCancelled(bool cancel)
        {
            if(isCancellable())
            {
                this.cancel = cancel;
            }
        }


        public bool isCancelled()
        {
            return cancel;
        }
        
    }


    public enum Priority : int
    {
        Low = 0,
        Medium = 1,
        High = 2,
        Very_High = 3,
        Severe = 4,

        Uncategorizable=99
    }
}
