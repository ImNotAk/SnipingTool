package com.example.signsearcher;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.SliderWidget.SliderSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.TextFieldWidget.TextFieldSetting;
import net.labymod.api.configuration.loader.annotation.ConfigName;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.configuration.settings.annotation.SettingSection;

@ConfigName("settings")
public class SignSearcherConfiguration extends AddonConfig {

  @SettingSection("general")
  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @SwitchSetting
  private final ConfigProperty<Boolean> showText = new ConfigProperty<>(true);

  @TextFieldSetting
  private final ConfigProperty<String> toggleKey = new ConfigProperty<>("G");

  @SliderSetting(min = 10, max = 60)
  private final ConfigProperty<Integer> textScale = new ConfigProperty<>(25);

  @SettingSection("sign1")
  @TextFieldSetting
  private final ConfigProperty<String> query1 = new ConfigProperty<>("shop");

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> red1 = new ConfigProperty<>(0);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> green1 = new ConfigProperty<>(255);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> blue1 = new ConfigProperty<>(0);

  @SettingSection("sign2")
  @TextFieldSetting
  private final ConfigProperty<String> query2 = new ConfigProperty<>("");

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> red2 = new ConfigProperty<>(255);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> green2 = new ConfigProperty<>(0);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> blue2 = new ConfigProperty<>(0);

  @SettingSection("sign3")
  @TextFieldSetting
  private final ConfigProperty<String> query3 = new ConfigProperty<>("");

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> red3 = new ConfigProperty<>(0);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> green3 = new ConfigProperty<>(0);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> blue3 = new ConfigProperty<>(255);

  @SettingSection("sign4")
  @TextFieldSetting
  private final ConfigProperty<String> query4 = new ConfigProperty<>("");

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> red4 = new ConfigProperty<>(255);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> green4 = new ConfigProperty<>(255);

  @SliderSetting(min = 0, max = 255)
  private final ConfigProperty<Integer> blue4 = new ConfigProperty<>(0);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  public ConfigProperty<Boolean> showText() {
    return this.showText;
  }

  public ConfigProperty<String> toggleKey() {
    return this.toggleKey;
  }

  public ConfigProperty<Integer> textScale() {
    return this.textScale;
  }

  public ConfigProperty<String> query1() {
    return this.query1;
  }

  public ConfigProperty<Integer> red1() {
    return this.red1;
  }

  public ConfigProperty<Integer> green1() {
    return this.green1;
  }

  public ConfigProperty<Integer> blue1() {
    return this.blue1;
  }

  public ConfigProperty<String> query2() {
    return this.query2;
  }

  public ConfigProperty<Integer> red2() {
    return this.red2;
  }

  public ConfigProperty<Integer> green2() {
    return this.green2;
  }

  public ConfigProperty<Integer> blue2() {
    return this.blue2;
  }

  public ConfigProperty<String> query3() {
    return this.query3;
  }

  public ConfigProperty<Integer> red3() {
    return this.red3;
  }

  public ConfigProperty<Integer> green3() {
    return this.green3;
  }

  public ConfigProperty<Integer> blue3() {
    return this.blue3;
  }

  public ConfigProperty<String> query4() {
    return this.query4;
  }

  public ConfigProperty<Integer> red4() {
    return this.red4;
  }

  public ConfigProperty<Integer> green4() {
    return this.green4;
  }

  public ConfigProperty<Integer> blue4() {
    return this.blue4;
  }
}