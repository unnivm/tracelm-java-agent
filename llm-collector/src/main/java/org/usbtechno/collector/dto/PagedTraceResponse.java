/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
