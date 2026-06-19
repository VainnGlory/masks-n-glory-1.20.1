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
    SoundEvent ITEM_BONE_HIT = createSoundEvent("item.bone.hit");
    SoundEvent ITEM_PRIDE_HIT = createSoundEvent("item.pride.hit");

    SoundEvent AMBIENT_FARLANDS_WIND = createSoundEvent("ambient.farlands.wind");
    SoundEvent AMBIENT_FARLANDS_HUM = createSoundEvent("ambient.farlands.hum");
    SoundEvent MUSIC_FARLANDS_A = createSoundEvent("music.farlands.a");
    SoundEvent MUSIC_FARLANDS_B = createSoundEvent("music.farlands.b");
    SoundEvent AMBIENT_VERDANT_BIRD1 = createSoundEvent("ambient.verdant_memory.bird1");
    SoundEvent AMBIENT_VERDANT_BIRD2 = createSoundEvent("ambient.verdant_memory.bird2");
    SoundEvent AMBIENT_VERDANT_CHEERS = createSoundEvent("ambient.verdant_memory.cheers");

    SoundEvent ENTITY_ARMOR_STAND_THING_WAIL = createSoundEvent("entity.armor_stand_thing.wail");

    SoundEvent ITEM_PANICKEDLY_CARVED_SWORD_WHISPER = createSoundEvent("item.panickedly_carved_sword.whisper");

    static void initialize() {
        SOUND_EVENTS.keySet().forEach(soundEvent -> Registry.register(Registries.SOUND_EVENT, SOUND_EVENTS.get(soundEvent), soundEvent));
    }

    private static SoundEvent createSoundEvent(String path) {
        SoundEvent soundEvent = SoundEvent.of(new Identifier(MasksNGlory.MOD_ID, path));
        SOUND_EVENTS.put(soundEvent, new Identifier(MasksNGlory.MOD_ID, path));
        return soundEvent;
    }
}
