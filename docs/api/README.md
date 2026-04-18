# API Documentation

## Standard
- OpenAPI 3
- Swagger UI via `springdoc-openapi`
- Versioned endpoints under `/api/v1/**`

## Runtime URLs
- JSON: `/v3/api-docs`
- YAML: `/v3/api-docs.yaml`
- Swagger UI: `/swagger-ui.html`

## Grouped API URLs
- `/v3/api-docs/all`
- `/v3/api-docs/analytics`
- `/v3/api-docs/carts`
- `/v3/api-docs/catalog`
- `/v3/api-docs/chat`
- `/v3/api-docs/orders`
- `/v3/api-docs/promotions`
- `/v3/api-docs/reviews`
- `/v3/api-docs/search`
- `/v3/api-docs/users`

## Generate And Export
Run:

```powershell
./mvnw -Papi-docs verify
```

This generates and exports:
- `docs/api/openapi.yaml`
- `docs/api/exports/frontend/openapi.yaml`
- `docs/api/exports/qa/openapi.yaml`

## Postman Flow
Import `docs/api/openapi.yaml` into Postman to generate the collection used by frontend and QA review.

## CI Drift Check
GitHub Actions regenerates `openapi.yaml` and fails if the committed documentation is out of sync with the code.
