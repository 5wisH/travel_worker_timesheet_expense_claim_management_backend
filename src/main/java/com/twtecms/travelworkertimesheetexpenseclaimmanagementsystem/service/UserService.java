package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.RoleDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.UserDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Role;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void initRoleAndUser() {

        // Only create Admin role if it doesn't exist
        if (!roleDao.findById("Admin").isPresent()) {
            Role adminRole = new Role();
            adminRole.setRoleName("Admin");
            adminRole.setRoleDescription("Admin role");
            roleDao.save(adminRole);
        }

        if (!roleDao.findById("Manager").isPresent()) {
            Role adminRole = new Role();
            adminRole.setRoleName("Manager");
            adminRole.setRoleDescription("Manager role");
            roleDao.save(adminRole);
        }

        // Only create User role if it doesn't exist
        if (!roleDao.findById("User").isPresent()) {
            Role userRole = new Role();
            userRole.setRoleName("User");
            userRole.setRoleDescription("Default role for newly created record");
            roleDao.save(userRole);
        }

        // Only create admin user if it doesn't exist
        if (!userDao.existsByUserName("admin123")) {
            User adminUser = new User();
            adminUser.setUserName("admin123");
            adminUser.setUserPassword(getEncodedPassword("admin@pass"));
            adminUser.setUserFirstName("admin");
            adminUser.setUserLastName("admin");
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleDao.findById("Admin").get());
            adminUser.setRole(adminRoles);
            userDao.save(adminUser);
        }

        // Only create manager user if it doesn't exist
        if (!userDao.existsByUserName("manager123")) {
            User adminUser = new User();
            adminUser.setUserName("manager123");
            adminUser.setUserPassword(getEncodedPassword("manager@pass"));
            adminUser.setUserFirstName("manager");
            adminUser.setUserLastName("manager");
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleDao.findById("Manager").get());
            adminUser.setRole(adminRoles);
            userDao.save(adminUser);
        }

        // Only create raj user if it doesn't exist
        if (!userDao.existsByUserName("user123")) {
            User user = new User();
            user.setUserName("user@123");
            user.setUserPassword(getEncodedPassword("raj@123"));
            user.setUserFirstName("raj");
            user.setUserLastName("sharma");
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(roleDao.findById("User").get());
            user.setRole(userRoles);
            userDao.save(user);
        }
    }

    public User registerNewUser(User user) {
        Role role = roleDao.findById("User").get();
        Set<Role> roleSet = new HashSet<>();
        roleSet.add(role);
        user.setRole(roleSet);
        String password = getEncodedPassword(user.getUserPassword());
        user.setUserPassword(password);
        return userDao.save(user);
    }

    public String getEncodedPassword(String password) {

        return passwordEncoder.encode(password);
    }
}