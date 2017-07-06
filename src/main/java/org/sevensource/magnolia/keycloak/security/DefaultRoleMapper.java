package org.sevensource.magnolia.keycloak.security;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class DefaultRoleMapper implements RoleMapper {

	private Map<String, String> mappings = new HashMap<>();
	private boolean mapUnmappedRolesAsIs = true;
	
	@Override
	public String mapRole(String role) {
		final String target = mappings.get(role);
		if(! StringUtils.isEmpty(target)) {
			return target;
		} else {
			return mapUnmappedRolesAsIs ? target : null;
		}
	}
	
	public void setMappings(Map<String, String> roleMappings) {
		this.mappings = roleMappings;
	}
	
	public void addMapping(String source, String target) {
		this.mappings.put(source, target);
	}
	
	public void setMapUnmappedRolesAsIs(boolean mapUnmappedRolesAsIs) {
		this.mapUnmappedRolesAsIs = mapUnmappedRolesAsIs;
	}
}
