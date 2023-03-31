package io.taraxacum.finaltech.core.group;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerPreResearchEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.taraxacum.finaltech.FinalTech;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import io.taraxacum.libs.slimefun.dto.SlimefunCraftRegistry;
import io.taraxacum.libs.slimefun.util.GuideUtil;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Final_ROOT
 * @since 2.2
 */
public class CraftItemGroup extends FlexItemGroup {
    private static final int BACK_SLOT = 1;
    private static final int PREVIOUS_SLOT = 3;
    private static final int NEXT_SLOT = 5;
    private static final int ICON_SLOT = 7;
    private static final int[] BORDER = new int[] {0, 2, 4, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int[] MAIN_CONTENT = new int[] {
            18, 19, 20, 21, 22, 23, 24, 25, 26,
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44,
            45, 46, 47, 48, 49, 50, 51, 52, 53};

    private static final JavaPlugin JAVA_PLUGIN = FinalTech.getInstance();

    private static final Map<SlimefunItem, CraftItemGroup> ITEM_MAP = new LinkedHashMap<>();

    private final int page;
    private Map<Integer, CraftItemGroup> pageMap = new LinkedHashMap<>();
    private final SlimefunItem slimefunItem;
    private final List<SlimefunItem> slimefunItemList;

    protected CraftItemGroup(NamespacedKey key, SlimefunItem slimefunItem) {
        super(key, ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem()));
        this.page = 1;
        this.slimefunItem = slimefunItem;
        this.slimefunItemList = new ArrayList<>();

        List<SlimefunItem> sfItemList = SlimefunCraftRegistry.getInstance().getCraftSlimefunItemList(slimefunItem);
        for(SlimefunItem sfItem : sfItemList) {
            if(!sfItem.isDisabled() && !sfItem.isHidden()) {
                this.slimefunItemList.add(sfItem);
            }
        }

        this.pageMap.put(1, this);
        ITEM_MAP.put(slimefunItem, this);
    }

    protected CraftItemGroup(NamespacedKey key, SlimefunItem slimefunItem, int page) {
        super(key, ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem()));
        this.page = page;
        this.slimefunItem = slimefunItem;
        this.slimefunItemList = new ArrayList<>();

