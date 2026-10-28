package com.revShop.user.dto;

import jakarta.validation.constraints.NotNull;

// {"active": false} blocks, {"active": true} unblocks
public record UpdateUserStatusRequest(@NotNull Boolean active) {}