package com.aftertime.aftertimefault.modules.dungeon;

import com.aftertime.aftertimefault.config.ModConfig;
import com.aftertime.aftertimefault.utils.DungeonUtils;
import com.aftertime.aftertimefault.utils.RenderUtils;
import com.aftertime.aftertimefault.events.ClientTickEventBus;
import com.aftertime.aftertimefault.events.GameMessageEventBus;
import com.aftertime.aftertimefault.events.WorldRenderEventBus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SecrectClick {
	private static final MinecraftClient MC = MinecraftClient.getInstance();
	private static final long HIGHLIGHT_TICKS = 100L; // ~5 seconds

	private static final Set<UUID> VALID_SKULL_IDS = Set.of(
			UUID.fromString("e0f3e929-869e-3dca-9504-54c666ee6f23"), // Wither Essence skull
			UUID.fromString("fed95410-aba1-39df-9b95-1d4f361eb66e")  // Redstone Key skull
	);

	private static final Map<BlockPos, HighlightedBlock> HIGHLIGHTS = new HashMap<>();

	private SecrectClick() {}

	public static void register() {
		ClientTickEventBus.register(client -> {
			if (!ModConfig.enableSecretClicks) {
				HIGHLIGHTS.clear();
				return;
			}

			if (client.world == null || client.player == null) {
				HIGHLIGHTS.clear();
				return;
			}

			cleanupExpired(client.world);

			while (client.options.useKey.wasPressed()) {
				handleUseAction(client);
			}
		});

		WorldRenderEventBus.registerAfterEntities(() -> renderHighlights());

		GameMessageEventBus.register((message, overlay) -> handleChatMessage(message));
	}

	private static void handleUseAction(MinecraftClient client) {
		if (!DungeonUtils.isInDungeon()) return;
		if (!(client.crosshairTarget instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos pos = hit.getBlockPos();
		ClientWorld world = client.world;
		if (pos == null || world == null) return;

		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (!isBlockEligible(block)) return;

		if (block instanceof SkullBlock && !isValidSkull(world, pos)) {
			return;
		}

		highlightBlock(pos, block, world.getTime());
	}

	private static void renderHighlights() {
		if (!ModConfig.enableSecretClicks || HIGHLIGHTS.isEmpty()) return;
		if (!DungeonUtils.isInDungeon()) return;
		if (MC.world == null) return;

		int color = ModConfig.secretClicksHighlightColor;
		float r = ((color >> 16) & 0xFF) / 255f;
		float g = ((color >> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;
		float a = ((color >> 24) & 0xFF) / 255f;
		if (a <= 0f) {
			a = 0.5f; // fallback when picker does not include alpha
		}

		for (Map.Entry<BlockPos, HighlightedBlock> entry : HIGHLIGHTS.entrySet()) {
			BlockPos pos = entry.getKey();
			HighlightedBlock highlighted = entry.getValue();

			if (highlighted == null || pos == null) continue;

			if (highlighted.locked) {
				RenderUtils.renderBlockHitbox(pos, 1f, 0f, 0f, a, false, 2f, false);
			} else {
				RenderUtils.renderBlockHitbox(pos, r, g, b, a, false, 2f, false);
			}
		}
	}

	private static void cleanupExpired(ClientWorld world) {
		long currentTime = world.getTime();

		HIGHLIGHTS.entrySet().removeIf(entry -> {
			BlockPos pos = entry.getKey();
			HighlightedBlock highlighted = entry.getValue();

			if (pos == null || highlighted == null) {
				return true;
			}

			BlockState state = world.getBlockState(pos);
			boolean blockMismatch = state == null || !state.isOf(highlighted.block);
			boolean expired = currentTime > highlighted.expireTick;
			return blockMismatch || expired;
		});
	}

	private static void handleChatMessage(Text message) {
		if (!ModConfig.enableSecretClicks || HIGHLIGHTS.isEmpty()) return;
		String text = message.getString();
		if (text == null || !text.contains("That chest is locked!")) return;

		HIGHLIGHTS.values().forEach(highlighted -> {
			if (highlighted != null && isChest(highlighted.block)) {
				highlighted.locked = true;
			}
		});
	}

	private static void highlightBlock(BlockPos pos, Block block, long worldTime) {
		BlockPos immutablePos = pos.toImmutable();
		HIGHLIGHTS.put(immutablePos, new HighlightedBlock(block, worldTime + HIGHLIGHT_TICKS));
	}

	private static boolean isValidSkull(ClientWorld world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (!(blockEntity instanceof SkullBlockEntity skull)) return false;

		ProfileComponent owner = skull.getOwner();
		if (owner == null) return false;

		// Some mappings expose the UUID via ProfileComponent#id(), others via the GameProfile.
		// To be compatible, prefer the GameProfile when present.
		UUID skullId = null;
		try {
			if (owner.gameProfile() != null) {
				skullId = owner.gameProfile().getId();
			}
		} catch (Throwable ignored) {
		}

		return skullId != null && VALID_SKULL_IDS.contains(skullId);
	}

	private static boolean isBlockEligible(Block block) {
		return block == Blocks.CHEST ||
			   block == Blocks.TRAPPED_CHEST ||
			   block == Blocks.LEVER ||
			   block instanceof SkullBlock;
	}

	private static boolean isChest(Block block) {
		return block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST;
	}

	private static final class HighlightedBlock {
		final Block block;
		final long expireTick;
		boolean locked;

		HighlightedBlock(Block block, long expireTick) {
			this.block = block;
			this.expireTick = expireTick;
		}
	}
}
