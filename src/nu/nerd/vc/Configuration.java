package nu.nerd.vc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

// ----------------------------------------------------------------------------
/**
 * Configuration wrapper.
 */
public class Configuration {
    /**
     * If true, log the configuration on load.
     */
    public boolean DEBUG_CONFIGURATION;

    /**
     * If true, log the time taken to run the scanning task.
     */
    public boolean DEBUG_OVERHEAD;

    /**
     * If true, log breaking of vehicles.
     */
    public boolean DEBUG_BREAK_VEHICLE;

    /**
     * If true, log vehicles that are exempt from breaking when scanned.
     */
    public boolean DEBUG_EXEMPT_VEHICLE;

    /**
     * The period, in seconds, between scans for carts and boats.
     */
    public int SCAN_PERIOD_SECONDS;

    /**
     * List of worlds that are scanned for vehicles to process.
     */
    public ArrayList<World> SCAN_WORLDS = new ArrayList<World>();

    /**
     * If true, remove carts and boats when the player exits.
     *
     * They will not drop as an item. They simply vanish.
     */
    public boolean VEHICLES_REMOVE_ON_EXIT;

    /**
     * If true, drop the vehicle as an item when broken (otherwise, it simply
     * vanishes).
     */
    public boolean VEHICLES_DROP_ITEM;

    /**
     * If true, break boats or passenger carts that are empty.
     */
    public boolean VEHICLES_BREAK_EMPTY;

    /**
     * The minimum period, in seconds, that an empty vehicle can persist before
     * breaking.
     */
    public int VEHICLES_BREAK_EMPTY_SECONDS;

    /**
     * If true, break vehicles with passengers of specified types.
     */
    public boolean VEHICLES_BREAK_WITH_PASSENGER;

    /**
     * The minimum period, in seconds, that a vehicle with a mob passenger can
     * persist before it is broken automatically.
     */
    public int VEHICLES_BREAK_WITH_PASSENGER_SECONDS;

    /**
     * Types of passengers that are vulnerable to their vehicle breaking.
     */
    public Set<EntityType> VEHICLES_BREAK_WITH_PASSENGER_TYPES = new HashSet<EntityType>();

    /**
     * If true, protect vehicles with passengers of specified types if the
     * passengers are named.
     */
    public boolean VEHICLES_EXEMPT_WITH_NAMED_PASSENGER;

    /**
     * Types of otherwise vulnerable passengers that are exempted from their
     * vehicle breaking if they have been named.
     */
    public Set<EntityType> VEHICLES_EXEMPT_WITH_NAMED_PASSENGER_TYPES = new HashSet<EntityType>();

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        VehicleControl.PLUGIN.reloadConfig();

        DEBUG_CONFIGURATION = getConfig().getBoolean("debug.configuration");
        DEBUG_OVERHEAD = getConfig().getBoolean("debug.overhead");
        DEBUG_BREAK_VEHICLE = getConfig().getBoolean("debug.break-vehicle");
        DEBUG_EXEMPT_VEHICLE = getConfig().getBoolean("debug.exempt-vehicle");

