package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import com.github.darksonic300.mob_effect_vfx.util.ActivationTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class MEVConfig {

    public static class Client {

        public final ForgeConfigSpec.IntValue duration;
        public final ForgeConfigSpec.DoubleValue opacity;
        public final ForgeConfigSpec.IntValue refresh_cooldown;
        public final ForgeConfigSpec.ConfigValue<EffectTypes> effect_type;
        //public final ForgeConfigSpec.ConfigValue<ActivationTriggers> action;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blacklist;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("Rendering");
            duration = builder
                    .comment("The duration in MS for the effects.")
                    .translation("config." + MobEffectsVFX.MODID + ".duration")
                    .defineInRange("Duration", 1000, 500, 2000);
            builder.pop();

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

            builder.push("Rendering");
            refresh_cooldown = builder
                    .comment("Set the cooldown time for the effects to show on apply.")
                    .translation("config." + MobEffectsVFX.MODID + ".refresh_cooldown")
                    .defineInRange("Cooldown", 150, 0, 5000);
            builder.pop();

            /*
            builder.push("Rendering");
            action = builder
                    .comment("Option to choose specific actions to trigger effects.")
                    .translation("config." + MobEffectsVFX.MODID + ".activation")
                    .defineEnum("Activation", ActivationTriggers.ALL);
            builder.pop();
            */

            builder.push("Rendering");
            blacklist = builder
                    .comment("A list of effects you want to exclude.")
                    .translation("config." + MobEffectsVFX.MODID + ".blacklist")
                    .defineList(
                            "List",
                            List.of(),
                            obj -> ResourceLocation.isValidResourceLocation((String) obj)
                    );
            builder.pop();
        }
    }

    static final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);

    public static final ForgeConfigSpec CLIENT_SPEC = clientSpecPair.getRight();
    public static final Client CLIENT = clientSpecPair.getLeft();
}
