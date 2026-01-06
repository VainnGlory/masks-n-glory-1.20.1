package net.vainnglory.masksnglory.util;

import net.minecraft.util.Formatting;

import net.minecraft.text.TextColor;

public enum ModRarities {

    PALE(TextColor.fromRgb(0xFDDC97)),
    BONE(TextColor.fromRgb(0xB2A583)),
    GOLDEN(TextColor.fromRgb(0xE8AB55)),
    WOODEN(TextColor.fromRgb(0x845959)),
    SPECIAL(TextColor.fromRgb(0x4089DF));

    public final TextColor color;

    ModRarities(TextColor color) {
        this.color = color;
    }
}
