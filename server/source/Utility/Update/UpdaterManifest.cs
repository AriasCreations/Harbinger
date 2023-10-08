﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Harbinger.Updater
{
    internal class UpdaterManifest
    {
        public ParticleManifest particle = new ParticleManifest();
        public List<RemoteFile> remoteFiles = new List<RemoteFile>();

        /// <summary>
        /// A list dedicated just to the Updater component
        /// </summary>
        public List<RemoteFile> updaterFiles = new();
    }

    internal class RemoteFile
    {
        public string localPath;
        public string remotePath;
    }
}
