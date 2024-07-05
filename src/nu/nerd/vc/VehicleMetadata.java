package nu.nerd.vc;

import org.bukkit.metadata.MetadataValueAdapter;

// ------------------------------------------------------------------------
/**
 * Metadata set on vehicles that will break in the future.
 *
 * The metadata records the system time at which the vehicle is due to break,
 * and whether that was computed on the basis of it being empty (in which case,
 * a new passenger could extend the timeout, or invalidate it).
 */
public class VehicleMetadata extends MetadataValueAdapter {
    // --------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param occupied true if the vehicle is occupied.
     * @parma timeOut the system time at which the vehicle should break.
     */
    protected VehicleMetadata(boolean occupied, long timeOut) {
        super(VehicleControl.PLUGIN);
        _occupied = occupied;
        _timeOut = timeOut;
    }

    // --------------------------------------------------------------------
    /**
     * @see org.bukkit.metadata.MetadataValue#invalidate()
     *
     *      We don't implement the cache invalidation semantics. Metadata is
     *      trivially computed.
     */
    @Override
    public void invalidate() {
    }

    // --------------------------------------------------------------------
    /**
     * @see org.bukkit.metadata.MetadataValue#value()
     */
    @Override
    public Object value() {
        return _timeOut;
    }

    // --------------------------------------------------------------------
    /**
     * Update the occupied state and timeout time stamp.
     *
     * @param occupied true if the vehicle is occupied.
     * @param timeOut the system time when the vehicle should break.
     */
    public void update(boolean occupied, long timeOut) {
        _occupied = occupied;
        _timeOut = timeOut;
    }

    // --------------------------------------------------------------------
    /**
     * Return true if the metadata was set when the vehicle was occupied by a
     * vulnerable passenger.
     *
     * @return true if the vehicle was occupied.
     */
    public boolean isOccupied() {
        return _occupied;
    }

    // --------------------------------------------------------------------
    /**
     * Return the system time when the vehicle should break.
     *
     * @return the system time when the vehicle should break.
     */
    public long getTimeOut() {
        return _timeOut;
    }

    // --------------------------------------------------------------------
    /**
     * The system time stamp at which the vehicle should drop.
     */
    private long _timeOut;

    /**
     * If true, the vehicle was occupied.
     */
    private boolean _occupied;
} // class VehicleMetadata