package com.glisco.owo.blockentity;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.function.Consumer;

public class LinearProcess<T> {

    private final Int2ObjectMap<Consumer<LinearProcessExecutor<T>>> clientEventTable = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinearProcessExecutor.ProcessStep<T>> clientProcessStepTable = new Int2ObjectOpenHashMap<>();

    private final Int2ObjectMap<Consumer<LinearProcessExecutor<T>>> serverEventTable = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinearProcessExecutor.ProcessStep<T>> serverProcessStepTable = new Int2ObjectOpenHashMap<>();

    private final int processLength;
    private boolean finished = false;

    public LinearProcess(int processLength) {
        this.processLength = processLength;
    }

    public LinearProcessExecutor<T> deriveExecutor(T target, boolean client) {
        if (!finished) throw new IllegalStateException("Illegal attempt to derive executor from unfinished process");

        if (client) {
            return new LinearProcessExecutor<>(target, processLength, clientEventTable, clientProcessStepTable);
        } else {
            return new LinearProcessExecutor<>(target, processLength, serverEventTable, serverProcessStepTable);
        }
    }

    public void addCommonStep(int when, int length, Consumer<LinearProcessExecutor<T>> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        clientProcessStepTable.put(when, step);
        serverProcessStepTable.put(when, step);
    }

    public void addClientStep(int when, int length, Consumer<LinearProcessExecutor<T>> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        clientProcessStepTable.put(when, step);
    }

    public void addServerStep(int when, int length, Consumer<LinearProcessExecutor<T>> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        serverProcessStepTable.put(when, step);
    }

    public void addCommonEvent(int when, Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(when, clientEventTable, executor);
        eventAtIndex(when, serverEventTable, executor);
    }

    public void addClientEvent(int when, Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(when, clientEventTable, executor);
    }

    public void addServerEvent(int when, Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(when, serverEventTable, executor);
    }

    public void whenFinishedCommon(Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, clientEventTable, executor);
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, serverEventTable, executor);
    }

    public void whenFinishedServer(Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, serverEventTable, executor);
    }

    public void whenFinishedClient(Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, clientEventTable, executor);
    }

    public void onCancelledCommon(Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, clientEventTable, executor);
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, serverEventTable, executor);
    }

    public void onCancelledServer(Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, serverEventTable, executor);
    }

    public void onCancelledClient(Consumer<LinearProcessExecutor<T>> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, clientEventTable, executor);
    }

    public void finish() {
        this.finished = true;
    }

    private void checkForIllegalModification() {
        if (finished) throw new IllegalStateException("Illegal attempt to modify finished process");
    }

    private void eventAtIndex(int index, Int2ObjectMap<Consumer<LinearProcessExecutor<T>>> eventTable, Consumer<LinearProcessExecutor<T>> executor) {
        checkForIllegalModification();
        eventTable.put(index, executor);
    }

}
