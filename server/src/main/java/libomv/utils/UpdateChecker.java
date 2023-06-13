/**
 * Copyright (c) 2006-2014, openmetaverse.org
 * Copyright (c) 2009-2017, Frederick Martian
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the name of the openmetaverse.org or libomv-java project nor the
 *   names of its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.utils;

import java.net.URI;

import com.google.common.util.concurrent.FutureCallback;
import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsClient;

public class UpdateChecker
{
	public class UpdateInfo
	{
	    private boolean Error;
	    private String ErrMessage;
	    private String CurrentVersion;
	    private String DownloadSite;
	    private boolean DisplayMOTD;
	    private String MOTD;
	    private boolean UpdateAvailable;

	    public boolean getError() { return this.Error; }

		public void setError(final boolean value) {
			this.Error = value; }

		public String getErrMessage() { return this.ErrMessage; }

		public void setErrMessage(final String value) {
			this.ErrMessage = value; }

		public String getCurrentVersion() { return this.CurrentVersion; }

		public void setCurrentVersion(final String value) {
			this.CurrentVersion = value; }

		public String getDownloadSite() { return this.DownloadSite; }

		public void setDownloadSite(final String value) {
			this.DownloadSite = value; }

		public boolean getDisplayMOTD() { return this.DisplayMOTD; }

		public void setDisplayMOTD(final boolean value) {
			this.DisplayMOTD = value; }

		public String getMOTD() { return this.MOTD; }

		public void setMOTD(final String value) {
			this.MOTD = value; }

		public boolean getUpdateAvailable() { return this.UpdateAvailable; }

		public void setUpdateAvailable(final boolean value) {
			this.UpdateAvailable = value; }
	}

	public class UpdateCheckerArgs implements CallbackArgs
	{
	    public boolean Success;
	    public UpdateInfo Info;

	    public boolean getSuccess() { return this.Success; }

		public void setSuccess(final boolean value) {
			this.Success = value; }

		public UpdateInfo getInfo() { return this.Info; }

		public void setInfo(final UpdateInfo value) {
			this.Info = value; }
	}

	private final Package Package;

	public CallbackHandler<UpdateCheckerArgs> OnUpdateInfoReceived = new CallbackHandler<UpdateCheckerArgs>(); 
	
    private CapsClient client;

    public UpdateChecker(final Class<?> clazz)
    {
		this.Package = clazz.getPackage();
    }

    public void dispose() throws InterruptedException
    {
        if (null != client)
        {
			this.client.shutdown(true);
			this.client = null;
        }
    }

    /**
     * Compare a new version with the one from this package
     * 
     * @param version Version string in the form <major>.<minor>.<bugfix>.<build>
     * @return true if the version is higher than the current version
     * @throws NumberFormatException
     */
    private boolean isNewerVersion(final String version) throws NumberFormatException
    {
    	final String[] verss = version.split("[\\.\\-]");
    	final String[] impls = this.Package.getImplementationVersion().split("[\\.\\-]");
    	int impl, vers;
    	for (int i = 0; i < verss.length && i < impls.length; i++)
    	{
    		impl = Integer.parseInt(impls[i].trim());
    		vers = Integer.parseInt(verss[i].trim());
    		if (impl != vers)
    			return vers > impl;
    	}
    	return verss.length > impls.length;
    }

}

