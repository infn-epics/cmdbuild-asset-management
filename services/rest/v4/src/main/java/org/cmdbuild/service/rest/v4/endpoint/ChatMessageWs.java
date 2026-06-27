package org.cmdbuild.service.rest.v4.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.chat.*;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.CHAT_ACCESS_AUTHORITY;
import static org.cmdbuild.chat.ChatMessage.*;
import static org.cmdbuild.chat.ChatMessageStatus.CMS_ARCHIVED;
import jakarta.ws.rs.*;
import org.cmdbuild.chat.ChatMessage;
import org.cmdbuild.chat.ChatMessageData;
import org.cmdbuild.chat.ChatMessageDataImpl;
import org.cmdbuild.chat.ChatMessageStatus;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.common.serializationhelpers.ChatMessageSerializer;
import org.cmdbuild.service.rest.v4.command.ChatMessageWsCommand;
import org.springframework.stereotype.Component;

import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.CHAT_ACCESS_AUTHORITY;
import static org.cmdbuild.service.rest.common.serializationhelpers.ChatMessageSerializer.serializeDetailedMessage;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.RECORD_ID;
import static org.cmdbuild.utils.date.CmDateUtils.toIsoDateTimeLocal;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.parseEnumOrNull;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

@Path("sessions/current/messages")
@Tag( name = "Chat", description = "Chat")
@Produces(APPLICATION_JSON)
@Component
public class ChatMessageWs {

    private final ChatMessageWsCommand command;

    public ChatMessageWs(ChatMessageWsCommand command) {
        this.command = command;
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get messages for current user",
            description = "Get messages for current user",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsQueryOptions.class)), description = "Query options"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of messages data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getMessages(
            WsQueryOptions options
    ) {
        PagedElements<ChatMessage> chatMessages = command.doGetMessages(options);
        return response(chatMessages.map(options.isDetailed() ? ChatMessageSerializer::serializeDetailedMessage : ChatMessageSerializer::serializeBasicMessage));
    }

    @PUT
    @Path("{recordId}")
    @Operation(
            summary = "Archive message",
            description = "Archive message",
            parameters = {
                    @Parameter(name = RECORD_ID, in = ParameterIn.QUERY, description = "Record Id")
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsMessageData.class)), description = "Message data"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful archive of message"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(CHAT_ACCESS_AUTHORITY)
    public Object updateMessage(
            @QueryParam(RECORD_ID) Long recordId,
            WsMessageData message
    ) {
        command.doUpdateMessage(recordId, message);
        return success();
    }

    @POST
    @Path("")
    @Operation(
            summary = "Send message",
            description = "Send message",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsMessageData.class)), description = "Message data"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful sending of message"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(CHAT_ACCESS_AUTHORITY)
    public Object sendMessage(
            WsMessageData message
    ) {
        ChatMessage chatMessage = command.doSendMessage(message);
        return response(serializeDetailedMessage(chatMessage));
    }

    @DELETE
    @Path("{recordId}")
    @Operation(
            summary = "Delete message",
            description = "Delete message",
            parameters = {
                    @Parameter(name = RECORD_ID, in = ParameterIn.QUERY, description = "Record Id")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of message"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(CHAT_ACCESS_AUTHORITY)
    public Object deleteMessage(
            @QueryParam(RECORD_ID) Long recordId
    ) {
        command.doDeleteMessage(recordId);
        return success();
    }



    public static class WsMessageData {

        private final String target, subject, content, thread;
        private final Map<String, String> meta;
        private final ChatMessageStatus status;

        public WsMessageData(
                @JsonProperty("status") String status,
                @JsonProperty("target") String target,
                @JsonProperty("subject") String subject,
                @JsonProperty("content") String content,
                @JsonProperty("thread") String thread,
                @JsonProperty("meta") Map<String, String> meta
        ) {
            this.status = parseEnumOrNull(status, ChatMessageStatus.class);
            this.target = target;
            this.subject = subject;
            this.content = content;
            this.thread = thread;
            this.meta = meta;
        }

        public ChatMessageData toChatMessageData() {
            return ChatMessageDataImpl.builder().withTarget(target).withSubject(subject).withThread(thread).withMeta(meta).withContent(content).build();
        }

        public ChatMessageStatus getStatus() {
            return this.status;
        }

    }
}
