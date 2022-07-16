package io.wispforest.uwu.persistence;

import io.wispforest.owo.persistence.OwoPersistentState;
import net.minecraft.nbt.NbtCompound;

public class UwuCounterState extends OwoPersistentState<UwuCounterState> {
    public static final String ID = "uwu_state";

    public int uwuws = 0;

    @Override
    public OwoPersistentState<UwuCounterState> gather(NbtCompound compound) {
        UwuCounterState state = new UwuCounterState();

        state.uwuws = compound.getInt("uwuCounts");
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putInt("uwuCounts", uwuws);
        return nbt;
    }
}
