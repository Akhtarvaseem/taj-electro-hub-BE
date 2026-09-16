package com.tajelectric.electrohub.controller;

import com.tajelectric.electrohub.entity.Address;
import com.tajelectric.electrohub.repository.AddressRepository;
import com.tajelectric.electrohub.security.AuthUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressRepository addressRepo;

    public AddressController(AddressRepository addressRepo) {
        this.addressRepo = addressRepo;
    }

    @GetMapping
    public Map<String, Object> list() {
        Long userId = AuthUtil.currentUserId();
        return Map.of("addresses",
                addressRepo.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId));
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody Map<String, Object> b) {
        Long userId = AuthUtil.currentUserId();
        Address a = new Address();
        a.setUserId(userId);
        a.setFullName((String) b.get("fullName"));
        a.setPhone((String) b.get("phone"));
        a.setPincode((String) b.get("pincode"));
        a.setLine1((String) b.get("line1"));
        a.setLine2((String) b.get("line2"));
        a.setCity((String) b.get("city"));
        a.setState((String) b.get("state"));
        a.setAddressType((String) b.getOrDefault("addressType", "Home"));
        a.setIsDefault(Boolean.parseBoolean(String.valueOf(b.getOrDefault("isDefault", false))));
        return Map.of("address", addressRepo.save(a));
    }

    // ===== UPDATE an existing address =====
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> b) {
        Long userId = AuthUtil.currentUserId();
        Optional<Address> found = addressRepo.findById(id);
        if (found.isEmpty() || !found.get().getUserId().equals(userId)) {
            return ResponseEntity.status(404).body(Map.of("error", "Address not found"));
        }
        Address a = found.get();
        if (b.get("fullName") != null) a.setFullName(b.get("fullName").toString());
        if (b.get("phone") != null) a.setPhone(b.get("phone").toString());
        if (b.get("pincode") != null) a.setPincode(b.get("pincode").toString());
        if (b.get("line1") != null) a.setLine1(b.get("line1").toString());
        if (b.get("line2") != null) a.setLine2(b.get("line2").toString());
        if (b.get("city") != null) a.setCity(b.get("city").toString());
        if (b.get("state") != null) a.setState(b.get("state").toString());
        if (b.get("addressType") != null) a.setAddressType(b.get("addressType").toString());
        return ResponseEntity.ok(Map.of("address", addressRepo.save(a)));
    }

    @DeleteMapping
    public Map<String, Object> delete(@RequestBody Map<String, Object> b) {
        Long userId = AuthUtil.currentUserId();
        Long id = Long.valueOf(b.get("id").toString());
        addressRepo.findById(id).ifPresent(a -> {
            if (a.getUserId().equals(userId)) addressRepo.delete(a);
        });
        return Map.of("ok", true);
    }
}
