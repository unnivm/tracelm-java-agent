package org.usbtechno.deve;


import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class LLMInterceptor {

    private static Logger logger = Logger.getLogger(LLMInterceptor.class.getName());

    public static Object intercept(@AllArguments Object[] args, @SuperCall Callable<?> zuper) throws Exception {

        String traceId = UUID.randomUUID().toString();

        // Extract prompt
        Chat chat = extractPrompt(args);
        String prompt = chat.content;
        String model  = chat.model;

        long start = System.currentTimeMillis();
        String status = "success";
        Object result = null;
        try {
            result = zuper.call();   // actual execution
        }catch (Exception e) {
            status = "error";
        }
            long latency = System.currentTimeMillis() - start;

        // 🔥 Extract response
        String response = extractResponse(result);

        logger.info("TRACE");
        logger.info("traceId: " + traceId);
        logger.info("prompt: " + prompt);
        logger.info("latency: " + latency);
        logger.info("response: " + response);
        logger.info("status: " + status);
        logger.info("model: " + model);

        // 👉 send to collector
        TraceSender.send(buildJson(traceId, prompt, response, latency, status, model));
        logger.info("metrics sent to the tracer service");

        return result;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")   // escape backslash
                .replace("\"", "\\\"")   // escape quotes
                .replace("\n", "\\n")    // newline
                .replace("\r", "\\r");   // carriage return
    }

    private static Chat extractPrompt(Object[] args) {
        Chat chat = new Chat();
        try {
            if (args != null && args.length >= 3) {
                Object request = args[2];

                if (request instanceof Object[] actualArgs && actualArgs.length > 0) {
                    Object req = actualArgs[0];

                    String text = req.toString();
                    int modelStart = text.indexOf("model=");
                    int start = text.indexOf("content=");
                    if (start != -1) {
                        int end = text.indexOf(",", start);
                        if (end == -1) end = text.length();
                        chat.content = text.substring(start + 8, end);
                    }
                    if (modelStart != -1) {
                        int end = text.indexOf(",", modelStart);
                        if (end == -1) end = text.length();
                        chat.model = text.substring(modelStart + 6, end);
                    }

                    return chat;
                }
            }
        } catch (Exception ignored) {}

        return chat;
    }

    private static String buildJson(String traceId,
                                    String prompt,
                                    String response,
                                    long latency,
                                    String status,
                                    String model) {
        return """
    {
      "traceId":"%s",
      "prompt":"%s",
      "response":"%s",
      "latency":%d,
      "timestamp":%d,
      "status":"%s",
      "model":"%s"
    }
    """.formatted(
                traceId,
                escape(prompt),
                escape(response),
                latency,
                System.currentTimeMillis(),
                status,
                model
        );
    }

    private static String extractResponse(Object result) {
        try {
            if (result == null) return "null";

            String text = result.toString();

            // Try extracting meaningful content
            if (text.contains("content=")) {
                int start = text.indexOf("content=");
                int end = text.indexOf(",", start);
                if (end == -1) end = text.length();
                return text.substring(start + 8, end);
            }

            return text;

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe(e.toString());
            return "error extracting response";
        }
    }

    private static class Chat {

        public String model;
        public String role;
        public String content;

    }
}