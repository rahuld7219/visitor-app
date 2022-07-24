package io.bootify.visitor_app.service;

import io.bootify.visitor_app.domain.User;
import io.bootify.visitor_app.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * service for database (MySQL) user store
 */
@Service
public class DBUserStore implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String emailId) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(emailId);
        if(user==null){
            throw new UsernameNotFoundException("No user");
        }
        return user;
    }
}
