package partition.poc.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class HelloController {
	@GetMapping
	public String helloGradle() {
		return "Hello Gradle!";
	}
}
