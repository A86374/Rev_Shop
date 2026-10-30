package com.revShop.user.dto;

import com.revShop.user.entity.Address;

public record AddressResponse(
        Long id,
        String fullName,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String pincode,
        boolean defaultAddress,
        String text            // the whole address on one line
) {
    public static AddressResponse from(Address a) {
        return new AddressResponse(a.getId(), a.getFullName(), a.getPhone(), a.getLine1(), a.getLine2(),
                a.getCity(), a.getState(), a.getPincode(), a.isDefaultAddress(), a.toText());
    }
}