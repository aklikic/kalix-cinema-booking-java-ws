package com.example.cinema.wallet;

import com.example.cinema.show.ShowPublicEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

@Profile("choreography")
@Subscribe.Stream(
        service = "cinema-show",
        id = "show_events",
        consumerGroup = "wallet-show-consumer",
        ignoreUnknown = true
)
public class ShowConsumerAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;

    public ShowConsumerAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect<WalletCommandResponse.Ack> onEvent(ShowPublicEvent.SeatReserved event){
        logger.info("onSeatReserved: [{}]",event);
        var call = componentClient.forEventSourcedEntity(event.walletId()).call(WalletEntity::chargeWallet).params(new WalletCommand.ChargeWallet(event.seatPrice(), event.reservationId(), event.showId()));
        return effects().forward(call);
    }
    public Effect<WalletCommandResponse.Ack> onEvent(ShowPublicEvent.SeatReservationCancelled event){
        logger.info("onSeatReservationCancelled: [{}]",event);
        var call = componentClient.forEventSourcedEntity(event.walletId()).call(WalletEntity::refundWalletCharge).params(new WalletCommand.Refund(event.reservationId(), event.reservationCancellationId()));
        return effects().forward(call);
    }
}
