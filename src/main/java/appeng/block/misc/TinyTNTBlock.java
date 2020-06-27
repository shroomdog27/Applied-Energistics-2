/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.block.misc;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

import appeng.block.AEBaseBlock;
import appeng.entity.TinyTNTPrimedEntity;

public class TinyTNTBlock extends AEBaseBlock {

    private static final VoxelShape SHAPE = VoxelShapes
            .create(new Box(0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f));

    public TinyTNTBlock(Settings props) {
        super(props);
        setFullSize(setOpaque(false));
    }

    @Override
    public int getOpacity(BlockState state, BlockView worldIn, BlockPos pos) {
        return 2; // FIXME: Validate that this is the correct value range
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public ActionResult onActivated(final World w, final BlockPos pos, final PlayerEntity player, final Hand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (heldItem != null && heldItem.getItem() == Items.FLINT_AND_STEEL) {
            this.startFuse(w, pos, player);
            w.removeBlock(pos, false);
            heldItem.damageItem(1, player, p -> {
                p.sendBreakAnimation(hand);
            }); // FIXME Check if onBroken is equivalent
            return ActionResult.SUCCESS;
        } else {
            return super.onActivated(w, pos, player, hand, heldItem, hit);
        }
    }

    public void startFuse(final World w, final BlockPos pos, final LivingEntity igniter) {
        if (!w.isClient) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter);
            w.addEntity(primedTinyTNTEntity);
            w.playSound(null, primedTinyTNTEntity.getPosX(), primedTinyTNTEntity.getPosY(),
                    primedTinyTNTEntity.getPosZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1, 1);
        }
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        if (world.getRedstonePowerFromNeighbors(pos) > 0) {
            this.startFuse(world, pos, null);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World w, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, w, pos, oldState, isMoving);

        if (w.getRedstonePowerFromNeighbors(pos) > 0) {
            this.startFuse(w, pos, null);
            w.removeBlock(pos, false);
        }
    }

    @Override
    public void onEntityWalk(final World w, final BlockPos pos, final Entity entity) {
        if (entity instanceof AbstractArrowEntity && !w.isClient) {
            final AbstractArrowEntity entityarrow = (AbstractArrowEntity) entity;

            if (entityarrow.isBurning()) {
                LivingEntity igniter = null;
                // Check if the shooter still exists
                if (w instanceof ServerWorld) {
                    ServerWorld serverWorld = (ServerWorld) w;
                    Entity shooter = serverWorld.getEntityByUuid(entityarrow.shootingEntity);
                    if (shooter instanceof LivingEntity) {
                        igniter = (LivingEntity) shooter;
                    }

                }
                this.startFuse(w, pos, igniter);
                w.removeBlock(pos, false);
            }
        }
    }

    @Override
    public boolean canDropFromExplosion(final Explosion exp) {
        return false;
    }

    @Override
    public void onExplosionDestroy(final World w, final BlockPos pos, final Explosion exp) {
        super.onExplosionDestroy(w, pos, exp);
        if (!w.isClient) {
            final TinyTNTPrimedEntity primedTinyTNTEntity = new TinyTNTPrimedEntity(w, pos.getX() + 0.5F,
                    pos.getY() + 0.5F, pos.getZ() + 0.5F, exp.getExplosivePlacedBy());
            primedTinyTNTEntity
                    .setFuse(w.rand.nextInt(primedTinyTNTEntity.getFuse() / 4) + primedTinyTNTEntity.getFuse() / 8);
            w.addEntity(primedTinyTNTEntity);
        }
    }

}