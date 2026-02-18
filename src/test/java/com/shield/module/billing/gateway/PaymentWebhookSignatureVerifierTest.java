package com.shield.module.billing.gateway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shield.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

class PaymentWebhookSignatureVerifierTest {

    @Test
    void shouldSkipValidationWhenProviderSecretNotConfigured() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("");
        assertDoesNotThrow(() -> verifier.assertValidSignature("STRIPE", "{\"event\":\"failed\"}", ""));
    }

    @Test
    void shouldAcceptValidHmacSignature() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("STRIPE=test_secret");
        String payload = "{\"event\":\"payment_intent.succeeded\"}";
        String signature = verifier.sign("test_secret", payload);

        assertDoesNotThrow(() -> verifier.assertValidSignature("stripe", payload, signature));
    }

    @Test
    void shouldRejectMissingSignatureWhenSecretExists() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("STRIPE=test_secret");
        assertThrows(BadRequestException.class, () -> verifier.assertValidSignature("STRIPE", "{}", null));
    }

    @Test
    void shouldRejectInvalidSignature() {
        PaymentWebhookSignatureVerifier verifier = new PaymentWebhookSignatureVerifier("STRIPE=test_secret");
        assertThrows(BadRequestException.class, () -> verifier.assertValidSignature("STRIPE", "{}", "invalid"));
    }
}
