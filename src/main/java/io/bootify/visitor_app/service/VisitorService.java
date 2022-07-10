package io.bootify.visitor_app.service;

import io.bootify.visitor_app.domain.Address;
import io.bootify.visitor_app.domain.Visitor;
import io.bootify.visitor_app.model.VisitorDTO;
import io.bootify.visitor_app.repos.AddressRepository;
import io.bootify.visitor_app.repos.VisitorRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class VisitorService {

    private static Logger LOGGER = LoggerFactory.getLogger(VisitorService.class);

    private final VisitorRepository visitorRepository;
    private final AddressRepository addressRepository;

    public VisitorService(final VisitorRepository visitorRepository,
                          final AddressRepository addressRepository) {
        this.visitorRepository = visitorRepository;
        this.addressRepository = addressRepository;
    }

    public List<VisitorDTO> findAll() {
        return visitorRepository.findAll(Sort.by("id"))
                .stream()
                .map(visitor -> mapToDTO(visitor, new VisitorDTO()))
                .collect(Collectors.toList());
    }

    public VisitorDTO get(final Long id) {
        return visitorRepository.findById(id)
                .map(visitor -> mapToDTO(visitor, new VisitorDTO()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public VisitorDTO getByIdNumber(String idNumber) {

        LOGGER.info("Searching visitor with idNumber : {}",idNumber);

        return visitorRepository.findByIdNumber(idNumber)
                .map(visitor -> mapToDTO(visitor, new VisitorDTO()))
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Long create(final VisitorDTO visitorDTO) {
        final Visitor visitor = new Visitor();
        mapToEntity(visitorDTO, visitor);
        return visitorRepository.save(visitor).getId();
    }

    public void update(final Long id, final VisitorDTO visitorDTO) {
        final Visitor visitor = visitorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        mapToEntity(visitorDTO, visitor);
        visitorRepository.save(visitor);
    }

    public void delete(final Long id) {
        visitorRepository.deleteById(id);
    }

    private VisitorDTO mapToDTO(final Visitor visitor, final VisitorDTO visitorDTO) {
        visitorDTO.setId(visitor.getId());
        visitorDTO.setName(visitor.getName());
        visitorDTO.setEmail(visitor.getEmail());
        visitorDTO.setPhone(visitor.getPhone());
        visitorDTO.setIdNumber(visitor.getIdNumber());
        visitorDTO.setAddress(visitor.getAddress() == null ? null : visitor.getAddress().getId());
        return visitorDTO;
    }

    private Visitor mapToEntity(final VisitorDTO visitorDTO, final Visitor visitor) {
        visitor.setName(visitorDTO.getName());
        visitor.setEmail(visitorDTO.getEmail());
        visitor.setPhone(visitorDTO.getPhone());
        visitor.setIdNumber(visitorDTO.getIdNumber());
        final Address address = visitorDTO.getAddress() == null ? null : addressRepository.findById(visitorDTO.getAddress())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "address not found"));
        visitor.setAddress(address);
        return visitor;
    }

}
