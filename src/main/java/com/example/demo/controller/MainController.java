package com.example.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @GetMapping(value = { "/", "{path:^(?!api|.*\\..*$).*}" })
    public ResponseEntity<Resource> serveIndex() {
        logger.info("Serving React app index.html right now...");

        try {
            Resource resource = new ClassPathResource("static/index.html");

            if (!resource.exists()) {
                logger.error("index.html not found in static directory");
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/html")
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error while serving index.html: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}