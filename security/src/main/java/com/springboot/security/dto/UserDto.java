package com.springboot.security.dto;

import com.springboot.security.entity.Role;
import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String name;

    private String password;

    private String phone;

    private String email;

    private Role role;
}
