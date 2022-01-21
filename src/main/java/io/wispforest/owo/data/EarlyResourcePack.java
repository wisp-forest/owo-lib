/*
 * This code is heavily based on the ModNioResourcePack class from Fabric API,
 * which is licensed under the Apache 2.0 License.
 *
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.wispforest.owo.data;

import net.minecraft.SharedConstants;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApiStatus.Internal
public class EarlyResourcePack extends AbstractFileResourcePack {
    private final Path basePath;
    private final String name;
    private final ResourceType type;

    public EarlyResourcePack(Path basePath, String name, ResourceType type) {
        super(null);

        this.basePath = basePath.toAbsolutePath().normalize();
        this.name = name;
        this.type = type;
    }

    private Path resolvePath(String name) {
        Path path = basePath.resolve(name).toAbsolutePath().normalize();
        if (!path.startsWith(basePath)) return null;
        return path;
    }

    @Override
    protected InputStream openFile(String name) throws IOException {
        Path path = resolvePath(name);
        if (path != null && Files.isRegularFile(path))
            return Files.newInputStream(path);

        if (name.equals("pack.mcmeta")) {
            String mcmetaData = "{\"pack\":{\"pack_format\":" + type.getPackVersion(SharedConstants.getGameVersion()) + "}}";
            return new ByteArrayInputStream(mcmetaData.getBytes(StandardCharsets.UTF_8));
        }

        throw new FileNotFoundException(name + " in early resource pack " + this.name);
    }

    @Override
    protected boolean containsFile(String name) {
        return Files.isRegularFile(basePath.resolve(name));
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
        Path typePath = basePath.resolve(type.getDirectory());

        if (!Files.isDirectory(typePath))
            return Collections.emptySet();

        try (var stream = Files.list(typePath)) {
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
