package io.taraxacum.finaltech.core.items.machine.operation;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.taraxacum.finaltech.FinalTech;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.operation.DustFactoryOperation;
import io.taraxacum.finaltech.core.menu.limit.DustFactoryDirtMenu;
import io.taraxacum.finaltech.setup.FinalTechItems;
import io.taraxacum.finaltech.util.ItemStackUtil;
import io.taraxacum.finaltech.util.slimefun.ConfigUtil;
import io.taraxacum.finaltech.util.slimefun.RecipeUtil;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

/**
 * @author Final_ROOT
 * @since 1.0
 */
public class DustFactoryDirt extends AbstractOperationMachine {
    public DustFactoryDirt(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new DustFactoryDirtMenu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        BlockMenu blockMenu = BlockStorage.getInventory(block);
        DustFactoryOperation operation = (DustFactoryOperation)this.getMachineProcessor().getOperation(block);

        for (int slot : this.getInputSlot()) {
            ItemStack inputItem = blockMenu.getItemInSlot(slot);
            if (ItemStackUtil.isItemNull(inputItem)) {
                continue;
            }

            if (operation == null) {
                operation = new DustFactoryOperation();
                this.getMachineProcessor().startOperation(block, operation);
            }
            operation.addItem(inputItem);

            ItemStack operationResult = operation.getResult();
            if(operationResult != null && InvUtils.fitAll(blockMenu.toInventory(), new ItemStack[] {operationResult}, this.getOutputSlot())) {
                blockMenu.pushItem(operationResult, this.getOutputSlot());
                this.getMachineProcessor().endOperation(block);
                operation = null;
            }

            blockMenu.consumeItem(slot, inputItem.getAmount());
        }

        if (operation == null) {
            operation = new DustFactoryOperation();
            this.getMachineProcessor().startOperation(block, operation);
        }
        if(blockMenu.hasViewer()) {
            ItemStack itemStack = blockMenu.getItemInSlot(DustFactoryDirtMenu.STATUS_SLOT);
            ItemStackUtil.setLore(itemStack, ConfigUtil.getStatusMenuLore(FinalTech.getLanguageManager(), this,
                    String.valueOf(operation.getAmountCount()),
                    String.valueOf(operation.getTypeCount())));
        }
    }

    @Override
    public void registerDefaultRecipes() {
        RecipeUtil.registerDescriptiveRecipe(FinalTech.getLanguageManager(), this,
                String.valueOf(DustFactoryOperation.AMOUNT_DIFFICULTY),
                String.valueOf(DustFactoryOperation.TYPE_DIFFICULTY));
    }
}
