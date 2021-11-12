package io.wispforest.owo.registration;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An action to be executed by a {@link RegistryHelper} if and only if
 * all of it's required entries are present in that helper's registry
 *
 * @see ComplexRegistryAction.Builder#create(Runnable)
 */
@SuppressWarnings("ClassCanBeRecord")
public class ComplexRegistryAction {

    private final List<Identifier> predicates;
    private final Runnable action;

    protected ComplexRegistryAction(List<Identifier> predicates, Runnable action) {
        this.predicates = predicates;
        this.action = action;
    }

    protected <T> boolean preCheck(Registry<T> registry) {
        predicates.removeIf(registry::containsId);
        if (!predicates.isEmpty()) return false;

        action.run();
        return true;
    }

    protected boolean update(Identifier id) {
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

        /**
         * Creates a new builder to link the provided action
         * to a list of identifiers
         *
         * @param action The action to run once all identifiers are found in the targeted registry
         * @see #entry(Identifier)
         * @see #entries(Collection)
         */
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

        /**
         * Creates a registry action that can get run by a {@link RegistryHelper} once all the entries
         * added via this builder are found in the target registry
         *
         * @return The built action
         */
        public ComplexRegistryAction build() {
            if (predicates.isEmpty()) throw new IllegalStateException("Predicate list must not be empty");
            return new ComplexRegistryAction(predicates, action);
        }

    }

}
