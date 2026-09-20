package com.drag0nge0de.lightsabers.recipe;

import com.drag0nge0de.lightsabers.component.CrystalColor;
import com.drag0nge0de.lightsabers.component.CrystalComponent;
import com.drag0nge0de.lightsabers.component.LightsaberComponent;
import com.drag0nge0de.lightsabers.hilt.Hilt;
import com.drag0nge0de.lightsabers.item.HiltItem;
import com.drag0nge0de.lightsabers.item.LightsaberItem;
import com.drag0nge0de.lightsabers.registry.ALComponents;
import com.drag0nge0de.lightsabers.registry.ALItems;
import com.drag0nge0de.lightsabers.registry.ALRecipes;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class SaberAssemblyRecipe implements CraftingRecipe {
   private static final int WHITE = 16777215;
   private static final Map<Item, CrystalColor> DYE_COLORS = buildDyeColors();

   public boolean matches(CraftingRecipeInput input, World world) {
      return this.classify(input) != null;
   }

   public ItemStack craft(CraftingRecipeInput input, WrapperLookup lookup) {
      return this.assemble(input);
   }

   public ItemStack getResult(WrapperLookup lookup) {
      return new ItemStack(ALItems.LIGHTSABER);
   }

   private static SaberAssemblyRecipe.Counts count(CraftingRecipeInput input) {
      int emitter = 0;
      int switchModule = 0;
      int grip = 0;
      int pommel = 0;
      int circuitry = 0;
      int focusing = 0;
      int saber = 0;
      int crystal = 0;
      int dye = 0;
      int hilt = 0;
      int other = 0;
      ItemStack firstSaber = null;
      ItemStack secondSaber = null;
      ItemStack crystalStack = null;
      ItemStack dyeStack = null;
      ItemStack hiltStack = null;

      for (int i = 0; i < input.getSize(); i++) {
         ItemStack stack = input.getStackInSlot(i);
         if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item == ALItems.EMITTER) {
               emitter++;
            } else if (item == ALItems.SWITCH_MODULE) {
               switchModule++;
            } else if (item == ALItems.GRIP) {
               grip++;
            } else if (item == ALItems.POMMEL) {
               pommel++;
            } else if (item == ALItems.CIRCUITRY) {
               circuitry++;
            } else if (item == ALItems.FOCUSING_CRYSTAL) {
               focusing++;
            } else if (stack.getItem() instanceof LightsaberItem) {
               saber++;
               if (firstSaber == null) {
                  firstSaber = stack;
               } else if (secondSaber == null) {
                  secondSaber = stack;
               }
            } else if (item == ALItems.KYBER_CRYSTAL) {
               crystal++;
               crystalStack = stack;
            } else if (item == ALItems.HILTS.get(Hilt.DEFAULT) || stack.getItem() instanceof HiltItem) {
               hilt++;
               hiltStack = stack;
            } else if (DYE_COLORS.containsKey(item)) {
               dye++;
               dyeStack = stack;
            } else {
               other++;
            }
         }
      }

      return new SaberAssemblyRecipe.Counts(
         emitter, switchModule, grip, pommel, circuitry, focusing, saber, crystal, dye, hilt, other, firstSaber, secondSaber, crystalStack, dyeStack, hiltStack
      );
   }

   private static Map<Item, CrystalColor> buildDyeColors() {
      Map<Item, CrystalColor> map = new HashMap<>();
      map.put(Items.WHITE_DYE, CrystalColor.WHITE);
      map.put(Items.LIGHT_GRAY_DYE, CrystalColor.ARCTIC_BLUE);
      map.put(Items.GRAY_DYE, CrystalColor.DEEP_BLUE);
      map.put(Items.BLACK_DYE, CrystalColor.DEEP_BLUE);
      map.put(Items.BROWN_DYE, CrystalColor.AMBER);
      map.put(Items.RED_DYE, CrystalColor.RED);
      map.put(Items.ORANGE_DYE, CrystalColor.BLOOD_ORANGE);
      map.put(Items.YELLOW_DYE, CrystalColor.YELLOW);
      map.put(Items.LIME_DYE, CrystalColor.LIME_GREEN);
      map.put(Items.GREEN_DYE, CrystalColor.GREEN);
      map.put(Items.CYAN_DYE, CrystalColor.CYAN);
      map.put(Items.LIGHT_BLUE_DYE, CrystalColor.LIGHT_BLUE);
      map.put(Items.BLUE_DYE, CrystalColor.MEDIUM_BLUE);
      map.put(Items.PURPLE_DYE, CrystalColor.PURPLE);
      map.put(Items.MAGENTA_DYE, CrystalColor.MAGENTA);
      map.put(Items.PINK_DYE, CrystalColor.PINK);
      return map;
   }

   private SaberAssemblyRecipe.Kind classify(CraftingRecipeInput input) {
      SaberAssemblyRecipe.Counts c = count(input);
      if (c.other() > 0) {
         return null;
      } else if (c.emitter() == 1
         && c.switchModule() == 1
         && c.grip() == 1
         && c.pommel() == 1
         && c.circuitry() == 1
         && c.focusing() == 1
         && c.saber() == 0
         && c.crystal() == 0
         && c.dye() == 0
         && c.hilt() <= 1) {
         return SaberAssemblyRecipe.Kind.ASSEMBLE;
      } else if (c.saber() == 1
         && c.crystal() == 1
         && c.emitter() == 0
         && c.switchModule() == 0
         && c.grip() == 0
         && c.pommel() == 0
         && c.circuitry() == 0
         && c.focusing() == 0
         && c.dye() == 0
         && c.hilt() == 0) {
         return SaberAssemblyRecipe.Kind.RECOLOR;
      } else if (c.saber() == 2
         && c.crystal() == 0
         && c.dye() == 0
         && c.hilt() == 0
         && c.emitter() == 0
         && c.switchModule() == 0
         && c.grip() == 0
         && c.pommel() == 0
         && c.circuitry() == 0
         && c.focusing() == 0
         && this.isVerticalSaberPair(input)) {
         return SaberAssemblyRecipe.Kind.DOUBLE;
      } else {
         return c.crystal() == 1
               && c.dye() == 1
               && c.saber() == 0
               && c.emitter() == 0
               && c.switchModule() == 0
               && c.grip() == 0
               && c.pommel() == 0
               && c.circuitry() == 0
               && c.focusing() == 0
               && c.hilt() == 0
            ? SaberAssemblyRecipe.Kind.DYE_CRYSTAL
            : null;
      }
   }

   private boolean isVerticalSaberPair(CraftingRecipeInput input) {
      int filled = 0;

      for (int i = 0; i < input.getSize(); i++) {
         if (!input.getStackInSlot(i).isEmpty()) {
            filled++;
         }
      }

      if (filled != 2) {
         return false;
      }

      for (int i = 0; i < input.getSize(); i++) {
         ItemStack upper = input.getStackInSlot(i);

         if (upper.getItem() instanceof LightsaberItem && i + input.getWidth() < input.getSize()
               && input.getStackInSlot(i + input.getWidth()).getItem() instanceof LightsaberItem) {
            return true;
         }
      }

      return false;
   }

   private ItemStack assemble(CraftingRecipeInput input) {
      SaberAssemblyRecipe.Kind kind = this.classify(input);
      if (kind == null) {
         return ItemStack.EMPTY;
      } else {
         SaberAssemblyRecipe.Counts c = count(input);

         return switch (kind) {
            case ASSEMBLE -> {
               Hilt hilt = Hilt.DEFAULT;
               if (c.hiltStack() != null && c.hiltStack().getItem() instanceof HiltItem hiltItem) {
                  hilt = hiltItem.getHilt();
               }

               yield LightsaberItem.createSaber(hilt, 16777215, false);
            }
            case RECOLOR -> {
               CrystalComponent crystal = (CrystalComponent)c.crystalStack().getOrDefault(ALComponents.CRYSTAL, CrystalComponent.DEFAULT);
               LightsaberComponent saber = LightsaberItem.getComponent(c.firstSaber());
               ItemStack result = new ItemStack(c.firstSaber().getItem());
               result.set(ALComponents.LIGHTSABER, saber.withColor(crystal.color()));
               yield result;
            }
            case DOUBLE -> {
               ItemStack upper = c.firstSaber();
               ItemStack lower = c.secondSaber();
               LightsaberComponent upperData = LightsaberItem.getComponent(upper);
               LightsaberComponent lowerData = LightsaberItem.getComponent(lower);
               LightsaberComponent.Blade second = LightsaberComponent.Blade.of(lowerData);

               LightsaberComponent main = new LightsaberComponent(false, upperData.emitterHilt(),
                  upperData.switchHilt(), upperData.gripHilt(), upperData.pommelHilt(),
                  upperData.color(), true, upperData.focusing(), java.util.Optional.empty(),
                  upperData.special());
               ItemStack result = new ItemStack(ALItems.DOUBLE_LIGHTSABER);
               result.set(ALComponents.LIGHTSABER, main.withSecond(second));
               result.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(0));
               yield result;
            }
            case DYE_CRYSTAL -> {
               CrystalColor color = DYE_COLORS.get(c.dyeStack().getItem());
               yield com.drag0nge0de.lightsabers.item.KyberCrystalItem.create(color != null ? color : CrystalColor.WHITE);
            }
         };
      }
   }

   public boolean fits(int width, int height) {
      return width * height >= 6;
   }

   public CraftingRecipeCategory getCategory() {
      return CraftingRecipeCategory.EQUIPMENT;
   }

   public RecipeSerializer<?> getSerializer() {
      return ALRecipes.SABER_ASSEMBLY;
   }

   private static record Counts(
      int emitter,
      int switchModule,
      int grip,
      int pommel,
      int circuitry,
      int focusing,
      int saber,
      int crystal,
      int dye,
      int hilt,
      int other,
      ItemStack firstSaber,
      ItemStack secondSaber,
      ItemStack crystalStack,
      ItemStack dyeStack,
      ItemStack hiltStack
   ) {
   }

   private static enum Kind {
      ASSEMBLE,
      RECOLOR,
      DOUBLE,
      DYE_CRYSTAL;
   }
}
