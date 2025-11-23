package net.vainnglory.masksnglory.item.custom;

import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;

public class PaleTemplateItem extends SmithingTemplateItem {


    private static final Formatting TITLE_FORMATTING;
    private static final Formatting DESCRIPTION_FORMATTING;
    private static final String TRANSLATION_KEY;
    private static final Identifier EMPTY_ARMOR_SLOT_HELMET_TEXTURE;
    private static final Identifier EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE;
    private static final Identifier EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE;
    private static final Identifier EMPTY_ARMOR_SLOT_BOOTS_TEXTURE;
    private static final Identifier EMPTY_SLOT_HOE_TEXTURE;
    private static final Identifier EMPTY_SLOT_AXE_TEXTURE;
    private static final Identifier EMPTY_SLOT_SWORD_TEXTURE;
    private static final Identifier EMPTY_SLOT_SHOVEL_TEXTURE;
    private static final Identifier EMPTY_SLOT_PICKAXE_TEXTURE;
    private static final Identifier EMPTY_SLOT_INGOT_TEXTURE;
    private final Text baseSlotDescriptionText;
    private final Text additionsSlotDescriptionText;
    private final List<Identifier> emptyBaseSlotTextures;
    private final List<Identifier> emptyAdditionsSlotTextures;

    private static final Text PALE_UPGRADE_TEXT;
    private static final Text PALE_UPGRADE_APPLIES_TO_TEXT;
    private static final Text PALE_UPGRADE_INGREDIENTS_TEXT;
    private static final Text PALE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT;
    private static final Text PALE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT;

    public PaleTemplateItem(Text appliesToText, Text ingredientsText, Text titleText, Text baseSlotDescriptionText, Text additionsSlotDescriptionText, List<Identifier> emptyBaseSlotTextures, List<Identifier> emptyAdditionsSlotTextures) {
        super(appliesToText, ingredientsText, titleText, baseSlotDescriptionText, additionsSlotDescriptionText, emptyBaseSlotTextures, emptyAdditionsSlotTextures);
        this.baseSlotDescriptionText = baseSlotDescriptionText;
        this.additionsSlotDescriptionText = additionsSlotDescriptionText;
        this.emptyBaseSlotTextures = emptyBaseSlotTextures;
        this.emptyAdditionsSlotTextures = emptyAdditionsSlotTextures;
    }

    public static PaleTemplateItem createPaleUpgrade() {
        return new PaleTemplateItem(PALE_UPGRADE_APPLIES_TO_TEXT, PALE_UPGRADE_INGREDIENTS_TEXT, PALE_UPGRADE_TEXT, PALE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT, PALE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT, getPaleUpgradeEmptyBaseSlotTextures(), getPaleUpgradeEmptyAdditionsSlotTextures());
    }

    private static List<Identifier> getPaleUpgradeEmptyBaseSlotTextures() {
        return List.of(EMPTY_ARMOR_SLOT_HELMET_TEXTURE, EMPTY_SLOT_SWORD_TEXTURE, EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE, EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE, EMPTY_SLOT_AXE_TEXTURE, EMPTY_ARMOR_SLOT_BOOTS_TEXTURE, EMPTY_SLOT_SHOVEL_TEXTURE, EMPTY_SLOT_HOE_TEXTURE, EMPTY_SLOT_PICKAXE_TEXTURE);
    }

    private static List<Identifier> getPaleUpgradeEmptyAdditionsSlotTextures() {
        return List.of(EMPTY_SLOT_INGOT_TEXTURE);
    }

    public Text getBaseSlotDescription() {
        return this.baseSlotDescriptionText;
    }

    public Text getAdditionsSlotDescription() {
        return this.additionsSlotDescriptionText;
    }

    public List<Identifier> getEmptyBaseSlotTextures() {
        return this.emptyBaseSlotTextures;
    }

    public List<Identifier> getEmptyAdditionsSlotTextures() {
        return this.emptyAdditionsSlotTextures;
    }

    public String getTranslationKey() {
        return TRANSLATION_KEY;
    }

    static {
        TITLE_FORMATTING = Formatting.GRAY;
        DESCRIPTION_FORMATTING = Formatting.GOLD;
        TRANSLATION_KEY = Util.createTranslationKey("item", new Identifier("smithing_template"));
        EMPTY_ARMOR_SLOT_HELMET_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
        EMPTY_ARMOR_SLOT_CHESTPLATE_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
        EMPTY_ARMOR_SLOT_LEGGINGS_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
        EMPTY_ARMOR_SLOT_BOOTS_TEXTURE = new Identifier("item/empty_armor_slot_boots");
        EMPTY_SLOT_HOE_TEXTURE = new Identifier("item/empty_slot_hoe");
        EMPTY_SLOT_AXE_TEXTURE = new Identifier("item/empty_slot_axe");
        EMPTY_SLOT_SWORD_TEXTURE = new Identifier("item/empty_slot_sword");
        EMPTY_SLOT_SHOVEL_TEXTURE = new Identifier("item/empty_slot_shovel");
        EMPTY_SLOT_PICKAXE_TEXTURE = new Identifier("item/empty_slot_pickaxe");
        EMPTY_SLOT_INGOT_TEXTURE = new Identifier("item/empty_slot_ingot");

        PALE_UPGRADE_TEXT = Text.translatable(Util.createTranslationKey("upgrade", new Identifier("pale_upgrade"))).formatted(TITLE_FORMATTING);
        PALE_UPGRADE_APPLIES_TO_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.pale_upgrade.applies_to"))).formatted(DESCRIPTION_FORMATTING);
        PALE_UPGRADE_INGREDIENTS_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.pale_upgrade.ingredients"))).formatted(DESCRIPTION_FORMATTING);
        PALE_UPGRADE_BASE_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.pale_upgrade.base_slot_description")));
        PALE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION_TEXT = Text.translatable(Util.createTranslationKey("item", new Identifier("smithing_template.pale_upgrade.additions_slot_description")));

    }
}


