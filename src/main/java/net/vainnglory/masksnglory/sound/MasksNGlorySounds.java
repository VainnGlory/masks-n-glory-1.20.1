package net.vainnglory.masksnglory.sound;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.vainnglory.masksnglory.MasksNGlory;

import java.util.LinkedHashMap;
import java.util.Map;

public interface MasksNGlorySounds {
    Map<SoundEvent, Identifier> SOUND_EVENTS = new LinkedHashMap<>();

    SoundEvent ITEM_PAN_HIT = createSoundEvent("item.pan.hit");
    SoundEvent ITEM_RUSTED_HIT = createSoundEvent("item.rusted.hit");
    SoundEvent ITEM_PALE_HIT = createSoundEvent("item.pale.hit");

    static void initialize() {
        SOUND_EVENTS.keySet().forEach(soundEvent -> Registry.register(Registries.SOUND_EVENT, SOUND_EVENTS.get(soundEvent), soundEvent));
    }

    private static SoundEvent createSoundEvent(String path) {
        SoundEvent soundEvent = SoundEvent.of(new Identifier(MasksNGlory.MOD_ID, path));
        SOUND_EVENTS.put(soundEvent, new Identifier(MasksNGlory.MOD_ID, path));
        return soundEvent;
    }


}

