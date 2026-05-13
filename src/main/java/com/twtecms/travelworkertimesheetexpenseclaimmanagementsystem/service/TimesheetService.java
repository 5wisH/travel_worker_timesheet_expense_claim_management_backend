package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.TimesheetDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Timesheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TimesheetService {

    @Autowired
    private TimesheetDao timesheetDao;

    public List<Timesheet> saveTimesheets(List<Timesheet> timesheets) {
        return timesheetDao.saveAll(timesheets);
    }

    public List<Timesheet> getTimesheets() {
        return timesheetDao.findAll();
    }
}
