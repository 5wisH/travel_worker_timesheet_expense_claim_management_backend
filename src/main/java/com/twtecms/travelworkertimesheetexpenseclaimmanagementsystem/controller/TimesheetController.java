package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.controller;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Timesheet;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service.TimesheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TimesheetController {

    @Autowired
    private TimesheetService timesheetService;

    @PostMapping({"/timesheets/save"})
    public List<Timesheet> saveTimesheets(@RequestBody List<Timesheet> timesheets) {
        return timesheetService.saveTimesheets(timesheets);
    }

    @GetMapping({"/timesheets"})
    public List<Timesheet> getTimesheets() {
        return timesheetService.getTimesheets();
    }
}
