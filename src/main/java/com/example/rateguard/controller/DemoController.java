
/**
 * we don't need controller for this project as this is a middleware and we have just created this controller to v
 * check our middleware as just a demo controller
 */

package com.example.rateguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/api/demo")
    public Map<String, String> demo() {
        return Map.of(
                "message", "Request allowed by RateGuard",
                "status", "success"
        );
    }

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Controller is working";
    }
}
