# partition-poc

Using a custom PartitionAssignor in order to prioritize canary instances to receive traffic from kafka.

### Current problem

Suppose that your have the scenario below for 3 consumers:

| Topics | Partition | 
|--------|-----------|
|  t1    |     2     |
|  t2    |     2     |
|  t3    |     2     |

You are likely to have the following assingment table:

| Consumer | Assignment          |
|----------|---------------------|
| c1       | t1 p0, t2 p0, t3 p0 |
| c2       | t1 p1, t2 p1, t3 p1 |
| c3       |                     |

Where `t1 p0` means the topic and the partition assigned.

### Proposal

When starting a canary deployment, we could inject an environment variable `IS_CANARY_INSTANCE` that will prioritize that particular instance. In the scenario above, the following assignment table would have been created:

| Consumer | Assignment          |
|----------|---------------------|
| c1       | t1 p1, t2 p1, t3 p1 |
| c2       |                     |
| c3c      | t1 p0, t2 p0, t3 p0 |

The consumer `c3c` mean it is a canary instance.

### Tests

The tests are using the following convention:

| Term       | Description     | Example                                                                  |
|------------|-----------------|--------------------------------------------------------------------------|
| tX         | Topic           | t1 means a topic named t1                                                |
| cX         | Consumer        | c2 means a consumer named c2                                             |
| cXc        | Canary consumer | c3c means a consumer from a canary instance named c3.                    |
| tp_tX_Y    | Topic partition | tp_t1_2 means a topic partition assignment for topic t1 and partition 2. |
| sub_tX...  | Subscription    | sub_t1t2 means that the consumer subscribes to the topics t1 and t2.     | 
| subc_tX... | Subscription    | subc_t1 means that the canary consumer subscribes to the topic t1.       | 

Example of scenario:

| partitionsPerTopic | subscriptions | expectedAssignments | Description                                                                                                                      |
|--------------------|---------------|---------------------|----------------------------------------------------------------------------------------------------------------------------------|
| [t1: 1]            | [c1: sub_t1]  | [c1: [tp_t1_0]]     | We have one topic t1 and one subscription, so the expected assignment is the consumer for the topic t1 and the only partition 0. |

