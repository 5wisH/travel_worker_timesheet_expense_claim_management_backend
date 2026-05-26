package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.UserSessionLogDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.Role;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.UserSessionLog;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class UserSessionLogService {

    private final UserSessionLogDao userSessionLogDao;

    public UserSessionLogService(UserSessionLogDao userSessionLogDao) {
        this.userSessionLogDao = userSessionLogDao;
    }

    public UserSessionLog logLogin(User user, String ipAddress, String userAgent) {
        UserSessionLog log = new UserSessionLog();
        log.setUserId(user.getUserId());
        log.setUserName(user.getUserName());
        log.setUserFirstName(user.getUserFirstName());
        log.setUserLastName(user.getUserLastName());
        log.setUserEmail(user.getUserEmail());
        log.setRoles(user.getRole() == null ? "" : user.getRole().stream().map(Role::getRoleName).collect(Collectors.joining(",")));
        log.setLoginAt(LocalDateTime.now());
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        return userSessionLogDao.save(log);
    }
}
