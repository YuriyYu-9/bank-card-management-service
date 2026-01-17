# Bank Card Management Service

Backend-сервис (Spring Boot) для управления банковскими картами:
- Админ создаёт/управляет картами и пользователями
- Пользователь видит свои карты, делает переводы между своими картами
- Пользователь может запросить блокировку карты, админ обрабатывает запрос

## Технологии
- Java 21, Spring Boot 3.4.x
- Spring Security (JWT, stateless)
- Spring Data JPA, PostgreSQL
- Liquibase migrations
- springdoc-openapi (Swagger UI)
- Docker Compose (dev DB)

## Быстрый старт (Dev)

### 1) Поднять PostgreSQL
```bash
docker compose up -d 
