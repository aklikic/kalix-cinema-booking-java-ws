# Prerequisites 
- Java 17 or higher
- Apache Maven
- Docker 20.10.14 or higher (build images, docker-compose)
- [Kalix CLI](https://docs.kalix.io/kalix/install-kalix.html) (required only for a local console run and managing service on Kalix Cloud runtime)

# How to use this repo

## Run and play with the code
Skip to [Local test](#local-test)<br>

## Developer Experience 
Use this repo as a reference implementation and use [Create kickstart maven project](#create-kickstart-maven-project) for kickstarting your own project.

# Create kickstart maven project
```
mvn archetype:generate \
  -DarchetypeGroupId=io.kalix \
  -DarchetypeArtifactId=kalix-spring-boot-archetype \
  -DarchetypeVersion=1.4.1
```
Define value for property 'groupId': `com.example`<br>
Define value for property 'artifactId': `cinema-seat-booking`<br>
Define value for property 'version' 1.0-SNAPSHOT: :<br>
Define value for property 'package' com.example: : `com.example.cinema.booking`<br>

# Setup
## Dependencies
```
    <dependency>
      <groupId>com.example.cinema</groupId>
      <artifactId>cinema-wallet-api</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>

    <dependency>
      <groupId>com.example.cinema</groupId>
      <artifactId>cinema-show-api</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
    
    <dependency>
      <groupId>org.awaitility</groupId>
      <artifactId>awaitility</artifactId>
      <version>4.2.0</version>
      <scope>test</scope>
    </dependency>
```
## Discovery
application.conf
```
kalix.dev-mode.service-port-mappings.cinema-show="localhost:9000"
kalix.dev-mode.service-port-mappings.cinema-wallet="localhost:9001"
```
docker-compose:
```
-Dkalix.dev-mode.service-port-mappings.cinema-show=host.docker.internal:9000
-Dkalix.dev-mode.service-port-mappings.cinema-wallet=host.docker.internal:9001
```

# Run
Orchestration:
Run:
```shell
mvn kalix:runAll
```

Choreography:
Run:
```shell
mvn -Dspring.profiles.active=choreography kalix:runAll
```

# Local test
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

## Orchestration

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


## Choreography
Reserve a seat:
```shell
curl -XPATCH -v -d '{
  "walletId": "1",
  "reservationId": "res456789",
  "seatNumber": "1"
}' http://localhost:9000/cinema-show/1/reserve -H "Content-Type: application/json"
```

# Deploy
```shell
mvn deploy kalix:deploy
```

# Test in Cloud runtime
Set proxies:
```shell
kalix service proxy cinema-show --port 9000
```
```shell
kalix service proxy cinema-wallet --port 9001
```
```shell
kalix service proxy cinema-seat-booking --port 9002
```