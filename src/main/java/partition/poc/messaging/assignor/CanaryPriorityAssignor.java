package partition.poc.messaging.assignor;

import org.apache.kafka.clients.consumer.internals.AbstractPartitionAssignor;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.UnaryOperator;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Byte.MAX_VALUE;
import static java.lang.Byte.MIN_VALUE;
import static java.lang.Short.BYTES;
import static java.nio.ByteBuffer.allocate;
import static java.nio.ByteBuffer.wrap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class CanaryPriorityAssignor extends AbstractPartitionAssignor {
	private static final String IS_CANARY_INSTANCE = "IS_CANARY_INSTANCE";
	private static final Logger log = LoggerFactory.getLogger(CanaryPriorityAssignor.class);

	// Used in order to be able to test properly, I could not find a working combination of a
	// way to mock static System methods with groovy/spock and the latest versions of Java
	private static UnaryOperator<String> environmentProvider = System::getenv;

	public static void setEnvironmentProvider(UnaryOperator<String> environmentProvider) {
		CanaryPriorityAssignor.environmentProvider = environmentProvider;
	}

	@Override
	public String name() {
		return "canary";
	}

	@Override
	public Subscription subscription(Set<String> topics) {
		boolean isCanaryInstance = parseBoolean(environmentProvider.apply(IS_CANARY_INSTANCE));
		if (isCanaryInstance) {
			log.info("Canary member found, it will have priority in the topic/partition assignment.");
		}

		// Boolean size is platform dependent, using short to represent it
		byte[] userData = allocate(BYTES)
				.put(isCanaryInstance ? MAX_VALUE : MIN_VALUE)
				.flip() // Prepares the buffer for a read operation in the @assign
				.array();

		return new Subscription(new ArrayList<>(topics), wrap(userData));
	}

	@Override
	public Map<String, List<TopicPartition>> assign(Map<String, Integer> partitionsPerTopic,
	                                                Map<String, Subscription> subscriptions) {

		// Topic/partition combination for each member to be returned
		Map<String, List<TopicPartition>> assignment = new HashMap<>();

		// Get all canary members, that has the metadata
		List<String> canaryMembers = subscriptions.entrySet()
				.stream()
				.filter(this::isCanaryMember)
				.map(Entry::getKey)
				.collect(toList());

		log.info("{} canary members found to prioritize.", canaryMembers.size());

		// Get the consumers per topic for the subscriptions
		Map<String, List<String>> consumersPerTopic = consumersPerTopic(subscriptions);
		for (Entry<String, Subscription> entry : subscriptions.entrySet()) {
			assignment.put(entry.getKey(), new ArrayList<>());
		}

		// For each topic to be assigned, balance the number of partitions between the consumers
		for (Entry<String, List<String>> topicEntry : consumersPerTopic.entrySet()) {
			String topic = topicEntry.getKey();
			List<String> consumersForTopic = topicEntry.getValue();

			log.debug("Assigning topic {} for consumers {}.", topic, consumersForTopic);

			Integer numPartitionsForTopic = partitionsPerTopic.get(topic);
			if (numPartitionsForTopic == null)
				continue;

			// Prioritizing the canary members first
			consumersForTopic.sort(comparing(canaryMembers::contains));

			int numPartitionsPerConsumer = numPartitionsForTopic / consumersForTopic.size();
			int consumersWithExtraPartition = numPartitionsForTopic % consumersForTopic.size();

			log.debug("Topic/partition distribution is {} with {} extra assignments.", numPartitionsPerConsumer, consumersWithExtraPartition);

			// Expand the number of partitions to a TopicPartition object
			List<TopicPartition> partitions = AbstractPartitionAssignor.partitions(topic, numPartitionsForTopic);
			for (int i = 0, n = consumersForTopic.size(); i < n; i++) {
				int start = numPartitionsPerConsumer * i + Math.min(i, consumersWithExtraPartition);
				int length = numPartitionsPerConsumer + (i + 1 > consumersWithExtraPartition ? 0 : 1);

				// Perform the assignment
				List<TopicPartition> subPartitionList = partitions.subList(start, start + length);
				String member = consumersForTopic.get(i);
				assignment.get(member).addAll(subPartitionList);

				log.debug("Assigned member {}(isCanary: {}) to topic/partitions {}.", member, canaryMembers.contains(member), subPartitionList);
			}
		}
		return assignment;
	}

	private boolean isCanaryMember(Entry<String, Subscription> e) {
		ByteBuffer userData = e.getValue().userData();
		return userData.remaining() > 0 && userData.get() == MAX_VALUE;
	}

	private Map<String, List<String>> consumersPerTopic(Map<String, Subscription> consumerMetadata) {
		Map<String, List<String>> res = new HashMap<>();
		for (Entry<String, Subscription> subscriptionEntry : consumerMetadata.entrySet()) {
			String consumerId = subscriptionEntry.getKey();
			for (String topic : subscriptionEntry.getValue().topics())
				put(res, topic, consumerId);
		}
		return res;
	}
}
