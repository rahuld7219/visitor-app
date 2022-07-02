package io.bootify.visitor_app.repos;

import io.bootify.visitor_app.domain.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VisitorRepository extends JpaRepository<Visitor, Long> {
}
