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
