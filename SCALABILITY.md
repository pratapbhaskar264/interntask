# Scalability Notes

Short note on how this system would evolve as load and team size grow, as requested in the
assignment deliverables.

## 1. Database
- **Connection pooling**: HikariCP (Spring Boot's default) is already in play; tune
  `maximum-pool-size` based on expected concurrent load and DB capacity.
- **Indexing**: add indexes on `tasks.owner_id` and `users.username`/`users.email`
  (the latter two are already unique-constrained, which Postgres indexes automatically).
- **Read replicas**: for read-heavy workloads (e.g. `GET /tasks` at scale), route reads to
  a replica and writes to the primary via Spring's routing DataSource or a proxy like PgBouncer.
- **Migrations**: replace `ddl-auto: update` with Flyway/Liquibase so schema changes are
  versioned and reviewable, not inferred from entity classes.

## 2. Caching
- Cache read-heavy, rarely-changing data (e.g. a user's task count, admin dashboards) with
  **Redis**, using `@Cacheable` on service methods and explicit eviction on writes.
- Cache JWT blacklist/revocation state in Redis if refresh-token revocation is added, so
  logout/revoke checks don't hit Postgres on every request.

## 3. Horizontal Scaling
- The API is already **stateless** (JWT auth, no server-side sessions), so it can scale
  horizontally behind a load balancer with zero sticky-session requirements — just add
  more instances.
- Package the app as a container (Docker) and run it on Kubernetes / ECS with an
  autoscaler tied to CPU/request-latency metrics.

## 4. Service Decomposition
- At current scope, a modular monolith is the right call — it's simpler to develop,
  test, and deploy for a small team.
- If it grows, the natural seams are already visible in the package structure:
  `auth` (users, tokens) could split into its own **Auth/Identity service**, and
  `tasks` into a **Task service**, communicating over REST/gRPC or async messaging
  (Kafka/RabbitMQ) for events like "task created" → notifications.

## 5. Observability & Reliability
- Add structured logging (JSON) + correlation IDs per request for tracing across services.
- Expose Spring Boot Actuator (`/actuator/health`, `/actuator/metrics`) and scrape with
  Prometheus + Grafana dashboards.
- Add rate limiting (e.g. Bucket4j or an API gateway like Kong/Nginx) in front of
  `/auth/login` and `/auth/register` to blunt brute-force and abuse.

## 6. API Evolution
- API versioning is already baked in via the `/api/v1/` prefix, so breaking changes can
  ship as `/api/v2/` without disrupting existing clients.
- Pagination is already implemented on list endpoints (`Pageable`) to avoid unbounded
  response sizes as data grows.

## Summary
The current design (stateless JWT auth, layered architecture, paginated APIs, versioned
routes) is intentionally chosen so the *easy* scaling levers — more app instances, a
cache layer, read replicas — can be pulled without an architectural rewrite. The bigger
step (splitting into separate services) only makes sense once team size or traffic
actually demands the added operational complexity.
