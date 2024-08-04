package io.wispforest.owo.blockentity;

import io.wispforest.owo.blockentity.LinearProcessExecutor.ProcessStep;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.world.level.Level;

/**
 * Represents a process made of steps than can be executed tick by tick using a respective
 * {@link LinearProcessExecutor}. This can, for example, be used on BlockEntities that perform
 * rituals or similar activities that are made of consecutive steps.
 * <p>
 * A process defines the pattern of steps and events that shall be followed, thus there is one (usually static)
 * instance of it. You then create a new instance of {@link LinearProcessExecutor} using the
 * {@link #createExecutor(Object)} method for each instance of your BlockEntity of whatever else if supposed to run it
 * <p>
 * To create a new process, call {@link #LinearProcess(int)} with the length it should have. A process always has the same
 * length. Then, in the constructor of each object that will use an executor, use {@link #createExecutor(Object)} to
 * obtain an instance. This then has to be told whether it lives on the client or server using
 * {@link #configureExecutor(LinearProcessExecutor, boolean)}. On a BlockEntity this can be achieved by overriding
 * {@link net.minecraft.world.level.block.entity.BlockEntity#setLevel(Level)} and configuring after the super call using the provided
 * world
 * <p>
 * Steps and events should be added to process once, ideally in the {@code static} initializer block of the containing class.
 * After the process is complete, call {@link #finish()} to prevent further changes
 *
 * @param <T> The type of object this process will be executed on,
 *            a {@link net.minecraft.world.level.block.entity.BlockEntity} in most cases
 */
public class LinearProcess<T> {

    private final Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> clientEventTable = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinearProcessExecutor.ProcessStep<T>> clientProcessStepTable = new Int2ObjectOpenHashMap<>();

    private final Int2ObjectMap<BiConsumer<LinearProcessExecutor<T>, T>> serverEventTable = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinearProcessExecutor.ProcessStep<T>> serverProcessStepTable = new Int2ObjectOpenHashMap<>();

    private Predicate<LinearProcessExecutor<T>> condition = tLinearProcessExecutor -> true;

    private final int processLength;
    private boolean finished = false;

    /**
     * Creates a new process
     *
     * @param processLength The length of the process. This is immutable
     */
    public LinearProcess(int processLength) {
        this.processLength = processLength;
    }

    /**
     * Creates a new executor for the given target object
     *
     * @param target The object the executor should operate on
     * @return The created executor. This is not ready for use yet
     * @see #configureExecutor(LinearProcessExecutor, boolean)
     */
    public LinearProcessExecutor<T> createExecutor(T target) {
        if (!finished) throw new IllegalStateException("Illegal attempt to create executor for unfinished process");
        return new LinearProcessExecutor<>(target, processLength, condition, serverProcessStepTable);
    }

    /**
     * Configures an executor to use either the
     * server or client instructions
     *
     * @param executor The executor to configure
     * @param client   {@code true} if the client instructions should be used
     */
    public void configureExecutor(LinearProcessExecutor<T> executor, boolean client) {
        if (!finished) throw new IllegalStateException("Illegal attempt to configure executor using unfinished process");

        if (client) {
            executor.configure(clientEventTable, clientProcessStepTable);
        } else {
            executor.configure(serverEventTable, serverProcessStepTable);
        }
    }

    /**
     * Adds a new step to this process on both client and server
     *
     * @param when     When the step should start
     * @param length   How long it should last
     * @param executor The code to be run each tick while the step is active
     */
    public void addCommonStep(int when, int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        clientProcessStepTable.put(when, step);
        serverProcessStepTable.put(when, step);
    }

    /**
     * @see #addCommonStep(int, int, BiConsumer)
     */
    public void addClientStep(int when, int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        clientProcessStepTable.put(when, step);
    }

    /**
     * @see #addCommonStep(int, int, BiConsumer)
     */
    public void addServerStep(int when, int length, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        checkForIllegalModification();
        var step = new LinearProcessExecutor.ProcessStep<>(length, executor);
        serverProcessStepTable.put(when, step);
    }

    /**
     * Adds an event that is executed once, on both client and server
     *
     * @param when     When the event should occur
     * @param executor The code to be run on the given tick
     * @see #addClientEvent(int, BiConsumer)
     * @see #addServerEvent(int, BiConsumer)
     */
    public void addCommonEvent(int when, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(when, clientEventTable, executor);
        eventAtIndex(when, serverEventTable, executor);
    }

    /**
     * @see #addCommonEvent(int, BiConsumer)
     */
    public void addClientEvent(int when, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(when, clientEventTable, executor);
    }

    /**
     * @see #addCommonEvent(int, BiConsumer)
     */
    public void addServerEvent(int when, BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(when, serverEventTable, executor);
    }

    /**
     * Defines code to be run when this process has successfully
     * finished, on both client and server
     *
     * @param executor The code to be run
     * @see #whenFinishedClient(BiConsumer)
     * @see #whenFinishedServer(BiConsumer)
     */
    public void whenFinishedCommon(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, clientEventTable, executor);
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, serverEventTable, executor);
    }

    /**
     * @see #whenFinishedCommon(BiConsumer)
     */
    public void whenFinishedServer(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, serverEventTable, executor);
    }

    /**
     * @see #whenFinishedCommon(BiConsumer)
     */
    public void whenFinishedClient(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.FINISH_EVENT_INDEX, clientEventTable, executor);
    }

    /**
     * Defines code to be run on both client and server when this process
     * is unexpectedly cancelled mid-execution, use this to clean up after you.
     *
     * @param executor The code to be run
     * @see #onCancelledClient(BiConsumer)
     * @see #onCancelledServer(BiConsumer)
     */
    public void onCancelledCommon(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, clientEventTable, executor);
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, serverEventTable, executor);
    }

    /**
     * @see #onCancelledCommon(BiConsumer)
     */
    public void onCancelledServer(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, serverEventTable, executor);
    }

    /**
     * @see #onCancelledCommon(BiConsumer)
     */
    public void onCancelledClient(BiConsumer<LinearProcessExecutor<T>, T> executor) {
        eventAtIndex(LinearProcessExecutor.CANCEL_EVENT_INDEX, clientEventTable, executor);
    }

    /**
     * Defines a condition that has to be met every tick this process runs,
     * otherwise it cancels itself
     *
     * @param condition The condition that should be satisfied during the entire
     *                  process execution
     */
    public void runConditionally(Predicate<LinearProcessExecutor<T>> condition) {
        this.condition = condition;
    }

    /**
     * Marks this process and completely built and ready for execution
     */
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
