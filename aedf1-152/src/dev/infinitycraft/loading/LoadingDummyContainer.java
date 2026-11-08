package dev.infinitycraft.loading;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import java.util.Arrays;

public final class LoadingDummyContainer extends DummyModContainer {
    public LoadingDummyContainer() {
        super(createMetadata());
    }

    private static ModMetadata createMetadata() {
        ModMetadata meta = new ModMetadata();
        meta.modId = "infinitycraftloading";
        meta.name = "InfinityCraft Loading Screen";
        meta.version = "1.5.2-r2";
        meta.authorList = Arrays.asList("InfinityCraft");
        meta.description = "Moss Island fullscreen splash and live progress bar for AEDF1 1.5.2.";
        return meta;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
