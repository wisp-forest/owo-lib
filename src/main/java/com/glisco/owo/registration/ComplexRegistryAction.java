package com.glisco.owo.registration;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ComplexRegistryAction {

    private final List<Identifier> predicates;
    private final Runnable action;

    public ComplexRegistryAction(List<Identifier> predicates, Runnable action) {
        this.predicates = predicates;
        this.action = action;
    }

    public <T> boolean preCheck(Registry<T> registry) {
        predicates.removeIf(registry::containsId);
        if (!predicates.isEmpty()) return false;

        action.run();
        return true;
    }

    public boolean update(Identifier id) {
        predicates.remove(id);
        if (!predicates.isEmpty()) return false;

        action.run();
        return true;
    }

    public static class Builder {

        private final Runnable action;
        private final List<Identifier> predicates;

        private Builder(Runnable action) {
            this.action = action;
            this.predicates = new ArrayList<>();
        }

        public static Builder create(Runnable action) {
            return new Builder(action);
        }

        public Builder entry(Identifier id) {
            this.predicates.add(id);
            return this;
        }

        public Builder entries(Collection<Identifier> ids) {
            this.predicates.addAll(ids);
            return this;
        }

        public ComplexRegistryAction build() {
            if (predicates.isEmpty()) throw new IllegalStateException("Predicate list must not be empty");
            return new ComplexRegistryAction(predicates, action);
        }

    }

}
