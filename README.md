[![GitHub Tag](https://img.shields.io/github/tag/sevensource/magnolia-module-keycloak-security.svg?maxAge=3600)](https://github.com/sevensource/magnolia-module-keycloak-security/tags)
[![Maven Central](https://img.shields.io/maven-central/v/org.sevensource.magnolia/magnolia-module-keycloak-security?maxAge=3600)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.sevensource.magnolia%22%20AND%20a%3A%22magnolia-module-keycloak-security%22)
[![License](https://img.shields.io/github/license/sevensource/magnolia-module-keycloak-security.svg)](https://github.com/sevensource/magnolia-module-keycloak-security/blob/master/LICENSE)


magnolia-module-keycloak-security
================================

[Keycloak](http://www.keycloak.org/) SSO/IAM integration for [Magnolia](http://www.magnolia-cms.com) 5.5.x 

This module delegates authentication - in addition to Magnolias builtin authentication mechanisms - to Keycloak.


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

  org.sevensource.magnolia.keycloak.security.KeycloakLoginModuleAdapter requisite realm=external skip_on_previous_success=true;
  info.magnolia.jaas.sp.jcr.JCRAuthorizationModule required;
};
```

* for further JAAS configuration options, see
  - http://docs.oracle.com/javase/8/docs/api/javax/security/auth/login/Configuration.html
  - https://documentation.magnolia-cms.com/display/DOCS/NTLM+Connector+module
  - https://documentation.magnolia-cms.com/display/DOCS/LDAP+Connector+module
  - https://documentation.magnolia-cms.com/display/DOCS/CAS+module
  - http://docs.oracle.com/javase/8/docs/technotes/guides/security/jaas/JAASRefGuide.html

Configuration
=============
All additional configuration is stored in Magnolias JCR.

* login into magnolia using the `superuser` account
* go into Configurations App and navigate to `/modules/keycloak-security/config` and add your keycloakConfigFile, i.e. `classpath:keycloak.json`
* the module features a RoleMapper, which maps Keycloak roles to Magnolia roles. It is configured in `/modules/keycloak-security/config/roleMapper`.
* the module installs a UserManager into `/server/security/userManagers/external` which can be used as an extension point for customisation
