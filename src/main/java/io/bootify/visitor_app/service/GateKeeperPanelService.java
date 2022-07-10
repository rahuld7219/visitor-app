package io.bootify.visitor_app.service;

import io.bootify.visitor_app.model.AddressDTO;
import io.bootify.visitor_app.model.CreateVisitorRequestDto;
import io.bootify.visitor_app.model.VisitorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GateKeeperPanelService {

    private static Logger LOGGER = LoggerFactory.getLogger(GateKeeperPanelService.class);

    @Autowired
    AddressService addressService;

    @Autowired
    VisitorService visitorService;

    /**
     * creates the visitor with address
     *
     * 1st create the address and then create the visitor and bind it with the address id
     * returns the created visitor id
     *
     * @param createVisitorRequestDto
     * @return
     */
    public Long create(CreateVisitorRequestDto createVisitorRequestDto) {
        LOGGER.info("Creating Visitor: {}",createVisitorRequestDto);
        // create the address
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setLine1(createVisitorRequestDto.getLine1());
        addressDTO.setLine2(createVisitorRequestDto.getLine2());
        addressDTO.setPincode(createVisitorRequestDto.getPincode());
        addressDTO.setCity(createVisitorRequestDto.getCity());
        addressDTO.setState(createVisitorRequestDto.getState());
        addressDTO.setCountry(createVisitorRequestDto.getCountry());

        Long addressId = addressService.create(addressDTO);

        // create the visitor
        // doing the similar thing as above but with builder-pattern instead of calling the setter methods
        VisitorDTO visitorDTO = VisitorDTO.builder() // returns VisitorDTOBuilder
                .name(createVisitorRequestDto.getName()) // returns VisitorDTOBuilder
                .email(createVisitorRequestDto.getEmail())
                .phone(createVisitorRequestDto.getPhone())
                .idNumber(createVisitorRequestDto.getIdNumber())
                .address(addressId)
                .build();

        return visitorService.create(visitorDTO);
    }
}
