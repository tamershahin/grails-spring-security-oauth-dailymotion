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

import org.scribe.model.Token


/**
 * Spring Security authentication token for Dailymotion users. It's a standard {@link OAuthToken}
 * that returns the Dailymotion name as the principal.
 *
 * @author <a href='mailto:tamer.shahin@gmail.com'>Tamer Shahin</a>
 * @author Thierry Nicola
 */
class DailymotionOAuthToken extends OAuthToken {

    public static final long serialVersionUID = 20130920230L
    public static final String PROVIDER_NAME = "dailymotion"

    Long expirationDate
    String refreshToken
    String profileId
    String screenname


    DailymotionOAuthToken(Token accessToken) {
        super(accessToken)
        this.refreshToken = tokenParams.refresh_token
        this.expirationDate = ((tokenParams.expires_in as Long) * 1000) + System.currentTimeMillis()
        //the principal can hold any object, usually an ID or a GrailsUser object
        this.principal = this.tokenParams["uid"]
        this.profileId = this.tokenParams["uid"]
    }

    String getScreenName() {
        return screenname
    }

    String setScreenName(String screenName) {
        screenname = screenName
    }

    /**
     * Get the social ID for this provider. The socialId for this implementation of OAuthToken will be the dmId: an id
     * created from Dailymotion to identify each
     * entity and used also all in dmg platform
     * @return the a String containing the dmId
     */
    String getSocialId() {
        return profileId
    }

    /**
     * Returns the provider name of the OAuth provider compatible with this OAuthToken
     * @return a String containing the name of the Oauth provider
     */
    String getProviderName() {
        return PROVIDER_NAME
    }

}
