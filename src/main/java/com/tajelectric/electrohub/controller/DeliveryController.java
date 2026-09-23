package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.util.PinUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {

    private static final Set<String> BLOCKED = Set.of("000000", "111111", "999999");

    @GetMapping("/check")
    public ResponseEntity<?> check(@RequestParam(required = false) String pincode) {
        String pin = PinUtil.from(pincode);
        Map<String, Object> body = new HashMap<>();
        body.put("pincode", pin);

        if (!PinUtil.isValid(pin)) {
            body.put("available", false);
            body.put("error", "Enter a valid 6-digit Indian pincode");
            return ResponseEntity.ok(body);
        }
        if (BLOCKED.contains(pin)) {
            body.put("available", false);
            body.put("error", "Sorry, we do not deliver to " + pin + " yet.");
            return ResponseEntity.ok(body);
        }

        body.put("available", true);
        body.put("days", "2–4 days");
        body.put("cod", true);
        body.put("message", "Delivery available to " + pin + " · Usually 2–4 days · COD available");
        return ResponseEntity.ok(body);
    }
}
