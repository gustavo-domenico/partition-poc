package partition.poc.messaging

import org.apache.kafka.clients.consumer.internals.PartitionAssignor
import org.apache.kafka.common.TopicPartition

class ObjectLabels {
	static def sub(topics) { new PartitionAssignor.Subscription(topics) }
	static def sub_t1 = sub(["t1"])
	static def sub_t1t2 = sub(["t1", "t2"])
	static def sub_t1t2t3 = sub(["t1", "t2", "t3"])
	static def sub_t2 = sub(["t2"])
	static def no_subs = sub([])

	static def tp(topic, partition) { new TopicPartition(topic, partition) }
	static def tp_t1_0 = tp("t1", 0)
	static def tp_t1_1 = tp("t1", 1)
	static def tp_t1_2 = tp("t1", 2)

	static def tp_t2_0 = tp("t2", 0)
	static def tp_t2_1 = tp("t2", 1)
	static def tp_t2_2 = tp("t2", 2)

	static def tp_t3_0 = tp("t3", 0)
	static def tp_t3_1 = tp("t3", 1)
	static def tp_t3_2 = tp("t3", 2)
}
