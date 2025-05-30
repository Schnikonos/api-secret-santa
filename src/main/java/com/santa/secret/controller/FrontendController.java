package com.santa.secret.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    @GetMapping({"/", "/index.html"})
    public ResponseEntity<Resource> getIndexHtml() {
        Resource indexHtml = new ClassPathResource("/static/index.html");
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(indexHtml);
    }
}