package org.recap.model.jpa;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.jpa.RolesDetailsRepositorty;
import org.recap.repository.jpa.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static org.junit.Assert.assertNotNull;

public class RoleEntityUT extends BaseTestCase{

    @Autowired
    RolesDetailsRepositorty rolesDetailsRepositorty;

    @Autowired
    UserDetailsRepository userDetailsRepository;

    @Test
    public void saveRole(){

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("test role");
        roleEntity.setRoleDescription("test role");
        RoleEntity savedRoleEntity = rolesDetailsRepositorty.save(roleEntity);
        assertNotNull(savedRoleEntity);
        assertNotNull(savedRoleEntity.getId());
        assertNotNull(savedRoleEntity.getRoleName());
        assertNotNull(savedRoleEntity.getRoleDescription());
    }

    @Test
    public void testRoles(){
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(1);
        roleEntity.setRoleName("test role");
        roleEntity.setRoleDescription("test role");
        roleEntity.setPermissions(new HashSet<>());
        roleEntity.setUsers(new HashSet<>());
        assertNotNull(roleEntity.getRoleDescription());
        assertNotNull(roleEntity.getId());
        assertNotNull(roleEntity.getRoleName());
        assertNotNull(roleEntity.getPermissions());
        assertNotNull(roleEntity.getUsers());
    }

}