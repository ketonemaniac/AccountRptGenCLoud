package net.ketone.accrptgen.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.service.stats.StatisticsService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.ketone.accrptgen.config.Constants.GEN_QUEUE_NAME;
import static net.ketone.accrptgen.config.Constants.GEN_QUEUE_ENDPOINT;

/**
 * Works for Google cloud Standard environment
 */
@Slf4j
@Profile("gCloudStandard")
@RestController
public class GCloudStandardTasksService {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    @Qualifier("fileBasedStatisticsService")
    private StatisticsService fileBasedStatisticsService;

    @Autowired
    private ApplicationContext ctx;

    @PostMapping(GEN_QUEUE_ENDPOINT)
    // public String doWork(@RequestParam("accountFileDto") AccountFileDto dto) {
    public String doWork(@RequestBody AccountJob dto) {
        log.info("inside doWork() " + dto.toString());
        dto.setStatus(Constants.Status.GENERATING.name());
        try {
            statisticsService.updateTask(dto);
        } catch (IOException e) {
            log.warn("History file write failed (GENERATING)", e);
            return "NOT OK";
        }
        Pipeline pipeline = ctx.getBean(Pipeline.class, dto);
        pipeline.run();
        return "OK";
    }

}
