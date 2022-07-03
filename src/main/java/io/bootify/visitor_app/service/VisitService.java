package io.bootify.visitor_app.service;

import io.bootify.visitor_app.domain.Flat;
import io.bootify.visitor_app.domain.Visit;
import io.bootify.visitor_app.domain.Visitor;
import io.bootify.visitor_app.model.VisitDTO;
import io.bootify.visitor_app.model.VisitStatus;
import io.bootify.visitor_app.repos.FlatRepository;
import io.bootify.visitor_app.repos.VisitRepository;
import io.bootify.visitor_app.repos.VisitorRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class VisitService {

    @Autowired
    private FlatRepository flatRepository;

    private final VisitRepository visitRepository;
    private final VisitorRepository visitorRepository;

    public VisitService(final VisitRepository visitRepository,
            final VisitorRepository visitorRepository) {
        this.visitRepository = visitRepository;
        this.visitorRepository = visitorRepository;
    }

    public List<VisitDTO> findAll() {
        return visitRepository.findAll(Sort.by("id"))
                .stream()
                .map(visit -> mapToDTO(visit, new VisitDTO()))
                .collect(Collectors.toList());
    }

    public VisitDTO get(final Long id) {
        return visitRepository.findById(id)
                .map(visit -> mapToDTO(visit, new VisitDTO()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Long create(final VisitDTO visitDTO) {
        visitDTO.setStatus(VisitStatus.PENDING);
        final Visit visit = new Visit();
        mapToEntity(visitDTO, visit);
        return visitRepository.save(visit).getId();
    }

    public void markEntry(Long visitId) {
        Optional<Visit> visit = visitRepository.findById(visitId);
        if (visit.isPresent() && visit.get().getStatus().equals(VisitStatus.APPROVED)) {
            visit.get().setInTime(LocalDateTime.now());
        } else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is not updated");
        }
    }

    public void markExit(Long visitId) {
        Optional<Visit> visit = visitRepository.findById(visitId);
        if (visit.isPresent() && visit.get().getStatus().equals(VisitStatus.APPROVED)) {
            visit.get().setOutTime(LocalDateTime.now());
            visit.get().setStatus(VisitStatus.COMPLETED);
        } else{
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is not updated");
        }
    }

    public void update(final Long id, final VisitDTO visitDTO) {
        final Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        mapToEntity(visitDTO, visit);
        visitRepository.save(visit);
    }

    public void delete(final Long id) {
        visitRepository.deleteById(id);
    }

    private VisitDTO mapToDTO(final Visit visit, final VisitDTO visitDTO) {
        visitDTO.setId(visit.getId());
        visitDTO.setStatus(visit.getStatus());
        visitDTO.setInTime(visit.getInTime());
        visitDTO.setOutTime(visit.getOutTime());
        visitDTO.setUrlOfImage(visit.getUrlOfImage());
        visitDTO.setNoOfPeople(visit.getNoOfPeople());
        visitDTO.setVisitor(visit.getVisitor() == null ? null : visit.getVisitor().getId());
        visitDTO.setFlatId(visit.getFlat().getId());
        return visitDTO;
    }

    private Visit mapToEntity(final VisitDTO visitDTO, final Visit visit) {
        visit.setStatus(visitDTO.getStatus());
        visit.setInTime(visitDTO.getInTime());
        visit.setOutTime(visitDTO.getOutTime());
        visit.setUrlOfImage(visitDTO.getUrlOfImage());
        visit.setNoOfPeople(visitDTO.getNoOfPeople());
        Flat flat = flatRepository.findById(visitDTO.getFlatId()).get(); // TODO: Handle it when no flat found
        visit.setFlat(flat);
        final Visitor visitor = visitDTO.getVisitor() == null ? null : visitorRepository.findById(visitDTO.getVisitor())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "visitor not found"));
        visit.setVisitor(visitor);
        return visit;
    }

}
