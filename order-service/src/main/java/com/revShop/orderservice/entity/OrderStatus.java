package com.revShop.orderservice.entity;

public enum OrderStatus {
    PAYMENT_PENDING,   // online order waiting for Pay / Cancel in the popup
    PLACED,            // paid online, or cash on delivery
    SHIPPED,
    DELIVERED,
    CANCELLED
}