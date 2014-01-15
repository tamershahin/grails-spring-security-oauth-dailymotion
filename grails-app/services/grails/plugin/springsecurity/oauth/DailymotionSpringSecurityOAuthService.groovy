/*
 * Copyright 2012 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springsecurity.oauth

import grails.converters.JSON
import org.scribe.model.Token

/**
 * @author Tamer Shahin(tamer.shahin@gmail.com)
 * Utility service that handles necessary data concerning the Spring Security authentication
 */
class DailymotionSpringSecurityOAuthService {

    /**
     * Return the DailymotionOAuthToken containing the authenticated user's info.
     * This token must be used to perform the authentication in spring security.
     * @param accessToken the Token instance created after a successful authentication with OAuth provider
     * @return a new DailymotionOAuthToken
     */
    DailymotionOAuthToken createAuthToken(Token accessToken) {

        DailymotionOAuthToken oauthToken
        try {
            oauthToken = new DailymotionOAuthToken(convertTokenFromJsonToUrlEncoded(accessToken))
        } catch (Throwable e) {
            log.debug "AccessToken not present or not correct! ${accessToken}", e
            throw new OAuthLoginException("Error creating the access token: " + e.toString())
        }
        if (!oauthToken?.profileId) {
            log.debug "No user id from Dailymotion. Response: \n${oauthToken}"
            throw new OAuthLoginException("No user id from Facebook")
        }
        return oauthToken
    }

    /**
     * Converts the token provided from Dailymotion (JSON) in a form usable by Spring Security (URLENCODED).
     * The DailymotionOAuthToken extends OAuthToken. The latter doesn't accept json as rawResponse so we 
     * must urlEncode it before creating the any entity because everything happens in the constructor.
     * @return a Token instance as needed by the OAuthToken constructor.
     */
    private static Token convertTokenFromJsonToUrlEncoded(Token accessToken) {

        Map response = JSON.parse(accessToken.rawResponse)

        Token newToken = new Token(accessToken.token, accessToken.secret,
                response.collect { k, v ->
                    return "${k}=${v}"
                }.join("&")
        )
        return newToken
    }
}

