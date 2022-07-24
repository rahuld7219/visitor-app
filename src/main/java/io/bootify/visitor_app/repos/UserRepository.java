package io.bootify.visitor_app.repos;

import io.bootify.visitor_app.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}
