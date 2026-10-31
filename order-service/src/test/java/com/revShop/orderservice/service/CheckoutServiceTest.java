package com.revShop.orderservice.service;

import com.revShop.orderservice.client.CheckoutCartClient;
import com.revShop.orderservice.client.CheckoutCartClient.CartLine;
import com.revShop.orderservice.client.CheckoutPaymentClient;
import com.revShop.orderservice.client.CheckoutPaymentClient.PaymentInfo;
import com.revShop.orderservice.client.CheckoutStockClient;
import com.revShop.orderservice.client.CheckoutStockClient.ReservedItem;
import com.revShop.orderservice.dto.CheckoutRequest;
import com.revShop.orderservice.dto.CheckoutResponse;
import com.revShop.orderservice.entity.Order;
import com.revShop.orderservice.entity.PaymentMethod;
import com.revShop.orderservice.exception.ApiException;
import com.revShop.orderservice.repository.OrderRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit tests: no database and no other services. Every dependency is a Mockito mock.
@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final Long BUYER = 1L;
    private static final String ADDRESS = "12 MG Road, Hyderabad 500001";

    @Mock private OrderRepository orderRepository;
    @Mock private CheckoutCartClient cartClient;
    @Mock private CheckoutStockClient stockClient;
    @Mock private CheckoutPaymentClient paymentClient;
    @Mock private OrderNotifier notifier;

    @InjectMocks private CheckoutService checkoutService;

    // mouse x2 from seller 2, lamp x1 from seller 3
    private void cartWithTwoSellers() {
        when(cartClient.getCart(BUYER)).thenReturn(List.of(
                new CartLine(10L, BUYER, 1L, 2),
                new CartLine(11L, BUYER, 5L, 1)));
        when(stockClient.reserve(any())).thenReturn(List.of(
                new ReservedItem(1L, "Wireless Mouse", new BigDecimal("799.00"), 2L, 2),
                new ReservedItem(5L, "Desk Lamp", new BigDecimal("999.00"), 3L, 1)));
    }

    // saveAll gives back what it was given, like the real repository
    private void ordersAreSaved() {
        when(orderRepository.saveAll(ArgumentMatchers.<Order>anyList())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void codCheckoutMakesOneOrderPerSellerAndEmptiesTheCart() {
        cartWithTwoSellers();
        ordersAreSaved();
        when(paymentClient.create(any())).thenReturn(
                new PaymentInfo(7L, "CHK-1", BUYER, new BigDecimal("2597.00"), "COD", "PENDING", null));

        CheckoutResponse response = checkoutService.checkout(BUYER, new CheckoutRequest(PaymentMethod.COD, ADDRESS));

        assertEquals(2, response.orders().size());
        assertEquals(0, new BigDecimal("2597.00").compareTo(response.totalAmount()));
        assertEquals("PLACED", response.orders().get(0).status());
        assertEquals(7L, response.paymentId());
        verify(cartClient).clearCart(BUYER);
        verify(notifier).placed(anyList());
    }

    @Test
    void onlineCheckoutWaitsForThePayment() {
        cartWithTwoSellers();
        ordersAreSaved();
        when(paymentClient.create(any())).thenReturn(
                new PaymentInfo(8L, "CHK-2", BUYER, new BigDecimal("2597.00"), "ONLINE", "PENDING", null));

        CheckoutResponse response = checkoutService.checkout(BUYER, new CheckoutRequest(PaymentMethod.ONLINE, ADDRESS));

        assertEquals("PAYMENT_PENDING", response.orders().get(0).status());
        verify(notifier, never()).placed(anyList());   // announced only after Pay
    }

    @Test
    void emptyCartIsRejected() {
        when(cartClient.getCart(BUYER)).thenReturn(List.of());

        ApiException error = assertThrows(ApiException.class,
                () -> checkoutService.checkout(BUYER, new CheckoutRequest(PaymentMethod.COD, ADDRESS)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        verify(stockClient, never()).reserve(any());
    }

    @Test
    void outOfStockGives409AndCreatesNoOrders() {
        when(cartClient.getCart(BUYER)).thenReturn(List.of(new CartLine(10L, BUYER, 1L, 50)));
        FeignException conflict = mock(FeignException.class);
        when(conflict.status()).thenReturn(409);
        when(stockClient.reserve(any())).thenThrow(conflict);

        ApiException error = assertThrows(ApiException.class,
                () -> checkoutService.checkout(BUYER, new CheckoutRequest(PaymentMethod.COD, ADDRESS)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(orderRepository, never()).saveAll(anyList());
    }

    @Test
    void failedPaymentDeletesTheOrdersAndGivesTheStockBack() {
        cartWithTwoSellers();
        ordersAreSaved();
        when(paymentClient.create(any())).thenThrow(new RuntimeException("payment-service is down"));

        ApiException error = assertThrows(ApiException.class,
                () -> checkoutService.checkout(BUYER, new CheckoutRequest(PaymentMethod.COD, ADDRESS)));

        assertEquals(HttpStatus.BAD_GATEWAY, error.getStatus());
        verify(orderRepository).deleteAll(anyList());
        verify(stockClient).release(any());
        verify(cartClient, never()).clearCart(any());
    }
}