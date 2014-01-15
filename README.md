grails-spring-security-oauth-dailymotion
====================================

Dailymotion extension for [Grails Spring Security OAuth][spring-security-oauth-plugin] plugin

Installation
------------

Add the following plugin definition to your BuildConfig:
```groovy
// ...
plugins {
  // ...
  compile ':spring-security-oauth:2.0.2'
  compile ':spring-security-oauth-dailymotion:0.1'
  // ...
}
```

Usage
-----

Add to your Config:
```

oauth {
  // ...
  providers {
    // ...
    dailymotion {
      api = grails.plugin.springsecurity.oauth.DailymotionApi
      key = 'oauth_dailymotion_key'
      secret = 'oauth_dailymotion_secret'
      successUri = '/oauth/dailymotion/success'
      failureUri = '/oauth/dailymotion/error'
      callback = "${baseURL}/oauth/dailymotion/callback"
      scope = 'read write delete email userinfo feed manage_videos manage_comments manage_playlists manage_tiles manage_subscriptions manage_friends manage_favorites manage_groups manage_records manage_subtitles manage_features'

    }
    // ...
  }
}

```

Notes
=====
Respect some other providers Dailymotion has the ablitity to obtain a new Access Token Starting from a Refresh Token created when the user log in succesfully
the first time.
Take a look at refreshToken property in  DailymotionOAuthToken class and getAccessTokenWithRefreshToken public method in CustomDMOAuth20ServiceImpl class.

So I suggest to add some methods the in SpringSecurityOAuthController (or create a new service that take cares of this actions):

```


    def oauthService

    /**
     * Perform a time check of the accessToken expire time. If it is passed it will request a new one and update persisted accessToken
     * @param user the User instance to check
     * @param doReAuthenticate whether the method has to perform a new login programmatic
     */
    void updateUserAccessTokenIfNeeded(User user, boolean doReAuthenticate = false) {
        if (user) {
            if (user.tokenExpireDate.before(new Date(System.currentTimeMillis()))) {
                createNewOAuthTokenFromRefreshToken("dailymotion", user.refreshToken, true, doReAuthenticate)
            }
        }
    }

    /**
     * Create the authentication token in order to proceed with spring security login.
     * This OAuthToken contains all need information to identify a successfully authenticated user.
     * If the user already exist the method update the token info in the persisted User
     *
     * @param providerName
     * @param scribeToken contains info from the oauthProvider after a successful remote login instances
     * @return a new OAuthToken instance
     */
    OAuthToken createAuthToken(String providerName, Token scribeToken, boolean updateInfo = true) {

        if (!providerName.equals("dailymotion"))
            throw new DmApiException("wrong provider configuration")

        OAuthToken oAuthToken = dailymotionSpringSecurityOAuthService.createAuthToken(scribeToken)

        if (updateInfo) {
            def user = User.findByDmId(oAuthToken.socialId)
            if (user) {
                updateOAuthToken(oAuthToken, user)
                updateUserOAuthInfo(oAuthToken, user)
            }
        }

        return oAuthToken
    }

    /**
     * Create the authentication token using the refresh token associated with the user
     * in order to proceed with spring security login.
     * This OAuthToken contains all need information to identify a successfully authenticated user
     *
     * @param providerName
     * @param refreshToken
     * @param updateInfo says if the method have to update info in OAuthToken (with the GrailsUser) and User instances
     * @return a new OAuthToken instance
     */
    OAuthToken createNewOAuthTokenFromRefreshToken(String providerName, String refreshToken,
                                                   boolean updateInfo = true) {

        if (!providerName.equals("dailymotion"))
            throw new DmApiException("wrong provider configuration")

        //this must be a CustomDMOAuth20ServiceImpl instance
        OAuthService oAuthService = oauthService.findService(providerName)

        //get the new token using the refreshToken
        Token token = oAuthService.getAccessTokenWithRefreshToken(refreshToken)

        //perform a regular authentication in order to create a new oAuthToken
        OAuthToken oAuthToken = createAuthToken(providerName, token, updateInfo)

        return oAuthToken
    }

    /**
     * Create a new user instance with the info returned from the Oauth provider. This method is called only the first time
     * the user access to the site with Oauth.
     *
     * @param oAuthToken
     * @return a new User instance
     */
    public User createNewUser(OAuthToken oAuthToken) {
        def user = User.withTransaction { status ->

            User user = new User(id: oAuthToken.dmId, tokenExpireDate: new Date(oAuthToken.expirationDate),
                    accessToken: oAuthToken.accessToken.token, refreshToken: oAuthToken.refreshToken,
                    enabled: true, accountExpired: false, accountLocked: false, passwordExpired: false)

            user.save()

            return user
        }
        updateOAuthToken(oAuthToken, user)
        return user
    }

    /**
     * Update the persisted info about the oauth token into the User
     * @param oAuthToken
     * @param user
     * @return the updated user
     */
    User updateUserOAuthInfo(OAuthToken oAuthToken, User user) {

        return User.withTransaction {
            user.accessToken = oAuthToken.accessToken.token
            user.refreshToken = oAuthToken.refreshToken
            user.tokenExpireDate = new Date((oAuthToken.expirationDate as Long) - 1000 * 60 * 5)
            user.save()
            return user
        }

    }

    /**
     * Update the OAuthToken with the info needed by spring security
     * @param oAuthToken
     * @param user
     * @return
     */
    OAuthToken updateOAuthToken(OAuthToken oAuthToken, User user) {

        oAuthToken.principal = createUserDetails(user)
        oAuthToken.authorities = getUserAuthorities(user)
        oAuthToken.authenticated = true

        return oAuthToken
    }

    /**
     * Create a new instance of GrailsUser class using the given user. Respect the original method this one use the dmId from the domain class
     * as id (instead of the standard id from the GORM convention).
     * @param user the User instance
     * @return an new instance of GrailsUser
     */
    GrailsUser createUserDetails(User user) {

        String usernamePropertyName = conf.userLookup.usernamePropertyName
        String passwordPropertyName = conf.userLookup.passwordPropertyName
        String enabledPropertyName = conf.userLookup.enabledPropertyName
        String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

        String username = user."$usernamePropertyName"
        String password = user."$passwordPropertyName"
        boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
        boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
        boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false


        new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
                !accountLocked, getUserAuthorities(user), user.id)
    }

    def getConf() {
        SpringSecurityUtils.securityConfig
    }

    /**
     * Retrieve the User Authorities using the parameter specified in Config.groovy
     * @param user
     * @return a Collection of GrantedAuthority
     */
    Collection<GrantedAuthority> getUserAuthorities(User user) {
        // authorities
        String authoritiesPropertyName = conf.userLookup.authoritiesPropertyName
        String authorityPropertyName = conf.authority.nameField
        Collection<?> userAuthorities = user."${authoritiesPropertyName}"
        return userAuthorities.collect { new GrantedAuthorityImpl(it."${authorityPropertyName}") }
    }

```

Release Notes
=============

* 0.1   - released 15/01/2014 - this is the first released revision of the plugin.


Credits
=======

This plugin is sponsored by <b>[GameTube]</b>.

[spring-security-oauth-plugin]: https://github.com/enr/grails-spring-security-oauth
[GameTube]: http://www.gametube.org/
