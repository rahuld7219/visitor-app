package io.bootify.visitor_app.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * if new visitor arrived which has not visited before then client-side would have to make 2 API calls,
 * 1st for adding address and then adding the visitor with that received address id,
 * this lead to vulnerability and 2 network calls which is costly,
 * To avoid that here we create another DTO which have both visitor and address details,
 * so client calls only 1 API with all the details for visitor and address,
 * and we use this CreateVisitorRequestDto with all the information to bind the request body
 * and then add the address and visitor, and bind the address id with the visitor
 * i.e., instead of calling 2 APIs, we do the same task at backend for one API call only
 */
@Setter
@Getter
public class CreateVisitorRequestDto {

    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max = 255)
    private String email;

    @NotNull
    @Size(max = 255)
    private String phone;

    @NotNull
    @Size(max = 255)
    private String idNumber;

    @NotNull
    @Size(max = 255)
    private String line1;

    @Size(max = 255)
    private String line2;

    @NotNull
    @Size(max = 6)
    private String pincode;

    @Size(max = 255)
    private String city;

    @Size(max = 255)
    private String state;

    @Size(max = 255)
    private String country;

}
