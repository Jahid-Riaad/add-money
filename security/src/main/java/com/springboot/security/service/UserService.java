package com.springboot.security.service;

import com.springboot.security.dto.UserDto;
import com.springboot.security.entity.User;
import com.springboot.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    public UserDto updateUser(UserDto userDto) {
        User user = mapToEntity(userDto);
        userRepository.save(user);

        return mapToDto(user);
    }

    public User mapToEntity(UserDto userDto) {
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setPhone(userDto.getPhone());
        user.setPassword(userDto.getPassword());
        user.setRole(userDto.getRole());
        return user;
    }

    public UserDto mapToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setPhone(user.getPhone());
        userDto.setPassword(user.getPassword());
        userDto.setRole(user.getRole());
        return userDto;
    }
}
