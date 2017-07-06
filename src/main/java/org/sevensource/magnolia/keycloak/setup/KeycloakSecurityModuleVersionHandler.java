package org.sevensource.magnolia.keycloak.setup;

import java.util.ArrayList;
import java.util.List;

import org.sevensource.magnolia.keycloak.security.DefaultRoleMapper;
import org.sevensource.magnolia.keycloak.security.KeycloakUserManager;

import info.magnolia.jcr.nodebuilder.Ops;
import info.magnolia.jcr.nodebuilder.task.ErrorHandling;
import info.magnolia.jcr.nodebuilder.task.ModuleConfigNodeBuilderTask;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.ArrayDelegateTask;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.NodeExistsDelegateTask;
import info.magnolia.module.delta.OrderNodeAfterTask;
import info.magnolia.module.delta.SetPropertyTask;
import info.magnolia.module.delta.Task;
import info.magnolia.repository.RepositoryConstants;

public class KeycloakSecurityModuleVersionHandler extends DefaultModuleVersionHandler {	
	private static final String USERMANAGERS_NODE = "/server/security/userManagers";
	private static final String REALM_NAME = "external";
	private static final String EXTERNAL_USERMANAGER_NODE = USERMANAGERS_NODE + "/" + REALM_NAME;
	private static final String SYSTEM_USERMANAGER = "system";
	
	
	@Override
	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        
        
        final Task createUserManager = new NodeExistsDelegateTask("Check existing external UserManager", USERMANAGERS_NODE + "/" + REALM_NAME, null,
        		new ArrayDelegateTask("Add Keycloak UserManager",
        				new CreateNodeTask("Create userManager node", USERMANAGERS_NODE, REALM_NAME, NodeTypes.ContentNode.NAME),
        				new SetPropertyTask(RepositoryConstants.CONFIG, EXTERNAL_USERMANAGER_NODE, "realmName", REALM_NAME),
        				new SetPropertyTask(RepositoryConstants.CONFIG, EXTERNAL_USERMANAGER_NODE, "class", KeycloakUserManager.class.getName()),
        				new OrderNodeAfterTask("Reorder userManager to correct position", EXTERNAL_USERMANAGER_NODE, SYSTEM_USERMANAGER)
        		));
        
        final ModuleConfigNodeBuilderTask task = new ModuleConfigNodeBuilderTask(
        		"Create RoleMapper configuration", "Create RoleMapper configuration", ErrorHandling.strict,
        		Ops.addProperty("keycloakConfigFile", ""),
        		Ops.addNode("roleMapper", NodeTypes.ContentNode.NAME).then(
        				Ops.addProperty("class", DefaultRoleMapper.class.getName()),
        				Ops.addProperty("mapUnmappedRolesAsIs", "true"),
        				Ops.addNode("mappings", NodeTypes.ContentNode.NAME).then(
        						Ops.addProperty("mgnl-superuser", "superuser")
        						)
        				)
        );
        
        final List<Task> tasks = new ArrayList<>();
        tasks.add(createUserManager);
        tasks.add(task);
        return tasks;
	}
}
