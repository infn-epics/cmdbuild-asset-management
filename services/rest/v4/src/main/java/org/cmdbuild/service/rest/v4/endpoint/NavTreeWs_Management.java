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
import org.cmdbuild.navtree.NavTree;
import org.cmdbuild.navtree.NavTreeService;
import org.cmdbuild.service.rest.common.serializationhelpers.NavTreeSerializationHelper.TreeMode;
import org.cmdbuild.service.rest.v4.command.NavTreeWsCommand;
import org.cmdbuild.translation.ObjectTranslationService;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.service.rest.common.serializationhelpers.NavTreeSerializationHelper.serializeTree;
import static org.cmdbuild.service.rest.common.serializationhelpers.NavTreeSerializationHelper.serializeTreeSimple;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.lang.CmConvertUtils.parseEnum;

/**
 *
 * @author schursin
 */
@Path("domainTrees")
@Tag(name = "Navigation Trees")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class NavTreeWs_Management {

    private final NavTreeService navTreeService;
    private final ObjectTranslationService translationService;
    private final NavTreeWsCommand command;

    public NavTreeWs_Management(NavTreeService navTreeService, ObjectTranslationService translationService, NavTreeWsCommand command) {
        this.navTreeService = checkNotNull(navTreeService);
        this.translationService = checkNotNull(translationService);
        this.command = checkNotNull(command);
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all active navigation trees",
            description = "Get all active navigation trees",
            parameters = {
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter navigation trees by name"),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of navigation trees"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            @QueryParam(FILTER) String filterStr,
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START) Integer offset
    ) {
        List<NavTree> listNavTree = command.doReadAll(navTreeService::getAllActive, filterStr);
        return response(paged(listNavTree, offset, limit).map((tree) -> serializeTreeSimple(tree, translationService)));
    }

    @GET
    @Path("{treeId}/")
    @Operation(
            summary = "Get a specific navigation tree",
            description = "Get a specific navigation tree",
            parameters = {
                    @Parameter(name = "treeId", description = "Id of the navigation tree", schema = @Schema(type = "string")),
                    @Parameter(name = "treeMode", description = "Tree mode", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of navigation tree"),
                    @ApiResponse(responseCode = "404", description = "The navigation tree was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam("treeId") String id,
            @QueryParam("treeMode") @DefaultValue("flat") String treeMode
    ) {
        NavTree root = command.doRead(id);
        return response(serializeTree(root, parseEnum(treeMode, TreeMode.class), translationService));
    }
}
