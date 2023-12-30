package net.aruneko.chainminer

import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@Mod(ChainMiner.MOD_ID)
@EventBusSubscriber(modid = ChainMiner.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
class ChainMiner {
    companion object {
        const val MOD_ID = "chain_miner"
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

        // ブロックがつるはしで採掘可能でなければ中断
        if (!blockState.`is`(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return
        }

        // 適正ツールでなければ中断
        if (!tool.isCorrectToolForDrops(blockState)) {
            return
        }

        // ブロックが鉱石タグを持っていなければ中断
        val tagPaths = blockState.tags.map { it.location.path }.toList()
        if (!tagPaths.contains("ores")) {
            return
        }

        val blockPosition = event.pos
        val level = event.level
        val vein = blockPosition.findVein(level)
        val world = event.player.level()

        // item in hand で block を破壊する
        vein.forEach {
            Block.dropResources(event.state, world, blockPosition, world.getBlockEntity(it), null, tool)
            world.setBlock(it, Blocks.AIR.defaultBlockState(), 3)
        }

        // つるはしの耐久値を減らす
        if (!player.isCreative) {
            tool.damageValue += vein.size
        }
    }
}
