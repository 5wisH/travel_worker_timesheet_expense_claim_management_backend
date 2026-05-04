package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends CrudRepository<User, String> {
}
