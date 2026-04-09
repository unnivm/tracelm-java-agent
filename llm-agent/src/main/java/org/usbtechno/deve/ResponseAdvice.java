package org.usbtechno.deve;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Method;
import java.util.logging.Logger;

public class ResponseAdvice {

    public static final ThreadLocal<Boolean> active = ThreadLocal.withInitial(() -> false);
    public static Logger logger = Logger.getLogger(ResponseAdvice.class.getName());

    @Advice.OnMethodEnter
    public static long onEnter() {
        return System.currentTimeMillis();
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(
            @Advice.Origin Method method,
            @Advice.Return Object result,
            @Advice.Enter long startTime
    ) {

        if(active.get()) {
            return;
        }
        active.set(true);

        logger.info("✅ HIT METHOD: " + method.getName());
        logger.info("CLASS: " + method.getDeclaringClass().getName());

        try {
            long latency = System.currentTimeMillis() - startTime;

            if(latency < 200) {
                return;
            }
            Trace trace = TraceContext.get();

            if (trace != null) {
                logger.info("➡️ RESULT TYPE: " + result.getClass().getName());
                String responseText = ResponseExtractor.extractResponse(result);
                logger.info("\n FINAL TRACE:");

                logger.info(" METHOD :" + method.getName());
             //   System.out.println(" RESPONSE :" + responseText);
                logger.info(" LATENCY :" + latency);

                trace.latency = latency;

                int promptTokens    = TokenUtil.countTokens(trace.prompt);
                int responseTokens  = TokenUtil.countTokens(responseText);
                int totalToken      = promptTokens + responseTokens;

                double cost = CostCalculator.calculate(
                        trace.model != null ? trace.model : "gpt-4o-mini",
                        promptTokens,
                        responseTokens
                );

                trace.response = responseText;
                trace.promptTokens = promptTokens;
                trace.responseTokens = responseTokens;
                trace.totalTokens = totalToken;
                trace.cost = cost;

                TraceSender.send(buildJson(trace));

                logger.info("Sent trace to the server :");

                // sets it to null
                TraceContext.set(null);
            }

        }catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        finally {
            active.set(false);
        }
    }

    /**
     *
     * @param trace
     * @return
     */
    public static String buildJson(Trace trace) {

        return """
    {
      "traceId":"%s",
      "prompt":"%s",
      "response":"%s",
      "latency":%d,
      "timestamp":%d,
      "status":"%s",
      "model":"%s",
      "tokenLength":%d,
      "promptTokens":%d,
      "responseTokens":%d,
      "totalTokens":%d,
      "cost":%f
    }
    """.formatted(
                trace.traceId,
                escape(trace.prompt),
                escape(trace.response),
                trace.latency,
                System.currentTimeMillis(),
                trace.status,
                trace.model,
                trace.promptTokens,
                trace.promptTokens,
                trace.responseTokens,
                trace.totalTokens,
                trace.cost
        );

    }

    /**
     *
     * @param s
     * @return
     */
    public static String escape(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")   // escape backslash
                .replace("\"", "\\\"")   // escape quotes
                .replace("\n", "\\n")    // newline
                .replace("\r", "\\r");   // carriage return
    }

    /**
     *
     * @param text
     * @return
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;

        String[] words = text.split("\\s+");

        return (int) (words.length * 1.3);
    }

}