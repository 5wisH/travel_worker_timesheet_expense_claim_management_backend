package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<User, Long> {
    boolean existsByUserName(String userName);
    User findByUserName(String userName);
}