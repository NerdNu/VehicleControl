VehicleControl
==============
Automatically break boats and minecarts.


Features
--------
The features of `VehicleControl` are a superset of equivalent, active
features that were removed from [`KitchenSink`](https://github.com/NerdNu/KitchenSink).
The option to remove special carts (chest, furnace and hopper carts) was not
implemented as it is no longer used on an nerd.nu servers.

 * Optionally break carts and boats when the player exits them.
 * Break empty passenger mincarts and boats after they have existed for a
   configurable amount of time, with the option to drop each as an item or not.
 * Optionally break passenger carts containing configured mob types.
 * Optionally exempt from breaking passenger carts containing configured mob
   types that have been named.


Principle
---------
Vehicles are scanned periodically and tagged with metadata if they must be
broken in the future. The metadata records whether the vehicle was empty when
tagged, and the system time stamp when the vehicle should break. Vehicles
that aren't eligible to break don't get tagged.

When the plugin re-scans a vehicle that is already tagged, it checks the
metadata:

 * If the vehicle was tagged while empty, but is now occupied by a vulnerable
   mob, then it gets an extension to the time limit for vehicles with
   vulnerable passengers.
 * If the vehicle was tagged as empty or with a vulnerable mob but is
   now occupied by an invulnerable entity (e.g. a player) then the metadata
   is cleared.
 * If the metadata matches the passenger (or lack thereof) and has reached its
   expiry time, the vehicle breaks, and drops if configured to do so.

Since vehicles must be scanned at least twice before they can be dropped,
configured time limits represent the minimum time that the vehicle will exist
in its current state. It will generally last a little longer before breaking,
depending on the phase of the scan task.

In the case of boats, the scanning process only considers the primary passenger.


Configuration
-------------

| Setting | Description |
| :--- | :--- |
| `debug.config` | If true, loaded configuration settings are logged. |
| `debug.overhead` | If true, log the time taken to run the scanning task. |
| `debug.break-vehicle` |  If true, log breaking of vehicles. |
| `debug.exempt-vehicle` | If true, log vehicles that are exempt from breaking when they are scanned. |
| `scan.period-seconds` | The period, in seconds, between scans for vehicles. |
| `scan.worlds` | The list of names of worlds that are scanned for vehicles. |
| `vehicles.remove-on-exit` | If true, remove carts and boats when the player exits. They will not drop as an item; they simply vanish. |
| `vehicles.drop-item` | If true, vehicles drop as an item when broken as part of the scanning process. Otherwise, they simply vanish. |
| `vehicles.break-empty` | If true, break boats or passenger carts that are empty. |
| `vehicles.break-empty-seconds` | The minimum period, in seconds, that an empty vehicle can persist before breaking. |
| `vehicles.break-with-passenger` | If true, break vehicles with passengers of specified types. |
| `vehicles.break-with-passenger-seconds` | The minimum period, in seconds, that a vehicle with a mob passenger can persist before it is broken automatically. |
| `vehicles.break-with-passenger-types` | Types of passengers that are vulnerable to their vehicle breaking. |
| `vehicles.exempt-with-named-passenger` | If true, protect vehicles with passengers of specified types if the passengers are named. |
| `vehicles.exempt-with-named-passenger-types` | Types of otherwise vulnerable passengers that are exempted from their vehicle breaking if they have been named. This setting carves out exemptions from the list of mob types in `vehicles.break-with-passenger-types`. That is, it is only necessary to exempt a mob type here if it has been previously explicitly listed as vulnerable. |


Commands
--------

 * `/vehiclecontrol reload` - Reload the configuration.


Permissions
-----------

 * `vehiclecontrol.admin` - Permission to run `/vehiclecontrol reload`.

