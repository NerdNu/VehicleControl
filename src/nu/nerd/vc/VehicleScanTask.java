package nu.nerd.vc;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

// ----------------------------------------------------------------------------
/**
 * The task that scans for vehicles and removes them.
 *
 * Where possible, the system time is used for measuring the lifetime of
 * vehicles as it is immune to slow tick rates and works even when the daylight
 * cycle is shut off.
 */
public class VehicleScanTask implements Runnable {
    // ------------------------------------------------------------------------
    /**
     * Schedule the next run of this task.
     */
    public void scheduleNextRun() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(VehicleControl.PLUGIN,
                                                      this,
                                                      20 * VehicleControl.CONFIG.SCAN_PERIOD_SECONDS);
    }

    // ------------------------------------------------------------------------
    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        long startNanos = System.nanoTime();
        long now = System.currentTimeMillis();
        for (World world : VehicleControl.CONFIG.SCAN_WORLDS) {
            scanWorld(world, now);
        }

        if (VehicleControl.CONFIG.DEBUG_OVERHEAD) {
            double elapsedMillis = (System.nanoTime() - startNanos) * 1e-6;
            VehicleControl.PLUGIN.getLogger().info("Scan task took " + elapsedMillis + " milliseconds");
        }

        scheduleNextRun();
    } // run

    // ------------------------------------------------------------------------
    /**
     * Perform all required tasks in the specified world.
     *
     * @param world the affected world.
     * @param now the current system time.
     */
    protected void scanWorld(World world, long now) {
        for (Boat boat : world.getEntitiesByClass(Boat.class)) {
            checkVehicle(boat, now);
        }

        for (RideableMinecart cart : world.getEntitiesByClass(RideableMinecart.class)) {
            checkVehicle(cart, now);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Update {@link VehicleMetadata} on a vehicle and break the vehicle if
     * required.
     *
     * @param vehicle the vehicle.
     * @param now the current system time.
     */
    protected void checkVehicle(Vehicle vehicle, long now) {
        VehicleMetadata meta = getVehicleMetadata(vehicle);
        Entity passenger = vehicle.getPassenger();
        if (meta == null) {
            // If not tagged, tag the vehicle if it will break and we're done.
            if (vehicle.isEmpty()) {
                if (VehicleControl.CONFIG.VEHICLES_BREAK_EMPTY) {
                    vehicle.setMetadata(VEHICLE_META_KEY,
                                        new VehicleMetadata(false,
                                            now + MILLIS * VehicleControl.CONFIG.VEHICLES_BREAK_EMPTY_SECONDS));
                }
            } else if (isBreakable(vehicle.getPassenger())) {
                vehicle.setMetadata(VEHICLE_META_KEY,
                                    new VehicleMetadata(true,
                                        now + MILLIS * VehicleControl.CONFIG.VEHICLES_BREAK_WITH_PASSENGER_SECONDS));
            } else {
                // Won't be tagged as scheduled for a break. Log exemption.
                if (VehicleControl.CONFIG.DEBUG_EXEMPT_VEHICLE) {
                    StringBuilder message = new StringBuilder();
                    message.append("Exempted ").append(vehicle.getType().name());
                    message.append(" at ").append(formatLoc(vehicle.getLocation()));
                    message.append(", passenger ").append(passenger.getType().name());
                    String customName = passenger.getCustomName();
                    if (customName != null) {
                        message.append(" custom name \"").append(customName).append("\"");
                    }
                    VehicleControl.PLUGIN.getLogger().info(message.toString());
                }
            }
        } else {
            // Vehicle is already tagged.
            if (now >= meta.getTimeOut()) {
                if (passenger == null) {
                    // Currently empty and timed out.
                    breakVehicle(vehicle);
                } else if (isBreakable(passenger)) {
                    if (meta.isOccupied()) {
                        // Still occupied by a breakable passenger.
                        breakVehicle(vehicle);
                    } else {
                        // Extend timeout: passenger boarded since tagging.
                        meta.update(true, now + MILLIS * VehicleControl.CONFIG.VEHICLES_BREAK_WITH_PASSENGER_SECONDS);
                    }
                } else {
                    // Passenger doesn't allow break. Remove timeout.
                    vehicle.removeMetadata(VEHICLE_META_KEY, VehicleControl.PLUGIN);
                }
            }
        }
    } // processVehicle

    // ------------------------------------------------------------------------
    /**
     * Break the vehicle, dropping the item if required and logging the action.
     *
     * @param vehicle the vehicle.
     */
    protected void breakVehicle(Vehicle vehicle) {
        Location loc = vehicle.getLocation();
        World world = loc.getWorld();

        Material dropMaterial = null;
        if (VehicleControl.CONFIG.VEHICLES_DROP_ITEM) {
            if (vehicle.getType() == EntityType.BOAT) {
                Boat boat = (Boat) vehicle;
                dropMaterial = BOAT_DROP_TABLE[boat.getWoodType().ordinal()];
            } else if (vehicle.getType() == EntityType.MINECART) {
                dropMaterial = Material.MINECART;
            }

            world.dropItem(loc, new ItemStack(dropMaterial, 1));
        }

        if (VehicleControl.CONFIG.DEBUG_BREAK_VEHICLE) {
            StringBuilder message = new StringBuilder();
            message.append("Breaking ").append(vehicle.getType().name());
            message.append(" at ").append(formatLoc(loc));
            if (dropMaterial != null) {
                message.append(" dropping ").append(dropMaterial.name());
            }
            Entity passenger = vehicle.getPassenger();
            if (passenger == null) {
                message.append(", no passenger");
            } else {
                message.append(", passenger ").append(passenger.getType().name());
            }
            VehicleControl.PLUGIN.getLogger().info(message.toString());
        }
        vehicle.remove();
    } // breakVehicle

    // ------------------------------------------------------------------------
    /**
     * Return true if the passenger would allow a vehicle to be broken.
     *
     * @param passenger the passenger, which must be non-null.
     * @return true if the passenger would allow a vehicle to be broken.
     */
    protected boolean isBreakable(Entity passenger) {
        return VehicleControl.CONFIG.VEHICLES_BREAK_WITH_PASSENGER &&
               VehicleControl.CONFIG.VEHICLES_BREAK_WITH_PASSENGER_TYPES.contains(passenger.getType()) &&
               !hasExemptedTypeAndName(passenger);
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if the passenger is of a type that would be exempted if named
     * and has a custom name.
     *
     * @param passenger the passenger, which must be non-null.
     * @return true if the passenger is of a type that would be exempted if
     *         named and has a custom name.
     */
    protected boolean hasExemptedTypeAndName(Entity passenger) {
        return VehicleControl.CONFIG.VEHICLES_EXEMPT_WITH_NAMED_PASSENGER &&
               passenger.getCustomName() != null &&
               VehicleControl.CONFIG.VEHICLES_EXEMPT_WITH_NAMED_PASSENGER_TYPES.contains(passenger.getType());
    }

    // ------------------------------------------------------------------------
    /**
     * Get the VehicleMetadata of the vehicle.
     *
     * @param vehicle the vehicle.
     * @return the VehicleMetadata, or null if not set.
     */
    protected VehicleMetadata getVehicleMetadata(Entity vehicle) {
        List<MetadataValue> meta = vehicle.getMetadata(VEHICLE_META_KEY);
        return (meta.size() != 0) ? (VehicleMetadata) meta.get(0) : null;
    }

    // ------------------------------------------------------------------------
    /**
     * Format a Location as a string containing integer coordinates.
     *
     * @param loc the location.
     * @return the location as a string.
     */
    protected String formatLoc(Location loc) {
        return loc.getWorld().getName() + ", " +
               loc.getBlockX() + ", " +
               loc.getBlockY() + ", " +
               loc.getBlockZ();
    }

    // ------------------------------------------------------------------------
    /**
     * Conversion factor from seconds to milliseconds.
     */
    private static final long MILLIS = 1000;

    /**
     * Look up table mapping TreeSpecies ordinal (boat type) to corresponding
     * dropped boat item type.
     */
    private static final Material BOAT_DROP_TABLE[] = { Material.BOAT, Material.BOAT_SPRUCE,
                                                       Material.BOAT_BIRCH, Material.BOAT_JUNGLE,
                                                       Material.BOAT_ACACIA, Material.BOAT_DARK_OAK };

    /**
     * Metadata key for storing the VehicleMetadata on a vehicle.
     */
    private static final String VEHICLE_META_KEY = "VC_Meta";
} // class VehicleScanTask