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

### Run

Run the docker compose file in the `docker` folder:

`docker-compose up`

Then you can start Spring Boot instances:

`./gradew bootRun`

or canary insances:

`IS_CANARY_INSTANCE=true ./gradlew bootRun`

You should see in the logs that the canary instances had priority in the partition assignment:

~~~~
 p.p.m.assignor.CanaryPriorityAssignor    : Assigned member paritionPoc-0-8480b121-4d49-4812-9e29-d104572ffcc9(isCanary: true) to topic/partitions [mySecondTopic-0].
 
p.p.m.assignor.CanaryPriorityAssignor    : Assigned member paritionPoc-0-b2bb1529-32cb-4409-ad9a-4b8fc6c717c3(isCanary: true) to topic/partitions [mySecondTopic-1].

p.p.m.assignor.CanaryPriorityAssignor    : Assigned member paritionPoc-0-d33b543e-4726-45a3-b48e-1873e95a147d(isCanary: false) to topic/partitions [mySecondTopic-2].

p.p.m.assignor.CanaryPriorityAssignor    : Assigned member paritionPoc-0-69f3b2e1-0237-428a-b1a2-04e68609c1bf(isCanary: false) to topic/partitions [].

p.p.m.assignor.CanaryPriorityAssignor    : Assigned member paritionPoc-0-49025c9f-f0bb-4a30-9764-449eb82d9d6e(isCanary: false) to topic/partitions [].
~~~~

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

Example of scenarios:

| partitionsPerTopic | subscriptions | expectedAssignments | Description                                                                                                                      |
|--------------------|---------------|---------------------|----------------------------------------------------------------------------------------------------------------------------------|
| [t1: 1]            | [c1: sub_t1]  | [c1: [tp_t1_0]]     | We have one topic t1 and one subscription, so the expected assignment is the consumer for the topic t1 and the only partition 0. |
| [t1: 1]            | [c1: sub_t1, c2c: subc_t1()]  | [c1: [], c2c: [tp_t1_0]]     | We have one topic t1 and two subscriptions, so the expected assignment is the canary consumer for the topic t1 and the only partition 0. |

