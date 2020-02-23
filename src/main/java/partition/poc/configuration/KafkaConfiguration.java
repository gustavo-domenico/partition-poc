package partition.poc.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableKafka
public class KafkaConfiguration {
	@Bean
	public NewTopic myTopic() {
		return new NewTopic("myTopic", 3, (short) 1);
	}

	@Bean
	public NewTopic mySecondTopic() {
		return new NewTopic("mySecondTopic", 3, (short) 1);
	}
}