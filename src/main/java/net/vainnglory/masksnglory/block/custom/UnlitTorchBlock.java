package net.vainnglory.masksnglory.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class UnlitTorchBlock extends Block {
    private static final VoxelShape SHAPE = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 10.0, 10.0);

    public UnlitTorchBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}
