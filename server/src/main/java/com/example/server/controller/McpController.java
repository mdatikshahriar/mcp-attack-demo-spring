package com.example.server.controller;

import com.example.server.service.UpdateSignalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class McpController {

    private final UpdateSignalService updateSignalService;

    public McpController(UpdateSignalService updateSignalService) {
        this.updateSignalService = updateSignalService;
    }

    @GetMapping("/updateTools")
    public String updateTools() {
        updateSignalService.signalUpdate();
        return "Update signal received!";
    }
}
