package com.shield.module.billing.gateway;

import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class DefaultPaymentGatewayCallbackAdapter implements PaymentGatewayCallbackAdapter {

    @Override
    public String provider() {
        return "DEFAULT";
    }

    @Override
    public String buildSignaturePayload(PaymentCallbackRequest request, PaymentGatewayTransactionEntity transaction) {
        return String.join("|",
                normalize(request.transactionRef()),
                normalize(request.gatewayOrderId()),
                normalize(request.gatewayPaymentId()),
                normalize(request.status()),
                normalize(request.payload()));
    }

    @Override
    public boolean isSuccessStatus(String status) {
        String normalized = normalize(status).toUpperCase(Locale.ROOT);
        return "SUCCESS".equals(normalized) || "PAID".equals(normalized);
    }

    protected String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
