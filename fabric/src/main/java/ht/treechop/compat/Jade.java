package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;

@WailaPlugin
public class Jade implements IWailaPlugin, IBlockComponentProvider {

    public static final Identifier SHOW_TREE_BLOCKS = Identifier.fromNamespaceAndPath(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final Identifier SHOW_NUM_CHOPS_REMAINING = Identifier.fromNamespaceAndPath(TreeChop.MOD_ID, "show_num_chops_remaining");
    private static final Identifier UID = TreeChop.resource("plugin");

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        registrar.registerBlockComponent(this, Block.class);
        registrar.registerBlockIcon(this, ChoppedLogBlock.class);
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        changeBlockName(tooltip, accessor);

        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        boolean showNumBlocks = config.get(SHOW_TREE_BLOCKS);
        boolean showChopsRemaining = config.get(SHOW_NUM_CHOPS_REMAINING);

        if (WailaUtil.playerWantsTreeInfo(level, pos, showNumBlocks, showChopsRemaining)) {
            WailaUtil.addTreeInfo(
                    level,
                    pos,
                    showNumBlocks,
                    showChopsRemaining,
                    tooltip::add,
                    stack -> tooltip.add(stack.getHoverName().copy().append(" x" + stack.getCount()))
            );
        }
    }

    private static void changeBlockName(ITooltip tooltip, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ChoppedLogBlock.MyEntity choppedEntity) {
            tooltip.clear();
            Component newName = WailaUtil.getPrefixedBlockName(choppedEntity);
            tooltip.add(IThemeHelper.get().title(newName));
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
