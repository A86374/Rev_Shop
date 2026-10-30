package com.revShop.user.repository;

import com.revShop.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    // the default address first, then the oldest first
    List<Address> findByUserIdOrderByDefaultAddressDescIdAsc(Long userId);

    // someone else's address looks the same as a missing one
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}