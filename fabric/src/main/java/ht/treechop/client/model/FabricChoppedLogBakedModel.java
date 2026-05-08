package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class FabricChoppedLogBakedModel extends ChoppedLogBakedModel implements FabricBlockStateModel {

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<Direction> cullTest) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
            Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
            getQuads(strippedState, entity.getShape(), entity.getRadius(), random, strippedNeighbors)
                    .forEach(quad -> emitBakedQuad(emitter, quad));
        } else {
            // Fallback for breaking animation: Sodium passes a fake level whose getBlockEntity() always returns null.
            // Fetch the real block entity from the client level so we render cracks on the actual stub shape.
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
                BlockState strippedState = ChopUtil.getStrippedState(level, pos, entity.getOriginalState());
                Map<Direction, BlockState> strippedNeighbors = getStrippedNeighbors(level, pos, entity);
                getQuads(strippedState, entity.getShape(), entity.getRadius(), random, strippedNeighbors)
                        .forEach(quad -> emitBakedQuad(emitter, quad));
            } else {
                // Last resort: full-size stripped oak log
                List<BlockModelPart> parts = new ArrayList<>();
                collectParts(random, parts);
                for (BlockModelPart part : parts) {
                    for (Direction dir : Direction.values()) {
                        for (BakedQuad quad : part.getQuads(dir)) {
                            emitBakedQuad(emitter, quad);
                        }
                    }
                    for (BakedQuad quad : part.getQuads(null)) {
                        emitBakedQuad(emitter, quad);
                    }
                }
            }
        }
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        // Used by block breaking animation and other non-world rendering contexts.
        // Fall back to stripped oak log since we have no block entity here.
        getBlockModel(net.minecraft.world.level.block.Blocks.STRIPPED_OAK_LOG.defaultBlockState())
                .collectParts(random, parts);
    }

    private void emitBakedQuad(QuadEmitter emitter, BakedQuad quad) {
        emitter.fromBakedQuad(quad);
        emitter.emit();
    }
}
