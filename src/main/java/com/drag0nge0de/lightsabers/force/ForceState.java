package com.drag0nge0de.lightsabers.force;

import com.drag0nge0de.lightsabers.AL;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class ForceState {
   public static final AttachmentType<Integer> XP = AttachmentRegistry.createPersistent(AL.id("force_xp"), Codec.INT);
   public static final AttachmentType<Float> ENERGY = AttachmentRegistry.createPersistent(AL.id("force_energy"), Codec.FLOAT);
   public static final AttachmentType<List<String>> UNLOCKED = AttachmentRegistry.createPersistent(AL.id("force_unlocked"), Codec.STRING.listOf());
   public static final AttachmentType<Boolean> INITIALIZED = AttachmentRegistry.createPersistent(AL.id("force_init"), Codec.BOOL);
   public static final AttachmentType<Integer> SELECTED_SLOT = AttachmentRegistry.createPersistent(AL.id("force_selected_slot"), Codec.INT);
   public static final AttachmentType<List<String>> SELECTED_POWERS = AttachmentRegistry.createPersistent(AL.id("force_selected_powers"), Codec.STRING.listOf());

   public static final AttachmentType<Map<String, Integer>> XP_INVESTED =
      AttachmentRegistry.createPersistent(AL.id("force_xp_invested"), Codec.unboundedMap(Codec.STRING, Codec.INT));

   public static final AttachmentType<String> DRAINING = AttachmentRegistry.create(AL.id("force_draining"));
   public static final AttachmentType<Integer> DRAINING_TICK = AttachmentRegistry.create(AL.id("force_drain_tick"));

   private ForceState() {
   }

   public static void touch() {
   }
}
