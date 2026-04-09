package org.usbtechno.deve;


import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class LLMInterceptor {

    private static Logger logger = Logger.getLogger(LLMInterceptor.class.getName());

    public static Object intercept(@AllArguments Object[] args, @SuperCall Callable<?> zuper) throws Exception {

        // 🔥 Extract real invoked method (important)
        Method realMethod = (Method) args[1];
        String methodName = realMethod.getName().toLowerCase();

        // 🎯 Only intercept LLM calls
        if (!methodName.contains("chat") && !methodName.contains("completion")) {
            return zuper.call();
        }

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
            e.printStackTrace();
            status = "error";
        }
        long latency = System.currentTimeMillis() - start;

        // 🔥 Extract response
        String response = "";//extractResponse(result);

        // get token length for the current prompt
        int tokenLength = estimateTokens(prompt);

        logger.info("TRACE");
        logger.info("traceId: " + traceId);
        logger.info("prompt: " + prompt);
        logger.info("latency: " + latency);
        logger.info("response: " + response);
        logger.info("status: " + status);
        logger.info("model: " + model);
        logger.info("token length: " + tokenLength);

        // create a trace object
        Trace trace = new Trace();
        trace.prompt = prompt;
        trace.traceId = traceId;
        trace.model = model;
        trace.status = status;
        trace.tokenLength = tokenLength;
        trace.response = response;

        TraceContext.set(trace);
        return result;
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

    private static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        String[] words = text.split("\\s+");
        return (int) (words.length * 1.3);
    }
}