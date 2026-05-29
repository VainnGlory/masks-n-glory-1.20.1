package net.vainnglory.masksnglory.cosmetic;

import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SheathItem extends Item implements GeoItem {

    public static final SheathItem INSTANCE = new SheathItem(new Settings());

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private SheathItem(Settings settings) {
        super(settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return () -> null;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {}
}
