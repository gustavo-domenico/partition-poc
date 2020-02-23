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
| c3       | t1 p0, t2 p0, t3 p0 |

