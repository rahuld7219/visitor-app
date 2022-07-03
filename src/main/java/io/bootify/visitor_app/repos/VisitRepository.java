package io.bootify.visitor_app.repos;

import io.bootify.visitor_app.domain.Flat;
import io.bootify.visitor_app.domain.Visit;
import io.bootify.visitor_app.model.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByFlatAndStatus(Flat flat, VisitStatus status);
}
