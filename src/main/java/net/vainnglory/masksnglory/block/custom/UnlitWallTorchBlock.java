package net.vainnglory.masksnglory.block.custom;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class UnlitWallTorchBlock extends Block {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(5.5,  3.0, 11.0, 10.5, 13.0, 16.0);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(5.5,  3.0,  0.0, 10.5, 13.0,  5.0);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(11.0, 3.0,  5.5, 16.0, 13.0, 10.5);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0.0,  3.0,  5.5,  5.0, 13.0, 10.5);

    public UnlitWallTorchBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> VoxelShapes.fullCube();
        };
    }
}
