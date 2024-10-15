package com.rio_rishabhNEU.UserApp.Controllers;

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

    @Autowired
    public HealthzController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @RequestMapping(value = "/healthz", method = RequestMethod.GET)
    public ResponseEntity<Void> healthCheck(@RequestParam Map<String, String> queryParams,
                                            @RequestBody(required = false) Map<String, Object> requestBody) {
        if (!queryParams.isEmpty() || requestBody != null) {
            // if any paramater or payload is present then should return 404
            return ResponseEntity.badRequest()
                    .cacheControl(CacheControl.noCache())
                    .build();
        }

        try (Connection connection = dataSource.getConnection()) {
            // Connection is successful
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache()) // Sets Cache-Control: no-cache
                    .build();
        } catch (SQLException e) {
            //Database disconnected
            return ResponseEntity.status(503) // Should return service not available
                    .cacheControl(CacheControl.noCache())
                    .build();
        }
    }


    //handling head and put request with standard 405 status code. i.e, These methods are not allowed.
    @RequestMapping(value = "/healthz", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headRequest() {
        return ResponseEntity.status(405).build();
    }


    @RequestMapping(value = "/healthz", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsRequest() {
        return ResponseEntity.status(405).build();
    }

}
