# config-repo

Configuration files served by `config-server` (port 8888, `native` profile).

- `application.yml`: shared by all services (Eureka URL, actuator exposure).
- `<service-name>.yml`: per-service values, matched on `spring.application.name`.
- `<service-name>-<profile>.yml`: per-profile overrides.

Check what a service receives: `http://localhost:8888/api-gateway/default`.

Do not put real secrets here. Use environment variables, e.g. `${DB_PASSWORD}`.
