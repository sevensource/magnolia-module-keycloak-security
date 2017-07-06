package org.sevensource.magnolia.keycloak.security;

import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.jaas.DirectAccessGrantsLoginModule;
import org.keycloak.adapters.jaas.RolePrincipal;
import org.sevensource.magnolia.keycloak.KeycloakSecurityModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.security.User;
import info.magnolia.cms.security.auth.GroupList;
import info.magnolia.cms.security.auth.RoleList;
import info.magnolia.jaas.principal.GroupListImpl;
import info.magnolia.jaas.principal.RoleListImpl;
import info.magnolia.jaas.sp.AbstractLoginModule;
import info.magnolia.jaas.sp.UserAwareLoginModule;
import info.magnolia.objectfactory.Components;

public class KeycloakLoginModuleAdapter extends AbstractLoginModule implements UserAwareLoginModule {

	private static final Logger logger = LoggerFactory.getLogger(KeycloakLoginModuleAdapter.class);
	
	private final DirectAccessGrantsLoginModule keycloakLoginModule;
	
	private User user = null;
	
	public KeycloakLoginModuleAdapter() {
		this.keycloakLoginModule = new DirectAccessGrantsLoginModule();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
		super.initialize(subject, callbackHandler, sharedState, options);
		keycloakLoginModule.initialize(subject, callbackHandler, sharedState, options);
	}
	
	@Override
	public boolean login() throws LoginException {
        if (this.getSkip()) {
            return true;
        }
        
		boolean result = keycloakLoginModule.login();
        this.success = result;
        this.setSharedStatus(result ? STATUS_SUCCEEDED : STATUS_FAILED);
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
		throw new IllegalStateException("This should never be executed");
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
		if(this.user == null) {
			final Set<KeycloakPrincipal> principals = this.subject.getPrincipals(KeycloakPrincipal.class);
			if(principals.isEmpty()) {
				final String msg = "No KeycloakPrincipal available";
				logger.error(msg);
				throw new IllegalStateException(msg);
			} else if(principals.size() > 1) {
				final String msg = String.format("%s KeycloakPrincipals available - don't know which to use", principals.size());
				logger.error(msg);
				throw new IllegalStateException(msg);
			}
			
			final KeycloakPrincipal<?> principal = principals.iterator().next();
			this.user = buildUser(principal);
		}
		
		return this.user;
	}
	
	
	protected User buildUser(KeycloakPrincipal<?> principal) {
		final KeycloakSecurityContext ctx = principal.getKeycloakSecurityContext();
		
		ctx.getTokenString();
		
		final KeycloakUser keycloakUser = new KeycloakUser(buildGroupList(), buildRoleList());
		keycloakUser.setEmail(ctx.getToken().getEmail());
		keycloakUser.setEnabled(true);
		keycloakUser.setLanguage(ctx.getToken().getLocale());
		keycloakUser.setName(ctx.getToken().getPreferredUsername());
		keycloakUser.setFullname(ctx.getToken().getName());
		keycloakUser.setIdentifier(ctx.getToken().getId());
		
		return keycloakUser;
	}
	
	protected RoleList buildRoleList() {
		final RoleList roleList = new RoleListImpl();
		
		final RoleMapper roleMapper = Components.getComponent(KeycloakSecurityModule.class).getRoleMapper();
		
		final Set<RolePrincipal> roles = this.subject.getPrincipals(RolePrincipal.class);
		for(RolePrincipal rolePrincipal : roles) {
			final String role = roleMapper.mapRole(rolePrincipal.getName());
			if(role != null) {
				roleList.add(role);
			}	
		}
		
		return roleList;
	}
	
	protected GroupList buildGroupList() {
		return new GroupListImpl();
	}
}
