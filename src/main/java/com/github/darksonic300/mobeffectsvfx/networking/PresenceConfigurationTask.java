package com.github.darksonic300.mobeffectsvfx.networking;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;

import java.util.function.Consumer;

public record PresenceConfigurationTask(
		ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
	public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type(
			MobEffectsVFX.getResource("presence_configuration_task"));

	@Override
	public void run(final Consumer<CustomPacketPayload> consumer) {
		final var payload = new ServerPresencePayload();
		consumer.accept(payload);
		this.listener.finishCurrentTask(this.type());
	}

	@Override
	public Type type() {
		return TYPE;
	}
}
