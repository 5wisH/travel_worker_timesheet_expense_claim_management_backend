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
        createRoleIfMissing("Employee", "Employee role");
        createRoleIfMissing("Supervisor", "Supervisor role");
        createRoleIfMissing("Logistics", "Logistics role");
        createRoleIfMissing("Finance", "Finance role");
        createRoleIfMissing("HR Admin", "HR Admin role");
        createRoleIfMissing("Admin", "Admin role");
        createRoleIfMissing("Auditor", "Auditor role");
        createRoleIfMissing("Senior Approver", "Senior Approver role");

        upsertSeedUser("employee123", "employee@pass", "Employee", "User", "employee@khokha.local", "Employee");
        upsertSeedUser("supervisor123", "supervisor@pass", "Supervisor", "User", "supervisor@khokha.local", "Supervisor", "manager123");
        upsertSeedUser("logistics123", "logistics@pass", "Logistics", "User", "logistics@khokha.local", "Logistics");
        upsertSeedUser("finance123", "finance@pass", "Finance", "User", "finance@khokha.local", "Finance");
        upsertSeedUser("hradmin123", "hradmin@pass", "HR", "Admin", "hradmin@khokha.local", "HR Admin");
        upsertSeedUser("admin123", "admin@pass", "Admin", "User", "admin@khokha.local", "Admin");
        upsertSeedUser("auditor123", "auditor@pass", "Auditor", "User", "auditor@khokha.local", "Auditor");
        upsertSeedUser("seniorapprover123", "seniorapprover@pass", "Senior", "Approver", "seniorapprover@khokha.local", "Senior Approver");
    }

    public User registerNewUser(User user) {
        Role role = roleDao.findById("Employee").get();
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

    private void createRoleIfMissing(String roleName, String roleDescription) {
        if (roleDao.findById(roleName).isPresent()) {
            return;
        }

        Role role = new Role();
        role.setRoleName(roleName);
        role.setRoleDescription(roleDescription);
        roleDao.save(role);
    }

    private void upsertSeedUser(String userName, String password, String firstName, String lastName, String email, String roleName, String... previousUserNames) {
        User user = findExistingSeedUser(userName, previousUserNames);
        if (user == null) {
            user = new User();
        }

        user.setUserName(userName);
        user.setUserPassword(getEncodedPassword(password));
        user.setUserFirstName(firstName);
        user.setUserLastName(lastName);
        user.setUserEmail(email);
        user.setStatus(true);
        Set<Role> roles = new HashSet<>();
        roles.add(roleDao.findById(roleName).get());
        user.setRole(roles);
        userDao.save(user);
    }

    private User findExistingSeedUser(String userName, String... previousUserNames) {
        User user = userDao.findByUserName(userName);
        if (user != null) {
            return user;
        }

        for (String previousUserName : previousUserNames) {
            user = userDao.findByUserName(previousUserName);
            if (user != null) {
                return user;
            }
        }

        return null;
    }
}
