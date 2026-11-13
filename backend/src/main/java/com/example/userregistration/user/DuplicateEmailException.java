package com.example.userregistration.user;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("A user with this email address already exists.");
    }
}
