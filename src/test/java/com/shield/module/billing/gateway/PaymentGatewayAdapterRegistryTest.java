package com.shield.module.billing.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;
import org.junit.jupiter.api.Test;

class PaymentGatewayAdapterRegistryTest {

    @Test
    void resolveShouldReturnProviderSpecificOrDefault() {
        PaymentGatewayCallbackAdapter defaultAdapter = new DefaultPaymentGatewayCallbackAdapter();
        PaymentGatewayCallbackAdapter stripeAdapter = new StripeCallbackAdapter();
        PaymentGatewayCallbackAdapter razorpayAdapter = new RazorpayCallbackAdapter();

        PaymentGatewayAdapterRegistry registry =
                new PaymentGatewayAdapterRegistry(java.util.List.of(defaultAdapter, stripeAdapter, razorpayAdapter));

        assertInstanceOf(StripeCallbackAdapter.class, registry.resolve("stripe"));
        assertInstanceOf(RazorpayCallbackAdapter.class, registry.resolve("RAZORPAY"));
        assertInstanceOf(DefaultPaymentGatewayCallbackAdapter.class, registry.resolve("unknown"));
    }

    @Test
    void adaptersShouldBuildExpectedPayloadAndStatus() {
        PaymentGatewayTransactionEntity transaction = new PaymentGatewayTransactionEntity();
        transaction.setProvider("STRIPE");

        DefaultPaymentGatewayCallbackAdapter defaultAdapter = new DefaultPaymentGatewayCallbackAdapter();
        String defaultPayload = defaultAdapter.buildSignaturePayload(
                new PaymentCallbackRequest("tx1", "ord1", "pay1", "SUCCESS", "{\"k\":1}", null),
                transaction);
        assertEquals("tx1|ord1|pay1|SUCCESS|{\"k\":1}", defaultPayload);
        assertTrue(defaultAdapter.isSuccessStatus("paid"));

        StripeCallbackAdapter stripeAdapter = new StripeCallbackAdapter();
        assertEquals("{\"type\":\"payment_intent.succeeded\"}", stripeAdapter.buildSignaturePayload(
                new PaymentCallbackRequest("tx2", "ord2", "pay2", "FAILED", "{\"type\":\"payment_intent.succeeded\"}", null),
                transaction));

        RazorpayCallbackAdapter razorpayAdapter = new RazorpayCallbackAdapter();
        assertEquals("ord3|pay3", razorpayAdapter.buildSignaturePayload(
                new PaymentCallbackRequest("tx3", "ord3", "pay3", "CAPTURED", null, null),
                transaction));
        assertTrue(razorpayAdapter.isSuccessStatus("captured"));
    }
}
