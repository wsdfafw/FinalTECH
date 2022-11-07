package io.taraxacum.finaltech.core.menu.manual;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.taraxacum.finaltech.FinalTech;
import io.taraxacum.libs.slimefun.dto.BasicCraft;
import io.taraxacum.libs.slimefun.dto.MachineRecipeFactory;
import io.taraxacum.finaltech.core.items.machine.AbstractMachine;
import io.taraxacum.finaltech.core.items.machine.manual.MatrixCraftingTable;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.plugin.util.ParticleUtil;
import io.taraxacum.finaltech.util.SfItemUtil;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class MatrixCraftingTableMenu extends AbstractManualMachineMenu{
    private static final int[] BORDER = new int[] {6, 7, 8, 15, 17, 24, 25, 26};
    private static final int[] INPUT_BORDER = new int[0];
    private static final int[] OUTPUT_BORDER = new int[] {33, 34, 35, 42, 44, 51, 52, 53};
    private static final int[] INPUT_SLOT = new int[] {0, 1, 2, 3, 4, 5, 9, 10, 11, 12, 13, 14, 18, 19, 20, 21, 22, 23, 27, 28, 29, 30, 31, 32, 36, 37, 38, 39, 40, 41, 45, 46, 47, 48, 49, 50};
    private static final int[] OUTPUT_SLOT = new int[] {43};

    private static final int PARSE_SLOT = 16;
    private static final ItemStack PARSE_ICON = new CustomItemStack(Material.YELLOW_STAINED_GLASS_PANE, FinalTech.getLanguageString("items", SfItemUtil.getIdFormatName(MatrixCraftingTable.class), "parse-icon", "name"), FinalTech.getLanguageStringArray("items", SfItemUtil.getIdFormatName(MatrixCraftingTable.class), "parse-icon", "lore"));

    public MatrixCraftingTableMenu(@Nonnull AbstractMachine machine) {
        super(machine);
    }

    @Override
    protected int[] getBorder() {
        return BORDER;
    }

    @Override
    protected int[] getInputBorder() {
        return INPUT_BORDER;
    }

    @Override
    protected int[] getOutputBorder() {
        return OUTPUT_BORDER;
    }

    @Override
    public int[] getInputSlot() {
        return INPUT_SLOT;
    }

    @Override
    public int[] getOutputSlot() {
        return OUTPUT_SLOT;
    }

    @Override
    public void init() {
        super.init();
        this.addItem(PARSE_SLOT, PARSE_ICON);
        this.addMenuClickHandler(PARSE_SLOT, ChestMenuUtils.getEmptyClickHandler());
    }

    @Override
    public void newInstance(@Nonnull BlockMenu blockMenu, @Nonnull Block block) {
        super.newInstance(blockMenu, block);
        blockMenu.addMenuClickHandler(PARSE_SLOT, (player, slot, item, action) -> {
            ParticleUtil.drawCubeByBlock(Particle.GLOW, 0, block);

            SlimefunItem slimefunItem;

            List<MachineRecipe> machineRecipeList = MachineRecipeFactory.getInstance().getRecipe(MatrixCraftingTable.class);
            List<SlimefunItem> slimefunItemList = new ArrayList<>(machineRecipeList.size());
            for (MachineRecipe machineRecipe : machineRecipeList) {
                ItemStack[] output = machineRecipe.getOutput();
                if (output.length == 1) {
                    slimefunItem = SlimefunItem.getByItem(output[0]);
                    if (slimefunItem != null) {
                        slimefunItemList.add(slimefunItem);
                    }
                }
            }

            BasicCraft basicCraft = BasicCraft.doCraft(slimefunItemList, blockMenu.toInventory(), MatrixCraftingTableMenu.this.getInputSlot());
            if (basicCraft != null) {
                ItemStack existedItem = blockMenu.getItemInSlot(MatrixCraftingTableMenu.this.getOutputSlot()[0]);
                if (ItemStackUtil.isItemNull(existedItem) || ItemStackUtil.isItemSimilar(existedItem, basicCraft.getMatchItem().getItem())) {
                    int amount = action.isRightClicked() || action.isShiftClicked() ? basicCraft.getMatchAmount() : 1;
                    basicCraft.setMatchAmount(amount);

                    basicCraft.consumeItem(blockMenu.toInventory(), MatrixCraftingTableMenu.this.getInputSlot());
                    blockMenu.pushItem(ItemStackUtil.cloneItem(basicCraft.getMatchItem().getItem(), amount), MatrixCraftingTableMenu.this.getOutputSlot());
                }
            }

            return false;
        });
    }

    @Override
    public void updateInventory(@Nonnull Inventory inventory, @Nonnull Location location) {
        SlimefunItem slimefunItem;

        List<MachineRecipe> machineRecipeList = MachineRecipeFactory.getInstance().getRecipe(MatrixCraftingTable.class);
        List<SlimefunItem> slimefunItemList = new ArrayList<>(machineRecipeList.size());
        for (MachineRecipe machineRecipe : machineRecipeList) {
            ItemStack[] output = machineRecipe.getOutput();
            if (output.length == 1) {
                slimefunItem = SlimefunItem.getByItem(output[0]);
                if (slimefunItem != null) {
                    slimefunItemList.add(slimefunItem);
                }
            }
        }

        BasicCraft basicCraft = BasicCraft.doCraft(slimefunItemList, inventory, this.getInputSlot());

        if (basicCraft != null) {
            slimefunItem = basicCraft.getMatchItem();
            ItemStack matchItem = ItemStackUtil.cloneItem(slimefunItem.getItem());
            ItemStackUtil.addLoresToLast(matchItem, FinalTech.getLanguageManager().replaceStringArray(FinalTech.getLanguageStringArray("items", SfItemUtil.getIdFormatName(MatrixCraftingTable.class), "show-icon", "lore"), String.valueOf(basicCraft.getMatchAmount())));
            inventory.setItem(PARSE_SLOT, matchItem);
        } else {
            inventory.setItem(PARSE_SLOT, PARSE_ICON);
        }
    }
}