package com.shield.module.billing.gateway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shield.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

class PaymentWebhookSignatureVerifierTest {

    @Test
    void shouldSkipValidationWhenProviderSecretNotConfiguredAndStrictModeDisabled() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("", false);
        assertDoesNotThrow(() -> verifier.assertValidSignature("STRIPE", "{\"event\":\"failed\"}", ""));
    }

    @Test
    void shouldRejectWhenProviderSecretNotConfiguredAndStrictModeEnabled() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("", true);
        assertThrows(BadRequestException.class, () -> verifier.assertValidSignature("STRIPE", "{\"event\":\"failed\"}", ""));
    }

    @Test
    void shouldAcceptValidHmacSignature() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("STRIPE=test_secret", true);
        String payload = "{\"event\":\"payment_intent.succeeded\"}";
        String signature = verifier.sign("test_secret", payload);

        assertDoesNotThrow(() -> verifier.assertValidSignature("stripe", payload, signature));
    }

    @Test
    void shouldRejectMissingSignatureWhenSecretExists() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("STRIPE=test_secret", true);
        assertThrows(BadRequestException.class, () -> verifier.assertValidSignature("STRIPE", "{}", null));
    }

    @Test
    void shouldRejectInvalidSignature() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("STRIPE=test_secret", true);
        assertThrows(BadRequestException.class, () -> verifier.assertValidSignature("STRIPE", "{}", "invalid"));
    }
}
