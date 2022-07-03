package io.bootify.visitor_app.repos;


import io.bootify.visitor_app.domain.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    public Optional<Visitor> findByIdNumber(String idNumber);
}
