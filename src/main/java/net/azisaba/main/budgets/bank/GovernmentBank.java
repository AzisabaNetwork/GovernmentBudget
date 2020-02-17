package net.azisaba.main.budgets.bank;

import java.math.BigDecimal;

public interface GovernmentBank {

    public boolean has(BigDecimal amount);

    public BigDecimal getBalance();

    public boolean deposit(BigDecimal amount);

    public boolean deposit(BigDecimal amount, String comment);

    public boolean withdraw(BigDecimal amount);

    public boolean withdraw(BigDecimal amount, String comment);

    public void onDisable();
}
