package com.glisco.owo.blockentity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class LinearProcessExecutor<T> {

    public static final int CANCEL_EVENT_INDEX = -1;
    public static final int FINISH_EVENT_INDEX = -2;

    private final T target;
    private final int processLength;

    private final Int2ObjectMap<Consumer<LinearProcessExecutor<T>>> eventTable;
    private final Int2ObjectMap<ProcessStep<T>> processStepTable;

    private final Set<ProcessStep<T>> activeSteps = new HashSet<>();

    private int processTick = 0;

    protected LinearProcessExecutor(T target, int processLength, Int2ObjectMap<Consumer<LinearProcessExecutor<T>>> eventTable, Int2ObjectMap<ProcessStep<T>> processStepTable) {
        this.target = target;
        this.processLength = processLength;
        this.eventTable = eventTable;
        this.processStepTable = processStepTable;
    }

    public void tick() {
        if (!running()) return;
        if (finishIfAppropriate()) return;

        if (eventTable.containsKey(processTick)) eventTable.get(processTick).accept(this);
        if (processStepTable.containsKey(processTick)) activeSteps.add(processStepTable.get(processTick));

        activeSteps.removeIf(step -> step.tick(this));

        this.processTick++;
    }

    public boolean begin() {
        if (this.processTick != 0) return false;

        this.processTick = 1;
        return true;
    }

    public boolean running() {
        return this.processTick > 0;
    }

    public int getProcessTick() {
        return processTick;
    }

    public T getTarget() {
        return target;
    }

    public boolean finishIfAppropriate() {
        if (!this.running()) return false;
        if (this.processTick < processLength) return false;

        if (this.eventTable.containsKey(FINISH_EVENT_INDEX)) this.eventTable.get(FINISH_EVENT_INDEX).accept(this);

        this.processTick = 0;
        this.activeSteps.clear();
        return true;
    }

    public boolean cancel() {
        if (!this.running()) return false;

        if (this.eventTable.containsKey(CANCEL_EVENT_INDEX)) this.eventTable.get(CANCEL_EVENT_INDEX).accept(this);

        this.processTick = 0;
        this.activeSteps.clear();
        return true;
    }

    public void writeData(NbtCompound targetTag) {
        targetTag.putInt("ProcessTick", processTick);
    }

    public void readData(NbtCompound targetTag) {
        this.processTick = targetTag.getInt("ProcessTick");
    }

    @ApiStatus.Internal
    public static class ProcessStep<T> {

        private final int length;
        private final Consumer<LinearProcessExecutor<T>> executor;

        private int processTick = 0;

        protected ProcessStep(int length, Consumer<LinearProcessExecutor<T>> executor) {
            this.length = length;
            this.executor = executor;
        }

        private boolean tick(LinearProcessExecutor<T> target) {
            this.processTick++;
            if (this.processTick == length) return false;

            this.executor.accept(target);

            return true;
        }
    }
}
