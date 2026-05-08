package ht.treechop.client.model;

import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.properties.ChoppedLogShape;
import ht.tuber.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ChoppedLogBakedModel implements BlockStateModel {
    private static TextureAtlasSprite defaultSprite;

    public static void setDefaultSprite(TextureAtlasSprite defaultSprite) {
        ChoppedLogBakedModel.defaultSprite = defaultSprite;
    }

    @Override
    public @NotNull TextureAtlasSprite particleIcon() {
        return getDefaultSprite();
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        // Subclasses handle this
    }

    private static BlockState getStrippedNeighbor(BlockAndTintGetter level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        return ChopUtil.getStrippedState(level, pos, level.getBlockState(neighborPos));
    }

    protected static Map<Direction, BlockState> getStrippedNeighbors(BlockAndTintGetter level, BlockPos pos, ChoppedLogBlock.MyEntity entity) {
        if (entity.getOriginalState().isSolidRender()) {
            return entity.streamSolidSides(level, pos).collect(Collectors.toMap(
                    side -> side,
                    side -> getStrippedNeighbor(level, pos, side)
            ));
        } else {
            return Collections.emptyMap();
        }
    }

    protected List<BakedQuad> getBlockQuads(BlockState blockState, Direction side, RandomSource rand) {
        BlockStateModel model = getBlockModel(blockState);
        List<BlockModelPart> parts = new ArrayList<>();
        model.collectParts(rand, parts);
        return parts.stream()
                .flatMap(part -> part.getQuads(side).stream())
                .collect(Collectors.toList());
    }

    @NotNull
    public static BlockStateModel getBlockModel(BlockState blockState) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(blockState);
    }

    protected TextureAtlasSprite getDefaultSprite() {
        if (defaultSprite == null) {
            defaultSprite = getBlockModel(net.minecraft.world.level.block.Blocks.STRIPPED_OAK_LOG.defaultBlockState()).particleIcon();
        }
        return defaultSprite;
    }

    protected Stream<BakedQuad> getQuads(BlockState strippedState, ChoppedLogShape shape, int radius, RandomSource random, Map<Direction, BlockState> strippedNeighbors) {
        final Direction[] allDirections = { Direction.UP, Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, null };
        AABB box = shape.getBoundingBox(radius);
        Vector3 mins = new Vector3(box.minX, box.minY, box.minZ);
        Vector3 maxes = new Vector3(box.maxX, box.maxY, box.maxZ);

        return Stream.concat(
                Arrays.stream(allDirections)
                .flatMap(
                        side -> getBlockQuads(strippedState, side, random).stream()
                                .map(quad -> ModelUtil.trimQuad(quad, mins, maxes))
                ),
                strippedNeighbors.entrySet().stream().flatMap(
                        entry -> {
                            Direction side = entry.getKey();
                            BlockState strippedNeighbor = entry.getValue();

                            Vec3i normal = side.getUnitVec3i().multiply(16);
                            Vector3 transform = new Vector3(normal.getX(), normal.getY(), normal.getZ());

                            List<BlockModelPart> parts = new ArrayList<>();
                            getBlockModel(strippedNeighbor).collectParts(random, parts);
                            return parts.stream()
                                    .flatMap(part -> part.getQuads(side.getOpposite()).stream())
                                    .map(quad -> ModelUtil.translateQuad(quad, transform));
                        }
                )
        ).filter(Objects::nonNull);
    }
}
