package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimesheetDao extends JpaRepository<Timesheet, Long> {
}
