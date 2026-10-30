package com.revShop.user.service;

import com.revShop.user.dto.AddressRequest;
import com.revShop.user.dto.AddressResponse;
import com.revShop.user.entity.Address;
import com.revShop.user.exception.ApiException;
import com.revShop.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private static final int MAX_ADDRESSES = 5;

    private final AddressRepository addressRepository;

    // US-04: my saved addresses, the default one first
    public List<AddressResponse> list(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(userId).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse add(Long userId, AddressRequest req) {
        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You can save up to " + MAX_ADDRESSES + " addresses");
        }
        Address address = new Address();
        address.setUserId(userId);
        copy(req, address);
        address.setDefaultAddress(count == 0);   // the first address becomes the default
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long id, AddressRequest req) {
        Address address = findOwn(userId, id);
        copy(req, address);
        return AddressResponse.from(addressRepository.save(address));
    }

    // exactly one default: the chosen one becomes default, every other one stops being it
    @Transactional
    public AddressResponse makeDefault(Long userId, Long id) {
        Address chosen = findOwn(userId, id);
        for (Address address : addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(userId)) {
            address.setDefaultAddress(address.getId().equals(chosen.getId()));
        }
        return AddressResponse.from(chosen);
    }

    // deleting the default hands it to the oldest address left
    @Transactional
    public void delete(Long userId, Long id) {
        Address address = findOwn(userId, id);
        addressRepository.delete(address);
        if (address.isDefaultAddress()) {
            addressRepository.findByUserIdOrderByDefaultAddressDescIdAsc(userId).stream()
                    .filter(a -> !a.getId().equals(id))
                    .findFirst()
                    .ifPresent(a -> a.setDefaultAddress(true));
        }
    }

    private Address findOwn(Long userId, Long id) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void copy(AddressRequest req, Address address) {
        address.setFullName(req.fullName().trim());
        address.setPhone(req.phone());
        address.setLine1(req.line1().trim());
        address.setLine2(req.line2() == null || req.line2().isBlank() ? null : req.line2().trim());
        address.setCity(req.city().trim());
        address.setState(req.state().trim());
        address.setPincode(req.pincode());
    }
}