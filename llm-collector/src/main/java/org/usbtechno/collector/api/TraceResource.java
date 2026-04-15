package org.usbtechno.collector.api;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.usbtechno.collector.domain.Trace;
import org.usbtechno.collector.dto.PagedTraceResponse;
import org.usbtechno.collector.repository.TraceRepository;
import org.usbtechno.collector.util.HeuristicEvaluator;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Path("/traces")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TraceResource {

    private static final Logger LOG = Logger.getLogger(TraceResource.class);

    @Inject
    TraceRepository traceRepository;

    @POST
    @Transactional
    public Response collectTrace(@Valid Trace trace) {
        if (traceRepository.findByIdOptional(trace.traceId).isPresent()) {
            throw new WebApplicationException("Trace with this traceId already exists", Response.Status.CONFLICT);
        }

        trace.qualityScore = HeuristicEvaluator.evaluate(trace.prompt == null ? "" : trace.prompt,
                trace.response == null ? "" : trace.response);

        traceRepository.persist(trace);
        LOG.infov("Stored trace {0} for model {1}", trace.traceId, trace.model);
        return Response.status(Response.Status.CREATED).entity(trace).build();
    }

    @GET
    public List<Trace> getRecentTraces(@QueryParam("limit") @DefaultValue("100") @Min(1) @Max(500) int limit) {
        return traceRepository.findRecent(limit);
    }

    @GET
    @Path("/costly-prompts")
    public List<Trace> getCostlyPrompts(@QueryParam("limit") @DefaultValue("5") @Min(1) @Max(50) int limit) {
        return traceRepository.findCostliest(limit);
    }

    @GET
    @Path("/slow-requests")
    public List<Trace> getSlowRequests(@QueryParam("limit") @DefaultValue("5") @Min(1) @Max(50) int limit) {
        return traceRepository.findSlowest(limit);
    }

    @GET
    @Path("/page")
    public PagedTraceResponse getPagedTraces(@QueryParam("page") @DefaultValue("0") @Min(0) int page,
                                             @QueryParam("size") @DefaultValue("25") @Min(1) @Max(200) int size,
                                             @QueryParam("model") String model,
                                             @QueryParam("status") String status,
                                             @QueryParam("from") Long from,
                                             @QueryParam("to") Long to) {
        return traceRepository.findPage(page, size, model, status, from, to);
    }

    @GET
    @Path("/metrics")
    public Map<String, Object> getMetrics() {
        List<Trace> traces = traceRepository.listAll();
        Map<String, Object> metrics = new HashMap<>();

        long totalRequests = traces.size();
        metrics.put("totalRequests", totalRequests);

        double avgLatency = traces.stream()
                .mapToLong(t -> t.latency)
                .average()
                .orElse(0);
        metrics.put("avgLatency", avgLatency);

        List<Long> latencies = traces.stream()
                .map(t -> t.latency)
                .sorted()
                .toList();

        long p95 = 0;
        if (!latencies.isEmpty()) {
            int index = Math.min((int) Math.ceil(0.95 * latencies.size()) - 1, latencies.size() - 1);
            p95 = latencies.get(Math.max(index, 0));
        }
        metrics.put("p95", p95);

        long errorCount = traces.stream()
                .filter(t -> "error".equalsIgnoreCase(t.status))
                .count();
        double errorRate = totalRequests == 0 ? 0 : (errorCount * 100.0) / totalRequests;
        metrics.put("errorRate", errorRate);

        long totalTokens = traces.stream()
                .mapToLong(t -> t.totalTokens > 0 ? t.totalTokens : t.tokenLength)
                .sum();
        metrics.put("totalTokens", totalTokens);

        Map<String, Long> requestsPerModel = traces.stream()
                .collect(Collectors.groupingBy(t -> t.model, Collectors.counting()));
        metrics.put("requestsPerModel", requestsPerModel);

        double totalCost = traces.stream()
                .mapToDouble(t -> t.cost)
                .sum();
        metrics.put("totalCost", totalCost);

        double avgQualityScore = traces.stream()
                .filter(t -> t.qualityScore != null)
                .mapToDouble(t -> t.qualityScore)
                .average()
                .orElse(0);
        metrics.put("avgQualityScore", avgQualityScore);
        return metrics;
    }

    @GET
    @Path("/model-metrics")
    public Map<String, Long> modelMetrics() {
        return traceRepository.listAll().stream()
                .collect(Collectors.groupingBy(t -> t.model, Collectors.counting()));
    }

    @GET
    @Path("/time-series")
    public Map<String, Long> timeSeries() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return traceRepository.listAll().stream()
                .collect(Collectors.groupingBy(
                        t -> Instant.ofEpochMilli(t.timestamp)
                                .atZone(ZoneId.systemDefault())
                                .format(formatter),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    @GET
    @Path("/model-analytics")
    public Map<String, Map<String, Object>> modelAnalytics() {
        Map<String, List<Trace>> grouped = traceRepository.listAll().stream()
                .collect(Collectors.groupingBy(t -> t.model));

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<String, List<Trace>> entry : grouped.entrySet()) {
            String model = entry.getKey();
            List<Trace> list = entry.getValue();

            int requests = list.size();
            int totalTokens = list.stream()
                    .mapToInt(t -> t.totalTokens)
                    .sum();
            double totalCost = list.stream()
                    .mapToDouble(t -> t.cost)
                    .sum();
            double avgLatency = list.stream()
                    .mapToLong(t -> t.latency)
                    .average()
                    .orElse(0);

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("requests", requests);
            metrics.put("tokens", totalTokens);
            metrics.put("cost", totalCost);
            metrics.put("avgLatency", avgLatency);
            result.put(model, metrics);
        }

        return result.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        HashMap::new
                ));
    }
}
