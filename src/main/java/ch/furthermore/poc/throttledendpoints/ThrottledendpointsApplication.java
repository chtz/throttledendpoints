package ch.furthermore.poc.throttledendpoints;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@EnableCaching
@RestController
@SpringBootApplication
public class ThrottledendpointsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThrottledendpointsApplication.class, args);
	}

	/**
	 * Sample 1: All endpoints and all source IPs share one global rate-limit bucket.
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
	    String clientIp = request.getRemoteAddr();
	    return "world 1 - " + clientIp;
	}
	
	@GetMapping("/h2")
    public String hello2(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        return "world 2 - " + clientIp;
    }
}
