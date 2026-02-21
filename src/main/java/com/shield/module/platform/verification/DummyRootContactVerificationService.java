package com.shield.module.platform.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DummyRootContactVerificationService implements RootContactVerificationService {

    private final String emailProvider;
    private final String mobileProvider;

    public DummyRootContactVerificationService(
            @Value("${shield.platform.root.verification.email-provider:DUMMY}") String emailProvider,
            @Value("${shield.platform.root.verification.mobile-provider:DUMMY}") String mobileProvider) {
        this.emailProvider = emailProvider;
        this.mobileProvider = mobileProvider;
    }

    @Override
    public boolean verifyEmailOwnership(String email) {
        log.info("Dummy root email verification placeholder. provider={}, email={}", emailProvider, email);
        return true;
    }

    @Override
    public boolean verifyMobileOwnership(String mobile) {
        log.info("Dummy root mobile verification placeholder. provider={}, mobile={}", mobileProvider, mobile);
        return true;
    }

    @Override
    public String emailProvider() {
        return emailProvider;
    }

    @Override
    public String mobileProvider() {
        return mobileProvider;
    }
}
