package io.wispforest;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.*;

public class BraidReloadAgent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        instrumentation.addTransformer(new RedefinitionListener());
    }
}

class RedefinitionListener implements ClassFileTransformer {

    private final Map<String, Integer> classHashes = new HashMap<>();
    private final Set<String> classesToWaitFor = new HashSet<>(Set.of(
        "io/wispforest/owo/braid/framework/widget/Widget",
        "io/wispforest/owo/braid/framework/proxy/WidgetState",
        "io/wispforest/owo/braid/core/BraidHotReloadCallback"
    ));

    private ClassLoader braidClassLoader;
    private boolean logSetupComplete = false;

    @Override
    public byte[] transform(Module module, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (this.logSetupComplete) {
            this.logSetupComplete = false;

            fallible(() -> {
                var callbackClass = Class.forName("io.wispforest.owo.braid.core.BraidHotReloadCallback", false, this.braidClassLoader);
                callbackClass.getMethod("setupComplete").invoke(null);
            });
        }

        if (!this.classesToWaitFor.isEmpty()) {
            if (this.classesToWaitFor.contains(className)) {
                this.classesToWaitFor.remove(className);

                if (this.braidClassLoader == null) {
                    this.braidClassLoader = loader;
                }

                if (this.classesToWaitFor.isEmpty()) {
                    this.logSetupComplete = true;
                }
            }

            return ClassFileTransformer.super.transform(module, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }

        fallible(() -> {
            var widgetClass = Class.forName("io.wispforest.owo.braid.framework.widget.Widget", false, this.braidClassLoader);
            var widgetStateClass = Class.forName("io.wispforest.owo.braid.framework.proxy.WidgetState", false, this.braidClassLoader);
            var callbackClass = Class.forName("io.wispforest.owo.braid.core.BraidHotReloadCallback", false, this.braidClassLoader);

            if (classBeingRedefined != null) {
                if (widgetClass.isAssignableFrom(classBeingRedefined) || widgetStateClass.isAssignableFrom(classBeingRedefined)) {
                    var newHash = Arrays.hashCode(classfileBuffer);

                    if (!this.classHashes.containsKey(className) || this.classHashes.get(className) != newHash) {
                        callbackClass.getMethod("invoke").invoke(null);
                    }

                    this.classHashes.put(className, newHash);
                }
            }
        });

        return ClassFileTransformer.super.transform(module, loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    private static void fallible(Fallible fallible) {
        fallible.run();
    }
}

interface Fallible {
    void body() throws Throwable;

    default void run() {
        try {
            this.body();
        } catch (Throwable error) {
            System.err.println("(braid reload agent) hotswap error: " + error.getMessage());
            //noinspection CallToPrintStackTrace
            error.printStackTrace();
        }
    }
}