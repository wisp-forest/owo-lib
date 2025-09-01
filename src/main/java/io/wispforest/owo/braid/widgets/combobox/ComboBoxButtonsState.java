package io.wispforest.owo.braid.widgets.combobox;

import java.util.List;
import java.util.OptionalInt;

record ComboBoxButtonsState<T>(List<T> options, OptionalInt highlightedOptionIdx) {}
