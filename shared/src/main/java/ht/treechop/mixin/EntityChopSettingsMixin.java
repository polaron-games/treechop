package ht.treechop.mixin;

import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.ChoppingEntity;
import ht.treechop.common.settings.SyncedChopData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityChopSettingsMixin implements ChoppingEntity {
    private SyncedChopData chopSettings;
    private final String KEY = "treechop:chopSettings";

    @Override
    public SyncedChopData getChopData() {
        return chopSettings;
    }

    @Override
    public SyncedChopData setChopData(SyncedChopData chopSettings) {
        this.chopSettings = chopSettings;
        return chopSettings;
    }

    @Inject(method = "saveWithoutId", at = @At("HEAD"))
    public void injectDataSaving(ValueOutput tag, CallbackInfo info) {
        if (chopSettings != null) {
            tag.store(KEY, CompoundTag.CODEC, chopSettings.makeSaveData());
        }
    }

    @Inject(method = "load", at = @At("HEAD"))
    public void injectDataLoading(ValueInput tag, CallbackInfo info) {
        CompoundTag data = tag.read(KEY, CompoundTag.CODEC).orElse(new CompoundTag());
        chopSettings = (new SyncedChopData(new ChopSettings())).readSaveData(data);
    }
}
