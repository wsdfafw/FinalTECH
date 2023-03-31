package io.taraxacum.finaltech.core.item.machine.range.point.face;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineProcessHolder;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineOperation;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.taraxacum.finaltech.FinalTech;
import io.taraxacum.finaltech.core.interfaces.LocationMachine;
import io.taraxacum.finaltech.core.interfaces.MenuUpdater;
import io.taraxacum.finaltech.core.interfaces.RecipeItem;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.unit.StatusL2Menu;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.finaltech.util.MachineUtil;
import io.taraxacum.finaltech.util.RecipeUtil;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.finaltech.util.BlockTickerUtil;
import io.taraxacum.libs.slimefun.dto.LocationInfo;
import io.taraxacum.libs.slimefun.util.EnergyUtil;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Final_ROOT
 * @since 2.0
 */
public class OperationAccelerator extends AbstractFaceMachine implements RecipeItem, EnergyNetComponent, MenuUpdater, LocationMachine {
    private final Set<String> notAllowedId = new HashSet<>(ConfigUtil.getItemStringList(this, "not-allowed-id"));
    private final int capacity = ConfigUtil.getOrDefaultItemSetting(20000000, this, "capacity");
    private final int efficiency = ConfigUtil.getOrDefaultItemSetting(1, this, "efficiency");

    public OperationAccelerator(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected BlockPlaceHandler onBlockPlace() {
        return MachineUtil.BLOCK_PLACE_HANDLER_PLACER_DENY;
    }

    @Nonnull
    @Override
    protected BlockBreakHandler onBlockBreak() {
        return MachineUtil.simpleBlockBreakerHandler(this);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new StatusL2Menu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        BlockMenu blockMenu = BlockStorage.getInventory(block);
        ItemStack item = blockMenu.getItemInSlot(this.getInputSlot()[0]);

        int amount;
        if(ItemStackUtil.isItemSimilar(item, this.getItem())) {
            amount = item.getAmount();
        } else {
            amount = 1;
        }

        int count = this.pointFunction(block, 1, location -> {
            LocationInfo locationInfo = LocationInfo.get(location);
            if(locationInfo != null && !this.notAllowedId.contains(locationInfo.getId())) {
                if (locationInfo.getSlimefunItem() instanceof MachineProcessHolder machineProcessHolder && locationInfo.getSlimefunItem() instanceof EnergyNetComponent energyNetComponent) {
                    MachineProcessor<?> machineProcessor = machineProcessHolder.getMachineProcessor();
                    Runnable runnable = () -> {
                        int energy = Integer.parseInt(EnergyUtil.getCharge(config));
                        int time = 0;
                        MachineOperation operation = machineProcessor.getOperation(location);
                        if (operation != null) {
                            time = Math.min(Math.min(FinalTech.getRandom().nextInt(1 + amount * this.efficiency), energy / energyNetComponent.getCapacity()), operation.getRemainingTicks());
                            if(time > 0) {
                                operation.addProgress(Math.min(time, operation.getRemainingTicks()));
                                EnergyUtil.setCharge(config, Math.max(0, energy - time * energyNetComponent.getCapacity()));
                            }
                        }

                        if(blockMenu.hasViewer()) {
                            this.updateMenu(blockMenu, StatusL2Menu.STATUS_SLOT, this,
                                    String.valueOf(energy),
                                    String.valueOf(time));
                        }
                    };
                    BlockTickerUtil.runTask(FinalTech.getLocationRunnableFactory(), FinalTech.isAsyncSlimefunItem(locationInfo.getId()), runnable, () -> new Location[]{location, block.getLocation()});
                    return 1;
                }
            }
            return 0;
        });

        if(count == 0 && blockMenu.hasViewer()) {
            this.updateMenu(blockMenu, StatusL2Menu.STATUS_SLOT, this,
                    EnergyUtil.getCharge(config),
                    "0");
        }
    }

    @Override
    protected boolean isSynchronized() {
        return false;
    }

    @Nonnull
    @Override
    protected BlockFace getBlockFace() {
        return BlockFace.UP;
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipeWithBorder(FinalTech.getLanguageManager(), this,
                String.valueOf(this.efficiency));

        for (SlimefunItem slimefunItem : Slimefun.getRegistry().getAllSlimefunItems()) {
            if (slimefunItem instanceof MachineProcessHolder && !this.notAllowedId.contains(slimefunItem.getId())) {
                this.registerDescriptiveRecipe(slimefunItem.getItem());
            }
        }
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return this.capacity;
    }

    @Override
    public Location[] getLocations(@Nonnull Location sourceLocation) {
        return new Location[] {this.getTargetLocation(sourceLocation, 1)};
    }
}
