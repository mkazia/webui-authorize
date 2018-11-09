/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudera.ps.authentication;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.hadoop.security.authentication.server.AuthenticationToken;
import org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler;
import org.apache.hadoop.security.authorize.AccessControlList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * This class extends {@link KerberosAuthenticationHandler} and overrides authenticate method
 * The authentication offered by this class delegates the parent class  {@Link KerberosAuthenticationHandler} to
 * authenticate the user with SPENGO. Once Authenticated, it checks if the user is authorized against a configured ACL.
 * This class borrows most of its functionality from  {@Link AltKerberosAuthenticationHandler}
 */

public class WebUIAuthorizationHandler extends KerberosAuthenticationHandler {
    private static final String AUTHORIZED_ACL = "alt-kerberos.authorize.acl";
    private static final String AUTHORIZED_ACL_DEFAULT = "*";
    private static final Logger LOG = LoggerFactory.getLogger(WebUIAuthorizationHandler.class);
    private AccessControlList authorizedAcl;

    /**
     * Constant that identifies the authentication mechanism.
     */
    private static final String TYPE = "alt-kerberos";

    /**
     * Constant for the configuration property that indicates which user agents
     * are not considered browsers (comma separated)
     */
    private static final String NON_BROWSER_USER_AGENTS =
            TYPE + ".non-browser.user-agents";
    private static final String NON_BROWSER_USER_AGENTS_DEFAULT =
            "java,curl,wget,perl,python";

    private String[] nonBrowserUserAgents;

    /**
     * Returns the authentication type of the authentication handler,
     * 'alt-kerberos'.
     *
     * @return the authentication type of the authentication handler,
     * 'alt-kerberos'.
     */
    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void init(Properties config) throws ServletException {

        super.init(config);

        authorizedAcl = new AccessControlList(config.getProperty(AUTHORIZED_ACL, AUTHORIZED_ACL_DEFAULT));

        nonBrowserUserAgents = config.getProperty(
                NON_BROWSER_USER_AGENTS, NON_BROWSER_USER_AGENTS_DEFAULT)
                .split("\\W*,\\W*");
        for (int i = 0; i < nonBrowserUserAgents.length; i++) {
            nonBrowserUserAgents[i] = nonBrowserUserAgents[i].toLowerCase();
        }
    }

    /**
     * Implementation of the custom authentication. The authentication delegated to the parent class
     * {@Link org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler} to authenticate the user
     * with SPENGO. Once Authenticated, it checks if the user is authorized against a configured ACL.
     *
     * @param request the HTTP client request.
     * @param response the HTTP client response.
     * @return an authentication token if the request is authorized, or null
     * @throws IOException thrown if an IO error occurs
     * @throws AuthenticationException thrown if an authentication error occurs
     */
    @Override
    public AuthenticationToken authenticate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, AuthenticationException {

        AuthenticationToken token  = super.authenticate(request, response);
        if (token != null && isBrowser(request.getHeader("User-Agent")))  {
            String user = token.getUserName();
            if (authorizedAcl.isUserAllowed(UserGroupInformation.createRemoteUser(user))) {
                return token;
            } else {
                return null;
            }

        }

        return token;
    }

    /**
     * This method parses the User-Agent String and returns whether or not it
     * refers to a browser.  If its not a browser, then Kerberos authentication
     * will be used; if it is a browser, alternateAuthenticate from the subclass
     * will be used.
     * <p>
     * A User-Agent String is considered to be a browser if it does not contain
     * any of the values from alt-kerberos.non-browser.user-agents; the default
     * behavior is to consider everything a browser unless it contains one of:
     * "java", "curl", "wget", or "perl".  Subclasses can optionally override
     * this method to use different behavior.
     *
     * @param userAgent The User-Agent String, or null if there isn't one
     * @return true if the User-Agent String refers to a browser, false if not
     */
    private boolean isBrowser(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        userAgent = userAgent.toLowerCase();
        LOG.debug("User-Agent: " + userAgent);
        boolean isBrowser = true;
        for (String nonBrowserUserAgent : nonBrowserUserAgents) {
            if (userAgent.contains(nonBrowserUserAgent)) {
                isBrowser = false;
                break;
            }
        }
        return isBrowser;
    }

}
