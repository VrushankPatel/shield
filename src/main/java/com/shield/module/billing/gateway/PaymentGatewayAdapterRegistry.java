package com.shield.module.billing.gateway;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayAdapterRegistry {

    private final PaymentGatewayCallbackAdapter defaultAdapter;
    private final Map<String, PaymentGatewayCallbackAdapter> adaptersByProvider;

    public PaymentGatewayAdapterRegistry(List<PaymentGatewayCallbackAdapter> adapters) {
        Map<String, PaymentGatewayCallbackAdapter> resolved = new HashMap<>();
        PaymentGatewayCallbackAdapter fallback = null;

        for (PaymentGatewayCallbackAdapter adapter : adapters) {
            String provider = normalize(adapter.provider());
            if ("DEFAULT".equals(provider)) {
                fallback = adapter;
            } else {
                resolved.put(provider, adapter);
            }
        }

        this.defaultAdapter = Objects.requireNonNull(fallback, "Default payment callback adapter is required");
        this.adaptersByProvider = Map.copyOf(resolved);
    }

    public PaymentGatewayCallbackAdapter resolve(String provider) {
        if (provider == null || provider.isBlank()) {
            return defaultAdapter;
        }
        return adaptersByProvider.getOrDefault(normalize(provider), defaultAdapter);
    }

    private String normalize(String provider) {
        return provider.trim().toUpperCase(Locale.ROOT);
    }
}
