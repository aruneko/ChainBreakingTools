package net.aruneko.chaindigger

import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager

@Mod(ChainDigger.MOD_ID)
@Mod.EventBusSubscriber(modid = ChainDigger.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
class ChainDigger {
    companion object {
        const val MOD_ID = "chain_digger"
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    private val logger = LogManager.getLogger()

    @SubscribeEvent
    fun onBlockBreaking(event: BlockEvent.BreakEvent) {
        val player = event.player
        // スニークしていなければ中断
        if (!player.isShiftKeyDown) {
            return
        }

        val tool = player.mainHandItem
        val blockState = event.state

        // ブロックがシャベルで採掘可能でなければ中断
        if (!blockState.`is`(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return
        }

        // 適正ツールでなければ中断
        if (!tool.isCorrectToolForDrops(blockState)) {
            return
        }

        // ブロックが採掘対象でなければ中断
        if (blockState.block != Blocks.CLAY) {
            return
        }

        val blockPosition = event.pos
        val level = event.level
        val range = 5
        val vein = blockPosition.findVein(level, range)
        val world = event.player.level()

        // item in hand で block を破壊する
        vein.forEach {
            Block.dropResources(event.state, world, blockPosition, world.getBlockEntity(it), null, tool)
            world.setBlock(it, Blocks.AIR.defaultBlockState(), 3)
        }

        // シャベルの耐久値を減らす
        if (!player.isCreative) {
            tool.damageValue += vein.size
        }
    }
}