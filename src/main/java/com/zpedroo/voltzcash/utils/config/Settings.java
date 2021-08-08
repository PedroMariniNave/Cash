package com.zpedroo.voltzcash.utils.config;

import com.zpedroo.voltzcash.utils.FileUtils;

import java.util.List;

public class Settings {

    public static final Integer TAX_PER_TRANSACTION = FileUtils.get().getInt(FileUtils.Files.CONFIG, "Settings.tax-per-transaction");

    public static final Long TOP_UPDATE = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.cash-top-update");

    public static final String CASH_CMD = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.commands.cash.cmd");

    public static final List<String> CASH_ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.commands.cash.aliases");

    public static final String SHOP_CMD = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.commands.shop.cmd");

    public static final List<String> SHOP_ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.commands.shop.aliases");
}