package io.taraxacum.finaltech.core.item.machine.electric.capacitor.expanded;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.taraxacum.finaltech.util.ConfigUtil;
import org.bukkit.inventory.ItemStack;

/**
 * @author Final_ROOT
 * @since 2.0
 */
public class OverloadedExpandedCapacitor extends AbstractExpandedElectricCapacitor{
    private final int capacity = ConfigUtil.getOrDefaultItemSetting(4194304, this, "capacity");
    private final int stack = ConfigUtil.getOrDefaultItemSetting(2097152, this, "max-stack");

    public OverloadedExpandedCapacitor(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public int getCapacity() {
        return this.capacity * 2;
    }

    @Override
    public int getMaxStack() {
        return this.stack - 2;
    }
}
