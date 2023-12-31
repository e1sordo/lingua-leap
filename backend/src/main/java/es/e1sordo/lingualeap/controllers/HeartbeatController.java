package es.e1sordo.lingualeap.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/heartbeat")
public class HeartbeatController {

    @GetMapping
    public ResponseEntity<Void> heartbeat() {
        return ResponseEntity.ok().build();
    }
}
