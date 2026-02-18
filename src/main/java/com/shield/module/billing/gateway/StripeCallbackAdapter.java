package com.shield.module.billing.gateway;

import com.shield.module.billing.dto.PaymentCallbackRequest;
import com.shield.module.billing.entity.PaymentGatewayTransactionEntity;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class StripeCallbackAdapter extends DefaultPaymentGatewayCallbackAdapter {

    @Override
    public String provider() {
        return "STRIPE";
    }

    @Override
    public String buildSignaturePayload(PaymentCallbackRequest request, PaymentGatewayTransactionEntity transaction) {
        if (request.payload() != null && !request.payload().isBlank()) {
            return request.payload();
        }
        return super.buildSignaturePayload(request, transaction);
    }

    @Override
    public boolean isSuccessStatus(String status) {
        String normalized = normalize(status).toUpperCase(Locale.ROOT);
        return "PAYMENT_INTENT.SUCCEEDED".equals(normalized) || super.isSuccessStatus(status);
    }
}
