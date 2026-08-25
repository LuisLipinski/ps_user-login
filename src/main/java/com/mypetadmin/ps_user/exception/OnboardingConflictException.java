package com.mypetadmin.ps_user.exception;

public class OnboardingConflictException extends RuntimeException {
    public OnboardingConflictException() {
        super("Onboarding já associado a outro usuário");
    }
}
