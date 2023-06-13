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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import libomv.StructuredData.OSD;
import libomv.StructuredData.OSDArray;
import libomv.StructuredData.OSDMap;
import libomv.capabilities.CapsClient;
import libomv.types.UUID;
import libomv.types.Vector3;

public class RegistrationApi
{
    final int REQUEST_TIMEOUT = 1000 * 100;

    private class UserInfo
    {
        public String FirstName;
        public String LastName;
        public String Password;
    }

    private class RegistrationCaps
    {
        public URI CreateUser;
        public URI CheckName;
        public URI GetLastNames;
        public URI GetErrorCodes;
    }

    public class ErrorCode
    {
    	public int Code;
    	public String Name;
    	public String Description;
    	
    	public ErrorCode(final int code, final String name, final String description)
    	{
			this.Code = code;
			this.Name = name;
			this.Description = description;
    	}
    	
    	@Override
		public String toString()
    	{
    		return String.format("Code: %d, Name: %s, Description: %s", this.Code, this.Name, this.Description);
    	}
    }

    // See https://secure-web6.secondlife.com/developers/third_party_reg/#service_create_user or
    // https://wiki.secondlife.com/wiki/RegAPIDoc for description
    public class CreateUserParam
    {
        public String FirstName;
        public int LastNameID;
        public String Email;
        public String Password;
        public Date Birthdate;

        // optional:
        public Integer LimitedToEstate;
        public String StartRegionName;
        public Vector3 StartLocation;
        public Vector3 StartLookAt;
    }

    private final UserInfo _userInfo;
    private RegistrationCaps _caps;
    private int _initializing;
    private Map<Integer, ErrorCode> _errors;
    private Map<String, Integer> _lastNames;

    public boolean getInitializing()
    {
        return (0 > _initializing);
    }

    public RegistrationApi(final String firstName, final String lastName, final String password) throws UnsupportedEncodingException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException
    {
		this._initializing = -2;

		this._userInfo = new UserInfo();

		this._userInfo.FirstName = firstName;
		this._userInfo.LastName = lastName;
		this._userInfo.Password = password;

		this.getCapabilities();
    }

    public void waitForInitialization() throws InterruptedException
    {
        while (this.getInitializing())
            Thread.sleep(10);
    }

    private URI getRegistrationApiCaps() throws URISyntaxException
    {
        return new URI("https://cap.secondlife.com/get_reg_capabilities");
    }

    private void getCapabilities() throws URISyntaxException,  UnsupportedEncodingException, InterruptedException, ExecutionException, TimeoutException
    {
        // build post data
        final String postData = String.format("first_name=%s&last_name=%s&password=%s", this._userInfo.FirstName, this._userInfo.LastName, this._userInfo.Password);

        final Future<OSD> future = new CapsClient(null, "get_reg_capabilities").executeHttpPost(this.getRegistrationApiCaps(), postData, "application/x-www-form-urlencoded", Helpers.UTF8_ENCODING);
        final OSD response = future.get(this.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof final OSDMap respTable)
        {
            // parse
			this._caps = new RegistrationCaps();

			this._caps.CreateUser = respTable.get("create_user").AsUri();
			this._caps.CheckName = respTable.get("check_name").AsUri();
			this._caps.GetLastNames = respTable.get("get_last_names").AsUri();
			this._caps.GetErrorCodes = respTable.get("get_error_codes").AsUri();

            // finalize
			this._initializing++;

			this._errors = this.getErrorCodes(this._caps.GetErrorCodes);
        }
    }

    /**
     * Retrieves a list of error codes, and their meaning, that the RegAPI can return.
     *
     * @param capability the capability URL for the "get_error_codes" RegAPI function.
     * @return a mapping from error codes (as a number) to an ErrorCode object
     * which contains more detail on that error code.
     */
    private Map<Integer, ErrorCode> getErrorCodes(final URI capability) throws InterruptedException, ExecutionException, TimeoutException
    {
        Map<Integer, ErrorCode> errorCodes = new HashMap<Integer, ErrorCode>();

        final Future<OSD> future = new CapsClient(null, "getErrorCodes").executeHttpGet(capability);
        final OSD response = future.get(this.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof final OSDArray respTable)
        {

            for (final Iterator<OSD> iter = respTable.iterator(); iter.hasNext();)
            {
            	final OSDArray errors = (OSDArray)iter.next();

            	errorCodes.put(errors.get(0).AsInteger(), new ErrorCode(errors.get(0).AsInteger(), errors.get(1).AsString(), errors.get(2).AsString()));
            }

            // finalize
			this._initializing++;
        }
        return errorCodes;
    }

