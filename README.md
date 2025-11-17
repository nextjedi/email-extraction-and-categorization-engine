# Intelligent Message Processor (IMP)

Production-grade email extraction and categorization engine with microservices architecture, event-driven design, and comprehensive monitoring.

## 🎯 Overview

The Intelligent Message Processor extracts messages from multiple sources (Gmail, WhatsApp, Telegram, SMS) and automatically categorizes them using rule-based and ML strategies. Built with Spring Boot, Kafka, PostgreSQL, Redis, and Kubernetes.

### Key Features

- ✅ **Multi-Source Support**: Gmail, WhatsApp, Telegram, SMS
- ✅ **Intelligent Classification**: Rule-based strategies for 6 categories
- ✅ **Event-Driven Architecture**: Kafka-based message streaming
- ✅ **Microservices**: Independent, scalable services
- ✅ **Production-Ready**: Health checks, metrics, monitoring
- ✅ **Containerized**: Docker & Kubernetes support
- ✅ **Database Per Service**: PostgreSQL with Flyway migrations
- ✅ **Caching & Deduplication**: Redis integration
- ✅ **Observability**: Prometheus, Grafana, comprehensive logging

## 📐 Architecture

```
┌─────────────┐
│   Sources   │  Gmail, WhatsApp, Telegram, SMS
└──────┬──────┘
       │
┌──────▼──────────────┐
│ Source Services     │  Abstract source implementations
│ (Gmail/WA/TG/SMS)   │
└──────┬──────────────┘
       │
┌──────▼──────────────┐
│ Extraction Service  │  Strategy pattern, deduplication
└──────┬──────────────┘
       │
       │ Kafka: raw-messages.extracted
       ▼
┌─────────────────────┐
│Classification       │  Rule-based + ML classification
│Service              │
└──────┬──────────────┘
       │
       │ Kafka: classified.{category}
       ▼
┌─────────────────────┐
│  Processors         │  Job Search, Transaction, etc.
│  (Domain-specific)  │
└─────────────────────┘
```

### Message Categories

1. **Transactional**: Invoices, receipts, payments
2. **Job Search**: Applications, interviews, positions
3. **Subscription**: Newsletters, marketing emails
4. **Travel**: Flights, hotels, bookings
5. **Personal**: Personal conversations
6. **Other**: Unclassified messages

## 🚀 Quick Start

### Prerequisites

- **Java 21**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **8GB+ RAM** (for running all services)

### Local Development Setup

#### 1. Clone Repository

```bash
git clone https://github.com/your-org/intelligent-message-processor.git
cd intelligent-message-processor
```

#### 2. Build All Services

```bash
# Build all modules
mvn clean install

# Or build specific service
mvn clean install -pl services/extraction-service -am
```

#### 3. Start Infrastructure & Services

```bash
cd infrastructure/docker-compose

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Check status
docker-compose ps
```

#### 4. Verify Services

```bash
# Check health endpoints
curl http://localhost:8080/actuator/health  # Extraction Service
curl http://localhost:8081/actuator/health  # Classification Service
curl http://localhost:8090/actuator/health  # Gmail Service

# Access UIs
open http://localhost:3000    # Grafana (admin/admin)
open http://localhost:9090    # Prometheus
open http://localhost:8082    # Kafka UI
```

## 🧪 Testing the System

### End-to-End Test

```bash
# 1. Trigger message extraction
curl -X POST http://localhost:8080/api/v1/extraction/extract \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "sourceType": "GMAIL",
    "fromDate": "2024-01-01T00:00:00",
    "toDate": "2024-12-31T23:59:59",
    "maxResults": 10
  }'

# 2. Check extracted messages
curl http://localhost:8080/api/v1/extraction/users/user123/messages?sourceType=GMAIL

# 3. Verify classification (messages flow through Kafka automatically)
# Check Kafka UI at http://localhost:8082

# 4. View metrics
open http://localhost:3000/d/spring-boot  # Grafana dashboard
```

### Manual Testing with Kafka

```bash
# View Kafka topics
docker exec -it imp-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Consume from raw messages topic
docker exec -it imp-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic raw-messages.extracted \
  --from-beginning

# Consume from classified topic
docker exec -it imp-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic classified.transactional \
  --from-beginning
```

