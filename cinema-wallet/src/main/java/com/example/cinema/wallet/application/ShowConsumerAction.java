package com.example.cinema.wallet.application;

import akka.Done;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import com.example.cinema.show.ShowPublicEvent;
import com.example.cinema.wallet.WalletCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO @Profile("choreography")
@Consume.FromServiceStream(
        service = "cinema-show",
        id = "show_events",
        consumerGroup = "wallet-show-consumer",
        ignoreUnknown = true
)
public class ShowConsumerAction extends Consumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;

    public ShowConsumerAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onEvent(ShowPublicEvent event){
        return switch (event){
            case ShowPublicEvent.SeatReserved evt -> {
                logger.info("onSeatReserved: [{}]",event);
                var call = componentClient.forEventSourcedEntity(evt.walletId()).method(WalletEntity::chargeWallet).invokeAsync(new WalletCommand.ChargeWallet(evt.seatPrice(), evt.reservationId(), evt.showId())).thenApply(ack -> Done.done());
                yield effects().acyncDone(call);
            }
            case ShowPublicEvent.SeatReservationCancelled evt -> {
                logger.info("onSeatReservationCancelled: [{}]",event);
                var call = componentClient.forEventSourcedEntity(evt.walletId()).method(WalletEntity::refundWalletCharge).invokeAsync(new WalletCommand.Refund(evt.reservationId(), evt.reservationCancellationId())).thenApply(ack -> Done.done());
                yield effects().acyncDone(call);
            }
        };
    }
}
