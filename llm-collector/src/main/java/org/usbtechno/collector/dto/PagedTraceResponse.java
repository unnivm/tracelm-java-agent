package org.usbtechno.collector.dto;

import org.usbtechno.collector.domain.Trace;

import java.util.List;

public class PagedTraceResponse {

    public List<Trace> items;
    public int page;
    public int size;
    public long total;
    public long totalPages;

    public PagedTraceResponse() {
    }

    public PagedTraceResponse(List<Trace> items, int page, int size, long total) {
        this.items = items;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = size == 0 ? 0 : (long) Math.ceil((double) total / size);
    }
}
