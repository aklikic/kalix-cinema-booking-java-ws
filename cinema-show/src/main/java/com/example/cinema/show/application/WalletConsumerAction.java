package com.example.cinema.show.application;

import akka.Done;
import akka.javasdk.annotations.Consume;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.consumer.Consumer;
import com.example.cinema.wallet.WalletPublicEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO @Profile("choreography")
@Consume.FromServiceStream(
        service = "cinema-wallet",
        id = "wallet_events",
        consumerGroup = "show-wallet-consumer",
        ignoreUnknown = true
)
public class WalletConsumerAction extends Consumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;

    public WalletConsumerAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect onEvent(WalletPublicEvent event){
        return switch (event){
            case WalletPublicEvent.WalletCharged evt -> {
                logger.info("onWalletCharged: [{}]",evt);
                var call = componentClient.forEventSourcedEntity(evt.showId()).method(ShowEntity::confirmSeatReservationPayment).invokeAsync(evt.expenseId()).thenApply(ack -> Done.done());
                yield  effects().acyncDone(call);
            }
            case WalletPublicEvent.WalletChargeRejected evt -> {
                logger.info("onWalletChargeRejected: [{}]",event);
                var call = componentClient.forEventSourcedEntity(evt.showId()).method(ShowEntity::cancelSeatReservation).invokeAsync(evt.expenseId()).thenApply(ack -> Done.done());;
                yield  effects().acyncDone(call);
            }
            default -> effects().done();
        };
    }

}
