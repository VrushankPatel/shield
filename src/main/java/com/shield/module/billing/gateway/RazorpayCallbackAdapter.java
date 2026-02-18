package com.shield.module.billing.gateway;

import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class RazorpayCallbackAdapter extends DefaultPaymentGatewayCallbackAdapter {

    @Override
    public String provider() {
        return "RAZORPAY";
    }

    @Override
    public String buildSignaturePayload(PaymentCallbackRequest request, PaymentGatewayTransactionEntity transaction) {
        if (request.gatewayOrderId() != null && request.gatewayPaymentId() != null) {
            return normalize(request.gatewayOrderId()) + "|" + normalize(request.gatewayPaymentId());
        }
        return super.buildSignaturePayload(request, transaction);
    }

    @Override
    public boolean isSuccessStatus(String status) {
        String normalized = normalize(status).toUpperCase(Locale.ROOT);
        return "CAPTURED".equals(normalized) || super.isSuccessStatus(status);
    }
}
