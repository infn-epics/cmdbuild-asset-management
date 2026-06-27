/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.email.EmailSignature;
import org.cmdbuild.service.rest.v4.command.EmailSignatureWsCommand;
import org.cmdbuild.translation.ObjectTranslationService;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.service.rest.v4.serializationhelpers.EmailSignatureSerializationHelper.serializeSignature;

/**
 *
 * @author schursin
 */
@Path("email/signatures/")
@Tag(name = "Email Signatures")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class EmailSignatureWs_Management {

    private final EmailConfiguration emailConfiguration;
    private final ObjectTranslationService objectTranslationService;
    private final EmailSignatureWsCommand command;

    public EmailSignatureWs_Management(EmailConfiguration emailConfiguration, ObjectTranslationService objectTranslationService, EmailSignatureWsCommand command) {
        this.objectTranslationService = checkNotNull(objectTranslationService);
        this.emailConfiguration = checkNotNull(emailConfiguration);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all active email signatures",
            description = "Get all active email signatures",
            parameters = {
                    @Parameter(name = FILTER, description = "How to filter results", schema = @Schema(ref = "DefaultEmailFilter")),
                    @Parameter(name = SORT, description = "How to order results", schema = @Schema(ref = "DefaultSortExample")),
                    @Parameter(name = LIMIT, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = DETAILED, description = "Include or not full details in the response", schema = @Schema(type = "boolean"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of email signatures"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            @QueryParam(FILTER) String filterStr,
            @QueryParam(SORT) String sort,
            @QueryParam(LIMIT) Long limit,
            @QueryParam(START) Long offset,
            @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean detailed
    ) {
        List<EmailSignature> listEmailSignature = command.doReadAll(sort, detailed, filterStr, limit, offset, true);
        return response(paged(listEmailSignature.stream().map(c -> serializeSignature(c, detailed, objectTranslationService, emailConfiguration)).collect(toList()), offset, limit));
    }

    @GET
    @Path("{signatureId}/")
    @Operation(
            summary = "Get a specific email signature",
            description = "Get a specific email signature",
            parameters = {
                    @Parameter(name = "signatureId", description = "Id of the email signature", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of email signature"),
                    @ApiResponse(responseCode = "404", description = "The email signature was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam("signatureId") String id
    ) {
        EmailSignature emailSignature = command.doRead(id);
        return response(serializeSignature(emailSignature, true, objectTranslationService, emailConfiguration));
    }
}
