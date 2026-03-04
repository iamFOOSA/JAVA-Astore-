package by.abram.astore.dto;

import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;
}