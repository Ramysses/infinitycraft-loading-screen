package dev.infinitycraft.loading;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.5.2")
@IFMLLoadingPlugin.TransformerExclusions({"dev.infinitycraft.loading"})
public final class LoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getLibraryRequestClass() {
        return new String[0];
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"dev.infinitycraft.loading.LoadingTransformer"};
    }

    @Override
    public String getModContainerClass() {
        return "dev.infinitycraft.loading.LoadingDummyContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }
}
