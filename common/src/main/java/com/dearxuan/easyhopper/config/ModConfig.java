package com.dearxuan.easyhopper.config;

import com.dearxuan.easyhopper.config.retention.EasyConfig;
import com.dearxuan.easyhopper.config.retention.Value;

public class ModConfig {

    @EasyConfig(value = @Value(min = 1, max = 1200, defined = true))
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    @EasyConfig(value = @Value(min = 1, max = 64, defined = true))
    public int HOPPER_INPUT_COUNT = 1;

    @EasyConfig(value = @Value(min = 1, max = 64, defined = true))
    public int HOPPER_OUTPUT_COUNT = 1;

    @EasyConfig
    public boolean HOPPER_FILTERING = false;

    @EasyConfig
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    @EasyConfig(value = @Value(min = 1, max = 1200, defined = true))
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    @EasyConfig(canModifyInGame = false)
    public boolean ALLOW_OP_MODIFY = true;

    public ModConfig() {}

}