package io.wispforest.owo.text;

import net.minecraft.text.TranslatableTextContent;

import java.util.ArrayList;
import java.util.List;

public class TranslationContext {
    private static final ThreadLocal<List<TranslatableTextContent>> translationStack = ThreadLocal.withInitial(ArrayList::new);

    public static boolean pushContent(TranslatableTextContent content) {
        var stack = translationStack.get();

        for (int i = 0; i < stack.size(); i++) {
            if (stack.get(i) == content)
                return false;
        }

        stack.add(content);

        return true;
    }

    public static void popContent() {
        var stack = translationStack.get();

        stack.remove(stack.size() - 1);
    }

    public static TranslatableTextContent getCurrent() {
        var stack = translationStack.get();

        if (stack.size() <= 0)
            return null;
        else
            return stack.get(stack.size() - 1);
    }
}
