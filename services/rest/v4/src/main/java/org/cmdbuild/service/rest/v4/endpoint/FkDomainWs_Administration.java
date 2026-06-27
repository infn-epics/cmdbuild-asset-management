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
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.dao.entrytype.FkDomain;
import org.cmdbuild.service.rest.v4.command.FkDomainWsCommand;
import org.cmdbuild.translation.ObjectTranslationService;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.services.serialization.FkDomainSerializationHelper.serializeFkDomain;

/**
 *
 * @author schursin
 */
@Path("administration/fkdomains/")
@Tags({
        @Tag(name = "FK Domains", description = "APIs to manage FK Domains."),
        @Tag(name = "Administration")
})
@Produces(APPLICATION_JSON)
@Component
public class FkDomainWs_Administration {

    private final FkDomainWsCommand command;
    private final ObjectTranslationService objectTranslationService;
    private final UserClassService userClassService;

    public FkDomainWs_Administration(UserClassService userClassService, ObjectTranslationService objectTranslationService, FkDomainWsCommand command) {
        this.objectTranslationService = checkNotNull(objectTranslationService);
        this.userClassService = checkNotNull(userClassService);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all FK domains",
            description = "Get all FK domains",
            parameters = {
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "How to filter results"),
                    @Parameter(name = SORT, in = ParameterIn.QUERY, description = "How to order results"),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of FK domains"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            @QueryParam(FILTER) String filterStr,
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START) Integer offset
    ) {
        List<FkDomain> listFkDomain = command.fetchFkDomains(filterStr);
        List<FluentMap<String, Object>> fkDomainSerializations = paged(listFkDomain, offset, limit).stream().map(d -> serializeFkDomain(d, objectTranslationService, userClassService)).collect(toList());
        return response(fkDomainSerializations, listFkDomain.size());
    }
}
