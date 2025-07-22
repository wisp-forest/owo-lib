package io.wispforest.owo.braid.core.events;

import java.nio.file.Path;
import java.util.List;

public record FilesDroppedEvent(List<Path> paths) implements UserEvent {}
