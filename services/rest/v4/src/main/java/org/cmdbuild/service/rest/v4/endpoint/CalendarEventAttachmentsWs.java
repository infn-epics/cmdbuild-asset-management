package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.activation.DataHandler;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.calendar.CalendarService;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.common.helpers.AttachmentWsHelper;
import org.cmdbuild.service.rest.common.helpers.AttachmentWsHelper.WsAttachmentData;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Path("calendar/events/{eventId}/attachments")
@Tag(
        name = "Calendar event attachment",
        description = "The following documentation aims to illustrate the usage of the REST APIs provided by cmdbuild in order to retrieve, create, update or delete an attachments of the chosen Calendar Event"
)
@Component
public class CalendarEventAttachmentsWs {

    private final AttachmentWsHelper attachmentWsHelper;
    private final CalendarService calendarService;

    private final String EVENT_CLASS_NAME = "_CalendarEvent";

    public CalendarEventAttachmentsWs(AttachmentWsHelper attachmentWsHelper, CalendarService calendarService) {
        this.attachmentWsHelper = checkNotNull(attachmentWsHelper);
        this.calendarService = checkNotNull(calendarService);
    }

    @POST
    @Path(EMPTY)
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    @Operation(
            summary = "Create a new attachment for the chosen event",
            description = "Create a new attachment for the chosen event. You can either upload a new file or copy an existing attachment from another card.",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = "copyFrom_class", in = ParameterIn.QUERY, description = "Class Id to copy from"),
                    @Parameter(name = "copyFrom_card", in = ParameterIn.QUERY, description = "Card Id to copy from"),
                    @Parameter(name = "copyFrom_id", in = ParameterIn.QUERY, description = "Attachment Id to copy from"),
                    @Parameter(name = "tempId", in = ParameterIn.QUERY, description = "List of temporary attachment identifiers for uploaded files")
            },
            requestBody = @RequestBody(),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attachment created successfully"),
                    @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Class not found"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object create(
            @PathParam(EVENT_ID) @Parameter(  description = "Id of the event to attach the file to" ) Long eventId,
            @Multipart(value = ATTACHMENT, required = false) @Nullable @Parameter(schema = @Schema(implementation = WsAttachmentData.class)) WsAttachmentData attachment,
            @Multipart(value = FILE, required = false) @Parameter(schema = @Schema(implementation = DataHandler.class)) DataHandler dataHandler,
            @QueryParam("copyFrom_class") @Parameter(description = "Class Id to copy from") String sourceClassId,
            @QueryParam("copyFrom_card") @Parameter(description = "Card Id to copy from") Long sourceCardId,
            @QueryParam("copyFrom_id")  @Parameter(description = "Attachment Id to copy from") String sourceAttachmentId,
            @QueryParam("tempId") @Parameter(description = "List of temporary attachment identifiers for uploaded files") List<String> tempId
    ) throws IOException {
        calendarService.getUserEvent(eventId);
        if (sourceClassId == null) {
            if (dataHandler != null) {
                return attachmentWsHelper.create(EVENT_CLASS_NAME, eventId, attachment, dataHandler);
            } else {
                return attachmentWsHelper.create(EVENT_CLASS_NAME, eventId, attachment, tempId);
            }
        } else {
            return attachmentWsHelper.copyFrom(EVENT_CLASS_NAME, eventId, attachment, sourceClassId, sourceCardId, sourceAttachmentId);
        }
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all the attachments for the chosen event",
            description = "Get all the attachments for the chosen event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsQueryOptions.class)), description = "Query options"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully") ,
                    @ApiResponse(responseCode = "404", description = "Class not found"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object readmany(
            WsQueryOptions wsQueryOptions,
            @PathParam(EVENT_ID) @Parameter(  description = "Id of the event to attach the file to" ) Long eventId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.readMany(wsQueryOptions, EVENT_CLASS_NAME, eventId);
    }

    @GET
    @Path("{" + ATTACHMENT_ID + "}/")
    @Operation(
            summary = "Get a specific attachment for the chosen event",
            description = "Get a specific attachment for the chosen event",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attachment retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object readOne(
            @PathParam(EVENT_ID)Long eventId,
            @PathParam(ATTACHMENT_ID)  String attachmentId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.readOne(EVENT_CLASS_NAME, eventId, attachmentId);
    }

    @GET
    @Path("{" + ATTACHMENT_ID + "}/{file}")
    @Operation(
            summary = "Download a specific attachment for the chosen event",
            description = "Download a specific attachment for the chosen event.",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attachment retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Attachment not found"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    public DataHandler download(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(ATTACHMENT_ID) String attachmentId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.download(EVENT_CLASS_NAME, eventId, attachmentId);
    }

    @GET
    @Path("{" + ATTACHMENT_ID + "}/preview")
    @Operation(
            summary = "Get a preview of a specific attachment for the chosen event",
            description = "Get a preview of a specific attachment for the chosen event.",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Preview of attachment retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Attachment not found"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object preview(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(ATTACHMENT_ID) String attachmentId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.preview(EVENT_CLASS_NAME, eventId, attachmentId);
    }

    @PUT
    @Path("{" + ATTACHMENT_ID + "}/")
    @Operation(
            summary = "Update an attachment for the chosen event",
            description = "Update an attachment for the chosen event.",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment")
            },
            requestBody = @RequestBody(
                    content = @Content(
                            mediaType = MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(name = ATTACHMENT, implementation = WsAttachmentData.class))),

            responses = {
                    @ApiResponse (responseCode = "200", description = "Attachment updated successfully"),
                    @ApiResponse (responseCode = "404", description = "Attachment not found"),
                    @ApiResponse (responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @Consumes(MULTIPART_FORM_DATA)
    @Produces(APPLICATION_JSON)
    public Object update(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(ATTACHMENT_ID) String attachmentId,
            @Nullable @Multipart(value = ATTACHMENT, required = false) WsAttachmentData attachment,
            @Nullable @Multipart(value = FILE, required = false) DataHandler dataHandler
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.update(EVENT_CLASS_NAME, eventId, attachmentId, attachment, dataHandler);
    }

    @DELETE
    @Path("{" + ATTACHMENT_ID + "}/")
    @Operation(
            summary = "Delete an attachment for the chosen event",
            description = "Delete an attachment for the chosen event.",
            parameters = {
                    @Parameter(name = "eventId", in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment")
            },
            responses = {
                    @ApiResponse (responseCode = "200", description = "Attachment deleted successfully"),
                    @ApiResponse (responseCode = "404", description = "Attachment not found"),
                    @ApiResponse (responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object delete(
            @PathParam("eventId")  Long eventId,
            @PathParam(ATTACHMENT_ID)  String attachmentId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.delete(EVENT_CLASS_NAME, eventId, attachmentId);
    }

    @GET
    @Path("{" + ATTACHMENT_ID + "}/history")
    @Operation(
            summary = "Get the history of an attachment for the chosen event",
            description = "Get the history of an attachment for the chosen event.",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Attachment history retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Attachment not found"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object getAttachmentHistory(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(ATTACHMENT_ID) String attachmentId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.getAttachmentHistory(EVENT_CLASS_NAME, eventId, attachmentId);
    }

    @GET
    @Path("{" + ATTACHMENT_ID + "}/history/{version}/{file}")
    @Operation(
            summary = "Download a previous version of an attachment for the chosen event",
            description =  "Download a previous version of an attachment for the chosen event.",
            parameters = {
                    @Parameter(name = EVENT_ID, in = ParameterIn.PATH, description = "Id of the event to attach the file to", schema = @Schema(type = "integer")),
                    @Parameter(name = ATTACHMENT_ID, in = ParameterIn.PATH, description = "Id of the attachment"),
                    @Parameter(name = VERSION, in = ParameterIn.PATH, description = "Id of the version")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Previous version of attachment retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Version not found"),
                    @ApiResponse(responseCode = "500", description = "Unexpected error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    public DataHandler downloadPreviousVersion(
            @PathParam(EVENT_ID) Long eventId,
            @PathParam(ATTACHMENT_ID) String attachmentId,
            @PathParam(VERSION)  String versionId
    ) {
        calendarService.getUserEvent(eventId);
        return attachmentWsHelper.downloadPreviousVersion(EVENT_CLASS_NAME, eventId, attachmentId, versionId);
    }
}
