package org.usbtechno.collector.repository;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.usbtechno.collector.domain.Trace;
import org.usbtechno.collector.dto.PagedTraceResponse;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TraceRepository implements PanacheRepositoryBase<Trace, String> {

    public List<Trace> findRecent(int limit) {
        return findAll(Sort.descending("timestamp")).page(Page.ofSize(limit)).list();
    }

    public List<Trace> findCostliest(int limit) {
        return findAll(Sort.descending("cost").and("timestamp", Sort.Direction.Descending))
                .page(Page.ofSize(limit))
                .list();
    }

    public List<Trace> findSlowest(int limit) {
        return findAll(Sort.descending("latency").and("timestamp", Sort.Direction.Descending))
                .page(Page.ofSize(limit))
                .list();
    }

    public PagedTraceResponse findPage(int page, int size, String model, String status, Long from, Long to) {
        List<String> clauses = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (model != null && !model.isBlank()) {
            clauses.add("model = ?" + (params.size() + 1));
            params.add(model);
        }

        if (status != null && !status.isBlank()) {
            clauses.add("lower(status) = lower(?" + (params.size() + 1) + ")");
            params.add(status);
        }

        if (from != null) {
            clauses.add("timestamp >= ?" + (params.size() + 1));
            params.add(from);
        }

        if (to != null) {
            clauses.add("timestamp <= ?" + (params.size() + 1));
            params.add(to);
        }

        String query = clauses.isEmpty() ? "" : String.join(" and ", clauses);
        PanacheQuery<Trace> panacheQuery = query.isEmpty()
                ? findAll(Sort.descending("timestamp"))
                : find(query, Sort.descending("timestamp"), params.toArray());

        long total = panacheQuery.count();
        List<Trace> items = panacheQuery.page(Page.of(page, size)).list();
        return new PagedTraceResponse(items, page, size, total);
    }
}
