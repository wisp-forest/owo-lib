package com.glisco.owo.blockentity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A handler that executes the steps defined in a {@link LinearProcess}. Each object that is
 * supposed to run the process needs an instance of this, and each instance of this refers back
 * to the object it operates on
 *
 * @param <T> The type of object this executor operates on
 */
public class LinearProcessExecutor<T> {

    public static final int CANCEL_EVENT_INDEX = -1;
    public static final int FINISH_EVENT_INDEX = -2;

    private final T target;
    private final int processLength;

    private final Predicate<LinearProcessExecutor<T>> condition;
    private Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> eventTable;
    private Int2ObjectMap<ProcessStep<T>> processStepTable;

    private final Set<ProcessStep.Info<T>> activeSteps = new HashSet<>();

    private int processTick = 0;

    protected LinearProcessExecutor(T target, int processLength, Predicate<LinearProcessExecutor<T>> condition, Int2ObjectMap<ProcessStep<T>> serverStepTable) {
        this.target = target;
        this.processLength = processLength;
        this.condition = condition;
        this.eventTable = null;
        this.processStepTable = serverStepTable;
    }

    protected void configure(Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> eventTable, Int2ObjectMap<ProcessStep<T>> processStepTable) {
        this.eventTable = eventTable;
        this.processStepTable = processStepTable;
    }

    public void tick() {
        if (eventTable == null) throw new IllegalStateException("Illegal attempt to tick unconfigured executor");

        if (!running()) return;

        if (cancelIfAppropriate()) return;
        if (finishIfAppropriate()) return;

        int tableIndex = processTick - 1;

        if (eventTable.containsKey(tableIndex)) eventTable.get(tableIndex).accept(this, this.target);
        if (processStepTable.containsKey(tableIndex)) activeSteps.add(processStepTable.get(tableIndex).createInfo(tableIndex));

        activeSteps.removeIf(stepInfo -> !stepInfo.tick(this));

        this.processTick++;
    }

    /**
     * Attempts to begin execution
     *
     * @return {@code true} if execution will start next tick,
     * {@code false} if execution is already running
     */
    public boolean begin() {
        if (this.processTick != 0) return false;

        this.processTick = 1;
        return true;
    }

    /**
     * @return {@code true} if this executor is currently running
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean running() {
        return this.processTick > 0;
    }

    /**
     * @return The last processing tick this executor completed
     */
    public int getProcessTick() {
        return processTick;
    }

    /**
     * @return The object this executor is operating on
     */
    public T getTarget() {
        return target;
    }

    /**
     * Attempts to instantly cancel execution
     *
     * @return {@code true} if execution was successfully cancelled,
     * {@code false} if this executor was not running
     */
    public boolean cancel() {
        if (!this.running()) return false;

        this.processTick = 0;
        this.activeSteps.clear();

        if (this.eventTable.containsKey(CANCEL_EVENT_INDEX)) this.eventTable.get(CANCEL_EVENT_INDEX).accept(this, this.target);

        return true;
    }

    private boolean finishIfAppropriate() {
        if (!this.running()) return false;
        if (this.processTick < processLength) return false;

        if (this.eventTable.containsKey(FINISH_EVENT_INDEX)) this.eventTable.get(FINISH_EVENT_INDEX).accept(this, this.target);

        this.processTick = 0;
        this.activeSteps.clear();
        return true;
    }

    private boolean cancelIfAppropriate() {
        if (this.condition.test(this)) return false;
        cancel();
        return true;
    }

    /**
     * Saves the state of this executor
     *
     * @param targetTag The nbt to write state into
     */
    public void writeState(NbtCompound targetTag) {
        targetTag.putInt("ProcessTick", processTick);
    }

    /**
     * Restores the saved state of this executor
     *
     * @param targetTag The nbt to read state from
     */
    public void readState(NbtCompound targetTag) {
        this.processTick = targetTag.getInt("ProcessTick");

        activeSteps.clear();
        processStepTable.forEach((index, step) -> {
            if (processTick >= index && processTick <= index + step.length) {
                activeSteps.add(step.createInfo(index, processTick - index));
            }
        });
    }

    @ApiStatus.Internal
    public static final record ProcessStep<T>(int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {

        public Info<T> createInfo(int index) {
            return new Info<>(index, this);
        }

        public Info<T> createInfo(int index, int tick) {
            return new Info<>(index, tick, this);
        }

        public static final class Info<T> {

            private final ProcessStep<T> step;
            private final int index;

            private int tick = 0;

            public Info(int index, ProcessStep<T> step) {
                this.index = index;
                this.step = step;
            }

            public Info(int index, int tick, ProcessStep<T> step) {
                this.index = index;
                this.tick = tick;
                this.step = step;
            }

            public boolean tick(LinearProcessExecutor<T> target) {
                this.tick++;
                if (this.tick == step.length) return false;

                this.step.executor.accept(target, target.getTarget());

                return true;
            }
        }
    }
}
