package partition.poc.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;

import static partition.poc.messaging.Channels.MY_TOPIC_CHANNEL;

@Slf4j
@RequiredArgsConstructor
@EnableBinding(Channels.class)
public class TopicListener {
	@StreamListener(MY_TOPIC_CHANNEL)
	public void process(Message<String> message) {
		log.info("[Message from MY_TOPIC] {}.", message);
	}
}
