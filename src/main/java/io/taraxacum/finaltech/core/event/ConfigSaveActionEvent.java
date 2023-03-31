package io.taraxacum.finaltech.core.event;

import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

/**
 * This event may be called in sync or async.
 * This event is used to update {@link me.mrCookieSlime.Slimefun.api.inventory.BlockMenu}
 * @see io.taraxacum.finaltech.core.listener.ConfigSaveListener
 * @see io.taraxacum.finaltech.core.item.usable.MachineConfigurator
 * @see io.taraxacum.finaltech.core.item.machine.range.ConfigurationPaster
 * @author Final_ROOT
 * @since 2.4
 */
public class ConfigSaveActionEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Location location;

    private final String id;

    public ConfigSaveActionEvent(boolean async, Location location, String id) {
        super(async);

        Validate.notNull(location, "The location cannot be null!");
        Validate.notNull(id, "id cannot be null");

        this.location = location;
        this.id = id;
    }
    public Location getLocation() {
        return location;
    }

    public String getId() {
        return id;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }


    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
