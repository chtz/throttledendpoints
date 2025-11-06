package ch.furthermore.poc.throttledendpoints;

import java.util.OptionalLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;

import jakarta.servlet.http.HttpServletRequest;

@EnableCaching
@RestController
@SpringBootApplication
public class ThrottledendpointsApplication {
    private final static Logger log = LoggerFactory.getLogger(ThrottledendpointsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ThrottledendpointsApplication.class, args);
    }

    @Bean
    JCacheManagerCustomizer caffeineCustomizer() {
        return cm -> {
            var c = new CaffeineConfiguration<Object, Object>();
            c.setMaximumSize(OptionalLong.of(100_000L));
            c.setExpireAfterWrite(OptionalLong.of(java.time.Duration.ofSeconds(5).toNanos()));
            cm.createCache("endpoints", c);

            c = new CaffeineConfiguration<Object, Object>();
            c.setMaximumSize(OptionalLong.of(10_000L));
            c.setExpireAfterAccess(OptionalLong.of(java.time.Duration.ofHours(1).toNanos()));
            cm.createCache("rate-limit-buckets", c);
        };
    }

    /**
     * Sample 1: All endpoints and all source IPs share one global rate-limit bucket.
     * 
     * <pre>
     * config:
     * rate-limits:
     *   - bandwidths:
     *     - capacity: 5
     *       time: 10
     *       unit: seconds
     * 
     * throttling (~every 2nd request):
     * watch -n 1 curl -v http://localhost:6363/
     * 
     * no throttling:
     * watch -n 2 curl -v http://localhost:6363/
     * </pre>
     * 
     * Sample 2: All endpoints share one global rate-limit bucket, instantiated per source IP.
     * 
     * <pre>
     * config:
     * rate-limits:
     *   - cache-key: "getRemoteAddr()"
     *     bandwidths:
     *     - capacity: 5
     *       time: 10
     *       unit: seconds
     * 
     * 50% throttling: 
     * watch -n 1 curl -v http://192.168.1.11:6363/  # throttling ~every 2nd request
     * watch -n 2 curl -v http://localhost:6363/     # no throttling
     * 
     * 100% throttling:
     * watch -n 1 curl -v http://192.168.1.11:6363/  # throttling ~every 2nd request
     * watch -n 1 curl -v http://localhost:6363/     # throttling ~every 2nd request
     * 
     * no throttling:
     * watch -n 2 curl -v http://192.168.1.11:6363/  # no throttling
     * watch -n 2 curl -v http://localhost:6363/     # no throttling
     * </pre>
     */
    @GetMapping("/")
    public String hello(HttpServletRequest request) {
        log.info("GET /");
        String clientIp = request.getRemoteAddr();
        return "world 1 - " + clientIp;
    }

    @Cacheable(cacheNames = "endpoints", key = "'/h2'")
    @GetMapping("/h2")
    public String hello2(HttpServletRequest request) {
        log.info("GET /h2");
        String clientIp = request.getRemoteAddr();
        return "world 2 - " + clientIp;
    }
}
