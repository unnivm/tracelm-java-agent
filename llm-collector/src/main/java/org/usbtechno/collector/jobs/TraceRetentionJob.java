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

package org.usbtechno.collector.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.usbtechno.collector.repository.TraceRepository;

@ApplicationScoped
public class TraceRetentionJob {

    private static final Logger LOG = Logger.getLogger(TraceRetentionJob.class);

    @Inject
    TraceRepository traceRepository;

    @Inject
    @ConfigProperty(name = "collector.retention.days")
    int retentionDays;

    @Scheduled(every = "{collector.retention.cleanup-interval}")
    @Transactional
    void purgeExpiredTraces() {
        if (retentionDays <= 0) {
            LOG.infov("Trace retention cleanup disabled because retentionDays={0}", retentionDays);
            return;
        }

        long cutoff = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
        long deleted = traceRepository.delete("timestamp < ?1", cutoff);
        if (deleted > 0) {
            LOG.infov("Deleted {0} traces older than cutoff {1}", deleted, cutoff);
        }
    }
}
