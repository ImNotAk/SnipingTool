package com.example.signsearcher;

import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class SignSearcherAddon extends LabyAddon<SignSearcherConfiguration> {

  private static SignSearcherAddon instance;

  @Override
  protected void enable() {
    instance = this;
    this.registerSettingCategory();
    this.logger().info("FV Celle Sniper 1.0 enabled");
  }

  @Override
  protected Class<? extends SignSearcherConfiguration> configurationClass() {
    return SignSearcherConfiguration.class;
  }

  public static SignSearcherAddon instance() {
    return instance;
  }
}