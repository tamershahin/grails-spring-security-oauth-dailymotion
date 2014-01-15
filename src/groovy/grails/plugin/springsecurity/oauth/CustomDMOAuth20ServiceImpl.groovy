/*
 * Copyright (c) 2014 GAMETUBE.
 */

package grails.plugin.springsecurity.oauth

import org.scribe.builder.api.DefaultApi20
import org.scribe.model.OAuthConfig
import org.scribe.model.OAuthConstants
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.oauth.OAuthService

/**
 * Implementation for the OAuthService for Dailymotion OAUTH provider.
 */
public class CustomDMOAuth20ServiceImpl implements OAuthService {
    private static final String VERSION = "2.0";

    private final DefaultApi20 api;
    private final OAuthConfig config;

    /**
     * Default constructor
     *
     * @param api OAuth2.0 api information
     * @param config OAuth 2.0 configuration param object
     */
    public CustomDMOAuth20ServiceImpl(DefaultApi20 api, OAuthConfig config) {
        this.api = api;
        this.config = config;
    }

    /**
     * Perform the actual request for the accessToken against the OAUTH provider.
     * Modified to use the body parameters for POST verb. The one that this method overrides use to urlencode parameter
     * anyway
     *
     * @param requestToken the request token provided from the provider for this request
     * @param verifier standard verifier
     */
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
        request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
        request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
        request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
        request.addBodyParameter(DailymotionOAuthConstants.GRANT_TYPE, DailymotionOAuthConstants.AUTHORIZATION_CODE);
        if (config.hasScope()) request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());
        Response response = request.send();
        return api.getAccessTokenExtractor().extract(response.getBody());
    }

    /**
     * Use the refreshToken to request a new accessToken from the OAUTH provider.
     *
     * @param refreshToken
     * @return
     */
    public Token getAccessTokenWithRefreshToken(String refreshToken) {
        OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
        request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
        request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
        request.addBodyParameter(DailymotionOAuthConstants.GRANT_TYPE, DailymotionOAuthConstants.REFRESH_TOKEN);
        request.addBodyParameter(DailymotionOAuthConstants.REFRESH_TOKEN, refreshToken);
        if (config.hasScope()) request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());
        Response response = request.send();
        return api.getAccessTokenExtractor().extract(response.getBody());
    }

    /**
     * {@inheritDoc}
     */
    public Token getRequestToken() {
        throw new UnsupportedOperationException("Unsupported operation, please use 'getAuthorizationUrl' and redirect your users there");
    }

    /**
     * {@inheritDoc}
     */
    public String getVersion() {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    public void signRequest(Token accessToken, OAuthRequest request) {
        request.addQuerystringParameter(OAuthConstants.ACCESS_TOKEN, accessToken.getToken());
    }

    /**
     * {@inheritDoc}
     */
    public String getAuthorizationUrl(Token requestToken) {
        return api.getAuthorizationUrl(config);
    }

}