package io.bootify.visitor_app.repos;

import io.bootify.visitor_app.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, Long> {
}
