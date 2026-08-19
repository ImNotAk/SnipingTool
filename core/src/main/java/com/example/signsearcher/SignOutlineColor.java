package com.example.signsearcher;

public enum SignOutlineColor {
  WHITE("FFFFFFFF"),
  GREEN("FF00FF00"),
  RED("FFFF0000"),
  CYAN("FF00FFFF"),
  YELLOW("FFFFFF00"),
  ORANGE("FFFFA500"),
  PINK("FFFF69B4"),
  BLUE("FF4DA6FF"),
  PURPLE("FFB266FF");

  private final String argb;

  SignOutlineColor(String argb) {
    this.argb = argb;
  }

  public String argb() {
    return this.argb;
  }
}