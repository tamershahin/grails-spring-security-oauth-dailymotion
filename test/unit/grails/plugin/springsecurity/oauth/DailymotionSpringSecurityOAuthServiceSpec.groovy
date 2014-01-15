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
import spock.lang.Specification

class DailymotionSpringSecurityOAuthServiceSpec extends Specification {

    DailymotionSpringSecurityOAuthService service

    def setup() {
        service = new DailymotionSpringSecurityOAuthService()
    }

    def "should throw OAuthLoginException for unexpected response"() {
        given:
        def exception = null
        def oauthAccessToken = new Token('token', 'secret', rawData)
        and:
        try {
            def token = service.createAuthToken(oauthAccessToken)
        } catch (Throwable throwable) {
            exception = throwable
        }
        expect:
        exception instanceof OAuthLoginException
        where:
        rawData                                                                                                                          | _
        ''                                                                                                                               | _
        null                                                                                                                             | _
        '{}'                                                                                                                             | _
        '{"access_token":"dHNADgRCXhsEFRpJWwoeB1MdDVVRCE","expires_in":36000,"refresh_token":"ed2484f7970115cca7e577b19cce1c3a50f843f"}' | _
    }

    def "should return the correct OAuth token"() {
        given:
        def rawData = '''
{"access_token":"dHNADgRCXhsEFRpJWwoeB1MdDVVRCE","expires_in":36000,"refresh_token":"ed2484f7970115cca7e577b19cce1c3a50f843f","scope":
"read delete email manage_videos manage_comments manage_playlists "
,"uid":"xkjll"}'''
        def oauthAccessToken = new Token('dHNADgRCXhsEFRpJWwoeB1MdDVVRCE', 'secret', rawData)
        when:
        def token = service.createAuthToken(oauthAccessToken)
        then:
        token.refreshToken == 'ed2484f7970115cca7e577b19cce1c3a50f843f'
        token.accessToken.token == 'dHNADgRCXhsEFRpJWwoeB1MdDVVRCE'
        token.socialId == 'xkjll'
        token.providerName == 'dailymotion'
    }
}
