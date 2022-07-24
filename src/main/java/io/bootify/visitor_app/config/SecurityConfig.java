package io.bootify.visitor_app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuring spring security
 *
 * spring security automatically creates/configure and maintain the session, by default session created in JVM.
 *
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService; // provides implemented user store

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * This method configures Authentication (i.e., configures login process)
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder); // specifies the user store and
                                                                                        // password encoder to use while login
    }


/**
 *
 * This method configures Authorization
 *
 * All the endpoints specified in this method are secured and others are not secured(i.e. not require authentication)
 *
 * @param http
 * @throws Exception
 */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        this.logger.debug("Using default configure(HttpSecurity). If subclassed this will potentially override subclass configure(HttpSecurity).");
        http.authorizeRequests().antMatchers("/api/user-panel/**").hasAuthority("RESIDENT")
                        .antMatchers("/api/gatekeeper-panel/**").hasAnyAuthority("GATEKEEPER")
                        .antMatchers("/api/admin-panel/**").hasAuthority("ADMIN");
        http.formLogin();
        http.httpBasic();
        http.csrf().disable();
    }

}
