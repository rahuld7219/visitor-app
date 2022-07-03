package io.bootify.visitor_app.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.*;


@Getter
@Setter
@Builder // @Builder creates all args constructor
@NoArgsConstructor
@AllArgsConstructor
public class VisitorDTO {

    private Long id;

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

    private Long address;

}
