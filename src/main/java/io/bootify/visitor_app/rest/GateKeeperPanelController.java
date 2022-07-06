package io.bootify.visitor_app.rest;

import io.bootify.visitor_app.model.CreateVisitorRequestDto;
import io.bootify.visitor_app.model.VisitDTO;
import io.bootify.visitor_app.model.VisitorDTO;
import io.bootify.visitor_app.service.GateKeeperPanelService;
import io.bootify.visitor_app.service.VisitService;
import io.bootify.visitor_app.service.VisitorService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;

@RestController
@RequestMapping("/api/gatekeeper-panel")
public class GateKeeperPanelController {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private GateKeeperPanelService gateKeeperPanelService;

    static final String basePath = "c:/static";
    static final String relativePath = "/vms/"; // (vms-->visiting management system)

    static private Logger LOGGER = LoggerFactory.getLogger(GateKeeperPanelController.class);

    // TODO: handle the case when visitor gives different id at different visit like one time Aadhaar another time Voter id, etc.
    /**
     * if visitor already exist(i.e., already visited before), get visitor by IdNumber like AadhaarNumber, etc.
     *
     * @param idNumber
     * @return
     */
    @GetMapping("/getVisitorByIdNumber")
    public ResponseEntity<VisitorDTO> getVisitor(@RequestParam final String idNumber) {
        return ResponseEntity.ok(visitorService.getByIdNumber(idNumber));
    }

    /**
     * create a new visitor with address and returns the visitor id of the created visitor
     *
     * @param createVisitorRequestDto
     * @return
     */
    @PostMapping("create-visitor")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createVisitor(@RequestBody @Valid final CreateVisitorRequestDto createVisitorRequestDto) {
        return new ResponseEntity<>(gateKeeperPanelService.create(createVisitorRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("create-visit")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createVisit(@RequestBody @Valid final VisitDTO visitDTO) {
        return new ResponseEntity<>(visitService.create(visitDTO), HttpStatus.CREATED);
    }

    @PostMapping("/entry/{visitId}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> markEntry(@PathVariable final Long visitId) {
        visitService.markEntry(visitId);
        return new ResponseEntity<>("updated", HttpStatus.OK);
    }

    @PostMapping("/exit/{visitId}")
    @ApiResponse(responseCode = "200")
    public ResponseEntity<String> markExit(@PathVariable final Long visitId) {
        visitService.markExit(visitId);
        return new ResponseEntity<>("updated", HttpStatus.OK);
    }

    /**
     * upload image of visitor while creating visit
     */
    @PostMapping("/image/upload") // TODO: move the code to service
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) {
        String message = "";

        try {
            String path = relativePath + "testfile_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadPath = basePath + path; // we send the path to the client side not the uploadPath
                                                // and when thw request come with that path we map that to
                                                // the uploadPath (this thing defined in the config file)
            file.transferTo(new File(uploadPath)); // we are storing static content like images, etc. in c:/static/vms folder at the server
                                                    // but we give only the relative path (/vms/file_name) to the client,
                                                    // so that it doesn't know the exact location at the server where the file is stored
                                                    // and when the client request for /vms/file_name,
                                                    // we map/route it to c:/static/vms/file_name and the requested file will be served
                                                    // (this resource handler mapping is configured in the config file)
            message = "Image URL: " + path;
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch(Exception e) {
            LOGGER.error("Exception occurred: {}" + e);
            message = "could not upload the file " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

}
