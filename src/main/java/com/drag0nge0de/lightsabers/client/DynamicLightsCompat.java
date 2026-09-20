package com.drag0nge0de.lightsabers.client;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.config.ALConfig;
import com.drag0nge0de.lightsabers.entity.ThrownLightsaberEntity;
import com.drag0nge0de.lightsabers.registry.ALEntities;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class DynamicLightsCompat {
   private static final String MODID = "lambdynamiclights";
   private static boolean initialized;

   private DynamicLightsCompat() {
   }

   public static void init() {
      if (initialized) {
         return;
      }

      initialized = true;
      if (!ALConfig.get().dynamicLightsEnabled) {
         return;
      }

      if (!FabricLoader.getInstance().isModLoaded(MODID)) {
         AL.LOGGER.info("Dynamic lights mod not installed, saber illumination will not activate");
         return;
      }

      try {
         Class<?> handlers = Class.forName("dev.lambdaurora.lambdynlights.api.DynamicLightHandlers");
         Class<?> handlerInterface = Class.forName("dev.lambdaurora.lambdynlights.api.DynamicLightHandler");
         Method register = handlers.getMethod("registerDynamicLightHandler", EntityType.class, handlerInterface);
         register.invoke(null, EntityType.PLAYER, newHandler(handlerInterface));
         register.invoke(null, ALEntities.THROWN_LIGHTSABER, newHandler(handlerInterface));
         AL.LOGGER.info("Registered saber dynamic light handlers with LambDynamicLights");
      } catch (ClassNotFoundException e) {
         AL.LOGGER.info("LambDynamicLights API not found, skipping dynamic light registration");
      } catch (Throwable t) {
         AL.LOGGER.warn("Dynamic light registration failed", t);
      }
   }

   private static Object newHandler(Class<?> handlerInterface) {
      return Proxy.newProxyInstance(DynamicLightsCompat.class.getClassLoader(), new Class<?>[]{handlerInterface},
         new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) {
               String name = method.getName();
               if (name.equals("getDynamicLightLevel")) {
                  return saberLight(args != null && args.length > 0 && args[0] instanceof Entity entity ? entity : null);
               }

               if (name.equals("isDynamicLightSource")) {
                  return saberLight(args != null && args.length > 0 && args[0] instanceof Entity entity ? entity : null) > 0;
               }

               if (name.equals("toString")) {
                  return "ALDynamicLightHandler";
               }

               if (name.equals("hashCode")) {
                  return 42;
               }

               if (name.equals("equals")) {
                  return proxy == args[0];
               }

               return null;
            }
         });
   }

   public static int saberLight(Entity entity) {
      if (!ALConfig.get().dynamicLightsEnabled) {
         return 0;
      }

      if (entity == null) {
         return 0;
      }

      if (entity instanceof ThrownLightsaberEntity thrown) {
         ItemStack stack = thrown.getSaberStack();
         return isActive(stack) ? 15 : 0;
      }

      if (entity instanceof LivingEntity living) {
         if (isActive(living.getEquippedStack(EquipmentSlot.MAINHAND))
            || isActive(living.getEquippedStack(EquipmentSlot.OFFHAND))) {
            return 15;
         }
      }

      return 0;
   }

   private static boolean isActive(ItemStack stack) {
      if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof com.drag0nge0de.lightsabers.item.LightsaberItem)) {
         return false;
      }

      LightsaberComponent component = stack.get(ALComponents.LIGHTSABER);
      return component != null && component.active();
   }
}
