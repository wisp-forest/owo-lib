package io.wispforest.owo.data;

import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EarlyResourcePack extends AbstractFileResourcePack {
    private final Path basePath;
    private final String name;

    public EarlyResourcePack(Path basePath, String name) {
        super(null);

        this.basePath = basePath;
        this.name = name;
    }

    @Override
    protected InputStream openFile(String name) throws IOException {
        return Files.newInputStream(basePath.resolve(name));
    }

    @Override
    protected boolean containsFile(String name) {
        return Files.exists(basePath.resolve(name));
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
        Path walkPath = nsPath.resolve(prefix.replace("/", basePath.getFileSystem().getSeparator()));

        if (!Files.exists(walkPath))
            return Collections.emptyList();

        List<Identifier> ids = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(walkPath, maxDepth)) {
            walk
                .filter(x -> {
                    String fileName = x.getFileName().toString();
                    return pathFilter.test(fileName) && !fileName.endsWith(".mcmeta");
                })
                .filter(Files::isRegularFile)
                .map(nsPath::relativize)
                .map(x -> x.toString().replace(basePath.getFileSystem().getSeparator(), "/"))
                .forEach(x -> ids.add(new Identifier(namespace, x)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ids;
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        try (var stream = Files.list(basePath.resolve(type.getDirectory()))) {
            return stream
                .filter(Files::isDirectory)
                .map(x -> x.getFileName().toString())
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String getName() {
        return name;
    }
}
