package org.usbtechno.deve;

import java.util.List;

public class ResponseExtractor {

    private static final int MAX_LENGTH = 2000; // prevent huge logs

    public static String extractResponse(Object result) {

        if (result == null) return "null";

        try {
            // String
            if (result instanceof String str) return str;

            // AiMessage (LangChain4J)
            try {
                Object content = result.getClass()
                        .getMethod("content")
                        .invoke(result);

                if (content != null) return content.toString();
            } catch (Exception ignored) {}

            // ChatResponse style
            try {
                Object message = result.getClass()
                        .getMethod("aiMessage")
                        .invoke(result);

                Object content = message.getClass()
                        .getMethod("content")
                        .invoke(message);

                return content.toString();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "error extracting response";
        }
    }

    // 🔧 Safe method invocation
    private static String invokeMethod(Object obj, String methodName) {
        try {
            Object value = obj.getClass()
                    .getMethod(methodName)
                    .invoke(obj);

            return value != null ? value.toString() : null;

        } catch (Exception ignored) {
            return null;
        }
    }

    // 🔥 Handle OpenAI-style nested response
    private static String extractNestedContent(Object result) {
        try {
            Object choices = result.getClass()
                    .getMethod("getChoices")
                    .invoke(result);

            if (choices instanceof List<?> list && !list.isEmpty()) {

                Object first = list.get(0);

                Object message = first.getClass()
                        .getMethod("getMessage")
                        .invoke(first);

                Object content = message.getClass()
                        .getMethod("getContent")
                        .invoke(message);

                return content != null ? content.toString() : null;
            }

        } catch (Exception ignored) {}

        return null;
    }

    // ✂️ Prevent huge payloads
    private static String truncate(String text) {
        if (text == null) return null;

        return text.length() > MAX_LENGTH
                ? text.substring(0, MAX_LENGTH) + "...(truncated)"
                : text;
    }
}