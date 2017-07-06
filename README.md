[![GitHub Tag](https://img.shields.io/github/tag/sevensource/magnolia-module-keycloak-security.svg?maxAge=3600)](https://github.com/sevensource/magnolia-module-keycloak-security/tags)
[![Maven Central](https://img.shields.io/maven-central/v/org.sevensource.magnolia/magnolia-module-keycloak-security?maxAge=3600)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.sevensource.magnolia%22%20AND%20a%3A%22magnolia-module-keycloak-security%22)
[![License](https://img.shields.io/github/license/sevensource/magnolia-module-keycloak-security.svg?maxAge=2592000)](https://github.com/sevensource/magnolia-module-keycloak-security/blob/master/LICENSE)

magnolia-module-keycloak-security
================================

[Keycloak](http://www.keycloak.org/) SSO/IAM integration for [Magnolia](http://www.magnolia-cms.com) 5.5.x 

**Contributions welcome!**

Installation
=============
* create a client in Keycloak with *Direct Access Grants* enabled
* export the configuration in *Keycloak OIDC JSON* format from the *Installation* tab
* save the configuration file into your projects classpath, i.e. `src/main/resources/keycloak.json`
* configure `src/main/webapp/WEB-INF/config/jaas.config` to include the KeycloakAuthenticationModule:
```
magnolia {
  info.magnolia.jaas.sp.jcr.JCRAuthenticationModule optional realm=system;

  org.sevensource.magnolia.keycloak.security.KeycloakLoginModuleAdapter requisite skip_on_previous_success=true keycloak-config-file="classpath:keycloak.json";
  info.magnolia.jaas.sp.jcr.JCRAuthorizationModule required;
};
```
* for further JAAS configuration options, see
  - http://docs.oracle.com/javase/6/docs/technotes/guides/security/jaas/JAASRefGuide.html
  - https://documentation.magnolia-cms.com/display/DOCS/NTLM+Connector+module
  - https://documentation.magnolia-cms.com/display/DOCS/LDAP+Connector+module
  - https://documentation.magnolia-cms.com/display/DOCS/CAS+module

Configuration
=============
All additional configuration is stored in Magnolias JCR:
* the module installs a UserManager into `/server/security/userManagers/external`
* RoleMapping from Keycloak roles to Magnolia roles can be configured in `/modules/keycloak-security/config/roleMapper`
