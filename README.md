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
curl -XPOST http://localhost:9000/wallet/1/create/100 -H "Content-Type: application/json"
```

Get wallet:
```shell
curl -XGET http://localhost:9000/wallet/1 -H "Content-Type: application/json"
```

Start seat booking:
```shell
curl -XPOST -d '{
  "showId": "1",
  "seatNumber": 1,
  "walletId": "1"
}' http://localhost:9000/seat-booking/res1 -H "Content-Type: application/json"
```
Get seat reservation state:
```shell
curl -XGET http://localhost:9000/seat-booking/res1 -H "Content-Type: application/json"
```