package io.bootify.visitor_app.service;

import io.bootify.visitor_app.domain.Flat;
import io.bootify.visitor_app.domain.User;
import io.bootify.visitor_app.domain.Visit;
import io.bootify.visitor_app.domain.Visitor;
import io.bootify.visitor_app.model.VisitDTO;
import io.bootify.visitor_app.model.VisitStatus;
import io.bootify.visitor_app.repos.FlatRepository;
import io.bootify.visitor_app.repos.UserRepository;
import io.bootify.visitor_app.repos.VisitRepository;
import io.bootify.visitor_app.repos.VisitorRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;


@Service
public class VisitService {

    private static Logger LOGGER = LoggerFactory.getLogger(VisitService.class);

    @Autowired
    private FlatRepository flatRepository;

    @Autowired
    private UserRepository userRepository;

    private final VisitRepository visitRepository;
    private final VisitorRepository visitorRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private String pendingVisitPrefix = "Pending_Flat_";

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

    /*
    We can also cache the data using userId as key,
    but we use flatId as key because it is more efficient
    as in one flat there can be more than one resident so if we go with userId then for the same flat,
    we may cache the same data multiple times with different resident(userId) of the same flat.

    user_1: data
    flat_1: data --> more efficient

    NOTE: Similar caching code below can be applied to many methods(especially for GET API)
          So, we can create our annotation for this instead of writing same code again and again,
          or use spring annotations like @Cacheable, etc. to do the same things (google caching in spring boot)
     */
    public List<VisitDTO> getPendingVisits(Long userId) {

        LOGGER.info("Fetching all pending visits for user:{}",userId);

        User user = userRepository.findById(userId).get(); // TODO: handle if not found

        Flat userFlat = user.getFlat();

        String key = pendingVisitPrefix + userFlat.getId();

        // getting cached data from the Redis
        List<VisitDTO> visitDTOList = (List<VisitDTO>) redisTemplate.opsForValue().get(key);

        // if cache not present for the key
        if (visitDTOList == null) {
            visitDTOList = visitRepository.findByFlatAndStatus(userFlat, VisitStatus.PENDING)
                    .stream()
                    .map(visit -> mapToDTO(visit, new VisitDTO()))
                    .collect(Collectors.toList());
            // cache the data in the Redis
            redisTemplate.opsForValue().set(key, visitDTOList);
        }
        return visitDTOList;
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

//    public void approveVisit(Long visitId, Long userId) {
//        Visit visit = visitRepository.findById(visitId).get(); // TODO: handle if not found
//        Flat flat = visit.getFlat();
//
//        User user = userRepository.findById(userId).get();
//
//        if(flat.getId() == user.getFlat().getId() && visit.getStatus().equals(VisitStatus.PENDING)) {
//            visit.setStatus(VisitStatus.APPROVED);
//            visitRepository.save(visit);
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flat is not mapped OR status is not pending");
//        }
//    }
//
//    public void rejectVisit(Long visitId, Long userId) {
//        Visit visit = visitRepository.findById(visitId).get(); // TODO: handle if not found
//        Flat flat = visit.getFlat();
//
//        User user = userRepository.findById(userId).get();
//
//        if(flat.getId() == user.getFlat().getId() && visit.getStatus().equals(VisitStatus.PENDING)) {
//            visit.setStatus(VisitStatus.REJECTED);
//            visitRepository.save(visit);
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flat is not mapped OR status is not pending");
//        }
//    }

    // Refactored in place of above two
    @Transactional
    // without @Transactional, as we are not accessing any flat data for the User before user.getFlat(),
    // so flat data was not fetched as it is lazy fetched in User
    // and also session is closed before it and no session opened for fetching flat, so user.getFlat()
    // gives LazyInitializationException with no session message and user flat have null values
    // by using @Transactional we are putting all the code in one transaction so session will remain open
    // and flat data will be fetched while user.getflat()
    public void updateVisit(Long visitId, Long userId, VisitStatus visitStatus) { // TODO: rename to updateVisitStatus
        LOGGER.info("Updating visit {} status to {}",visitId, visitStatus);
        Visit visit = visitRepository.findById(visitId).get(); // here without @Transactional, one session opened and closed
        // TODO: handle if not found

        Flat flat = visit.getFlat();
        // here flat data would be available without @Transactional
        // as in Visit flat is not lazy fetched so while fetching the visit above, flat data was also fetched

        User user = userRepository.findById(userId).get(); // here without @Transactional, one session opened and closed

//        if(flat.getId() == user.getFlat().getId() && visit.getStatus().equals(VisitStatus.PENDING)) {
            // can use above line instead if not using @Transactional as in this we are accessing user flat data,
            // so a session will be opened and data will be fetched
        if(flat == user.getFlat() && visit.getStatus().equals(VisitStatus.PENDING)) {
            //NOTE: visit.getFlat() and user.getFlat() returns the same object as the flat is same,
            // for the same flat 2 different object is not created, it is singleton design

            visit.setStatus(visitStatus);
            visitRepository.save(visit);

            // delete the cache data for pending visits,
            // so that next time it get updated when getPendingVisits called,
            // otherwise we get old cached data.
            // (the best way to update cache data is to delete the cache entry, so it gets created again with the updated data)
            String key = pendingVisitPrefix + flat.getId();
//            redisTemplate.opsForValue().getAndDelete(key); // this may give RedisSystemException if there is compatibility
                                                                // problem between dependency version added in the project
                                                                // and the redis server version running at the system
            redisTemplate.delete(key); // opsForValue(), etc. related to particular data structures,
                                        // but deleting a key may not, so we can call it directly, this works instead of above way
        } else {
            LOGGER.error("Invalid update visit request by user {} for visit {}",userId,visitId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flat is not mapped OR status is not pending");
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

        // can use below code instead of above, which is better???

//        if (visitDTO.getVisitor() != null && (visit.getVisitor() == null || !visit.getVisitor().getId().equals(visitDTO.getVisitor()))) {
//            final Visitor visitor = visitorRepository.findById(visitDTO.getVisitor())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "visitor not found"));
//            visit.setVisitor(visitor);
//        }

        return visit;
    }

}
