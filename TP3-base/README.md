How to run the project:

1 - Run maven
mvn clean package

2 - Start infrastructure (Docker)
cd .devcontainer
docker compose -f docker-compose-cluster.yml up -d

check if brokers are running:
docker ps

you should see, among other results:
609adb7c1749   confluentinc/cp-kafka:7.5.1       "/etc/confluent/dock…"   28 hours ago   Up 9 seconds                      9092/tcp, 0.0.0.0:29094->29094/tcp, [::]:29094->29094/tcp                       devcontainer-broker3-1
7eb91f94d455   confluentinc/cp-kafka:7.5.1       "/etc/confluent/dock…"   28 hours ago   Up 9 seconds                      9092/tcp, 0.0.0.0:29093->29093/tcp, [::]:29093->29093/tcp                       devcontainer-broker2-1
f85506569599   confluentinc/cp-kafka:7.5.1       "/etc/confluent/dock…"   28 hours ago   Up 9 seconds                      9092/tcp, 0.0.0.0:29092->29092/tcp, [::]:29092->29092/tcp                       devcontainer-broker1-1

3 - Run from project root (in another terminal)
cd TP3-base
mvn exec:java -Dexec.mainClass="Main"

Project topology:
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