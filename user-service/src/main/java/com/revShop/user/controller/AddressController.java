package com.revShop.user.controller;

import com.revShop.user.dto.AddressRequest;
import com.revShop.user.dto.AddressResponse;
import com.revShop.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public List<AddressResponse> list(@RequestHeader("X-User-Id") Long userId) {
        return addressService.list(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse add(@RequestHeader("X-User-Id") Long userId,
                               @Valid @RequestBody AddressRequest request) {
        return addressService.add(userId, request);
    }

    @PutMapping("/{id}")
    public AddressResponse update(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable("id") Long id,
                                  @Valid @RequestBody AddressRequest request) {
        return addressService.update(userId, id, request);
    }

    @PatchMapping("/{id}/default")
    public AddressResponse makeDefault(@RequestHeader("X-User-Id") Long userId,
                                       @PathVariable("id") Long id) {
        return addressService.makeDefault(userId, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestHeader("X-User-Id") Long userId,
                       @PathVariable("id") Long id) {
        addressService.delete(userId, id);
    }
}