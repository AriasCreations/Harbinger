﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.EventBus
{
    [AttributeUsage(AttributeTargets.Class)]
    public class CancellableAttribute : Attribute
    {
    }
}