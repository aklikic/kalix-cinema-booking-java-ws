package com.example.cinema.show;

import com.example.cinema.wallet.WalletPublicEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Acl;
import kalix.javasdk.annotations.Publish;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

@Profile("choreography")
@Subscribe.Stream(
        service = "cinema-wallet",
        id = "wallet_events",
        consumerGroup = "show-wallet-consumer",
        ignoreUnknown = true
)
public class WalletConsumerAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ComponentClient componentClient;

    public WalletConsumerAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    public Effect<ShowCommandResponse.Ack> onEvent(WalletPublicEvent.WalletCharged event){
        logger.info("onWalletCharged: [{}]",event);
        var call = componentClient.forEventSourcedEntity(event.showId()).call(ShowEntity::confirmSeatReservationPayment).params(event.expenseId());
        return effects().forward(call);
    }
    public Effect<ShowCommandResponse.Ack> onEvent(WalletPublicEvent.WalletChargeRejected event){
        logger.info("onWalletChargeRejected: [{}]",event);
        var call = componentClient.forEventSourcedEntity(event.showId()).call(ShowEntity::cancelSeatReservation).params(event.expenseId());
        return effects().forward(call);
    }
}
