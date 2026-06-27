/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.command;


import org.cmdbuild.classe.access.UserCardService;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.dao.beans.Card;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.common.helpers.CardsForDomainFetcher;
import org.cmdbuild.service.rest.common.serializationhelpers.card.CardWsSerializationHelperv3;
import org.cmdbuild.service.rest.v4.model.WsCardData;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ldare
 */
@Component
public class CardWsCommand {

    private final UserClassService userClassService;
    private final UserCardService userCardService;
    private final DaoService daoService;
    private final CardWsSerializationHelperv3 cardWsSerializationHelperv3;
    private final DmsService dmsService;
    private final CardsForDomainFetcher cardsForDomainFetcher;

    public CardWsCommand(UserClassService userClassService, UserCardService userCardService, DaoService daoService, CardWsSerializationHelperv3 cardWsSerializationHelperv3, DmsService dmsService, CardsForDomainFetcher cardsForDomainFetcher) {
        this.userClassService = checkNotNull(userClassService);
        this.userCardService = checkNotNull(userCardService);
        this.daoService = checkNotNull(daoService);
        this.cardWsSerializationHelperv3 = checkNotNull(cardWsSerializationHelperv3);
        this.dmsService = checkNotNull(dmsService);
        this.cardsForDomainFetcher = checkNotNull(cardsForDomainFetcher);
    }

    public Card doReadOne(String classId, Long cardId, Boolean infoOnly) {
        if (infoOnly) {
            return userCardService.getUserCardInfo(classId, cardId);
        }
        return userCardService.getUserCard(classId, cardId);
    }

    public Card doCreate(String classId, WsCardData data) {
        return userCardService.createCard(classId, data.getValues());
    }

    public Card doUpdate(String classId, Long cardId, WsCardData data) {
        return userCardService.updateCard(classId, cardId, data.getValues());
    }

    public void doDelete(String classId, Long cardId) {
        userCardService.deleteCard(classId, cardId);
    }

    public void doUpdateMany(String classId, WsCardData data, WsQueryOptions wsQueryOptions) {
        userCardService.updateCards(classId, wsQueryOptions.getQuery().getFilter(), data.getValues());
    }
}
