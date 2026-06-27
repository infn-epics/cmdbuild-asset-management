package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.audit.RequestData;
import org.cmdbuild.audit.RequestInfo;
import org.cmdbuild.audit.RequestTrackingRepository;
import org.cmdbuild.fault.FaultEvent;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.SYSTEM_ACCESS_AUTHORITY;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.date.CmDateUtils.*;
import static org.cmdbuild.utils.encode.CmPackUtils.packOrNull;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import org.cmdbuild.service.rest.v4.command.AuditWsCommand;
import org.cmdbuild.services.serialization.RequestSerializer;
import org.springframework.stereotype.Component;

import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.SYSTEM_ACCESS_AUTHORITY;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.services.serialization.RequestSerializer.serializeDetailedRequestData;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

@Path("system/audit/")
@Tags({
        @Tag( name = "Administration"),
        @Tag( name = "Audit", description = "Auditing and logging of requests and errors")
})
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RolesAllowed(SYSTEM_ACCESS_AUTHORITY)
@Component
public class AuditWs {

    private final AuditWsCommand command;

    public AuditWs(AuditWsCommand command) {
        this.command = command;
    }

    @GET
    @Path("mark")
    @Operation(
            summary = "Get current audit mark",
            description = "Returns a timestamp mark representing the current point in time. This mark can be used to retrieve audit records (requests and errors) that occurred after this point.",
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema( ref = "APIAuditMark"))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
)
    public Object mark() {
        String mark = command.doMark();
        return map("success", true, "data", map("mark", mark));
    }

    @GET
    @Path("requests")
    @Operation(
            summary = "Get audit requests",
            description = "Returns a list of audit requests that occurred after the specified mark, or last limit requests if mark is not provided.",
            parameters = {
                    @Parameter(name = SINCE, in = ParameterIn.QUERY, description = "Timestamp mark representing the point in time after which to retrieve audit requests", schema = @Schema(type = "string")),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Maximum number of audit requests to retrieve", schema = @Schema(type = "integer"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RequestData.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request mark"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object getRequests(
            @QueryParam(SINCE) @Nullable String mark,
            @QueryParam(LIMIT) @Nullable Long limit
    ) {
        List<RequestInfo> requests = command.doGetRequests(mark, limit);
        return response(requests.stream().map(RequestSerializer::serializeRequestInfo).collect(toList()));
    }

    @GET
    @Path("errors")
    @Operation(
            summary = "Get request errors",
            description = "Returns a list of error requests that occurred after the specified mark, or last limit error requests if mark is not provided.",
            parameters = {
                    @Parameter(name = SINCE, in = ParameterIn.QUERY, description = "Timestamp mark representing the point in time after which to retrieve audit requests", schema = @Schema(type = "string")),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Maximum number of audit requests to retrieve", schema = @Schema(type = "integer"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RequestData.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request mark"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object getErrors(
            @QueryParam(SINCE) @Nullable String mark,
            @QueryParam(LIMIT) @Nullable Long limit
    ) {
        List<RequestInfo> requests = command.doGetErrors(mark, limit);
        return response(requests.stream().map(RequestSerializer::serializeRequestInfo).collect(toList()));
    }

    @GET
    @Path("requests/{id}")
    @Operation(
            summary = "Get audit request details",
            description = "Returns details of a specific audit request.",
            parameters = {
                    @Parameter(name = ID, in = ParameterIn.PATH, description = "Id of audit request", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = RequestData.class))),
                    @ApiResponse(responseCode = "404", description = "Audit request not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object getRequest(
            @PathParam(ID) @Parameter(description = "Id of audit request") String id
    ) {
        RequestData requestData = command.doGetRequest(id);
        return response(serializeDetailedRequestData(requestData));

    }
}
