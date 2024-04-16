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