package io.wispforest.owo.braid.widgets.slider.rangeXlyder;

/**
 * Callback interface for RawRangeXlyder that provides the new positions of both handles.
 * Each handle can move independently in 2D space.
 */
@FunctionalInterface
public interface RangeXlyderCallback {
    /**
     * Called when either handle position changes.
     *
     * @param newMinX the new X coordinate of the minimum handle
     * @param newMinY the new Y coordinate of the minimum handle
     * @param newMaxX the new X coordinate of the maximum handle
     * @param newMaxY the new Y coordinate of the maximum handle
     */
    void accept(double newMinX, double newMinY, double newMaxX, double newMaxY);
}
