package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;




import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends CrudRepository<Role, String> {

}

