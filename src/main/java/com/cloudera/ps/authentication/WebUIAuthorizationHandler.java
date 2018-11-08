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
import org.apache.hadoop.security.authentication.server.AltKerberosAuthenticationHandler;
import org.apache.hadoop.security.authentication.server.AuthenticationToken;
import org.apache.hadoop.security.authorize.AccessControlList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

/**
 * This class provides an implementation of the {@link AltKerberosAuthenticationHandler}
 * The alternate authentication offered by this class delegates the parent class
 * {@Link org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler}
 * to authenticate the user with SPENGO. Once Authenticated, it checks if the user is authorized against a configured
 * ACL.
 */

public class WebUIAuthorizationHandler extends AltKerberosAuthenticationHandler {
    private static final String AUTHORIZED_ACL = "alt-kerberos.authorize.acl";
    private static final String AUTHORIZED_ACL_DEFAULT = "*";
    private static Logger LOG = LoggerFactory.getLogger(WebUIAuthorizationHandler.class);
    private AccessControlList authorizedAcl;

    @Override
    public void init(Properties config) throws ServletException {

        super.init(config);

        authorizedAcl = new AccessControlList(config.getProperty(AUTHORIZED_ACL, AUTHORIZED_ACL_DEFAULT));

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
    public AuthenticationToken alternateAuthenticate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, AuthenticationException {

        AuthenticationToken token  = super.authenticate(request, response);
        if (token != null) {
            String user = token.getUserName();
            if (authorizedAcl.isUserAllowed(UserGroupInformation.createRemoteUser(user))) {
                return token;
            }
        }

        return null;
    }

}
