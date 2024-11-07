@RestController
public class HealthzController {

    private final DataSource dataSource;

    @Autowired
    private MeterRegistry meterRegistry; // Add metrics registry

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