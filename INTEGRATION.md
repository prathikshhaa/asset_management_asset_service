# asset-service — Integration Guide for the rest of the platform

## 1. Service registration
- `asset-service` registers itself with Eureka (`EUREKA_ENABLED=true`, `EUREKA_URL=http://<eureka-host>:8761/eureka/`).
- The Gateway should route `/assets/**` (and any new prefixes you add) to `lb://asset-service`.
- Until Eureka is actually running in the project, leave `EUREKA_ENABLED=false` — the service runs standalone, the dependency is just ready.

## 2. Who calls whom
| Caller | Calls | Why |
|---|---|---|
| Gateway | asset-service (all `/assets/**` GET/POST/PUT/PATCH/DELETE) | proxies end-user requests, forwards user JWT |
| assignment-service | `PATCH /assets/{id}/status` | flips asset to `ASSIGNED` when an asset is handed to a user, back to `AVAILABLE` on return |
| maintenance-service | `PATCH /assets/{id}/status` | flips asset to `MAINTENANCE` when a repair ticket opens, back to `AVAILABLE` when closed |
| assignment-service / maintenance-service | `GET /assets/{id}` | check current status before attempting an assignment/repair (avoid assigning an asset already `ASSIGNED`) |

## 3. Authenticating service-to-service calls
`PATCH /assets/{id}/status` is locked down — only callers that send a shared header are allowed:

```
X-Internal-Api-Key: <INTERNAL_API_KEY env var value>
```

Set the **same** `INTERNAL_API_KEY` value as an environment variable on asset-service, assignment-service, and maintenance-service. Example call from assignment-service:

```http
PATCH http://asset-service/assets/{id}/status
X-Internal-Api-Key: <shared-key>
Content-Type: application/json

{ "status": "ASSIGNED", "reason": "Assigned to employee EMP-204" }
```

Every status change is recorded in `asset_history` (`GET /assets/{id}/history`) — this is the audit trail other services and admins can use to answer "who changed this and when."

## 4. End-user authentication (JWT)
Code is already wired (`JwtAuthFilter`, `JwtUtil`) but **disabled** (`JWT_ENABLED=false`) until your platform agrees on a token contract. When user-service / gateway issues JWTs:
1. Set `JWT_SECRET` to the same HMAC secret across every service.
2. Flip `JWT_ENABLED=true` on asset-service.
3. Tokens must contain a `sub` (username) and a `roles` claim (list of strings, e.g. `["ADMIN"]`).

## 5. Recommended next contract decisions for the team
- Confirm Eureka vs. plain Gateway routing (asset-service supports either).
- Confirm the JWT claim shape above with user-service so all services agree.
- Decide whether assignment-service/maintenance-service should *also* call `GET /assets/{id}` first to verify current status server-side (recommended, to avoid race conditions — two services trying to claim the same asset).
