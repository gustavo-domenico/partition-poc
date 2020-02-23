package partition.poc.messaging

import org.apache.kafka.clients.consumer.internals.PartitionAssignor
import org.apache.kafka.common.TopicPartition

import static java.lang.Byte.MAX_VALUE
import static java.lang.Short.BYTES
import static java.nio.ByteBuffer.allocate
import static java.nio.ByteBuffer.wrap

trait ObjectLabels {
	static def sub(topics) { new PartitionAssignor.Subscription(topics) }

	static def subc(topics) {
		byte[] userData = allocate(BYTES)
				.put(MAX_VALUE)
				.flip() // Prepares the buffer for a read operation in the @assign
				.array()

		new PartitionAssignor.Subscription(topics, wrap(userData))
	}

	static def sub_t1 = sub(["t1"])
	static def sub_t1t2 = sub(["t1", "t2"])
	static def sub_t1t2t3 = sub(["t1", "t2", "t3"])
	static def sub_t2 = sub(["t2"])
	static def no_subs = sub([])

	// This guys must be a function in order to recreate the ByteBuffer every time
	// otherwise we need to manually rewind them.
	static def subc_t1() { subc(["t1"]) }
	static def subc_t1t2() { subc(["t1", "t2"]) }
	static def subc_t1t2t3()  { subc(["t1", "t2", "t3"]) }

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
