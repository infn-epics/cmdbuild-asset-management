/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.navtree.NavTree;
import org.cmdbuild.navtree.NavTreeService;
import org.cmdbuild.service.rest.v4.command.NavTreeWsCommand;
import org.cmdbuild.service.rest.v4.model.WsTreeData;
import org.cmdbuild.translation.ObjectTranslationService;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_NAVTREES_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_NAVTREES_VIEW_AUTHORITY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.service.rest.common.serializationhelpers.NavTreeSerializationHelper.*;
import static org.cmdbuild.service.rest.common.serializationhelpers.NavTreeSerializationHelper.TreeMode.TREE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.lang.CmConvertUtils.parseEnum;

/**
 *
 * @author schursin
 */
@Path("administration/domainTrees")
@Tags({
        @Tag( name = "Nav Trees", description = "APIs to manage navigation trees." ),
        @Tag( name = "Administration" )
})
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class NavTreeWs_Administration {

    private final NavTreeService navTreeService;
    private final ObjectTranslationService translationService;
    private final NavTreeWsCommand command;

    public NavTreeWs_Administration(NavTreeService navTreeService, ObjectTranslationService translationService, NavTreeWsCommand command) {
        this.navTreeService = checkNotNull(navTreeService);
        this.translationService = checkNotNull(translationService);
        this.command = checkNotNull(command);
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all navigation trees",
            description = "Get all navigation trees",
            parameters = {
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter navigation trees by name"),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of navigation trees"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_NAVTREES_VIEW_AUTHORITY)
    public Object readAll(
            @QueryParam(FILTER) String filterStr,
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START) Integer offset
    ) {
        List<NavTree> listNavTree = command.doReadAll(navTreeService::getAll, filterStr);
        return response(paged(listNavTree, offset, limit).map((tree) -> serializeTreeSimple(tree, translationService)));
    }

    @GET
    @Path("{treeId}/")
    @Operation(
            summary = "Get a navigation tree",
            description = "Get a navigation tree by id",
            parameters = {
                    @Parameter(name = "treeId", in = ParameterIn.PATH, description = "Id of the navigation tree to query", schema = @Schema(type = "string")),
                    @Parameter(name = "treeMode", in = ParameterIn.QUERY, description = "Tree Mode")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of navigation tree"),
                    @ApiResponse(responseCode = "404", description = "The navigation tree was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_NAVTREES_VIEW_AUTHORITY)
    public Object read(
            @PathParam("treeId") String id,
            @QueryParam("treeMode") @DefaultValue("flat") String treeMode
    ) {
        NavTree root = command.doRead(id);
        return response(serializeTree(root, parseEnum(treeMode, TreeMode.class), translationService));
    }

    @POST
    @Path("")
    @Operation(
            summary = "Create a new navigation tree",
            description = "Create a new navigation tree",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsTreeData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of navigation tree"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_NAVTREES_MODIFY_AUTHORITY)
    public Object create(
            WsTreeData data
    ) {
        NavTree tree = command.doCreate(data);
        return response(serializeTree(tree, TREE, translationService));
    }

    @PUT
    @Path("{treeId}")
    @Operation(
            summary = "Update a navigation tree",
            description = "Update a navigation tree",
            parameters = {
                    @Parameter(name = "treeId", in = ParameterIn.PATH, description = "Id of the navigation tree to update", schema = @Schema(type = "string"))
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsTreeData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of navigation tree"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_NAVTREES_MODIFY_AUTHORITY)
    public Object update(
            @PathParam("treeId") String id,
            WsTreeData data
    ) {
        NavTree tree = command.doUpdate(id, data);
        return response(serializeTree(tree, TREE, translationService));
    }

    @DELETE
    @Path("{treeId}")
    @Operation(
            summary = "Delete a navigation tree",
            description = "Delete a navigation tree",
            parameters = {
                    @Parameter(name = "treeId", in = ParameterIn.PATH, description = "Id of the navigation tree to delete", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of navigation tree"),
                    @ApiResponse(responseCode = "404", description = "The navigation tree was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_NAVTREES_MODIFY_AUTHORITY)
    public Object delete(
            @PathParam("treeId") String id
    ) {
        command.doDelete(id);
        return success();
    }

    @POST
    @Path("{treeId}/fixDirections")
    @Operation(
            summary = "Fix navigation tree directions",
            description = "Fix navigation tree directions",
            parameters = {
                    @Parameter(name = "treeId", in = ParameterIn.PATH, description = "Id of the navigation tree to fix", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful fix of navigation tree directions"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {} ), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_NAVTREES_MODIFY_AUTHORITY)
    public Object fixNavtreeDirections(
            @PathParam("treeId") String id
    ) {
        command.doFixNavTreeDirections(id);
        return success();
    }
}
