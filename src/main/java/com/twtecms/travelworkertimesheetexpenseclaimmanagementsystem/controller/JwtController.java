package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.controller;




import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.JwtRequest;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.JwtResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class JwtController {

    @Autowired
    private JwtService jwtService;

    @PostMapping({"/authenticate"})
    public JwtResponse createJwtToken(@RequestBody JwtRequest jwtRequest, HttpServletRequest request) throws Exception {
        return jwtService.createJwtToken(jwtRequest, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}

