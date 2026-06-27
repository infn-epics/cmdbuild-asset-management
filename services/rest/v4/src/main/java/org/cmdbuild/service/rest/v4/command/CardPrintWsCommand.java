/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.command;


import jakarta.activation.DataHandler;
import org.cmdbuild.classe.access.UserCardService;
import org.cmdbuild.dao.beans.Card;
import org.cmdbuild.report.SysReportService;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.cmdbuild.report.utils.ReportExtUtils.reportExtFromString;

/**
 * @author ldare
 */
@Component
public class CardPrintWsCommand {

    private final UserCardService cardService;
    private final SysReportService reportService;

    public CardPrintWsCommand(UserCardService cardService, SysReportService reportService) {
        this.cardService = checkNotNull(cardService);
        this.reportService = checkNotNull(reportService);
    }

    public DataHandler doReadOne(String classId, Long cardId, String extension) {
        Card card = cardService.getUserCard(classId, cardId);
        checkArgument(card.getType().hasServiceReadPermission(), "user not authorized to access card %s.%s", classId, cardId);
        return reportService.executeCardReport(card, reportExtFromString(extension));
    }
}
