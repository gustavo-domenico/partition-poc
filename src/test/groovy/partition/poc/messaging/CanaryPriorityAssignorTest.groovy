package partition.poc.messaging

import partition.poc.messaging.assignor.CanaryPriorityAssignor
import spock.lang.Specification
import spock.lang.Unroll

import static java.lang.Byte.MAX_VALUE
import static java.lang.Byte.MIN_VALUE
import static partition.poc.messaging.ObjectLabels.no_subs
import static partition.poc.messaging.ObjectLabels.sub_t1
import static partition.poc.messaging.ObjectLabels.sub_t1t2
import static partition.poc.messaging.ObjectLabels.sub_t1t2t3
import static partition.poc.messaging.ObjectLabels.sub_t2
import static partition.poc.messaging.ObjectLabels.tp_t1_0
import static partition.poc.messaging.ObjectLabels.tp_t1_1
import static partition.poc.messaging.ObjectLabels.tp_t1_2
import static partition.poc.messaging.ObjectLabels.tp_t2_0
import static partition.poc.messaging.ObjectLabels.tp_t2_1
import static partition.poc.messaging.ObjectLabels.tp_t2_2
import static partition.poc.messaging.ObjectLabels.tp_t3_0
import static partition.poc.messaging.ObjectLabels.tp_t3_1
import static partition.poc.messaging.ObjectLabels.tp_t3_2

class CanaryPriorityAssignorTest extends Specification {
	private static final String IS_CANARY_INSTANCE = "IS_CANARY_INSTANCE"

	CanaryPriorityAssignor assignor = new CanaryPriorityAssignor()

	@Unroll
	def "subscription for #topics when canary instance is #isCanaryInstance should be #userData."() {
		given:
			assignor.environmentProvider = {
				name ->
					if (name == IS_CANARY_INSTANCE)
						return isCanaryInstance.toString()
					else
						throw new RuntimeException("Invalid name.")
			}
		when:
			def subscription = assignor.subscription(topics as Set)
		then:
			subscription.topics() == topics
			subscription.userData().get() == userData
		where:
			isCanaryInstance | topics       | userData
			false            | ["t1", "t2"] | MIN_VALUE
			"false"          | ["t1", "t2"] | MIN_VALUE
			true             | ["t1", "t2"] | MAX_VALUE
			"true"           | ["t1", "t2"] | MAX_VALUE
			null             | ["t1", "t2"] | MIN_VALUE
	}

	@Unroll
	def "assign for #partitionsPerTopic using #subscriptions should be #expectedAssignments"() {
		/*
		 */
		when:
			def actualAssignments = assignor.assign(partitionsPerTopic, subscriptions)
		then:
			actualAssignments == expectedAssignments
		where:
			partitionsPerTopic    | subscriptions                                    | expectedAssignments

			// Basic scenarios with one topic, multiple consumers and no canary priority
			[t1: 1]               | [c1: sub_t1]                                     | [c1: [tp_t1_0]]
			[t1: 2]               | [c1: sub_t1]                                     | [c1: [tp_t1_0, tp_t1_1]]
			[t1: 0]               | [c1: sub_t1]                                     | [c1: []]
			[t1: 1]               | [c1: no_subs]                                    | [c1: []]
			[t1: 1]               | [c1: sub_t1, c2: sub_t1]                         | [c1: [tp_t1_0], c2: []]
			[t1: 2]               | [c1: sub_t1, c2: sub_t1]                         | [c1: [tp_t1_0], c2: [tp_t1_1]]

			// Scenarios with multiple topics, multiple consumers and no canary priority
			[t1: 2, t2: 3]        | [c1: sub_t1t2]                                   | [c1: [tp_t1_0, tp_t1_1, tp_t2_0, tp_t2_1, tp_t2_2]]
			[t1: 2, t2: 1, t3: 0] | [c1: sub_t1t2t3]                                 | [c1: [tp_t1_0, tp_t1_1, tp_t2_0]]
			[t1: 2, t2: 1, t3: 1] | [c1: sub_t1t2t3]                                 | [c1: [tp_t1_0, tp_t1_1, tp_t2_0, tp_t3_0]]
			[t1: 2, t2: 2, t3: 2] | [c1: sub_t1t2t3, c2: sub_t1t2t3]                 | [c1: [tp_t1_0, tp_t2_0, tp_t3_0], c2: [tp_t1_1, tp_t2_1, tp_t3_1]]
			[t1: 2, t2: 2, t3: 2] | [c1: sub_t1t2t3, c2: sub_t1t2t3, c3: sub_t1t2t3] | [c1: [tp_t1_0, tp_t2_0, tp_t3_0], c2: [tp_t1_1, tp_t2_1, tp_t3_1], c3: []]
			[t1: 2, t2: 2, t3: 3] | [c1: sub_t1t2t3, c2: sub_t1t2t3, c3: sub_t1t2t3] | [c1: [tp_t1_0, tp_t2_0, tp_t3_0], c2: [tp_t1_1, tp_t2_1, tp_t3_1], c3: [tp_t3_2]]
			[t1: 3, t2: 3]        | [c1: sub_t1t2, c2: sub_t1t2, c3: sub_t2]         | [c1: [tp_t1_0, tp_t1_1, tp_t2_0, ], c2: [tp_t1_2, tp_t2_1], c3: [tp_t2_2]]


	}
}