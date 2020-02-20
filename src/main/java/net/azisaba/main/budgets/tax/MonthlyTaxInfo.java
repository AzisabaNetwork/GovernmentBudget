package net.azisaba.main.budgets.tax;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MonthlyTaxInfo {

    private final UUID uuid;
    private final BigDecimal lastBank;
    private final int currentStage;

    public BigDecimal getSubtractMoney() {
        return lastBank.divide(BigDecimal.valueOf(20));
    }
}
