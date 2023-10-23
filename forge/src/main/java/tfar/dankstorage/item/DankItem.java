package tfar.dankstorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import tfar.dankstorage.container.PortableDankProvider;
import tfar.dankstorage.mixin.ItemUsageContextAccessor;
import tfar.dankstorage.network.DankPacketHandler;
import tfar.dankstorage.utils.*;
import tfar.dankstorage.world.DankInventoryForge;

import javax.annotation.Nonnull;

public class DankItem extends CoDankItem {

  public static final Rarity DARK_GRAY = Rarity.create("dark_gray", ChatFormatting.DARK_GRAY);
  public static final Rarity DARK_RED = Rarity.create("dark_red", ChatFormatting.DARK_RED);
  public static final Rarity GOLD = Rarity.create("gold", ChatFormatting.GOLD);
  public static final Rarity GREEN = Rarity.create("green", ChatFormatting.GREEN);
  public static final Rarity BLUE = Rarity.create("blue", ChatFormatting.AQUA);
  public static final Rarity DARK_PURPLE = Rarity.create("dark_purple", ChatFormatting.DARK_PURPLE);
  public static final Rarity WHITE = Rarity.create("white", ChatFormatting.WHITE);

    public DankItem(Properties $$0, DankStats stats) {
        super($$0, stats);
    }

    @Nonnull
  @Override
  public Rarity getRarity(ItemStack stack) {
      return switch (stats) {
          case one -> DARK_GRAY;
          case two -> DARK_RED;
          case three -> GOLD;
          case four -> GREEN;
          case five -> BLUE;
          case six -> DARK_PURPLE;
          case seven -> WHITE;
          default -> super.getRarity(stack);
      };
  }

    //this is called on the client
    @Override
    public InteractionResult interactLivingEntity(ItemStack bag, Player player, LivingEntity entity, InteractionHand hand) {
        if (!Utils.isConstruction(bag)) return InteractionResult.PASS;

        ItemStack toUse = Utils.getSelectedItem(bag,player.level());
        EquipmentSlot hand1 = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        player.setItemSlot(hand1, toUse);
        InteractionResult result = toUse.getItem().interactLivingEntity(toUse, player, entity, hand);

        //the client doesn't have access to the full inventory
        if (!player.level().isClientSide) {
            DankInventoryForge handler = Utils.getInventory(bag, player.level());
            handler.setStackInSlot(Utils.getSelectedSlot(bag), toUse);
        }

        player.setItemSlot(hand1, bag);
        return result;
    }

    @Override
    public void inventoryTick(ItemStack bag, Level level, Entity entity, int i, boolean equipped) {
        //there has to be a better way
        if (entity instanceof ServerPlayer player && equipped) {
            ItemStack sel = Utils.getSelectedItem(bag,level);
            DankPacketHandler.sendSelectedItem(player, sel);
        }
    }

    @Override
    public MenuProvider createProvider(ItemStack stack) {
        return new PortableDankProvider(stack);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (Utils.getFrequency(stack)!= Utils.INVALID) {
            return new DankItemCapability(stack);
        }
        return super.initCapabilities(stack, nbt);
    }
}
