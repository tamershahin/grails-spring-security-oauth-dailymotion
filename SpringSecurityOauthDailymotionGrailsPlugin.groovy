/**
* @author <a href='mailto:tamer.shahin@gmail.com'>Tamer Shahin</a>
*/
class SpringSecurityOauthDailymotionGrailsPlugin {
    def version = "0.1"
    def grailsVersion = "1.2.2 > *"
    def dependsOn = [springSecurityOauth: '2.0.2 > *']
    def pluginExcludes = [
            "grails-app/domain/**", "grails-app/views/**", "grails-app/controllers/**"
    ]

    def title = "Dailymotion for Spring Security OAuth plugin"
    def author = "Tamer Shahin"
    def authorEmail = "tamer.shahin@gmail.com"
    def description = '''\
Integrate [Dailymotion|http://www.dailymotion.com] to [Spring Security OAuth plugin|http://grails.org/plugin/spring-security-oauth].
'''

    def documentation = "https://github.com/tamershahin/grails-spring-security-oauth-dailymotion"

    def license = "APACHE"

    def organization = [name: "GameTube SAS", url: "http://www.gametube.org/"]
    def developers = [[name: "Germán Sancho", email: "german@gametube.org"]]
    def issueManagement = [system: "GITHUB", url: "https://github.com/tamershahin/grails-spring-security-oauth-dailymotion/issues"]

    def scm = [url: 'https://github.com/tamershahin/grails-spring-security-oauth-dailymotion']

}