## 📦 Services Overview

| Service | Port | Description | Database |
|---------|------|-------------|----------|
| **Extraction Service** | 8080 | Extracts messages from sources | PostgreSQL (imp_extraction) |
| **Classification Service** | 8081 | Classifies messages by category | PostgreSQL (imp_classification) |
| **Gmail Service** | 8090 | Gmail API integration | - |
| **WhatsApp Service** | 8091 | WhatsApp integration (stub) | - |
| **SMS Service** | 8092 | SMS integration (stub) | - |
| **Job Search Processor** | 8083 | Processes job-related messages | PostgreSQL (imp_processors) |

### Infrastructure Services

| Service | Port | Description |
|---------|------|-------------|
| **PostgreSQL (Extraction)** | 5432 | Extraction database |
| **PostgreSQL (Classification)** | 5433 | Classification database |
| **PostgreSQL (Processors)** | 5434 | Processors database |
| **Redis** | 6379 | Caching & deduplication |
| **Kafka** | 9092 | Event streaming |
| **Zookeeper** | 2181 | Kafka coordination |
| **Prometheus** | 9090 | Metrics collection |
| **Grafana** | 3000 | Metrics visualization |
| **Kafka UI** | 8082 | Kafka management UI |

## 🏗️ Project Structure

```
intelligent-message-processor/
├── shared/                          # Common interfaces, DTOs, constants
│   └── src/main/java/com/imp/shared/
│       ├── api/                     # SourceMessagesApi interface
│       ├── constant/                # Enums, Kafka topics
│       ├── dto/                     # DTOs for messages
│       ├── event/                   # Kafka event models
│       ├── strategy/                # Strategy interfaces
│       └── util/                    # Utilities
│
├── source-services/                 # Source abstraction layer
│   ├── gmail-service/               # Gmail integration
│   ├── whatsapp-service/            # WhatsApp integration
│   └── sms-service/                 # SMS integration
│
├── services/                        # Core services
│   ├── extraction-service/          # Message extraction
│   ├── classification-service/      # Message classification
│   └── job-search-processor/        # Job search processing
│
├── infrastructure/
│   ├── docker/                      # Dockerfiles
│   ├── docker-compose/              # Docker Compose setup
│   │   ├── docker-compose.yml       # Main compose file
│   │   └── prometheus/              # Prometheus config
│   └── kubernetes/                  # K8s manifests
│
├── pom.xml                          # Parent POM
└── README.md                        # This file
```

## 🔧 Configuration

### Environment Variables

Each service supports the following environment variables:

```bash
# Database
DB_HOST=postgres-extraction
DB_PORT=5432
DB_NAME=imp_extraction
DB_USER=imp_user
DB_PASSWORD=dev_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# Kafka
KAFKA_BOOTSTRAP_SERVERS=kafka:29092

# Service URLs
GMAIL_SERVICE_URL=http://gmail-service:8090
```

### Database Credentials (Development)

| Database | User | Password |
|----------|------|----------|
| imp_extraction | imp_user | dev_password |
| imp_classification | imp_user | dev_password |
| imp_processors | imp_user | dev_password |

## 📊 Monitoring & Observability

### Prometheus Metrics

All services expose Prometheus metrics at `/actuator/prometheus`:

- JVM metrics (memory, GC, threads)
- HTTP request metrics
- Custom business metrics
- Kafka consumer/producer metrics

### Grafana Dashboards

Access Grafana at `http://localhost:3000` (admin/admin):

- Spring Boot metrics
- JVM metrics
- Kafka metrics
- Custom application metrics

### Health Checks

All services provide health endpoints:

```bash
# Liveness probe
curl http://localhost:8080/actuator/health/liveness

# Readiness probe
curl http://localhost:8080/actuator/health/readiness

# Detailed health
curl http://localhost:8080/actuator/health
```

