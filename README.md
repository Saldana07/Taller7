# Taller 7 – API REST Java en Kubernetes (réplica del ejemplo de clase)

API REST Java 17 (Spring Boot + JPA + Swagger + Flyway + Lombok) con PostgreSQL 11,
desplegada en Minikube con 2 réplicas y un balanceador de carga, en el namespace `demo-app`.
Sigue la guía *"Creando un API Rest Java + Postgres en MiniKube"*.

## Estructura

```
java-api-demo/
├── pom.xml
├── Dockerfile
├── src/main/java/co/edu/univalle/demo/
│   ├── DemoApplication.java
│   ├── model/Student.java              # entidad (tabla student) con Lombok
│   ├── repository/StudentRepository.java  # JpaRepository + consultas propias
│   ├── service/StudentService.java     # lógica de negocio
│   └── controller/
│       ├── StudentController.java      # /api/demo/student/...
│       └── InfoController.java         # /api/demo/info (muestra el pod)
├── src/main/resources/
│   ├── application.yml                 # perfil activo + config común
│   ├── application-local.yml           # BD en 127.0.0.1:2392 (IDE)
│   ├── application-k8s.yml             # BD en postgres-service.demo-app.svc.cluster.local
│   ├── application-aws.yml             # BD en la nube (no se usa en el taller)
│   └── db/migration/V1__create_table_student.sql   # Flyway
├── k8s/
│   ├── postgres-deployment.yaml        # Secret + Deployment + Service de PostgreSQL
│   └── java-api-demo-deployment.yaml   # Service LoadBalancer + Deployment (2 réplicas)
└── postman/Demo-REST-Service.postman_collection.json
```

## Endpoints (base: `http://<host>:8085/api/demo`)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/student/all` | Todos los estudiantes |
| GET | `/student/id?id=1` | Por id |
| GET | `/student/name?name=Manuel` | Por nombre |
| GET | `/student/age?age=30` | Por edad |
| POST | `/student/save` | Crear `{"name","lastName","age","state"}` |
| PUT | `/student/update` | Actualizar (el body incluye `id`) |
| DELETE | `/student/delete?id=1` | Eliminar |
| GET | `/info` | Equipo y **pod** que respondió (evidencia del balanceo) |
| GET | `/actuator/health` | Salud (readiness / liveness) |
| GET | `/swagger-ui.html` | Documentación Swagger |

## Requisitos (PowerShell en Windows)

`minikube version` · `kubectl version --client` · `docker version` · `java -version` (17) · `mvn -version` · pgAdmin 4 · Postman · cuenta en Docker Hub.

## Paso a paso

### Paso 1 — Iniciar Minikube
```powershell
minikube start --driver=docker
minikube status
kubectl get nodes
```

### Paso 2 — Crear el namespace
```powershell
kubectl create namespace demo-app
kubectl get namespaces
```

### Paso 3 — Levantar PostgreSQL
```powershell
kubectl apply -f .\k8s\postgres-deployment.yaml
kubectl get pods -n demo-app
kubectl get svc -n demo-app
```

### Paso 4 — Probar la BD con pgAdmin
En **otra consola** (no cerrarla):
```powershell
kubectl port-forward svc/postgres-service 2392:2392 -n demo-app
```
pgAdmin → nueva conexión `Postgres-MiniKube`: host `127.0.0.1`, puerto `2392`, usuario `postgres`, password `postgres`. Debe aparecer la base `demo`.

### Paso 5 — Probar el API desde el IDE (perfil `local`)
Con el port-forward del paso 4 abierto, correr `DemoApplication` desde el IDE
(o `mvn spring-boot:run`). Flyway crea la tabla `student`.
Importar `postman/Demo-REST-Service.postman_collection.json` en Postman y probar
`createStudent`, `StudentById`, `findAllStudents`…  (baseUrl = `http://127.0.0.1:8085/api/demo`).

### Paso 6 — Construir la imagen y subirla a Docker Hub
```powershell
docker login
docker build -t TU_USUARIO/java-api-demo:latest .
docker images
docker push TU_USUARIO/java-api-demo:latest
```
> El Dockerfile compila el proyecto con Maven dentro de Docker, así que no hace falta
> correr `mvn package` antes.

### Paso 7 — Desplegar el API en Kubernetes
1. En `k8s/java-api-demo-deployment.yaml` reemplazar `TU_USUARIO_DOCKERHUB` y `TEAM_NAME`.
2. Aplicar:
```powershell
kubectl apply -f .\k8s\java-api-demo-deployment.yaml
kubectl get pods -n demo-app        # esperar 2/2 pods del API en READY 1/1 (≈2 min por las probes)
kubectl get svc -n demo-app
```

### Paso 8 — Exponer el API con `minikube tunnel`
En **otra consola** (como administrador, no cerrarla):
```powershell
minikube tunnel
kubectl get svc -n demo-app         # java-api-demo-service ya tiene EXTERNAL-IP
```
Probar en Postman con `http://127.0.0.1:8085/api/demo/student/all`.
Llamar varias veces a `/info`: el campo `pod` cambia entre las 2 réplicas.

### Paso 9 — Que el otro equipo consuma su API (y ustedes el de ellos)
`minikube tunnel` solo publica el API en **su propio** computador (127.0.0.1).
Para que otro equipo en la misma red Wi-Fi lo consuma:
```powershell
kubectl port-forward --address 0.0.0.0 svc/java-api-demo-service 8085:8085 -n demo-app
ipconfig            # su IPv4, ej. 192.168.1.50
# Permitir el puerto en el firewall (PowerShell como administrador):
New-NetFirewallRule -DisplayName "Taller7-8085" -Direction Inbound -LocalPort 8085 -Protocol TCP -Action Allow
```
Compartan: `http://<SU-IP>:8085/api/demo/student/all`

Para consumir el API del otro equipo: en Postman, carpeta **"API del otro equipo"**,
cambien la variable `otroEquipoUrl` a `http://<IP-OTRO-EQUIPO>:8085/api/demo`.
Si están en redes distintas: `ngrok http 8085` y compartir la URL pública.

### Comandos útiles para evidencias
```powershell
kubectl get all -n demo-app
kubectl describe deployment java-api-demo-deployment -n demo-app
kubectl logs -n demo-app -l app=java-api-demo --tail=30
```

### Limpiar
```powershell
kubectl delete namespace demo-app
```

## Problemas comunes
- **Pods del API en `0/1` mucho tiempo:** normal los primeros ~2 min (`initialDelaySeconds: 120`). Si siguen así, `kubectl logs` para ver el error.
- **`ImagePullBackOff`:** la imagen no está en Docker Hub con ese nombre, o el repositorio es privado.
- **`Connection refused` a la BD en el IDE:** falta el `port-forward` del paso 4.
- **`EXTERNAL-IP <pending>`:** falta `minikube tunnel` corriendo.
