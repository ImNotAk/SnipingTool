package com.example.signsearcher.v1_8_9.mixins;

import com.example.signsearcher.SignSearcherAddon;
import com.example.signsearcher.v1_8_9.SignSearchRenderState;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

  @Unique
  private int signsearcher$ticks = 0;

  @Unique
  private boolean signsearcher$toggleKeyDown = false;

  @Inject(method = "runTick()V", at = @At("TAIL"))
  private void signsearcher$runTick(CallbackInfo ci) {
    Minecraft mc = Minecraft.getMinecraft();
    SignSearcherAddon addon = SignSearcherAddon.instance();

    if (addon == null || addon.configuration() == null) {
      return;
    }

    signsearcher$handleToggleKey(addon);

    if (mc.theWorld == null || mc.thePlayer == null) {
      SignSearchRenderState.MATCH_SIGNS.clear();
      return;
    }

    if (!addon.configuration().enabled().get()) {
      SignSearchRenderState.MATCH_SIGNS.clear();
      return;
    }

    this.signsearcher$ticks++;
    if (this.signsearcher$ticks < 10) {
      return;
    }
    this.signsearcher$ticks = 0;

    List<String> terms = new ArrayList<>();
    addTerm(terms, addon.configuration().query1().get());
    addTerm(terms, addon.configuration().query2().get());
    addTerm(terms, addon.configuration().query3().get());
    addTerm(terms, addon.configuration().query4().get());

    if (terms.isEmpty()) {
      SignSearchRenderState.MATCH_SIGNS.clear();
      return;
    }

    SignSearchRenderState.MATCH_SIGNS.clear();

    for (TileEntity tileEntity : mc.theWorld.loadedTileEntityList) {
      if (!(tileEntity instanceof TileEntitySign)) {
        continue;
      }

      TileEntitySign sign = (TileEntitySign) tileEntity;

      StringBuilder text = new StringBuilder();
      for (int i = 0; i < sign.signText.length; i++) {
        if (sign.signText[i] == null) {
          continue;
        }

        String line = sign.signText[i].getUnformattedText();
        if (line == null || line.isEmpty()) {
          continue;
        }

        if (text.length() > 0) {
          text.append(' ');
        }
        text.append(line);
      }

      String signText = text.toString().trim().toLowerCase();
      if (signText.isEmpty()) {
        continue;
      }

      boolean match = false;
      for (String term : terms) {
        if (signText.contains(term)) {
          match = true;
          break;
        }
      }

      if (match) {
        SignSearchRenderState.MATCH_SIGNS.add(sign);
      }
    }
  }

  @Unique
  private void signsearcher$handleToggleKey(SignSearcherAddon addon) {
    String keyName = addon.configuration().toggleKey().get();
    if (keyName == null || keyName.trim().isEmpty()) {
      return;
    }

    int keyCode = Keyboard.getKeyIndex(keyName.trim().toUpperCase());
    if (keyCode == Keyboard.KEY_NONE) {
      return;
    }

    boolean down = Keyboard.isKeyDown(keyCode);

    if (down && !this.signsearcher$toggleKeyDown) {
      this.signsearcher$toggleKeyDown = true;
      addon.configuration().enabled().set(!addon.configuration().enabled().get());
    } else if (!down) {
      this.signsearcher$toggleKeyDown = false;
    }
  }

  @Unique
  private static void addTerm(List<String> terms, String value) {
    if (value == null) {
      return;
    }

    String term = value.trim().toLowerCase();
    if (!term.isEmpty()) {
      terms.add(term);
    }
  }
}