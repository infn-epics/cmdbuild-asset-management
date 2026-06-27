/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.cmdbuild.email.EmailAccount;
import org.cmdbuild.service.rest.v4.command.EmailAccountWsCommand;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.services.serialization.EmailAccountSerializationHelper.*;

/**
 *
 * @author schursin
 */
@Path("email/accounts/")
@Tag(name = "Email Accounts")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class EmailAccountWs_Management {

    private final EmailAccountWsCommand command;

    public EmailAccountWs_Management(EmailAccountWsCommand command) {
        this.command = command;
    }

    @GET
    @Path("{accountId}/public")
    @Operation(
            summary = "Get a public view of an email account by ID or code",
            description = "Obtain a public view of a specific email account by its ID or code",
            parameters = {
                    @Parameter(name = ACCOUNT_ID, in = ParameterIn.PATH, description = "Id of the account", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of public email account data"),
                    @ApiResponse(responseCode = "404", description = "The email account was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {}) }
    )
    public Object readPublic(
            @PathParam(ACCOUNT_ID) String idOrCode
    ) {
        EmailAccount emailAccount = command.doReadPublic(idOrCode);
        return response(serializePublicAccount(emailAccount));
    }
}