    /**
     * Retrieves a list of valid last names for newly created accounts.
     *
     * @param capability the capability URL for the "get_last_names" RegAPI function.
     * @return a mapping from last names, to their ID (needed for createUser()).
     */
    private Map<String, Integer> getLastNames(final URI capability) throws  InterruptedException, ExecutionException, TimeoutException
    {
        SortedMap<String, Integer> lastNames = new TreeMap<String, Integer>();
        
        final Future<OSD> future = new CapsClient(null, "getLastNames").executeHttpGet(capability);
        final OSD response = future.get(this.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof final OSDMap respTable)
        {

            for (final Map.Entry<String, OSD> entry : respTable.entrySet())
            {
            	lastNames.put(entry.getValue().AsString(), Integer.valueOf(entry.getKey()));
            }
        }
        return lastNames;
    }

    /**
     * Retrieves a list of valid last names for newly created accounts.
     *
     * @return a mapping from last names, to their ID (needed for createUser()).
     */
    public synchronized Map<String, Integer> getLastNames() throws  InterruptedException, ExecutionException, TimeoutException
    {
        if (0 >= _lastNames.size())
        {
            if (this.getInitializing())
                throw new IllegalStateException("still initializing");

            if (null == _caps.GetLastNames)
                throw new UnsupportedOperationException("access denied: only approved developers have access to the registration api");

			this._lastNames = this.getLastNames(this._caps.GetLastNames);
        }
        return this._lastNames;
    }

    /**
     * Checks whether a name is already used in Second Life.
     *
     * @param firstName of the name to check.
     * @param lastNameID the ID (see getLastNames() for the list of valid last name IDs) to check.
     * @return true if they already exist, false if the name is available.
     * @throws Exception 
     */
    public boolean checkName(final String firstName, final int lastNameID) throws Exception
    {
        if (this.getInitializing())
            throw new IllegalStateException("still initializing");

        if (null == _caps.CheckName)
            throw new UnsupportedOperationException("access denied; only approved developers have access to the registration api");

        // Create the POST data
        final OSDMap query = new OSDMap();
        query.put("username", OSD.FromString(firstName));
        query.put("last_name_id", OSD.FromInteger(lastNameID));

        final Future<OSD> future = new CapsClient(null, "checkName").executeHttpPost(this._caps.GetLastNames, query, OSD.OSDFormat.Xml);
        final OSD response = future.get(this.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (OSD.OSDType.Boolean != response.getType())
        	throw new Exception("check_name did not return a boolean as the only element inside the <llsd> tag.");
       	return response.AsBoolean();
    }

	/**
     * Returns the new user ID or throws an exception containing the error code
     * The error codes can be found here: https://wiki.secondlife.com/wiki/RegAPIError
     *
     * @param user New user account to create
     * @returns The UUID of the new user account
	 * @throws Exception
     */
    public UUID createUser(final CreateUserParam user) throws Exception
    {
        if (this.getInitializing())
            throw new IllegalStateException("still initializing");

        if (null == _caps.CreateUser)
            throw new UnsupportedOperationException("access denied; only approved developers have access to the registration api");

        // Create the POST data
        final OSDMap query = new OSDMap();
        query.put("username", OSD.FromString(user.FirstName));
        query.put("last_name_id", OSD.FromInteger(user.LastNameID));
        query.put("email", OSD.FromString(user.Email));
        query.put("password", OSD.FromString(user.Password));
        query.put("dob", OSD.FromString(new SimpleDateFormat("yyyy-MM-dd").format(user.Birthdate)));

        if (null != user.LimitedToEstate)
            query.put("limited_to_estate", OSD.FromInteger(user.LimitedToEstate));

        if (null != user.StartRegionName && !user.StartRegionName.isEmpty())
            query.put("start_region_name", OSD.FromString(user.StartRegionName));

        if (null != user.StartLocation)
        {
            query.put("start_local_x", OSD.FromReal(user.StartLocation.X));
            query.put("start_local_y", OSD.FromReal(user.StartLocation.Y));
            query.put("start_local_z", OSD.FromReal(user.StartLocation.Z));
        }

        if (null != user.StartLookAt)
        {
            query.put("start_look_at_x", OSD.FromReal(user.StartLookAt.X));
            query.put("start_look_at_y", OSD.FromReal(user.StartLookAt.Y));
            query.put("start_look_at_z", OSD.FromReal(user.StartLookAt.Z));
        }

        // Make the request
        final Future<OSD> future = new CapsClient(null, "createUser").executeHttpPost(this._caps.CreateUser, query, OSD.OSDFormat.Xml);
        final OSD response = future.get(this.REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
        if (response instanceof final OSDMap map)
        {
            return map.get("agent_id").AsUUID();
        }
        
		// an error happened
		final OSDArray al = (OSDArray)response;

		final StringBuilder sb = new StringBuilder();

		for (final OSD ec : al)
		{
		    if (0 < sb.length())
		        sb.append("; ");

		    sb.append(this._errors.get(ec.AsInteger()));
		}
		throw new Exception("failed to create user: " + sb);
    }
}