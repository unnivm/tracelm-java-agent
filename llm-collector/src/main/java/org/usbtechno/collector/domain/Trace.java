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

package org.usbtechno.collector.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "traces")
public class Trace extends PanacheEntityBase {

    @Id
    @NotBlank(message = "traceId is required")
    @Size(max = 128, message = "traceId must be 128 characters or fewer")
    @Column(name = "trace_id", nullable = false, updatable = false, length = 128)
    public String traceId;

    @NotBlank(message = "model is required")
    @Size(max = 120, message = "model must be 120 characters or fewer")
    @Column(nullable = false, length = 120)
    public String model;

    @Column(columnDefinition = "TEXT")
    public String prompt;

    @Column(columnDefinition = "TEXT")
    public String response;

    @PositiveOrZero(message = "latency must be non-negative")
    @Column(nullable = false)
    public long latency;

    @Column(nullable = false)
    public long timestamp;

    @NotBlank(message = "status is required")
    @Size(max = 32, message = "status must be 32 characters or fewer")
    @Column(nullable = false, length = 32)
    public String status;

    @PositiveOrZero(message = "tokenLength must be non-negative")
    @Column(name = "token_length", nullable = false)
    public long tokenLength;

    @PositiveOrZero(message = "promptTokens must be non-negative")
    @Column(name = "prompt_tokens", nullable = false)
    public int promptTokens;

    @PositiveOrZero(message = "responseTokens must be non-negative")
    @Column(name = "response_tokens", nullable = false)
    public int responseTokens;

    @PositiveOrZero(message = "totalTokens must be non-negative")
    @Column(name = "total_tokens", nullable = false)
    public int totalTokens;

    @PositiveOrZero(message = "cost must be non-negative")
    @Column(nullable = false)
    public double cost;

    @PositiveOrZero(message = "qualityScore must be non-negative")
    @Column(name = "quality_score")
    public Double qualityScore;

    @PrePersist
    void prePersist() {
        if (timestamp <= 0) {
            timestamp = System.currentTimeMillis();
        }
        if (totalTokens == 0 && tokenLength > 0) {
            totalTokens = Math.toIntExact(tokenLength);
        }
        if (tokenLength == 0 && totalTokens > 0) {
            tokenLength = totalTokens;
        }
        if (status != null) {
            status = status.trim().toLowerCase();
        }
    }
}
