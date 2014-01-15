/*
 * Copyright (c) 2014 GAMETUBE.
 */

package grails.plugin.springsecurity.oauth

import org.scribe.builder.api.DefaultApi20
import org.scribe.extractors.AccessTokenExtractor
import org.scribe.extractors.JsonTokenExtractor
import org.scribe.model.OAuthConfig
import org.scribe.model.Verb
import org.scribe.oauth.OAuthService
import org.scribe.utils.OAuthEncoder
import org.scribe.utils.Preconditions

/**
* Contains all the configuration needed to instantiate a valid {@link OAuthService}
*/
public class DailymotionOAuthApi extends DefaultApi20 {

    private static final String AUTHORIZE_URL = "https://api.dailymotion.com/oauth/authorize?display=popup&response_type=code&grant_type=authorization_code&client_id=%s&redirect_uri=%s";
    private static final String SCOPED_AUTHORIZE_URL = AUTHORIZE_URL + "&scope=%s";

    /**
     * {@inheritDoc}
     */
    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAccessTokenEndpoint() {
        return "https://api.dailymotion.com/oauth/token";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback. Facebook does not support OOB");

        // Append scope if present
        if (config.hasScope()) {
            return String.format(SCOPED_AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()), OAuthEncoder.encode(config.getScope()));
        } else {
            return String.format(AUTHORIZE_URL, config.getApiKey(), OAuthEncoder.encode(config.getCallback()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }

    /**
     * {@inheritDoc}
     */
    public OAuthService createService(OAuthConfig config) {
        return new CustomDMOAuth20ServiceImpl(this, config);
    }
}