        SCAN_PERIOD_SECONDS = getConfig().getInt("scan.period-seconds");
        SCAN_WORLDS.clear();
        for (String worldName : getConfig().getStringList("scan.worlds")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("There is no world named \"" + worldName + "\" to scan.");
            } else {
                SCAN_WORLDS.add(world);
            }
        }

        VEHICLES_REMOVE_ON_EXIT = getConfig().getBoolean("vehicles.remove-on-exit");
        VEHICLES_DROP_ITEM = getConfig().getBoolean("vehicles.drop-item");
        VEHICLES_BREAK_EMPTY = getConfig().getBoolean("vehicles.break-empty");
        VEHICLES_BREAK_EMPTY_SECONDS = getConfig().getInt("vehicles.break-empty-seconds");

        VEHICLES_BREAK_WITH_PASSENGER = getConfig().getBoolean("vehicles.break-with-passenger");
        VEHICLES_BREAK_WITH_PASSENGER_SECONDS = getConfig().getInt("vehicles.break-with-passenger-seconds");
        VEHICLES_BREAK_WITH_PASSENGER_TYPES.clear();
        for (String typeName : getConfig().getStringList("vehicles.break-with-passenger-types")) {
            try {
                VEHICLES_BREAK_WITH_PASSENGER_TYPES.add(EntityType.valueOf(typeName));
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Cannot break vehicles containing invalid entity type \"" + typeName + "\".");
            }
        }

        VEHICLES_EXEMPT_WITH_NAMED_PASSENGER = getConfig().getBoolean("vehicles.exempt-with-named-passenger");
        VEHICLES_EXEMPT_WITH_NAMED_PASSENGER_TYPES.clear();
        for (String typeName : getConfig().getStringList("vehicles.exempt-with-named-passenger-types")) {
            try {
                VEHICLES_EXEMPT_WITH_NAMED_PASSENGER_TYPES.add(EntityType.valueOf(typeName));
            } catch (IllegalArgumentException ex) {
                getLogger().warning("Cannot exempt invlid entity type \"" + typeName + "\" from vehicle breakage.");
            }
        }

        if (DEBUG_CONFIGURATION) {
            VehicleControl.PLUGIN.getLogger().info("Configuration: ");
            getLogger().info("DEBUG_OVERHEAD: " + DEBUG_OVERHEAD);
            getLogger().info("DEBUG_BREAK_VEHICLE: " + DEBUG_BREAK_VEHICLE);
            getLogger().info("DEBUG_EXEMPT_VEHICLE: " + DEBUG_EXEMPT_VEHICLE);

            getLogger().info("SCAN_PERIOD_SECONDS: " + SCAN_PERIOD_SECONDS);
            StringBuilder scannedWorlds = new StringBuilder();
            for (World world : SCAN_WORLDS) {
                scannedWorlds.append(' ').append(world.getName());
            }
            getLogger().info("SCAN_WORLDS:" + scannedWorlds.toString());

            getLogger().info("VEHICLES_REMOVE_ON_EXIT: " + VEHICLES_REMOVE_ON_EXIT);
            getLogger().info("VEHICLES_DROP_ITEM: " + VEHICLES_DROP_ITEM);
            getLogger().info("VEHICLES_BREAK_EMPTY: " + VEHICLES_BREAK_EMPTY);
            getLogger().info("VEHICLES_BREAK_EMPTY_SECONDS: " + VEHICLES_BREAK_EMPTY_SECONDS);

            StringBuilder breakTypes = new StringBuilder();
            for (EntityType type : VEHICLES_BREAK_WITH_PASSENGER_TYPES) {
                breakTypes.append(' ').append(type.name());
            }
            getLogger().info("VEHICLES_BREAK_WITH_PASSENGER: " + VEHICLES_BREAK_WITH_PASSENGER);
            getLogger().info("VEHICLES_BREAK_WITH_PASSENGER_SECONDS: " + VEHICLES_BREAK_WITH_PASSENGER_SECONDS);
            getLogger().info("VEHICLES_BREAK_WITH_PASSENGER_TYPES:" + breakTypes.toString());

            StringBuilder exemptTypes = new StringBuilder();
            for (EntityType type : VEHICLES_EXEMPT_WITH_NAMED_PASSENGER_TYPES) {
                exemptTypes.append(' ').append(type.name());
            }
            getLogger().info("VEHICLES_EXEMPT_WITH_NAMED_PASSENGER: " + VEHICLES_EXEMPT_WITH_NAMED_PASSENGER);
            getLogger().info("VEHICLES_EXEMPT_WITH_NAMED_PASSENGER_TYPES:" + exemptTypes.toString());

        }
    } // reload

    // ------------------------------------------------------------------------
    /**
     * Return the plugin's FileConfiguration instance.
     *
     * @return the plugin's FileConfiguration instance.
     */
    protected static FileConfiguration getConfig() {
        return VehicleControl.PLUGIN.getConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the plugin's Logger.
     *
     * @return the plugin's Logger.
     */
    protected static Logger getLogger() {
        return VehicleControl.PLUGIN.getLogger();
    }
} // class Configuration