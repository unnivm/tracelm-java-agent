package org.usbtechno.collector;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@Path("/traces")
public class TraceResource {

    private Logger logger = Logger.getLogger(TraceResource.class.getName());

    private static final AtomicLong requestCount = new AtomicLong();
    private static final List<Trace> traces = Collections.synchronizedList(new ArrayList<>());


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response collectTrace(Trace trace) {
        logger.info("RAW TRACE RECEIVED:");
        // INCREMENT HERE
        requestCount.incrementAndGet();

        logger.info("trace id " + trace.traceId);
        logger.info("latency "  + trace.latency);
        logger.info("prompt "   + trace.prompt);
        logger.info("response " + trace.response);
        logger.info("status "   + trace.status);
        logger.info("model "    + trace.model);

        return Response.ok().build();
    }

    // 🔥 METRICS ENDPOINT
    @GET
    @Path("/metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalRequests", requestCount.get());
        return metrics;
    }
}