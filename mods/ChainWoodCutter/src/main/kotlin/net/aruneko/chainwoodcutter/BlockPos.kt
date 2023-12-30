package net.aruneko.chainwoodcutter

import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.state.BlockState

fun BlockPos.getAround(): List<BlockPos> {
    val blockX = this.x
    val blockY = this.y
    val blockZ = this.z

    val xs = listOf(blockX - 1, blockX, blockX + 1)
    val ys = listOf(blockY, blockY + 1)
    val zx = listOf(blockZ - 1, blockZ, blockZ + 1)

    return xs.flatMap { x ->
        ys.flatMap { y ->
            zx.map { z ->
                BlockPos(x, y, z)
            }
        }
    }.filter { it != this }
}

fun BlockPos.findLogAndLeaves(level: LevelAccessor): List<BlockPos> {
    val paths = ArrayDeque<ArrayDeque<BlockPos>>()
    val foundBlocks = ArrayDeque<BlockPos>()
    var target = this

    while (true) {
        // まず対象になっているブロックを探索済みにする
        foundBlocks.addFirst(target)
        // 周囲の破壊対象ブロックを取得する
        val around = ArrayDeque(
            target.getAround().extractEqualsOrLeaves(level, target).filterNot {
                // ただし探索済みおよび経路として追加済みのブロックは外す
                foundBlocks.contains(it) || paths.flatten().contains(it)
            }
        )

        // 周囲に何もない = 木構造の先端まで来たとき
        if (around.isEmpty()) {
            // 未探索のノードを取得
            val path = paths.removeFirstOrNull()
            if (path == null) {
                // 未探索のノードがなければ終了
                break
            } else {
                // 未探索の経路を持つノードがあればそのうちのひとつを次の対象とする
                target = path.removeFirst()
                if (path.isNotEmpty()) {
                    // ただし残りの経路は未探索のものとして残しておく
                    paths.addFirst(path)
                }
                continue
            }
        }
        // 木構造の途中にあるとき
        // 周囲のブロックからひとつ取ってくる
        target = around.removeFirst()
        if (around.isNotEmpty()) {
            // 周囲のブロックが残っていれば未探索の経路として登録
            paths.addFirst(around)
        }
    }

    return foundBlocks.toList()
}

fun BlockPos.toBlockState(level: LevelAccessor): BlockState {
    return level.getBlockState(this)
}

fun BlockPos.isLeaf(level: LevelAccessor): Boolean {
    val blockState = this.toBlockState(level)
    return blockState.`is`(net.minecraft.tags.BlockTags.LEAVES)
}

fun List<BlockPos>.extractEqualsOrLeaves(level: LevelAccessor, target: BlockPos): List<BlockPos> {
    val targetBlock = target.toBlockState(level)
    return this.filter { targetBlock.`is`(level.getBlockState(it).block) || it.isLeaf(level) }
}
