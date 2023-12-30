package net.aruneko.chainwoodcutter

import net.minecraft.world.level.block.Block
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod(ChainWoodCutter.MOD_ID)
@Mod.EventBusSubscriber(modid = ChainWoodCutter.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
class ChainWoodCutter {
    companion object {
        const val MOD_ID = "chain_wood_cutter"
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onBlockBreaking(event: BlockEvent.BreakEvent) {
        val player = event.player
        // スニークしていなければ中断
        if (!player.isShiftKeyDown) {
            return
        }

        val tool = player.mainHandItem
        val blockState = event.state

        // ブロックが斧で採掘可能でなければ中断
        if (!blockState.`is`(net.minecraft.tags.BlockTags.MINEABLE_WITH_AXE)) {
            return
        }

        // 適正ツールでなければ中断
        if (!tool.isCorrectToolForDrops(blockState)) {
            return
        }

        // ブロックが原木タグを持っていなければ中断
        val tagPaths = blockState.tags.map { it.location.path }.toList()
        if (!tagPaths.contains("logs")) {
            return
        }

        val blockPosition = event.pos
        val level = event.level
        val logAndLeaves = blockPosition.findLogAndLeaves(level)

        // 葉が付いていなければ中断
        if (logAndLeaves.none { it.isLeaf(level) }) {
            return
        }

        // item in hand で block を破壊する
        val logs = logAndLeaves.filterNot { it.isLeaf(level) }
        val world = event.player.level()
        logs.forEach {
            Block.dropResources(event.state, world, blockPosition, world.getBlockEntity(it), null, tool)
            world.setBlock(it, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3)
        }

        // 斧の耐久値を減らす
        if (!player.isCreative) {
            tool.damageValue += logs.size
        }
    }
}