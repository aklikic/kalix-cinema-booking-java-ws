package com.example.cinema.show.application;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Produce;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import com.example.cinema.show.ShowPublicEvent;
import com.example.cinema.show.domain.ShowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO @Profile("choreography")
@Consume.FromEventSourcedEntity(value = ShowEntity.class, ignoreUnknown = true)
@Produce.ServiceStream(id = "show_events")
@Acl(allow = @Acl.Matcher(service = "*"))
public class ShowProducerAction extends Consumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;

    public ShowProducerAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onEvent(ShowEvent event){
        logger.info("onEvent:[{}]",event);
        return switch (event){
            case ShowEvent.SeatReserved evt -> effects().produce(new ShowPublicEvent.SeatReserved(evt.showId(),evt.walletId(),evt.reservationId(), evt.seatNumber(),evt.seatPrice()));
            case ShowEvent.SeatReservationCancelled evt -> effects().produce(new ShowPublicEvent.SeatReservationCancelled(evt.showId(),evt.walletId(),evt.reservationId(), evt.reservationCancellationId(), evt.seatNumber()));
            default -> effects().done();
        };
    }
}
