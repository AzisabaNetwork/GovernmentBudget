package net.azisaba.main.budgets.config;

import java.util.UUID;

import net.azisaba.main.budgets.GovernmentBudget;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class DefaultConfig extends Config {

    private UUID accountUUID;
    private boolean dontUsePlayerAccount = false;

    public DefaultConfig(@NonNull GovernmentBudget plugin) {
        super(plugin, "configs/config.yml", "config.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() {
        super.loadConfig();

        String accountUUIDStr = config.getString("AccountUUID", null);
        if ( accountUUIDStr == null ) {
            plugin.getLogger().info("Configのロードに失敗 (AccountUUIDが設定されていません！)");
        } else if ( accountUUIDStr.equalsIgnoreCase("none") ) {
            dontUsePlayerAccount = true;
        } else {
            try {
                accountUUID = UUID.fromString(accountUUIDStr);
            } catch ( Exception e ) {
                plugin.getLogger().info("Configのロードに失敗 (AccountUUIDが正しくありません！)");
                accountUUID = null;
            }
        }
    }
}