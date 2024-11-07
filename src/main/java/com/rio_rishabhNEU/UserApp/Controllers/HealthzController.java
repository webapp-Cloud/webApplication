package com.rio_rishabhNEU.UserApp.Controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class HealthzController {

    private final DataSource dataSource;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    public HealthzController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.GET)
    public ResponseEntity<Void> healthCheck(@RequestParam Map<String, String> queryParams,
                                            @RequestBody(required = false) Map<String, Object> requestBody) {
        // Record metric for health check
        meterRegistry.counter("api.healthcheck").increment();
        Timer.Sample timer = Timer.start(meterRegistry);

        try {
            if (!queryParams.isEmpty() || requestBody != null) {
                return ResponseEntity.badRequest()
                        .cacheControl(CacheControl.noCache())
                        .build();
            }

            try (Connection connection = dataSource.getConnection()) {
                timer.stop(meterRegistry.timer("api.healthcheck.time"));
                return ResponseEntity.ok()
                        .cacheControl(CacheControl.noCache())
                        .build();
            } catch (SQLException e) {
                meterRegistry.counter("api.healthcheck.error").increment();
                return ResponseEntity.status(503)
                        .cacheControl(CacheControl.noCache())
                        .build();
            }
        } catch (Exception e) {
            meterRegistry.counter("api.healthcheck.error").increment();
            throw e;
        }
    }
}