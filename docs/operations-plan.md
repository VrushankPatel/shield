# Operations Plan

## 1. Service Topology Decision
Current backend is a modular monolith, so runtime deployment is:
- Multiple `app` instances of one Spring Boot service
- One PostgreSQL instance (or managed external DB in production)
- One Redis instance
- One reverse proxy/load balancer (`haproxy` or `nginx`)

## 2. Recommended Instance Baselines
Use these as starting points and tune with load testing:
- Small society (<= 500 units): `2` app instances + `1` proxy
- Medium society (500-2000 units): `4` app instances + `1` proxy
- Large society (> 2000 units): `6-8` app instances + `2` proxies behind external LB

## 3. Load Balancing Strategy
- Default LB algorithm:
  - HAProxy: `roundrobin`
  - NGINX: `least_conn`
- Sticky sessions are not required because auth is JWT-based and stateless.
- Keep database and redis on private network only; expose only proxy port to host.

## 4. State and Persistence
Local/self-hosted persistent paths (kept outside containers):
- PostgreSQL: `db_files/postgres`
- Redis AOF: `db_files/redis`

This ensures container recreation does not wipe data.

## 5. One-Click Runtime
`run.sh` generates and runs per-topology folders:
- `System2NodesHaProxy`
- `System4NodesNginx`

Commands:
```bash
./run.sh --instances 4 --proxy haproxy
./run.sh --instances 2 --proxy nginx
./run.sh --instances 4 --proxy haproxy --down
```

## 6. Production Hardening Next Steps
- External managed PostgreSQL with automated backups + PITR
- Multi-AZ deployment for app/proxy
- TLS termination at edge LB or reverse proxy
- Centralized logs and metrics (Prometheus + Grafana + ELK/Loki)
- Horizontal autoscaling policy from CPU/RPS/SLA thresholds
