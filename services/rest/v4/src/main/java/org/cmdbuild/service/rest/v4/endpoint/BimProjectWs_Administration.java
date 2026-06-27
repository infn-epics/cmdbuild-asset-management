/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
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
import jakarta.activation.DataHandler;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.bim.BimObject;
import org.cmdbuild.bim.BimProjectExt;
import org.cmdbuild.service.rest.common.serializationhelpers.BimProjectSerializationHelper;
import org.cmdbuild.service.rest.v4.command.BimProjectWsCommand;
import org.cmdbuild.service.rest.v4.model.WsProjectData;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_BIM_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_BIM_VIEW_AUTHORITY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.serializationhelpers.BimProjectSerializationHelper.serializeProjectAndObject;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.FILE;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.ID;
import static org.cmdbuild.utils.date.CmDateUtils.toIsoDateTime;
import static org.cmdbuild.utils.lang.CmExceptionUtils.unsupported;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 * @author ldare
 */
@Path("administration/bim/projects/")
@Tag( name = "BIM", description = "BIM Projects" )
@Produces(APPLICATION_JSON)
@Component
public class BimProjectWs_Administration {

    private final BimProjectWsCommand command;

    public BimProjectWs_Administration(BimProjectWsCommand command) {
        this.command = command;
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get projects and objects",
            description = "Returns sorted serialized projects and objects",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = BimProjectExt.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} ) }
    )
    @RolesAllowed(ADMIN_BIM_VIEW_AUTHORITY)
    public Object getAll() {
        return response(command.doGetAll().stream().map(BimProjectSerializationHelper::serializeProjectAndObject));
    }

    /**
     * Gets BIM object by project and global ID
     */
    @GET
    @Path("{projectId}/values/{globalId}")
    @Operation(
            summary = "Gets BIM object by project and global ID",
            description = "Gets BIM object by project and global ID",
            parameters = {
                    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "Id of Project"),
                    @Parameter(name = "globalId", in = ParameterIn.PATH, description = "Global Id"),
                    @Parameter(name = "if_exists", in = ParameterIn.QUERY, description = "If true, return only if exists")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = BimObject.class))),
                    @ApiResponse(responseCode = "404", description = "BIM object not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @RolesAllowed(ADMIN_BIM_VIEW_AUTHORITY)
    public Object getValue(
            @PathParam("projectId") Long projectId,
            @PathParam("globalId") String globalId,
            @QueryParam("if_exists") @DefaultValue(FALSE) Boolean ifExists
    ) {
        BimObject value = command.doGetValue(projectId, globalId);
        if (ifExists && value == null) {
            return response(map("exists", false));
        }
        checkNotNull(value, "bim value not found for gid =< %s >", globalId);
        return response(map(
                "_id", value.getId(),
                "ownerType", value.getOwnerClassId(),
                "ownerId", value.getOwnerCardId(),
                "projectId", value.getProjectId(),
                "globalId", value.getGlobalId()
        ).accept((m) -> {
            if (ifExists) {
                m.put("exists", true);
            }
        }));
    }

    @GET
    @Path("{id}")
    @Operation(
            summary = "Get project by id",
            description = "Returns serialized project by id",
            parameters = {
                    @Parameter(name = ID, in = ParameterIn.PATH, description = "Id of Project to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = BimProjectExt.class))),
                    @ApiResponse(responseCode = "404", description = "Project not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @RolesAllowed(ADMIN_BIM_VIEW_AUTHORITY)
    public Object getOne(
            @PathParam(ID) Long id
    ) {
        BimProjectExt projectExt = command.doGetOne(id);
        return response(serializeProjectAndObject(projectExt));
    }

    @POST
    @Path("")
    @Operation(
            summary = "Create new project",
            description = "Create new project",
            parameters = {
                    @Parameter(name = FILE, schema = @Schema(implementation = DataHandler.class))
            },
            requestBody = @RequestBody(description = "Project data", required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = WsProjectData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = BimProjectExt.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @Consumes(MULTIPART_FORM_DATA)
    @RolesAllowed(ADMIN_BIM_MODIFY_AUTHORITY)
    public Object createProjectWithFile(
            @Multipart(value = FILE, required = false) DataHandler dataHandler,
            WsProjectData data
    ) {
        BimProjectExt project = command.doCreateProjectWithFile(dataHandler, data);
        return response(serializeProjectAndObject(project));
    }

    @PUT
    @Path("{id}")
    @Operation(
            summary = "Update project",
            description = "Update project",
            parameters = {
                    @Parameter(name = ID, in = ParameterIn.PATH, description = "Id of Project to update"),
                    @Parameter(name = FILE, schema = @Schema(implementation = DataHandler.class))
            },
            requestBody = @RequestBody(description = "Project data", required = true, content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = WsProjectData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful Operation", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(implementation = BimProjectExt.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @RolesAllowed(ADMIN_BIM_MODIFY_AUTHORITY)
    public Object update(
            @PathParam(ID) Long id,
            @Multipart(value = FILE, required = false) DataHandler dataHandler,
            WsProjectData data
    ) {
        BimProjectExt bimProjectExt = command.doUpdate(id, dataHandler, data);
        return response(serializeProjectAndObject(bimProjectExt));
    }

    @GET
    @Path("{id}/file")
    @Operation(
            summary = "Download project file",
            description = "Download project file",
            parameters = {
                    @Parameter(name = ID, in = ParameterIn.PATH, description = "Id of Project to query"),
                    @Parameter(name = "ifcFormat", in = ParameterIn.QUERY, description = "Ifc format to download"),
                    @Parameter(name = "bimFormat", in = ParameterIn.QUERY, description = "Bim format to download")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/octet-stream")),
                    @ApiResponse(responseCode = "404", description = "The project file was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @RolesAllowed(ADMIN_BIM_VIEW_AUTHORITY)
    public DataHandler downloadIfcFile(
            @PathParam(ID) Long id,
            @QueryParam("ifcFormat") @Nullable String ifcFormat,
            @QueryParam("bimFormat") String bimFormat
    ) {
        return command.doDownloadIfcFile(id, ifcFormat, bimFormat);
    }

    @POST
    @Path("{id}/file")
    @Operation(
            summary = "Upload project file",
            description = "Upload project file",
            parameters = {
                    @Parameter(name = ID, in = ParameterIn.PATH, description = "Id of Project to query"),
                    @Parameter(name = FILE, schema = @Schema(implementation = DataHandler.class))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON)),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    @Consumes(MULTIPART_FORM_DATA)
    @RolesAllowed(ADMIN_BIM_MODIFY_AUTHORITY)
    public Object uploadIfcFile(
            @PathParam(ID) Long id,
            @Multipart(FILE) DataHandler dataHandler
    ) throws IOException {
        command.doUploadIfcFile(id, dataHandler);
        return success();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete project",
            description = "Delete project",
            parameters = {
                    @Parameter(name = ID, in = ParameterIn.PATH, description = "Id of Project to delete")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON)),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @RolesAllowed(ADMIN_BIM_MODIFY_AUTHORITY)
    public Object delete(
            @PathParam(ID) @Parameter(description = "Id of Project to delete") Long id
    ) {
        command.doDelete(id);
        return success();
    }


}
