package com.revShop.productservice.dto;

import jakarta.validation.constraints.NotNull;

public record ProductStatusRequest(

        @NotNull(message = "active is required (true or false)")
        Boolean active

) {}