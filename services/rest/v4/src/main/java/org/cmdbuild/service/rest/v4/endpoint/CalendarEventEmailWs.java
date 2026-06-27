package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.cmdbuild.calendar.CalendarService;
import org.cmdbuild.dao.driver.postgres.q3.DaoQueryOptions;
import org.cmdbuild.email.Email;
import org.cmdbuild.email.EmailAttachment;
import org.cmdbuild.email.EmailService;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.common.helpers.AttachmentWsHelper;
import org.cmdbuild.service.rest.v4.model.WsEmailData;
import org.cmdbuild.service.rest.v4.serializationhelpers.EmailWsHelper;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.calendar.beans.CalendarEvent.EVENT_TABLE;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.email.EmailStatus.ES_DRAFT;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.calendar.beans.CalendarEvent.EVENT_TABLE;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.email.EmailStatus.ES_DRAFT;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.io.CmIoUtils.readToString;
import static org.cmdbuild.utils.json.CmJsonUtils.fromJson;
import static org.cmdbuild.utils.lang.CmCollectionUtils.onlyElement;
import static org.cmdbuild.utils.lang.CmConvertUtils.toInt;

@Path("calendar/events/{eventId}/emails")
@Tag(
        name = "Calendar event email attachment",
        description = "The following documentation aims to illustrate the usage of the REST APIs provided by cmdbuild in order to retrieve, create, update or delete an attachments of an Email that is attached to the chosen Card"
)
@Component
public class CalendarEventEmailWs {

    private final EmailService emailService;
    private final CalendarService calendarService;
    private final EmailWsHelper emailWsHelper;
    private final AttachmentWsHelper attachmentWsHelper;

    public CalendarEventEmailWs(EmailService emailService,
            CalendarService calendarService,
                                EmailWsHelper emailWsHelper,
                                AttachmentWsHelper attachmentWsHelper) {
        this.emailService = checkNotNull(emailService);
        this.calendarService = checkNotNull(calendarService);
        this.emailWsHelper = checkNotNull(emailWsHelper);
        this.attachmentWsHelper = checkNotNull(attachmentWsHelper);
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create an email attachment for a calendar event",
            description = "Creates an email attachment for a calendar event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of event", schema = @Schema(type = "long")),
                    @Parameter(name = "apply_template", in = ParameterIn.QUERY, description = "Apply template to the email", schema = @Schema(type = "boolean")),
                    @Parameter(name = "template_only", in = ParameterIn.QUERY, description = "Only apply template to the email", schema = @Schema(type = "boolean")),
                    @Parameter(name = ATTACHMENTS, in = ParameterIn.QUERY, array = @ArraySchema(schema = @Schema(type = "string")), description = "List of temporary attachment identifiers for uploaded files")
            },
            requestBody = @RequestBody( description = "Card Event Email attachments", content = @Content(schema = @Schema(implementation = Attachment.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email attachment created successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object create(
            @PathParam(EVENT_ID) Long eventId,
            @QueryParam("apply_template") @DefaultValue(FALSE) Boolean applyTemplate,
            @QueryParam("template_only") @DefaultValue(FALSE) Boolean templateOnly,
            @QueryParam(ATTACHMENTS) List<String> tempId,
            List<Attachment> parts)
    {
        calendarService.getUserEvent(eventId);
        List<Attachment> attachments = parts.size() == 1 ? emptyList() : parts.stream().filter(a -> !equal(a.getDataHandler().getName(), "email")).collect(toImmutableList());
        Attachment body = parts.size() == 1 ? getOnlyElement(parts) : parts.stream().filter(a -> equal(a.getDataHandler().getName(), "email")).collect(onlyElement("missing 'email' multipart json payload"));
        List<EmailAttachment> emailAttachments = attachmentWsHelper.convertAttachmentsToEmailAttachments(attachments, tempId);
        WsEmailData emailData = fromJson(readToString(body.getDataHandler()), WsEmailData.class);
        return response(emailWsHelper.createEmail(EVENT_TABLE, eventId.toString(), emailData, applyTemplate, templateOnly, emailAttachments));
    }

    @PUT
    @Path("{" + EMAIL_ID + "}/")
    @Operation(
            summary = "Update an existing email attachment for a calendar event",
            description = "Update an existing email attachment for a calendar event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of event", schema = @Schema(type = "long")),
                    @Parameter(name = EMAIL_ID, in = ParameterIn.PATH, description = "Id of email attachment", schema = @Schema(type = "long"))
            },
            requestBody = @RequestBody(description = "Card Event Email attachments", content = @Content(schema = @Schema(implementation = WsEmailData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email attachment updated successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )

    public Object update(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(EMAIL_ID) Long emailId,
            WsEmailData emailData
    ) {
        calendarService.getUserEvent(eventId);
        Email current = emailService.getOne(emailId);
        checkArgument(equal(ES_DRAFT, current.getStatus()), "cannot update email with status = %s", current.getStatus());
        Email email = emailData.toEmail().withReference(eventId).withId(emailId).build();
        email = emailService.update(email);
        return response(emailWsHelper.serializeDetailedEmail(email, null));
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all email attachments for a calendar event",
            description = "Retrieve all email attachments for a calendar event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of event", schema = @Schema(type = "long"))
            },
            requestBody = @RequestBody(description = "Query options", content = @Content(schema = @Schema(implementation = WsQueryOptions.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of email attachments"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            @PathParam(EVENT_ID) Long eventId,
            WsQueryOptions wsQueryOptions
    ) {
        DaoQueryOptions queryOptions = wsQueryOptions.getQuery();
        calendarService.getUserEvent(eventId);
        Collection<Email> list = emailService.getAllForCard(eventId, queryOptions);
        return response(paged(list, wsQueryOptions.isDetailed() ? emailWsHelper::serializeDetailedEmail : emailWsHelper::serializeBasicEmail, toInt(wsQueryOptions.getOffset()), toInt(wsQueryOptions.getLimit())));
    }

    @GET
    @Path("{" + EMAIL_ID + "}/")
    @Operation(
            summary = "Get a specific email attachment for a calendar event",
            description = "Retrieve a specific email attachment for a calendar event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of event", schema = @Schema(type = "long")),
                    @Parameter(name = EMAIL_ID, in = ParameterIn.PATH, description = "Id of email attachment", schema = @Schema(type = "long"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of the email attachment"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(EMAIL_ID) Long emailId
    ) {
        calendarService.getUserEvent(eventId);
        Email email = emailService.getOne(emailId);
        return response(emailWsHelper.serializeDetailedEmail(email));//TODO check email id
    }

    @DELETE
    @Path("{" + EMAIL_ID + "}/")
    @Operation(
            summary = "Delete an email attachment from a calendar event",
            description = "Deletes an email attachment from a calendar event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of event", schema = @Schema(type = "long")),
                    @Parameter(name = EMAIL_ID, in = ParameterIn.PATH, description = "Id of email attachment", schema = @Schema(type = "long"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email attachment deleted successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(EMAIL_ID) Long emailId
    ) {
        calendarService.getUserEvent(eventId);
        emailService.delete(emailService.getOne(emailId));
        return success();
    }

    private Object createEmail(Long eventId, WsEmailData emailData) {
        Email email = emailData.toEmail().withReference(eventId).build();
        email = emailService.create(email);
        return response(emailWsHelper.serializeDetailedEmail(email, null));
    }
}
