package partition.poc.messaging;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

@Component
public interface Channels {
	String MY_TOPIC_CHANNEL = "myTopicChannel";

	@Input(MY_TOPIC_CHANNEL)
	SubscribableChannel myTopicChannel();
}
 