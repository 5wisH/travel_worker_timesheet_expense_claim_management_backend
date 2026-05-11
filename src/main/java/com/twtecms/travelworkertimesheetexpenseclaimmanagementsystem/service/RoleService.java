package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.RoleDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleDao roleDao;

    public Role createNewRole(Role role) {
        return roleDao.save(role);
    }
}

