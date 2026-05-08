package ht.treechop.client.model;

import ht.treechop.TreeChop;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelLoadingPlugin implements ModelLoadingPlugin {
    private final Supplier<ChoppedLogBakedModel> model;

    public ChoppedLogModelLoadingPlugin(Supplier<ChoppedLogBakedModel> model) {
        this.model = model;
    }

    @Override
    public void initialize(Context pluginContext) {
        pluginContext.modifyBlockModelAfterBake().register((original, context) -> {
            if (context.state().getBlock() == TreeChop.platform.getChoppedLogBlock()) {
                return model.get();
            }
            return original;
        });
    }
}
