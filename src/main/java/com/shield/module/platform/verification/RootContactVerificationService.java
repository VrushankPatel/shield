package com.shield.module.platform.verification;

public interface RootContactVerificationService {

    boolean verifyEmailOwnership(String email);

    boolean verifyMobileOwnership(String mobile);

    String emailProvider();

    String mobileProvider();
}
