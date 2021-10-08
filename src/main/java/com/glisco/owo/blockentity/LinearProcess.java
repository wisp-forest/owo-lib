package com.glisco.owo.blockentity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class LinearProcess<T> {

    private final Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> clientEventTable = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinearProcessExecutor.ProcessStep<T>> clientProcessStepTable = new Int2ObjectOpenHashMap<>();

    private final Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> serverEventTable = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinearProcessExecutor.ProcessStep<T>> serverProcessStepTable = new Int2ObjectOpenHashMap<>();

    private Predicate<LinearProcessExecutor<T>> condition = tLinearProcessExecutor -> true;

    private final int processLength;
    private boolean finished = false;

    public LinearProcess(int processLength) {
        this.processLength = processLength;
    }

    public void reconfigureExecutor(LinearProcessExecutor<T> executor, boolean client) {
        if (!finished) throw new IllegalStateException("Illegal attempt to derive executor from unfinished process");

        if (client) {
            executor.reconfigure(clientEventTable, clientProcessStepTable);
        } else {
            executor.reconfigure(serverEventTable, serverProcessStepTable);
        }
    }

    public LinearProcessExecutor<T> deriveExecutor(T target, boolean client) {
        if (!finished) throw new IllegalStateException("Illegal attempt to derive executor from unfinished process");

        if (client) {
            return new LinearProcessExecutor<>(target, processLength, condition, clientEventTable, clientProcessStepTable);
        } else {
            return new LinearProcessExecutor<>(target, processLength, condition, serverEventTable, serverProcessStepTable);
        }
    }

    public void addCommonStep(int when, int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        clientProcessStepTable.put(when, step);
        serverProcessStepTable.put(when, step);
    }

    public void addClientStep(int when, int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        clientProcessStepTable.put(when, step);
    }

    public void addServerStep(int when, int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        serverProcessStepTable.put(when, step);
    }

    public void addCommonEvent(int when, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(when, clientEventTable, executor);
        eventAtIndex(when, serverEventTable, executor);
    }

    public void addClientEvent(int when, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(when, clientEventTable, executor);
    }

    public void addServerEvent(int when, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(when, serverEventTable, executor);
    }

    public void whenFinishedCommon(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, clientEventTable, executor);
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, serverEventTable, executor);
    }

    public void whenFinishedServer(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, serverEventTable, executor);
    }

    public void whenFinishedClient(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, clientEventTable, executor);
    }

    public void onCancelledCommon(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, clientEventTable, executor);
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, serverEventTable, executor);
    }

    public void onCancelledServer(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, serverEventTable, executor);
    }

    public void onCancelledClient(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, clientEventTable, executor);
    }

    public void runConditionally(Predicate<LinearProcessExecutor<T>> condition) {
        this.condition = condition;
    }

    public void finish() {
        this.finished = true;
    }

    private void checkForIllegalModification() {
        if (finished) throw new IllegalStateException("Illegal attempt to modify finished process");
    }

    private void eventAtIndex(int index, Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> eventTable, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        eventTable.put(index, executor);
    }

}
