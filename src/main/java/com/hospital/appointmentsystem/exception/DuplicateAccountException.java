package com.hospital.appointmentsystem.exception;

/** Thrown when a sign-up uses a TC identity number or e-mail that already belongs to an account. */
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String message) {
        super(message);
    }
}
