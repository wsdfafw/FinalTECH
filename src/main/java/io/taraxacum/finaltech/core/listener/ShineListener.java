package io.taraxacum.finaltech.core.listener;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.taraxacum.common.util.JavaUtil;
import io.taraxacum.finaltech.FinalTech;
import io.taraxacum.finaltech.core.task.effect.VoidCurse;
import io.taraxacum.finaltech.setup.FinalTechItemStacks;
import io.taraxacum.finaltech.setup.FinalTechItems;
import io.taraxacum.finaltech.util.ConfigUtil;
import io.taraxacum.libs.plugin.task.TaskTicker;
import io.taraxacum.libs.plugin.util.ItemStackUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * @see io.taraxacum.finaltech.core.item.unusable.Shine
 * @author Final_ROOT
 * @since 2.0
 */
public class ShineListener implements Listener {
    private final double damage = ConfigUtil.getOrDefaultItemSetting(0.3, FinalTechItemStacks.SHINE.getItemId(), "damage");
    private final int liveTime = ConfigUtil.getOrDefaultItemSetting(40, FinalTechItemStacks.SHINE.getItemId(), "live-time");
    private final int baseEffectTime = ConfigUtil.getOrDefaultItemSetting(100, FinalTechItemStacks.SHINE.getItemId(), "effect-time-base");
    private final int deathMulEffectTime = ConfigUtil.getOrDefaultItemSetting(40, FinalTechItemStacks.SHINE.getItemId(), "effect-time-mul-death");
    private final int obtainMulEffectTime = ConfigUtil.getOrDefaultItemSetting(2, FinalTechItemStacks.SHINE.getItemId(), "effect-time-mul-obtain");
    private final double baseTeleportRange = ConfigUtil.getOrDefaultItemSetting(16, FinalTechItemStacks.SHINE.getItemId(), "teleport-range-base");
    private final double mulTeleportRange = ConfigUtil.getOrDefaultItemSetting(0.25, FinalTechItemStacks.SHINE.getItemId(), "teleport-range-mul");

    private final Map<Player, Integer> deathCount = new HashMap<>();
    private final Map<Player, Integer> obtainCount = new HashMap<>();

