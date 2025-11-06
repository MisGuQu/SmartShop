package com.smartshop.exception;

import java.time.LocalDateTime;

public class AccountLockedException extends AuthException {

    private final LocalDateTime unlockAt;

    public AccountLockedException(String message, LocalDateTime unlockAt) {
        super(message);
        this.unlockAt = unlockAt;
    }

    public LocalDateTime getUnlockAt() {
        return unlockAt;
    }
}

