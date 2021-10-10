package com.glisco.owo.blockentity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

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

    protected LinearProcessExecutor(T target, int processLength, Predicate<LinearProcessExecutor<T>> condition, Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> eventTable, Int2ObjectMap<ProcessStep<T>> processStepTable) {
        this.target = target;
        this.processLength = processLength;
        this.condition = condition;
        this.eventTable = eventTable;
        this.processStepTable = processStepTable;
    }

    protected void reconfigure(Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> eventTable, Int2ObjectMap<ProcessStep<T>> processStepTable) {
        this.eventTable = eventTable;
        this.processStepTable = processStepTable;
    }

    public void tick() {
        if (!running()) return;

        if (cancelIfAppropriate()) return;
        if (finishIfAppropriate()) return;

        int tableIndex = processTick - 1;

        if (eventTable.containsKey(tableIndex)) eventTable.get(tableIndex).accept(this, this.target);
        if (processStepTable.containsKey(tableIndex)) activeSteps.add(processStepTable.get(tableIndex).createInfo(tableIndex));

        activeSteps.removeIf(stepInfo -> !stepInfo.tick(this));

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

    public void writeState(NbtCompound targetTag) {
        targetTag.putInt("ProcessTick", processTick);
    }

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
