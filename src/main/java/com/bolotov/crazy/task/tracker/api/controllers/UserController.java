package com.bolotov.crazy.task.tracker.api.controllers;

import com.bolotov.crazy.task.tracker.store.entities.UserEntity;
import com.bolotov.crazy.task.tracker.store.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
@AllArgsConstructor
public class UserController {

    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    @PostMapping("/new-user")
    public String addUser(@RequestBody UserEntity user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "success";
    }
}
