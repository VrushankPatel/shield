package com.shield.module.accounting.dto;

import java.math.BigDecimal;

public record FinancialReportLine(String label, BigDecimal amount) {
}
