package com.example.cinema.wallet;

import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Acl;
import kalix.javasdk.annotations.Publish;
import kalix.javasdk.annotations.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

@Profile("choreography")
@Subscribe.EventSourcedEntity(value = WalletEntity.class, ignoreUnknown = true)
@Publish.Stream(id = "wallet_events")
@Acl(allow = @Acl.Matcher(service = "*"))
public class WalletProducerAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    public Effect<WalletPublicEvent.WalletCharged> onEvent(WalletEvent.WalletCharged event){
        logger.info("onWalletCharged: [{}]",event);
        return effects().reply(new WalletPublicEvent.WalletCharged(event.walletId(),event.showId(), event.amount(), event.expenseId()));
    }
    public Effect<WalletPublicEvent.WalletChargeRejected> onEvent(WalletEvent.WalletChargeRejected event){
        logger.info("onWalletChargeRejected: [{}]",event);
        return effects().reply(new WalletPublicEvent.WalletChargeRejected(event.walletId(), event.showId(), event.expenseId(), event.reason()));
    }
    public Effect<WalletPublicEvent.WalletRefunded> onEvent(WalletEvent.WalletRefunded event){
        logger.info("onWalletRefunded: [{}]",event);
        return effects().reply(new WalletPublicEvent.WalletRefunded(event.walletId(),event.showId(), event.amount(),event.chargeExpenseId(),event.refundExpenseId()));
    }
}
