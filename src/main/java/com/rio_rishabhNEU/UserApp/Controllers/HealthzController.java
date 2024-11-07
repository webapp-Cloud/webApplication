package com.rio_rishabhNEU.UserApp.Controllers;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class HealthzController {

    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;

    @Autowired
    public HealthzController(DataSource dataSource, @Autowired(required = false) MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.GET)
    public ResponseEntity<Void> healthCheck(@RequestParam Map<String, String> queryParams,
                                            @RequestBody(required = false) Map<String, Object> requestBody) {
        // Increment metrics counter if MeterRegistry is available
        if (meterRegistry != null) {
            meterRegistry.counter("api.calls", "endpoint", "/healthz", "method", "GET").increment();
        }

        if (!queryParams.isEmpty() || requestBody != null) {
            return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noCache())
                    .build();
        }

        try (Connection connection = dataSource.getConnection()) {
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .build();
        } catch (SQLException e) {
            return ResponseEntity.status(503)
                    .cacheControl(CacheControl.noCache())
                    .build();
        }
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headRequest() {
        return ResponseEntity.status(405).build();
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsRequest() {
        return ResponseEntity.status(405).build();
    }
}