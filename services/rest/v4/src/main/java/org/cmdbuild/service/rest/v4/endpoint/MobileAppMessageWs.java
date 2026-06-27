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
import jakarta.ws.rs.*;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.notification.mobileapp.beans.MobileAppMessage;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.v4.command.MobileAppMessageWsCommand;
import org.cmdbuild.service.rest.v4.model.WsMessageData;
import org.cmdbuild.services.serialization.MobileAppMessageSerializer;
import org.springframework.stereotype.Component;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.RECORD_ID;
import static org.cmdbuild.services.serialization.MobileAppMessageSerializer.serializeDetailedMessage;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotNull;

@Path("sessions/current/mobile/messages")
@Tag( name = "Mobile App Messages", description = "Mobile App Messages")
@Produces(APPLICATION_JSON)
@Component
public class MobileAppMessageWs {

    private final MobileAppMessageWsCommand command;

    public MobileAppMessageWs(MobileAppMessageWsCommand command) {
        this.command = checkNotNull(command);
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get messages for current user",
            description = "Get messages for current user",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsQueryOptions.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of mobile app message data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getMessages(
            WsQueryOptions options
    ) {
        try {
            PagedElements<MobileAppMessage> mobileAppMessages = command.doGetMessages(options);
            return response(mobileAppMessages.map(options.isDetailed() ? MobileAppMessageSerializer::serializeDetailedMessage : MobileAppMessageSerializer::serializeBasicMessage));
        } catch (UnsupportedOperationException unsExc) {
            return command.doFailureWith(unsExc.getMessage());
        }
    }

    @POST
    @Path("")
    @Operation(
            summary = "Send message to mobile app",
            description = "Send message to mobile app",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsMessageData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object sendMessage(
            WsMessageData message
    ) {
        try {
            MobileAppMessage mobileAppMessage = command.doSendMessage(message);
            return response(serializeDetailedMessage(mobileAppMessage));
        } catch (UnsupportedOperationException unsExc) {
            return command.doFailureWith(unsExc.getMessage());
        }
    }

    @PUT
    @Path("{recordId}")
    @Operation(
            summary = "Archive message",
            description = "Archive message",
            parameters = {
                    @Parameter(name = RECORD_ID, in = ParameterIn.QUERY, description = "Id of message to archive", required = true)
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsMessageData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful archive of mobile app message"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object updateMessage(
            @QueryParam(RECORD_ID) Long recordId,
            WsMessageData message
    ) {
        try {
            command.doUpdateMessage(recordId, message);
            return success();
        } catch (UnsupportedOperationException unsExc) {
            return command.doFailureWith(unsExc.getMessage());
        }
    }

    @DELETE
    @Path("{recordId}")
    @Operation(
            summary = "Delete message",
            description = "Delete message",
            parameters = {
                    @Parameter(name = RECORD_ID, in = ParameterIn.QUERY, description = "Id of message to delete", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of mobile app message"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object deleteMessage(
            @QueryParam(RECORD_ID) Long recordId
    ) {
        try {
            command.doDeleteMessage(recordId);
            return success();
        } catch (UnsupportedOperationException unsExc) {
            return command.doFailureWith(unsExc.getMessage());
        }
    }
}
