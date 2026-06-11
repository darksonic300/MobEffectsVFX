package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public record MEVConfig() {
	public static class Client {
		public final ModConfigSpec.IntValue duration;
		public final ModConfigSpec.DoubleValue opacity;
		public final ModConfigSpec.IntValue refresh_cooldown;
		public final ModConfigSpec.ConfigValue<EffectTypes> effect_type;
		public final ModConfigSpec.ConfigValue<List<? extends String>> blocklist;
		public final ModConfigSpec.ConfigValue<List<? extends String>> entityBlocklist;
		public final ModConfigSpec.ConfigValue<String> soundEffect;
		public final ModConfigSpec.IntValue volume;

		public Client(ModConfigSpec.Builder builder) {
			builder.push("Rendering");
			duration = builder.comment("The duration in MS for the effects.")
					.translation("config." + MobEffectsVFX.MODID + ".duration")
					.defineInRange("Duration", 1000, 500, 2000);
			builder.pop();

			builder.push("Rendering");
			opacity = builder.comment("The base opacity for the effects.")
					.translation("config." + MobEffectsVFX.MODID + ".opacity").defineInRange("Opacity", 0.8d, 0.5, 1);
			builder.pop();

			builder.push("Rendering");
			effect_type = builder.comment("Select the type of effect you want to display.")
					.translation("config." + MobEffectsVFX.MODID + ".effect_type")
					.defineEnum("Effect Type", EffectTypes.RISING);
			builder.pop();

			builder.push("Rendering");
			refresh_cooldown = builder.comment("Set the cooldown time for the effects to show on apply.")
					.translation("config." + MobEffectsVFX.MODID + ".refresh_cooldown")
					.defineInRange("Cooldown", 150, 0, 5000);
			builder.pop();

			builder.push("General");
			blocklist = builder.comment("A list of effects you want to exclude.")
					.translation("config." + MobEffectsVFX.MODID + ".blocklist").defineListAllowEmpty("Blocklist",
							List.of(), () -> "", obj -> ResourceLocation.isValidPath((String) obj));
			builder.pop();

			builder.push("General");
			entityBlocklist = builder.comment("A list of entities you want to exclude.")
					.translation("config." + MobEffectsVFX.MODID + ".entityBlocklist").defineListAllowEmpty(
							"Entity Blocklist", List.of(), () -> "", obj -> ResourceLocation.isValidPath((String) obj));
			builder.pop();

			builder.push("Sound");
			soundEffect = builder.comment("Change the sound used when an effect is applied.")
					.translation("config." + MobEffectsVFX.MODID + ".sound_effect")
					.define("Sound Effect", "minecraft:block.enchantment_table.use");
			builder.pop();

			builder.push("Sound");
			volume = builder.comment("The volume value for the effect sounds.")
					.translation("config." + MobEffectsVFX.MODID + ".volume").defineInRange("Volume", 70, 0, 100);
			builder.pop();
		}
	}

	static final Pair<Client, ModConfigSpec> clientSpecPair = new ModConfigSpec.Builder().configure(Client::new);

	public static final ModConfigSpec CLIENT_SPEC = clientSpecPair.getRight();
	public static final Client CLIENT = clientSpecPair.getLeft();
}
