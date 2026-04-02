package org.usbtechno.collector;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/traces")
public class TraceResource {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TraceResource.class);
    private Logger logger = Logger.getLogger(TraceResource.class.getName());

    private static final AtomicLong requestCount = new AtomicLong();
    private static final List<Trace> traces = Collections.synchronizedList(new ArrayList<>());


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response collectTrace(Trace trace) {
        logger.info("RAW TRACE RECEIVED:");

        // INCREMENT HERE
        requestCount.incrementAndGet();
        traces.add(trace);

        logger.info("trace id: " + trace.traceId);
        logger.info("latency: "  + trace.latency);
        logger.info("prompt: "   + trace.prompt);
        logger.info("status: "   + trace.status);
        logger.info("model: "    + trace.model);
        logger.info("total requests: "    + traces.size());
        logger.info("token length:" + trace.tokenLength);

        return Response.ok().build();
    }

    // 🔥 METRICS ENDPOINT
    @GET
    @Path("/metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        long totalRequests = requestCount.get();
        metrics.put("totalRequests", totalRequests);

        // avg latency
        double avgLatency = traces.stream()
                .mapToLong(t -> t.latency)
                .average()
                .orElse(0);
        metrics.put("avgLatency", avgLatency);

        // p95 latency
        List<Long> latencies = traces.stream()
                .map(t -> t.latency)
                .sorted()
                .toList();

        int index = (int) (0.95 * latencies.size());
        long p95 = latencies.get(index);
        metrics.put("p95", p95);

        long errorCount = traces.stream()
                .filter(t -> "error".equals(t.status))
                .count();

        // error rate
        double errorRate = (errorCount * 100.0) / totalRequests;
        metrics.put("errorRate", errorRate);

        // total tokens
        long totalTokens = traces.stream()
                .mapToLong(t -> t.tokenLength)
                .sum();
        metrics.put("totalTokens", totalTokens);

        // requests per model
        Map<String, Long> requestsPerModel =
                traces.stream()
                        .collect(Collectors.groupingBy(
                                t -> t.model,
                                Collectors.counting()
                        ));

        metrics.put("requestsPerModel", requestsPerModel);

        return metrics;
    }
}