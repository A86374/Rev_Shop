package com.revShop.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String fullName;      // who receives the parcel

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false, length = 200)
    private String line1;         // house number, street

    @Column(length = 200)
    private String line2;         // area, landmark (optional)

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 80)
    private String state;

    @Column(nullable = false, length = 6)
    private String pincode;

    @Builder.Default
    @Column(nullable = false)
    private boolean defaultAddress = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // one line, ready for the checkout page
    public String toText() {
        String area = (line2 == null || line2.isBlank()) ? "" : ", " + line2;
        return fullName + ", " + line1 + area + ", " + city + ", " + state + " " + pincode + ". Phone " + phone;
    }
}