        List<SlimefunItem> sfItemList = SlimefunCraftRegistry.getInstance().getCraftSlimefunItemList(slimefunItem);
        for(SlimefunItem sfItem : sfItemList) {
            if(!sfItem.isDisabled() && !sfItem.isHidden()) {
                this.slimefunItemList.add(sfItem);
            }
        }
    }

    @Override
    public boolean isVisible(@Nonnull Player player, @Nonnull PlayerProfile playerProfile, @Nonnull SlimefunGuideMode slimefunGuideMode) {
        return false;
    }

    @Override
    public void open(Player player, PlayerProfile playerProfile, SlimefunGuideMode slimefunGuideMode) {
        playerProfile.getGuideHistory().add(this, this.page);
        this.generateMenu(player, playerProfile, slimefunGuideMode).open(player);
    }

    public void refresh(@Nonnull Player player, @Nonnull PlayerProfile playerProfile, @Nonnull SlimefunGuideMode slimefunGuideMode) {
        GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
        this.open(player, playerProfile, slimefunGuideMode);
    }

    @Nonnull
    private ChestMenu generateMenu(@Nonnull Player player, @Nonnull PlayerProfile playerProfile, @Nonnull SlimefunGuideMode slimefunGuideMode) {
        ChestMenu chestMenu = new ChestMenu(this.slimefunItem.getItemName());

        chestMenu.setEmptySlotsClickable(false);
        chestMenu.addMenuOpeningHandler(pl -> pl.playSound(pl.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1));

        chestMenu.addItem(BACK_SLOT, ChestMenuUtils.getBackButton(player));
        chestMenu.addMenuClickHandler(BACK_SLOT, (pl, s, is, action) -> {
            GuideHistory guideHistory = playerProfile.getGuideHistory();
            if (action.isShiftClicked()) {
                SlimefunGuide.openMainMenu(playerProfile, slimefunGuideMode, guideHistory.getMainMenuPage());
            } else {
                guideHistory.goBack(Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE));
            }
            return false;
        });

        chestMenu.addItem(PREVIOUS_SLOT, ChestMenuUtils.getPreviousButton(player, this.page, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1));
        chestMenu.addMenuClickHandler(PREVIOUS_SLOT, (p, slot, item, action) -> {
            GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
            CraftItemGroup craftItemGroup = this.getByPage(Math.max(this.page - 1, 1));
            craftItemGroup.open(player, playerProfile, slimefunGuideMode);
            return false;
        });

        chestMenu.addItem(NEXT_SLOT, ChestMenuUtils.getNextButton(player, this.page, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1));
        chestMenu.addMenuClickHandler(NEXT_SLOT, (p, slot, item, action) -> {
            GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
            CraftItemGroup craftItemGroup = this.getByPage(Math.min(this.page + 1, (this.slimefunItemList.size() - 1) / MAIN_CONTENT.length + 1));
            craftItemGroup.open(player, playerProfile, slimefunGuideMode);
            return false;
        });

        chestMenu.addItem(ICON_SLOT, ItemStackUtil.cloneWithoutNBT(super.item));
        chestMenu.addMenuClickHandler(ICON_SLOT, ChestMenuUtils.getEmptyClickHandler());

        for (int slot : BORDER) {
            chestMenu.addItem(slot, ChestMenuUtils.getBackground());
            chestMenu.addMenuClickHandler(slot, ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i = 0; i < MAIN_CONTENT.length; i++) {
            int index = i + this.page * MAIN_CONTENT.length - MAIN_CONTENT.length;
            if (index < this.slimefunItemList.size()) {
                SlimefunItem slimefunItem = this.slimefunItemList.get(index);
                Research research = slimefunItem.getResearch();
                if (playerProfile.hasUnlocked(research)) {
                    ItemStack itemStack = ItemStackUtil.cloneWithoutNBT(slimefunItem.getItem());
                    ItemStackUtil.addLoreToFirst(itemStack, "§7" + slimefunItem.getId());
                    chestMenu.addItem(MAIN_CONTENT[i], itemStack);
                    chestMenu.addMenuClickHandler(MAIN_CONTENT[i], (p, slot, item, action) -> {
                        RecipeItemGroup recipeItemGroup = RecipeItemGroup.getByItemStack(player, playerProfile, slimefunGuideMode, slimefunItem.getItem());
                        if (recipeItemGroup != null) {
                            Bukkit.getScheduler().runTask(JAVA_PLUGIN, () -> recipeItemGroup.open(player, playerProfile, slimefunGuideMode));
                        }
                        return false;
                    });
                } else {
                    ItemStack icon = ItemStackUtil.cloneItem(ChestMenuUtils.getNotResearchedItem());
                    ItemStackUtil.setLore(icon,
                            "§7" + research.getName(player),
                            "§4§l" + Slimefun.getLocalization().getMessage(player, "guide.locked"),
                            "",
                            "§a> Click to unlock",
                            "",
                            "§7Cost: §b" + research.getCost() + " Level(s)");
                    chestMenu.addItem(MAIN_CONTENT[i], icon);
                    chestMenu.addMenuClickHandler(MAIN_CONTENT[i], (p, slot, item, action) -> {
                        PlayerPreResearchEvent event = new PlayerPreResearchEvent(player, research, slimefunItem);
                        Bukkit.getPluginManager().callEvent(event);

                        if (!event.isCancelled() && !playerProfile.hasUnlocked(research)) {
                            if (research.canUnlock(player)) {
                                Slimefun.getRegistry().getSlimefunGuide(SlimefunGuideMode.SURVIVAL_MODE).unlockItem(player, slimefunItem, player1 -> this.refresh(player, playerProfile, slimefunGuideMode));
                            } else {
                                this.refresh(player, playerProfile, slimefunGuideMode);
                                Slimefun.getLocalization().sendMessage(player, "messages.not-enough-xp", true);
                            }
                        } else {
                            GuideUtil.removeLastEntry(playerProfile.getGuideHistory());
                            this.open(player, playerProfile, slimefunGuideMode);
                        }
                        return false;
                    });
                }
            }
        }

        return chestMenu;
    }

    @Nonnull
    private CraftItemGroup getByPage(int page) {
        if (this.pageMap.containsKey(page)) {
            return this.pageMap.get(page);
        } else {
            synchronized (this.pageMap.get(1)) {
                if (this.pageMap.containsKey(page)) {
                    return this.pageMap.get(page);
                }
                CraftItemGroup craftItemGroup = new CraftItemGroup(new NamespacedKey(JAVA_PLUGIN, this.getKey().getKey() + "_" + page), this.slimefunItem, page);
                craftItemGroup.pageMap = this.pageMap;
                this.pageMap.put(page, craftItemGroup);
                return craftItemGroup;
            }
        }
    }

    @Nonnull
    public static CraftItemGroup getBySlimefunItem(@Nonnull SlimefunItem slimefunItem) {
        return new CraftItemGroup(new NamespacedKey(JAVA_PLUGIN, "FINALTECH_" + slimefunItem.getId().hashCode()), slimefunItem);
    }
}
