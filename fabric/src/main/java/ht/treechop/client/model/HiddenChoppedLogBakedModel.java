package ht.treechop.client.model;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.util.RandomSource;

import java.util.List;

public class HiddenChoppedLogBakedModel extends ChoppedLogBakedModel {

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        // Hidden model renders nothing
    }
}
