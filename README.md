# Prerequisites 
- Java 21 or higher
- Apache Maven
- Docker 20.10.14 or higher (build images, docker-compose)
- [Kalix CLI](https://docs.kalix.io/kalix/install-kalix.html) (required only for a local console run and managing service on Kalix Cloud runtime)

# Build 
In root Maven project (mono repo root) run:
```shell
mvn install
```
## Testing
# Unit tests
in each Maven module/project (`cinema-wallet`, `cinema-show`, `cinema-seat-booking`) run:
```shell
mvn test
```

## Integration tests
in each Maven module/project (`cinema-wallet`, `cinema-show`) (<b>excluding</b> `cinema-seat-booking`) run:
```shell
mvn verify
```
### Multi-project integration test
in each Maven module/project (`cinema-wallet`, `cinema-show`) (<b>excluding</b> `cinema-seat-booking`) run:
```shell
mvn exec:java
```
Run `cinema-seat-booking` Maven project:
```shell
mvn verify
```

# Run locally
in each Maven module/project (`cinema-wallet`, `cinema-show`, `cinema-seat-booking`) run: 
```shell
mvn exec:java
```

# Demo (test) locally
Create show:
```shell
curl -XPOST -d '{
  "title": "title",
  "seatPrice": 10,
  "maxSeats": 5
}' http://localhost:9000/cinema-show/1 -H "Content-Type: application/json"
```
Get show:
```shell
curl -XGET http://localhost:9000/cinema-show/1 -H "Content-Type: application/json"
```
Get show's seat:
```shell
curl -XGET http://localhost:9000/cinema-show/1/seat-status/1 -H "Content-Type: application/json"
```

Create wallet with initial balance:
```shell
curl -XPOST http://localhost:9001/wallet/1/create/100 -H "Content-Type: application/json"
```

Get wallet:
```shell
curl -XGET http://localhost:9001/wallet/1 -H "Content-Type: application/json"
```

Start seat booking:
```shell
curl -XPOST -d '{
  "showId": "1",
  "seatNumber": 1,
  "walletId": "1"
}' http://localhost:9002/seat-booking/res1 -H "Content-Type: application/json"
```
Get seat reservation state:
```shell
curl -XGET http://localhost:9002/seat-booking/res1 -H "Content-Type: application/json"
```

# Deploy
## Configure KCR (Kalix Container Registry)
https://docs.kalix.io/operations/container-registries.html#_kalix_container_registry
## Build images
In project root:
```shell
mvn install -DskipTests
```
### Check local build images:
```shell
docker images | grep -v latest | head -4
```
## Deploy each service (with image push)
In each module execute
```shell
akka service deploy <service name> <service name>:tag --push
```
### Example:
```shell
akka service deploy cinema-wallet cinema-wallet:1.0-SNAPSHOT-20250128094523 --push
```
```shell
akka service deploy cinema-show cinema-show:1.0-SNAPSHOT-20250116090616 --push
```
```shell
akka service deploy cinema-util cinema-util:1.0-SNAPSHOT-20250116090616 --push
```
# Demo (test) in Cloud runtime
## Set proxies:
```shell
kalix service proxy cinema-show --port 9000
```
```shell
kalix service proxy cinema-wallet --port 9001
```
```shell
kalix service proxy cinema-seat-booking --port 9002
```
### Test (demo)
Run the same commands as with local test.




