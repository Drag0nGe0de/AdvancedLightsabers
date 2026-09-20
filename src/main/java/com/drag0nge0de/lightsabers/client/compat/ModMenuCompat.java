package com.drag0nge0de.lightsabers.client.compat;

import com.drag0nge0de.lightsabers.client.screen.LightsaberConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {

   @Override
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return LightsaberConfigScreen::new;
   }
}
