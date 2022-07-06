package io.bootify.visitor_app.rest;

import io.bootify.visitor_app.model.UserDTO;
import io.bootify.visitor_app.service.UserService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/api/admin-panel")
public class AdminPanelController {

    /*
        create flat with CSV file
        create single User/ CSV file/ update user (CRUD on User)
        create GateKeeper (CRUD on GateKeeper)
        generate Daily visit reports
     */

    static private org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AdminPanelController.class);

    @Autowired
    private UserService userService;

    /**
     * create a single user
     *
     * @param userDTO
     * @return
     */
    @PostMapping("/create-user")
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUser(@RequestBody @Valid final UserDTO userDTO) {
        return new ResponseEntity<>(userService.create(userDTO), HttpStatus.CREATED);
    }

    // TODO: read address and flat data from file instead of just passing address id and flat id and save all to the database, just like we did in create-visitor API
    // TODO: move the code to service and put @Transactional
    @PostMapping("user-csv/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            // CSVParser a library of apache commons to work with csv files
            CSVParser csvParser = new CSVParser(fileReader,
                    CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            for (CSVRecord csvRecord: csvRecords) {
                UserDTO userDTO = UserDTO.builder()
                        .name(csvRecord.get("name")) // first line of CSV file have name,email,phone,flat,address,roleId as header
                        .email(csvRecord.get("email"))
                        .phone(csvRecord.get("phone"))
                        .flat(Long.valueOf(csvRecord.get("flat")))
                        .address(Long.valueOf(csvRecord.get("address")))
                        .roleId(Long.valueOf(csvRecord.get("roleId")))
                        .build();

                userService.create(userDTO);
                LOGGER.info("Read user name :{}", userDTO.getName());
            }
            message = "Uploaded the file successfully: " + file.getOriginalFilename();

            return ResponseEntity.status(HttpStatus.OK).body(message);

        } catch (Exception e) {
            LOGGER.error("Exception occurred: {}" + e);
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }

    }

}
