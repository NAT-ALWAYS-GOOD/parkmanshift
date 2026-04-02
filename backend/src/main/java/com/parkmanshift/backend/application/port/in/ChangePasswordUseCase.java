package com.parkmanshift.backend.application.port.in;

public interface ChangePasswordUseCase {
    void changePassword(String username, String currentPassword, String newPassword);
}
