package io.wispforest.owo.braid.util.kdl;

import dev.kdl.KdlNode;
import dev.kdl.KdlString;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public record KdlMapper(Function<KdlNode, Boolean> export, String key, Function<KdlNode, KdlElement> get, BiFunction<KdlNode, KdlElement, KdlNode> set) {
    public static final List<KdlMapper> DEFAULT_MAPPERS = List.of(
        new KdlMapper(
            kdlNode -> true,
            "@name",
            node -> new KdlElement.KdlValueElement(new KdlString(node.name())),
            (node, element) -> node.mutate().name((String) ((KdlElement.KdlValueElement) element).value().value()).build()
        ),
        new KdlMapper(
            kdlNode -> kdlNode.arguments().size() == 1,
            "@argument",
            node -> !node.arguments().isEmpty() ? new KdlElement.KdlValueElement(node.arguments().getFirst()) : null,
            (node, element) -> node.mutate().argument(((KdlElement.KdlValueElement) element).value()).build()
        ),
        new KdlMapper(
            kdlNode -> kdlNode.arguments().size() > 1,
            "@arguments",
            node -> new KdlElement.KdlElementList(node.arguments().stream().<KdlElement>map(KdlElement.KdlValueElement::new).toList()),
            (node, element) -> {
                var builder = node.mutate();
                ((KdlElement.KdlElementList) element).elements()
                    .stream()
                    .map(kdlElement -> ((KdlElement.KdlValueElement) kdlElement).value())
                    .forEach(builder::argument);
                return builder.build();
            }
        ),
        new KdlMapper(
            kdlNode -> kdlNode.children().size() == 1,
            "@child",
            node -> {
                var candidates = node.children().stream().filter(kdlNode -> !kdlNode.name().startsWith(".")).toList();
                return !candidates.isEmpty() ? new KdlElement.KdlNodeElement(candidates.getFirst()) : null;
            },
            (node, element) -> node.mutate().child(((KdlElement.KdlNodeElement) element).node()).build()
        ),
        new KdlMapper(
            kdlNode -> kdlNode.children().size() > 1,
            "@children",
            node -> new KdlElement.KdlElementList(
                node.children().stream()
                    .filter(kdlNode -> !kdlNode.name().startsWith("."))
                    .<KdlElement>map(KdlElement.KdlNodeElement::new)
                    .toList()
            ),
            (node, element) -> {
                var builder = node.mutate();
                ((KdlElement.KdlElementList) element).elements().stream()
                    .map(kdlElement -> ((KdlElement.KdlNodeElement) kdlElement).node())
                    .forEach(builder::child);
                return builder.build();
            }
        )
    );
}
