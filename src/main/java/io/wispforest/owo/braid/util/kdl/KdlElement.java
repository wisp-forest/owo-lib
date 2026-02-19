package io.wispforest.owo.braid.util.kdl;

import dev.kdl.KdlNode;
import dev.kdl.KdlValue;

import java.util.List;

public sealed interface KdlElement {
    record KdlElementList(List<KdlElement> elements) implements KdlElement {}
    record KdlNodeElement(KdlNode node) implements KdlElement {}
    record KdlValueElement(KdlValue<?> value) implements KdlElement {}
}
