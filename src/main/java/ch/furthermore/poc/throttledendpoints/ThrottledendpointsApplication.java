package ch.furthermore.poc.throttledendpoints;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableCaching
@RestController
@SpringBootApplication
public class ThrottledendpointsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThrottledendpointsApplication.class, args);
	}

	/**
	 * <pre>
	 * config:
	 * rate-limits:
     *   - bandwidths:
     *     - capacity: 5
     *       time: 10
     *       unit: seconds
	 * 
	 * throttling (~every 2nd request):
	 * watch -1 2 curl -v http://localhost:6363/
	 * 
	 * no throttling:
	 * watch -n 2 curl -v http://localhost:6363/
	 * </pre>
	 */
	@GetMapping("/")
	public String hello() {
	    return "world";
	}
}
