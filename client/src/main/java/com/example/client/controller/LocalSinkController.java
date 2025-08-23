package com.example.client.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LocalSinkController {
    private static final Logger log = LoggerFactory.getLogger(LocalSinkController.class);


    @PostMapping("/collect")
    public ResponseEntity<String> collect(@RequestBody String body) {
        log.info("[LOCAL-SINK] {}", body);
        return ResponseEntity.ok("ok");
    }
}
