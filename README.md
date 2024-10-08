# Product Availability Service
TO BE POPULATED
Initial Configuration
---
#### Required
- Run a postgreSQL container 
- Verify that your DB credentials and uri matches with the ones that are in the [application-local.yaml]
- Notice that there are more customizable properties via environment variables, such as the uris that point to the others services.

Environments
---
TO BE POPULATED

Technologies
---
Technologies used in the project:
- Spring actuator
- Prometheus
- Spring cloud gateway, based on [spring webflux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
- Spring cloud sleuth
- Redis
- Spring data R2DBC