    /**
     * Player will lose all "shine" while dead in low place.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent playerDeathEvent) {
        Player player = playerDeathEvent.getEntity();

        boolean haveBox = false;
        List<ItemStack> shineItemList = new ArrayList<>();
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (!haveBox && FinalTechItems.BOX.verifyItem(itemStack)) {
                haveBox = true;
            }
            if (FinalTechItems.SHINE.verifyItem(itemStack)) {
                shineItemList.add(itemStack);
            }
        }

        boolean inCurse = false;
        boolean inLowPlace = false;
        Location location = playerDeathEvent.getEntity().getLocation();
        if (location.getY() < location.getWorld().getMinHeight() - 64) {
            inLowPlace = true;
        }
        if(TaskTicker.has(player, LivingEntity.class, VoidCurse.ID)) {
            inCurse = true;
        }

        if (haveBox || inCurse || inLowPlace) {
            for (ItemStack itemStack : shineItemList) {
                itemStack.setAmount(0);
            }
        }

        if (haveBox && (inCurse || inLowPlace)) {
            playerDeathEvent.setKeepInventory(false);
        }

        if(inCurse || inLowPlace) {
            if(!this.deathCount.containsKey(player)) {
                this.deathCount.put(player, 1);
            } else {
                this.deathCount.put(player, this.deathCount.get(player) + 1);
            }

            if(player.getLastDamageCause() != null && EntityDamageEvent.DamageCause.VOID.equals(player.getLastDamageCause().getCause())) {
                playerDeathEvent.setDeathMessage(FinalTech.getLanguageString("effect", "VOID_CURSE", "message", "death").replace("{1}", player.getName()));
            }

            if(inCurse) {
                Optional<PlayerProfile> playerProfile = PlayerProfile.find(player);
                if(playerProfile.isPresent()) {
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
                        if(slimefunItem != null && slimefunItem.getResearch() != null) {
                            playerProfile.get().setResearched(slimefunItem.getResearch(), false);
                        }
                    }
                    List<Research> researchList = new ArrayList<>(playerProfile.get().getResearches());
                    if(!researchList.isEmpty()) {
                        playerProfile.get().setResearched(researchList.get(FinalTech.getRandom().nextInt(researchList.size())), false);
                    }

                    Research research = FinalTechItems.SHINE.getResearch();
                    if(research != null) {
                        playerProfile.get().setResearched(research, false);
                    }
                    research = FinalTechItems.BOX.getResearch();
                    if(research != null) {
                        playerProfile.get().setResearched(research, false);
                    }
                }

                player.sendMessage(FinalTech.getLanguageString("items", "FINALTECH_SHINE", "message"));
            }
        }
    }

    /**
     * Player will get a shine while holding item box.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent entityDamageEvent) {
        if (EntityDamageEvent.DamageCause.VOID.equals(entityDamageEvent.getCause()) && EntityType.PLAYER.equals(entityDamageEvent.getEntityType())) {
            Entity entity = entityDamageEvent.getEntity();
            Location location = entity.getLocation();
            if (entity instanceof Player player && location.getWorld() != null && location.getY() < location.getWorld().getMinHeight() - 64) {
                boolean haveBox = false;
                int shineCount = 0;
                for (ItemStack itemStack : player.getInventory().getContents()) {
                    if (!haveBox && FinalTechItems.BOX.verifyItem(itemStack)) {
                        haveBox = true;
                    } else if(FinalTechItems.SHINE.verifyItem(itemStack)) {
                        shineCount += itemStack.getAmount();
                    }
                }
                if (haveBox || shineCount > 0) {
                    Research research = FinalTechItems.SHINE.getResearch();
                    Optional<PlayerProfile> playerProfile = PlayerProfile.find(player);
                    boolean unlock;
                    if(research == null) {
                        unlock = true;
                    } else unlock = playerProfile.isPresent() && playerProfile.get().getResearches().contains(research);

                    int obtain = this.obtainCount.getOrDefault(player, 1);
                    if (!player.isFlying() && haveBox && !player.isDead() && unlock) {
                        Vector nowVector = player.getVelocity().clone();
                        Location nowLocation = player.getLocation();
                        Location newLocation = nowLocation.add(
                                FinalTech.getRandom().nextDouble() * this.baseTeleportRange * (1 + obtain * this.mulTeleportRange) * 2 - this.baseTeleportRange * (1 + obtain * this.mulTeleportRange),
                                0,
                                FinalTech.getRandom().nextDouble() * this.baseTeleportRange * (1 + obtain * this.mulTeleportRange) - this.baseTeleportRange * (1 + obtain * this.mulTeleportRange));
                        player.teleport(newLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
                        player.setVelocity(nowVector);

                        PlayerInventory playerInventory = player.getInventory();
                        int[] ints = JavaUtil.generateRandomInts(playerInventory.getSize() - playerInventory.getArmorContents().length);
                        for (int anInt : ints) {
                            if (ItemStackUtil.isItemNull(playerInventory.getItem(anInt))) {
                                playerInventory.setItem(anInt, ItemStackUtil.cloneItem(FinalTechItems.SHINE.getValidItem(), shineCount + 1));
                                break;
                            }
                        }
                        obtain++;
                        this.obtainCount.put(player, obtain);
                    }

                    int deathCount = this.deathCount.getOrDefault(player, 0);

                    TaskTicker.applyOrAddTo(new VoidCurse(this.baseEffectTime + deathCount * this.deathMulEffectTime + obtain * this.obtainMulEffectTime, 1), player, LivingEntity.class);

                    double health = player.getHealth();
                    double expectedHealth = Math.max(0, player.getHealth() - player.getMaxHealth() * this.damage);
                    entityDamageEvent.setDamage(player.getMaxHealth() * this.damage);
                    FinalTech.getInstance().getServer().getScheduler().runTaskLater(FinalTech.getInstance(), () -> {
                        if(player.isDead()) {
                            return;
                        }

                        if(player.getNoDamageTicks() == 0 || expectedHealth == 0 || player.getHealth() >= health) {
                            player.setHealth(0);
                            return;
                        }

                        player.setMaximumNoDamageTicks(this.liveTime);
                        player.setNoDamageTicks(Math.min(player.getNoDamageTicks() + this.liveTime - 11, this.liveTime - 1));

                        double nowHealth = player.getHealth();

                        if(nowHealth > expectedHealth) {
                            player.setHealth(Math.max(expectedHealth * 2 - nowHealth, 0));
                        }
                    }, 1);

                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent playerTeleportEvent) {
        Player player = playerTeleportEvent.getPlayer();
        Location location = player.getLocation();
        World world = location.getWorld();
        if(world == null || location.getY() < world.getMinHeight() || TaskTicker.has(player, LivingEntity.class, VoidCurse.ID)) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (FinalTechItems.SHINE.verifyItem(itemStack)) {
                    itemStack.setAmount(0);
                }
            }
        }
    }
}
