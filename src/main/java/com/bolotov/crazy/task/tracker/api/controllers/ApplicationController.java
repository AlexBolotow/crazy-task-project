package com.bolotov.crazy.task.tracker.api.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
@AllArgsConstructor
public class ApplicationController {

    @GetMapping("welcome")
    public String welcome() {
        return "Welcome to the task tracker";
    }


}
