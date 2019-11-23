package com.tfar.dankstorage.block;

import com.tfar.dankstorage.client.Client;
import com.tfar.dankstorage.container.AbstractAbstractDankContainer;
import com.tfar.dankstorage.inventory.DankHandler;
import com.tfar.dankstorage.inventory.PortableDankHandler;
import com.tfar.dankstorage.network.CMessageTogglePickup;
import com.tfar.dankstorage.network.CMessageToggleUseType;
import com.tfar.dankstorage.utils.Utils;
import com.tfar.dankstorage.tile.AbstractDankStorageTile;
import com.tfar.dankstorage.tile.DankTiles;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.tfar.dankstorage.network.CMessageTogglePickup.*;

public class DankBlock extends Block {
  public DankBlock(Properties p_i48440_1_) {
    super(p_i48440_1_);
  }

  @Override
  public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
    if (!world.isRemote) {
      final TileEntity tile = world.getTileEntity(pos);

      if (player.isSneaking()){
        if (tile instanceof AbstractDankStorageTile){
          ItemStack dank = new ItemStack(((AbstractDankStorageTile) tile).getDank());
          CompoundNBT nbt = ((AbstractDankStorageTile) tile).getHandler().serializeNBT();
          nbt.putInt("mode",((AbstractDankStorageTile) tile).mode);
          nbt.putInt("selectedSlot",((AbstractDankStorageTile) tile).selectedSlot);
          dank.setTag(nbt);
          ItemEntity itemEntity = new ItemEntity(world,pos.getX()+ .5,pos.getY() + .5,pos.getZ()+.5,dank);
          world.addEntity(itemEntity);
          world.removeBlock(pos,false);
          return true;
        }
      }

      if (tile instanceof INamedContainerProvider) {
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tile, tile.getPos());

      } else throw new IllegalStateException("Our named container provider is missing!");
    }
    return true;
  }

  @Override
  public void onBlockHarvested(World world, BlockPos pos, BlockState state, PlayerEntity player) {
    final TileEntity tile = world.getTileEntity(pos);
    if (tile instanceof AbstractDankStorageTile && !world.isRemote){
      ItemStack dank = new ItemStack(((AbstractDankStorageTile) tile).getDank());
      CompoundNBT nbt = ((AbstractDankStorageTile) tile).getHandler().serializeNBT();
      nbt.putInt("mode",((AbstractDankStorageTile) tile).mode);
      nbt.putInt("selectedSlot",((AbstractDankStorageTile) tile).selectedSlot);
      dank.setTag(nbt);
      ItemEntity itemEntity = new ItemEntity(world,pos.getX()+ .5,pos.getY() + .5,pos.getZ()+.5,dank);
      world.addEntity(itemEntity);
    }
  }

  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof AbstractDankStorageTile && !world.isRemote && entity != null) {
      if (stack.hasTag()){
        ((AbstractDankStorageTile) te).setContents(stack.getTag());
        ((AbstractDankStorageTile) te).mode = stack.getTag().getInt("mode");
        ((AbstractDankStorageTile) te).selectedSlot = stack.getTag().getInt("selectedSlot");
        if (stack.hasDisplayName()) {
          ((AbstractDankStorageTile) te).setCustomName(stack.getDisplayName());
        }
      }
    }
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockItemUseContext ctx) {
    ItemStack bag = ctx.getItem();

    Block block = Block.getBlockFromItem(bag.getItem());
    if (block instanceof DankBlock)return block.getDefaultState();
    return block.isAir(block.getDefaultState(),null,null) ? null : block.getStateForPlacement(ctx);
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    int type = Utils.getTier(this.getRegistryName());
    switch (type) {
      case 1:
      default:
        return new DankTiles.DankStorageTile1();
      case 2:
        return new DankTiles.DankStorageTile2();
      case 3:
        return new DankTiles.DankStorageTile3();
      case 4:
        return new DankTiles.DankStorageTile4();
      case 5:
        return new DankTiles.DankStorageTile5();
      case 6:
        return new DankTiles.DankStorageTile6();
      case 7:
        return new DankTiles.DankStorageTile7();
    }
  }

  @Override
  @OnlyIn(Dist.CLIENT)
  public void addInformation(ItemStack bag, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag) {
    //if (bag.hasTag())tooltip.add(new StringTextComponent(bag.getTag().toString()));

    if (!Screen.hasShiftDown()){
      tooltip.add(new TranslationTextComponent("text.dankstorage.shift",
              new StringTextComponent("Shift").applyTextStyle(TextFormatting.YELLOW)).applyTextStyle(TextFormatting.GRAY));
    }

    if (Screen.hasShiftDown()) {
      tooltip.add(new TranslationTextComponent("text.dankstorage.changemode",new StringTextComponent(Client.CONSTRUCTION.getLocalizedName()).applyTextStyle(TextFormatting.YELLOW)).applyTextStyle(TextFormatting.GRAY));
      CMessageToggleUseType.UseType mode = Utils.getUseType(bag);
      tooltip.add(
              new TranslationTextComponent("text.dankstorage.currentusetype",new TranslationTextComponent(
                      "dankstorage.usetype."+mode.name().toLowerCase(Locale.ROOT)).applyTextStyle(TextFormatting.YELLOW)).applyTextStyle(TextFormatting.GRAY));
      tooltip.add(
              new TranslationTextComponent("text.dankstorage.stacklimit",new StringTextComponent(Utils.getStackLimit(getRegistryName())+"").applyTextStyle(TextFormatting.GREEN)).applyTextStyle(TextFormatting.GRAY));

      DankHandler handler = Utils.getHandler(bag);

      if (handler.isEmpty()){
        tooltip.add(
                new TranslationTextComponent("text.dankstorage.empty").applyTextStyle(TextFormatting.ITALIC));
        return;
      }
      int count1 = 0;
      for (int i = 0; i < handler.getSlots(); i++) {
        if (count1>10)break;
        ItemStack item = handler.getStackInSlot(i);
        if (item.isEmpty())continue;
        ITextComponent count = new StringTextComponent(Integer.toString(item.getCount())).applyTextStyle(TextFormatting.AQUA);
        tooltip.add(new TranslationTextComponent("text.dankstorage.formatcontaineditems", count, item.getDisplayName().applyTextStyle(item.getRarity().color)));
        count1++;
      }
    }
  }

  @Nonnull
  @Override
  public ITextComponent getNameTextComponent() {
    int tier = Utils.getTier(this.getRegistryName());
    switch (tier){
      case 1:return super.getNameTextComponent().applyTextStyle(TextFormatting.DARK_GRAY);
      case 2:return super.getNameTextComponent().applyTextStyle(TextFormatting.RED);
      case 3:return super.getNameTextComponent().applyTextStyle(TextFormatting.GOLD);
      case 4:return super.getNameTextComponent().applyTextStyle(TextFormatting.GREEN);
      case 5:return super.getNameTextComponent().applyTextStyle(TextFormatting.AQUA);
      case 6:return super.getNameTextComponent().applyTextStyle(TextFormatting.DARK_PURPLE);
      case 7:return super.getNameTextComponent().applyTextStyle(TextFormatting.WHITE);
    }
    return super.getNameTextComponent();
  }

  @Override
  public void onReplaced(BlockState state, World p_196243_2_, BlockPos p_196243_3_, BlockState newState, boolean p_196243_5_) {
super.onReplaced(state,p_196243_2_,p_196243_3_,newState,p_196243_5_);
  }

  public static boolean onItemPickup(EntityItemPickupEvent event, ItemStack bag) {

    Mode mode = Utils.getMode(bag);
    if (mode == Mode.NORMAL)return false;
    PortableDankHandler inv = Utils.getHandler(bag);
    ItemStack toPickup = event.getItem().getItem();
    int count = toPickup.getCount();
    ItemStack rem = toPickup.copy();
    boolean oredict = Utils.tag(bag);

        //stack with existing items
        List<Integer> emptyslots = new ArrayList<>();
        for (int i = 0; i < inv.getSlots(); i++){
          if (inv.getStackInSlot(i).isEmpty()){
            emptyslots.add(i);
            continue;
          }
          rem = insertIntoHandler(mode,inv,i,rem,false,oredict);
          if (rem.isEmpty())break;
        }
        //only iterate empty slots
        if (!rem.isEmpty())
          for (int slot : emptyslots) {
            rem = insertIntoHandler(mode,inv,slot,rem,false,oredict);
            if (rem.isEmpty())break;
          }
    //leftovers
    toPickup.setCount(rem.getCount());
    if (rem.getCount() != count) {
      bag.setAnimationsToGo(5);
      PlayerEntity player = event.getPlayer();
      player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
      inv.writeItemStack();
    }
    return toPickup.isEmpty();
  }



  public static ItemStack insertIntoHandler(Mode mode, PortableDankHandler inv, int slot, ItemStack toInsert, boolean simulate, boolean oredict){

    ItemStack existing = inv.getStackInSlot(slot);
    if (ItemHandlerHelper.canItemStacksStack(toInsert,existing) || (oredict && Utils.doItemStacksShareWhitelistedTags(toInsert,existing))){
      int stackLimit = inv.stacklimit;
      int total = toInsert.getCount() + existing.getCount();
      int remainder = total - stackLimit;
      if (remainder <= 0) {
        if (!simulate)inv.getContents().set(slot, ItemHandlerHelper.copyStackWithSize(existing, total));
        return ItemStack.EMPTY;
      }
      else {
        if (!simulate) inv.getContents().set(slot, ItemHandlerHelper.copyStackWithSize(toInsert, stackLimit));
        if (mode == Mode.VOID_PICKUP) return ItemStack.EMPTY;
        return ItemHandlerHelper.copyStackWithSize(toInsert, remainder);
      }
    } else if (existing.isEmpty() && mode == Mode.FILTERED_PICKUP && toInsert.isItemEqual(existing) && ItemStack.areItemStackTagsEqual(existing, toInsert)){
      if (!simulate)inv.getContents().set(slot, toInsert);
      return ItemHandlerHelper.copyStackWithSize(toInsert,toInsert.getCount() - inv.getStackLimit(slot,toInsert));
    } else return toInsert;
  }
}