1. Run post_connectors.sh (in config folder context)
2. Run producers (*Producer.java)
3. Run Proje3Streams.java


Checks:
1. check with kafka-topics.sh --bootstrap-server broker1:9092 --list
2. see data in postgre container
3. hookup to the producer streams with kafka-console-consumer.sh --bootstrap-server broker1:9092 --include "Proj3SockPurchasesTopic|Proj3SockSalesTopic" --from-beginning

TP3-base/
│
├── devcontainer
│   ├── docker-compose-cluster.yml
│   ├── docker-compose-standalone.yml
│   └── Dockerfile
│
├── config/
│   ├── delete_connectors.sh
│   ├── get_connectors.sh
│   ├── post_connectors.sh
│   ├── sink.json
│   └── source.json
│
├── lib/
│   ├── (several required .jar libraries)
│
├── sql/
│   └── create_tables.sql
│
├── target/
│
├── commands.sh
│
└── pom.xml


NEW

TP3-project3/
│
├── devcontainer/
│   ├── docker-compose-cluster.yml        ✅ (you already have)
│   ├── docker-compose-standalone.yml
│   └── Dockerfile
│
├── config/
│   ├── source.json                       ✅ JDBC source (DB → Kafka)
│   ├── sink.json                         ✅ JDBC sink (Kafka → DB)
│   ├── post_connectors.sh                (start connectors)
│   ├── get_connectors.sh
│   ├── delete_connectors.sh
│
├── sql/
│   └── create_tables.sql                ✅ DB schema
│
├── lib/                                  (Kafka Connect JDBC jars)
│
├── src/main/java/is/project3/
│   │
│   ├── Main.java                         🚀 starts everything
│   │
│   ├── config/
│   │   └── KafkaConfig.java             (bootstrap, serde, properties)
│   │
│   ├── models/
│   │   ├── Sale.java                    (item, price, units, country, ts)
│   │   ├── Purchase.java                (item, price, units, ts)
│   │   ├── ItemStats.java              (revenue, expense, profit)
│   │   ├── CountryStats.java
│   │   └── AggregateStats.java
│   │
│   ├── producers/
│   │   ├── SalesProducer.java          🎯 simulates customers
│   │   ├── PurchaseProducer.java       🎯 simulates suppliers
│   │   └── DBInfoProducer.java         (optional seed data)
│   │
│   ├── streams/
│   │   ├── AnalyticsTopology.java      🔥 ALL Kafka Streams logic
│   │   ├── RevenueProcessor.java
│   │   ├── ExpenseProcessor.java
│   │   ├── ProfitProcessor.java
│   │   ├── WindowedStatsProcessor.java
│   │   └── RankingProcessor.java
│   │
│   ├── rest/
│   │   ├── RestServer.java             🌐 API layer
│   │   ├── StatsController.java
│   │   └── DatabaseClient.java
│   │
│   ├── cli/
│   │   └── CommandLineApp.java         💻 admin interface
│   │
│   └── util/
│       ├── JsonUtils.java              (Gson helper)
│       ├── TopicNames.java             (central constants)
│       └── RandomDataGenerator.java    (fake data)
│
├── target/
│
├── commands.sh                          🧪 Kafka testing helpers
├── pom.xml                              Maven config
└── README.md                            (important for grading)