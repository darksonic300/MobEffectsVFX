package com.github.darksonic300.mob_effect_vfx;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MEVConfig {

    public static class Client {

        public final ForgeConfigSpec.DoubleValue opacity;
        public final ForgeConfigSpec.ConfigValue<EffectTypes> effect_type;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("Rendering");
            opacity = builder
                    .comment("The base opacity for the effects.")
                    .translation("config." + MobEffectsVFX.MODID + ".opacity")
                    .defineInRange("Opacity", 0.8d, 0.5, 1);
            builder.pop();

            builder.push("Rendering");
            effect_type = builder
                    .comment("Select the type of effect you want to display.")
                    .translation("config." + MobEffectsVFX.MODID + ".effect_type")
                    .defineEnum("Effect Type", EffectTypes.RISING);
            builder.pop();
        }
    }

    static final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);

    public static final ForgeConfigSpec CLIENT_SPEC = clientSpecPair.getRight();
    public static final Client CLIENT = clientSpecPair.getLeft();
}