## 🎛️ Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `raw-messages.extracted` | Extraction Service | Classification Service | Raw extracted messages |
| `classified.transactional` | Classification Service | Transaction Processor | Transactional messages |
| `classified.job-search` | Classification Service | Job Search Processor | Job-related messages |
| `classified.subscription` | Classification Service | Subscription Processor | Newsletter/marketing |
| `classified.travel` | Classification Service | Travel Processor | Travel-related messages |
| `classified.personal` | Classification Service | Personal Processor | Personal conversations |
| `classified.other` | Classification Service | Other Processor | Unclassified messages |
| `dlq.*` | All Services | Error Handler | Dead letter queue |

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl services/extraction-service
```

### Integration Tests

```bash
# Start test containers
mvn verify -P integration-tests
```

### Load Testing

```bash
# Use k6 or similar tool
k6 run tests/load/extraction-load-test.js
```

## 🚢 Deployment

### Docker Compose (Development)

```bash
cd infrastructure/docker-compose
docker-compose up -d
```

### Kubernetes (Production)

```bash
cd infrastructure/kubernetes

# Deploy infrastructure
kubectl apply -f postgres-deployment.yaml
kubectl apply -f redis-deployment.yaml
kubectl apply -f kafka-deployment.yaml

# Deploy services
kubectl apply -f extraction-service-deployment.yaml
kubectl apply -f classification-service-deployment.yaml
kubectl apply -f job-search-processor-deployment.yaml
```

See [Kubernetes README](infrastructure/kubernetes/README.md) for detailed instructions.

## 🔐 Security Considerations

### Production Checklist

- [ ] Use secrets management (Vault, AWS Secrets Manager)
- [ ] Enable TLS for all services
- [ ] Configure OAuth2 for Gmail API
- [ ] Set up network policies in Kubernetes
- [ ] Enable Kafka encryption and authentication
- [ ] Use strong database passwords
- [ ] Implement rate limiting
- [ ] Enable audit logging
- [ ] Configure backup strategies

## 📈 Scaling

### Horizontal Scaling

Services are designed to scale horizontally:

```bash
# Docker Compose
docker-compose up -d --scale extraction-service=3

# Kubernetes (HPA configured)
kubectl scale deployment extraction-service --replicas=5
```

### Performance Tuning

- **Kafka partitions**: Increase for higher throughput
- **Database connection pools**: Tune based on load
- **Redis cache TTL**: Adjust based on data staleness requirements
- **JVM heap**: Configure based on message size and volume

## 🛠️ Development

### Adding a New Source

1. Create service in `source-services/`
2. Implement `SourceMessagesApi` interface
3. Create extraction strategy in extraction service
4. Add configuration and Dockerfile

### Adding a New Processor

1. Create service in `services/`
2. Subscribe to appropriate Kafka topic
3. Implement domain-specific logic
4. Add database schema if needed

## 📚 API Documentation

### Extraction Service API

```bash
# Extract messages
POST /api/v1/extraction/extract
Content-Type: application/json
{
  "userId": "user123",
  "sourceType": "GMAIL",
  "fromDate": "2024-01-01T00:00:00",
  "toDate": "2024-12-31T23:59:59"
}

# Get user messages
GET /api/v1/extraction/users/{userId}/messages?sourceType=GMAIL

# Get message count
GET /api/v1/extraction/users/{userId}/count?sourceType=GMAIL
```

## 🐛 Troubleshooting

### Common Issues

**Services not starting:**
```bash
# Check logs
docker-compose logs -f extraction-service

# Verify dependencies
docker-compose ps
```

**Kafka connection issues:**
```bash
# Check Kafka logs
docker-compose logs kafka

# Verify topics
docker exec -it imp-kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Database migration errors:**
```bash
# Check Flyway status
docker-compose exec extraction-service ./mvnw flyway:info

# Repair if needed
docker-compose exec extraction-service ./mvnw flyway:repair
```

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📞 Support

- **Documentation**: See `/docs` directory
- **Issues**: GitHub Issues
- **Architecture**: See `GOOGLE-DOCS-DOCUMENTATION.html`

## 🎓 Architecture Decisions

For detailed architecture decisions, rationale, and design patterns, see:
- [GOOGLE-DOCS-DOCUMENTATION.html](GOOGLE-DOCS-DOCUMENTATION.html)

Key decisions:
- **3-tier pipeline**: Extract → Classify → Process
- **Source services abstraction**: Easy library swapping
- **Event-driven**: Kafka for loose coupling
- **Per-source schemas**: Optimized for each source type
- **Hybrid storage**: PostgreSQL + Redis
- **Polyglot**: Java + Python where appropriate

---

**Built with ❤️ for production-grade message processing**
