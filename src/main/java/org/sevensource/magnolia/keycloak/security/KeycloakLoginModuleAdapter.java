package org.sevensource.magnolia.keycloak.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.StringUtils;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.jaas.DirectAccessGrantsLoginModule;
import org.keycloak.adapters.jaas.RolePrincipal;
import org.sevensource.magnolia.keycloak.KeycloakSecurityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.sp.UserAwareLoginModule;
import info.magnolia.objectfactory.Components;

public class KeycloakLoginModuleAdapter extends AbstractLoginModule implements UserAwareLoginModule {

	private static final Logger logger = LoggerFactory.getLogger(KeycloakLoginModuleAdapter.class);

	private DirectAccessGrantsLoginModule keycloakLoginModule;
	private KeycloakSecurityModule keycloakSecurityModule;

	private User user = null;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
		this.keycloakSecurityModule = Components.getComponent(KeycloakSecurityModule.class);
		this.keycloakLoginModule = new DirectAccessGrantsLoginModule();

		final Map<String, Object> newOptions = new HashMap<>(options);
		setupKeycloakOptionsFile(newOptions);

		super.initialize(subject, callbackHandler, sharedState, newOptions);
		keycloakLoginModule.initialize(subject, callbackHandler, sharedState, newOptions);
	}

	protected void setupKeycloakOptionsFile(Map<String, Object> options) {
		if (!options.containsKey(DirectAccessGrantsLoginModule.KEYCLOAK_CONFIG_FILE_OPTION)) {
			final String configFile = keycloakSecurityModule.getKeycloakConfigFile();
			if (StringUtils.isNotBlank(configFile)) {
				options.put(DirectAccessGrantsLoginModule.KEYCLOAK_CONFIG_FILE_OPTION, configFile);
			}
		}
	}

	protected void checkKeycloakOptionsFile(Map options) throws LoginException {
		if (!options.containsKey(DirectAccessGrantsLoginModule.KEYCLOAK_CONFIG_FILE_OPTION)) {
			final String msg = "No keycloakConfigFile specified";
			logger.error(msg);
			throw new LoginException(msg);
		}
	}

	@Override
	public boolean login() throws LoginException {

		if (this.getSkip()) {
			return true;
		}

		checkKeycloakOptionsFile(options);

		boolean result = false;

		try {
			result = keycloakLoginModule.login();
		} catch (LoginException le) {
			logger.warn(le.getMessage());
			FailedLoginException e = new FailedLoginException();
			e.initCause(le);
			throw e;
		}

		this.success = result;
		this.setSharedStatus(result ? STATUS_SUCCEEDED : STATUS_SKIPPED);
		return this.success;
	}

	@Override
	public boolean commit() throws LoginException {
		boolean result = keycloakLoginModule.commit();
		super.commit();
		return result;
	}

	@Override
	public boolean abort() throws LoginException {
		super.abort();
		return keycloakLoginModule.abort();
	}

	@Override
	public boolean logout() throws LoginException {
		super.logout();
		return keycloakLoginModule.logout();
	}

	@Override
	public void validateUser() throws LoginException {
		throw new UnsupportedOperationException("validateUser() is not used in this implementation");
	}

	@Override
	public void setACL() {
		// no-op
	}

	@Override
	public void setEntity() {
		this.subject.getPrincipals().add(getUser());
		this.subject.getPrincipals().add(this.realm);

		for (String group : this.getUser().getAllGroups()) {
			addGroupName(group);
		}

		for (String role : this.getUser().getAllRoles()) {
			addRoleName(role);
		}
	}

	@Override
	public User getUser() {
		if (this.user == null) {
			final Set<KeycloakPrincipal> principals = this.subject.getPrincipals(KeycloakPrincipal.class);
			if (principals.isEmpty()) {
				final String msg = "No KeycloakPrincipal available";
				logger.error(msg);
				throw new IllegalStateException(msg);
			} else if (principals.size() > 1) {
				final String msg = String.format("%s KeycloakPrincipals available - which one should I choose?",
						principals.size());
				logger.error(msg);
				throw new IllegalStateException(msg);
			}

			final KeycloakPrincipal<?> principal = principals.iterator().next();
			final Set<RolePrincipal> roles = this.subject.getPrincipals(RolePrincipal.class);

			this.user = buildUser(principal, roles);
		}

		return this.user;
	}

	protected User buildUser(KeycloakPrincipal<?> principal, Set<RolePrincipal> roles) {
		final UserManager userManager = Components.getComponent(SecuritySupport.class).getUserManager(realm.getName());
		if (userManager == null) {
			final String msg = String.format("No UserManager found for realm %s", realm.getName());
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		} else if (!(userManager instanceof KeycloakUserManager)) {
			final String msg = String.format("UserManager is of type %s, but expected %s",
					userManager.getClass().getName(), KeycloakUserManager.class.getName());
			logger.error(msg);
			throw new IllegalArgumentException(msg);
		}

		return ((KeycloakUserManager) userManager).getUser(principal, roles);
	}
}
