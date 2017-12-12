/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.auth.introspection.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.auth.introspection.IntrospectionException;
import org.wso2.carbon.auth.introspection.TokenValidator;
import org.wso2.carbon.auth.introspection.TokenValidatorHandler;
import org.wso2.carbon.auth.introspection.dto.IntrospectionContext;
import org.wso2.carbon.auth.introspection.dto.IntrospectionResponse;
import org.wso2.carbon.auth.oauth.TokenManager;
import org.wso2.carbon.auth.oauth.dto.AccessTokenDTO;
import org.wso2.carbon.auth.oauth.impl.TokenManagerImpl;

/**
 * Default TokenValidatorHandler implementation
 */
public class TokenValidatorHandlerImpl implements TokenValidatorHandler {
    private static final Logger log = LoggerFactory.getLogger(TokenValidatorHandlerImpl.class);

    @Override
    public void validate(IntrospectionContext context) throws IntrospectionException {
        TokenValidator tokenValidator = new OAuth2TokenValidator();
        if (!tokenValidator.validateAccessToken(context)) {
            buildIntrospectionError(context, "Access token validation failed");
            return;
        }

        AccessTokenDTO accessTokenDTO = findAccessToken(context.getAccessToken());
        log.info(accessTokenDTO.getAccessToken() + " " + accessTokenDTO.getConsumerKey());
        if (hasAccessTokenExpired(accessTokenDTO)) {
            buildIntrospectionError(context, "Access token expired");
            return;
        }
        IntrospectionResponse introspectionResponse = new IntrospectionResponse();

        // should be in seconds
        introspectionResponse
                .setExp((accessTokenDTO.getValidityPeriod() * 1000L + accessTokenDTO.getTimeCreated()) / 1000);
        // should be in seconds
        introspectionResponse.setIat(accessTokenDTO.getTimeCreated() / 1000);
        // token scopes
        introspectionResponse.setScope("");
        // set user-name
        introspectionResponse.setUsername("");
        // add client id
        introspectionResponse.setClientId(accessTokenDTO.getConsumerKey());

        context.setIntrospectionResponse(introspectionResponse);
        // adding the AccessTokenDO as a context property for further use
        //        messageContext.addProperty("AccessTokenDO", accessTokenDO);

        if (!tokenValidator.validateAccessDelegation(context)) {
            buildIntrospectionError(context, "Invalid access delegation");
            return;
        }

        if (!tokenValidator.validateScope(context)) {
            buildIntrospectionError(context, "Scope validation failed");
            return;
        }

        context.getIntrospectionResponse().setActive(true);
    }

    private void buildIntrospectionError(IntrospectionContext context, String errorMessage) {
        if (log.isDebugEnabled()) {
            log.debug(errorMessage);
        }
        context.getIntrospectionResponse().setActive(false);
        context.getIntrospectionResponse().setError(errorMessage);
    }

    private AccessTokenDTO findAccessToken(String tokenIdentifier) throws IntrospectionException {
        TokenManager tokenManager = new TokenManagerImpl();
        return tokenManager.getTokenInfo(tokenIdentifier);
    }

    private boolean hasAccessTokenExpired(AccessTokenDTO accessTokenDTO) {
        // check whether the grant is expired
        if (accessTokenDTO.getValidityPeriod() < 0) {
            if (log.isDebugEnabled()) {
                log.debug("Access Token has infinite lifetime");
            }
        } else {
            if (getAccessTokenExpireMillis(accessTokenDTO) == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Access Token has expired");
                }
                return true;
            }
        }
        return false;
    }

    public static long getAccessTokenExpireMillis(AccessTokenDTO accessTokenDTO) {

        if (accessTokenDTO == null) {
            throw new IllegalArgumentException("accessTokenDO is " + "\'NULL\'");
        }
        long validityPeriodMillis = accessTokenDTO.getValidityPeriod() * 1000L;

        if (validityPeriodMillis < 0) {
            log.debug("Access Token has infinite lifetime");
            return -1;
        }

        long issuedTime = accessTokenDTO.getTimeCreated();
        long validityMillis = calculateValidityInMillis(issuedTime, validityPeriodMillis);
        if (validityMillis > 1000) {
            return validityMillis;
        } else {
            return 0;
        }
    }

    public static long calculateValidityInMillis(long issuedTimeInMillis, long validityPeriodMillis) {
        long timestampSkew = 5 * 1000;
        return issuedTimeInMillis + validityPeriodMillis - (System.currentTimeMillis() - timestampSkew);
    }
}