package io.taraxacum.finaltech.core.items.machine.template.conversion;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.tools.GoldPan;
import io.github.thebusybiscuit.slimefun4.implementation.settings.GoldPanDrop;
import io.taraxacum.finaltech.api.dto.RandomMachineRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Final_ROOT
 * @since 2.0
 */
public class GravelConversion extends AbstractConversionMachine{
    public GravelConversion(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void registerDefaultRecipes() {
        GoldPan goldPan = SlimefunItems.GOLD_PAN.getItem(GoldPan.class);
        try {
            Method method = GoldPan.class.getDeclaredMethod("getGoldPanDrops");
            method.setAccessible(true);
            Set<GoldPanDrop> invoke = (Set<GoldPanDrop>)method.invoke(goldPan);
            List<RandomMachineRecipe.RandomOutput> randomOutputList = new ArrayList<>(invoke.size());
            for (GoldPanDrop goldPanDrop : invoke) {
                randomOutputList.add(new RandomMachineRecipe.RandomOutput(goldPanDrop.getOutput(), goldPanDrop.getValue()));
            }
            this.registerRecipe(new RandomMachineRecipe(new ItemStack[] {new ItemStack(goldPan.getInputMaterial())}, randomOutputList));
        } catch (Exception e) {
            List<RandomMachineRecipe.RandomOutput> randomOutputList = new ArrayList<>();
            randomOutputList.add(new RandomMachineRecipe.RandomOutput(new ItemStack[] {new ItemStack(Material.FLINT)}, 40));
            randomOutputList.add(new RandomMachineRecipe.RandomOutput(new ItemStack[] {new ItemStack(Material.CLAY_BALL)}, 20));
            randomOutputList.add(new RandomMachineRecipe.RandomOutput(new ItemStack[] {SlimefunItems.SIFTED_ORE}, 35));
            randomOutputList.add(new RandomMachineRecipe.RandomOutput(new ItemStack[] {new ItemStack(Material.IRON_NUGGET)}, 5));
            this.registerRecipe(new RandomMachineRecipe(new ItemStack[] {new ItemStack(Material.GRAVEL)}, randomOutputList));
        }
    }
}
