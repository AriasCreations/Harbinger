using Harbinger.EventsBus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Framework.HTTP.Events
{
    [Cancellable()]
    /// <summary>
    /// This event is cancellable.
    /// 
    /// If not cancelled, a not found response code will be sent
    /// </summary>
    public class HTTPRequestEvent : Event
    {
        public HttpListenerContext context;
        public HttpListenerRequest request;

        public HttpListenerResponse response;

        public string requestPath;
        public string responseBody = "None";

        public HTTPRequestEvent(HttpListenerContext context)
        {
            this.context = context;
            request = context.Request;
            response = context.Response;

            requestPath = request.Url?.LocalPath;
        }
    }
}
