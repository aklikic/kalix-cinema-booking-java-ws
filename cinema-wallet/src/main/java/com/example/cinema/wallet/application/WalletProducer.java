package com.example.cinema.wallet.application;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Produce;
import akka.javasdk.consumer.Consumer;
import com.example.cinema.wallet.WalletPublicEvent;
import com.example.cinema.wallet.domain.WalletEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO @Profile("choreography")
@Consume.FromEventSourcedEntity(value = WalletEntity.class, ignoreUnknown = true)
@Produce.ServiceStream(id = "wallet_events")
@Acl(allow = @Acl.Matcher(service = "*"))
public class WalletProducer extends Consumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Effect onEvent(WalletEvent event){
        logger.info("onEvent: [{}]",event);
        return switch (event){
            case WalletEvent.WalletCharged evt -> effects().produce(new WalletPublicEvent.WalletCharged(evt.walletId(),evt.showId(), evt.amount(), evt.expenseId()));
            case WalletEvent.WalletChargeRejected evt -> effects().produce(new WalletPublicEvent.WalletChargeRejected(evt.walletId(), evt.showId(), evt.expenseId(), evt.reason()));
            case WalletEvent.WalletRefunded evt -> effects().produce(new WalletPublicEvent.WalletRefunded(evt.walletId(),evt.showId(), evt.amount(),evt.chargeExpenseId(),evt.refundExpenseId()));
            default -> effects().done();
        };
    }
}
