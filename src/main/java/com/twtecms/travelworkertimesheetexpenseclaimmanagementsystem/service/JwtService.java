package com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.service;

//import com.youtube.ecommerce.dao.UserDao;
//import com.youtube.ecommerce.entity.JwtRequest;
//import com.youtube.ecommerce.entity.JwtResponse;
//import com.youtube.ecommerce.entity.User;
//import com.youtube.ecommerce.util.JwtUtil;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.config.Dao.UserDao;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.JwtRequest;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.JwtResponse;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.entity.User;
import com.twtecms.travelworkertimesheetexpenseclaimmanagementsystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service("jwtService")
public class JwtService implements UserDetailsService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    public JwtResponse createJwtToken(JwtRequest jwtRequest) throws Exception {
        String userName = jwtRequest.getUserName();
        String userPassword = jwtRequest.getUserPassword();
        authenticate(userName, userPassword);

        UserDetails userDetails = loadUserByUsername(userName);
        String newGeneratedToken = jwtUtil.generateToken(userDetails);

        // Find user by username for login purposes
        User user = userDao.findByUserName(userName);
        return new JwtResponse(user, newGeneratedToken);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDao.findByUserName(username);

        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                    user.getUserName(),
                    user.getUserPassword(),
                    getAuthority(user)
            );
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    private Set getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRole().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
        });
        return authorities;
    }

    private void authenticate(String userName, String userPassword) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, userPassword));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}

