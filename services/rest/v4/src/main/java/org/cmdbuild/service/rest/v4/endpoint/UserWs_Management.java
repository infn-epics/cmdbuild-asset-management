/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.endpoint;


import com.google.common.collect.ImmutableMap;
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
import org.cmdbuild.service.rest.v4.command.UserWsCommand;
import org.cmdbuild.service.rest.v4.model.WsPasswordRecoveryData;
import org.cmdbuild.service.rest.v4.model.WsUserPswData;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.dao.constants.SystemAttributes.ATTR_DESCRIPTION;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;

/**
 * @author ldare
 */
@Path("users/")
@Tag(name = "Users", description = "Users management")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class UserWs_Management {

    private final static Map<String, String> USER_TABLE_ATTR_NAME_MAPPING = ImmutableMap.of(
            "username", "Username",
            "description", ATTR_DESCRIPTION,
            "email", "Email",
            "active", "Active"
    );

    private final UserWsCommand command;

    public UserWs_Management(UserWsCommand command) {
        this.command = checkNotNull(command);
    }

    @PUT
    @Path("current/password")
    @Operation(
            summary = "Change password for current user",
            description = "Change password for the currently logged-in user",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsUserPswData.class)), description = "Password data"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object changePasswordForCurrentUser(
            WsUserPswData data
    ) {
        command.doChangePsswordForCurrentUser(data);
        return success();
    }

    @PUT
    @Path("{username}/password")
    @Operation(
            summary = "Change password for specified user",
            description = "Change password for the specified user by username",
            parameters = {
                    @Parameter(name = "username", in = ParameterIn.PATH, description = "Username of the user to change password", required = true),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsUserPswData.class)), description = "Password data"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password changed successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object changePassword(
            @PathParam("username") String username,
            WsUserPswData data
    ) {
        command.doChangePassword(username, data);
        return success();
    }

    @POST
    @Path("{username}/password/recovery")
    @Operation(
            summary = "Require password recovery for specified user",
            description = "Initiate password recovery process for the specified user by username",
            parameters = {
                    @Parameter(name = "username", in = ParameterIn.PATH, description = "Username of the user to recover password", required = true),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsPasswordRecoveryData.class)), description = "Password recovery data"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password recovery initiated successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object requirePasswordRecovery(
            @PathParam("username") String username,
            WsPasswordRecoveryData data
    ) {
        command.doRequirePasswordRecovery(username, data);
        return success();
    }
}
