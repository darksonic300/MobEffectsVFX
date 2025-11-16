package com.github.darksonic300.mob_effect_vfx;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MEVConfig {

    public static class Client {

        public final ForgeConfigSpec.ConfigValue<String> effect_type;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("Rendering");
            effect_type = builder
                    .comment("Select the type of effect you want to display.",
                            "Permitted values: vertical, stationary, round.")
                    .translation("config." + MobEffectsVFX.MODID + ".effect_type")
                    .define("Effect Type", "stationary");
            builder.pop();
        }
    }

    static final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);

    public static final ForgeConfigSpec CLIENT_SPEC = clientSpecPair.getRight();
    public static final Client CLIENT = clientSpecPair.getLeft();
}
