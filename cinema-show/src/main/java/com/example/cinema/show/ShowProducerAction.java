package com.example.cinema.show;

import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Acl;
import kalix.javasdk.annotations.Publish;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

@Profile("choreography")
@Subscribe.EventSourcedEntity(value = ShowEntity.class, ignoreUnknown = true)
@Publish.Stream(id = "show_events")
@Acl(allow = @Acl.Matcher(service = "*"))
public class ShowProducerAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;

    public ShowProducerAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    public Effect<ShowPublicEvent.SeatReserved> onEvent(ShowEvent.SeatReserved event){
        logger.info("onSeatReserved:[{}]",event);
        return effects().reply(new ShowPublicEvent.SeatReserved(event.showId(),event.walletId(),event.reservationId(), event.seatNumber(),event.seatPrice()));
    }
    public Effect<ShowPublicEvent.SeatReservationCancelled> onEvent(ShowEvent.SeatReservationCancelled event){
        logger.info("onSeatReservationCancelled:[{}]",event);
        return effects().reply(new ShowPublicEvent.SeatReservationCancelled(event.showId(),event.walletId(),event.reservationId(), event.reservationCancellationId(), event.seatNumber()));
    }
}
