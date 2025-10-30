package io.wispforest.owo.config.base;

import io.wispforest.owo.config.options.OptionBase;
import io.wispforest.owo.config.options.OptionControlSpec;

import java.util.function.Predicate;

///
/// [OptionConstraint] is a holder object used to primarily limit the setting of a given options value within
/// [OptionBase#verifyConstraint] using the given [applyPredicate][#applyPredicate] and allowing for the
/// ability when possible to define a user focused [inputPredicate][#inputPredicate]
///
@SuppressWarnings({"rawtypes", "unchecked"})
public record OptionConstraint<T>(String formatted, Predicate<T> applyPredicate, Predicate<String> inputPredicate) {

    public OptionConstraint(String formatted, Predicate<T> applyPredicate) {
        this(formatted, applyPredicate, s -> true);
    }
}
