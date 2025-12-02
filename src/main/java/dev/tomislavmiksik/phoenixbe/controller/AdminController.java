package dev.tomislavmiksik.phoenixbe.controller;

import dev.tomislavmiksik.phoenixbe.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController()
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    final private AdminService adminService;

    @PostMapping("/keygen")
    public ResponseEntity<URI> createApiKey(@RequestBody String body){
        URI uri = URI.create("");

        return ResponseEntity.created(uri).build();
    }
}
