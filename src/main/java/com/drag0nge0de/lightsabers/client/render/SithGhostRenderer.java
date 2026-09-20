package com.drag0nge0de.lightsabers.client.render;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.ALClient;
import com.drag0nge0de.lightsabers.client.render.model.SithGhostModel;
import com.drag0nge0de.lightsabers.entity.SithGhostEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.util.Identifier;

public class SithGhostRenderer extends MobEntityRenderer<SithGhostEntity, SithGhostModel> {
   private static final Identifier TEXTURE = AL.id("textures/entity/sith_ghost.png");

   public SithGhostRenderer(EntityRendererFactory.Context context) {
      super(context, new SithGhostModel(context.getPart(ALClient.SITH_GHOST_LAYER)), 0.5F);

      this.addFeature(new HeldItemFeatureRenderer<>(this, context.getHeldItemRenderer()));
   }

   @Override
   public Identifier getTexture(SithGhostEntity entity) {
      return TEXTURE;
   }
}
