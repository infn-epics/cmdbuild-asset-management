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
import jakarta.activation.DataHandler;
import jakarta.ws.rs.*;
import org.cmdbuild.dao.entrytype.Attribute;
import org.cmdbuild.service.rest.common.serializationhelpers.AttributeTypeConversionService;
import org.cmdbuild.service.rest.common.serializationhelpers.ViewSerializer;
import org.cmdbuild.service.rest.v4.command.ViewWsCommand;
import org.cmdbuild.service.rest.v4.model.WsViewData;
import org.cmdbuild.view.View;
import org.cmdbuild.view.ViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;

/**
 *
 * @author ldare
 */
@Path("administration/views/")
@Produces(APPLICATION_JSON)
@Component
public class ViewWs_Administration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ViewService viewService;
    private final ViewSerializer viewSerializer;
    private final AttributeTypeConversionService attributeTypeConversionService;
    private final ViewWsCommand command;

    public ViewWs_Administration(
            ViewService viewService,
            ViewSerializer viewSerializer,
            AttributeTypeConversionService attributeTypeConversionService,
            ViewWsCommand command) {
        this.viewService = checkNotNull(viewService);
        this.viewSerializer = checkNotNull(viewSerializer);
        this.attributeTypeConversionService = checkNotNull(attributeTypeConversionService);
        this.command = checkNotNull(command);
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all views",
            description = "Get all views",
            parameters = {
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = DETAILED, in = ParameterIn.QUERY, description = "Include or not full details in the response", schema = @Schema(type = "boolean")),
                    @Parameter(name = "shared", in = ParameterIn.QUERY, description = "Include or not shared views", schema = @Schema(type = "boolean"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of views"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getMany(
            @QueryParam(LIMIT) Long limit,
            @QueryParam(START) Long offset,
            @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean detailed,
            @QueryParam("shared") @DefaultValue(FALSE) Boolean sharedOnly
    ) {
        logger.debug("list all views");
        List<View> views = command.doGetMany(viewService::getAllSharedViews);
        logger.trace("processing views = {}", views);
        return response(views.stream().map(v -> detailed ? viewSerializer.serializeDetailedView(v) : viewSerializer.serializeView(v)).collect(toList()));
    }

    @GET
    @Path("{viewId}")
    @Operation(
            summary = "Get a specific view",
            description = "Get a specific view",
            parameters = {
                    @Parameter(name = "viewId", in = ParameterIn.PATH, description = "Id of the view to query"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of view"),
                    @ApiResponse(responseCode = "404", description = "The view was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getOne(
            @PathParam("viewId") String viewId
    ) {
        View view = command.doGetOne(viewId, viewService::getSharedByName);
        return response(viewSerializer.serializeDetailedView(view));
    }

    @GET
    @Path("{viewId}/attributes")
    @Operation(
            summary = "Get attributes for a specific view",
            description = "Get attributes for a specific view",
            parameters = {
                    @Parameter(name = "viewId", in = ParameterIn.PATH, description = "Id of the view to query"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of attributes"),
                    @ApiResponse(responseCode = "404", description = "The view was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getAttributes(
            @PathParam("viewId") String viewId
    ) {
        Collection<Attribute> attributesForView = command.doGetAttributes(viewId, viewService::getSharedByName);
        return response(attributeTypeConversionService.serialize(attributesForView));
    }

    @POST
    @Path("")
    @Operation(
            summary = "Create a new view",
            description = "Create a new view",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsViewData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of view"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object create(
            @Parameter(schema = @Schema(implementation = WsViewData.class)) WsViewData data
    ) {
        View view = command.doCreate(data);
        return response(viewSerializer.serializeDetailedView(view));
    }

    @PUT
    @Path("{viewId}")
    @Operation(
            summary = "Update an existing view",
            description = "Update an existing view",
            parameters = {
                    @Parameter(name = "viewId", in = ParameterIn.PATH, description = "Id of the view to query"),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsViewData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of view"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object update(
            @PathParam("viewId") String viewId,
            @Parameter(schema = @Schema(implementation = WsViewData.class)) WsViewData data
    ) {
        View view = command.doUpdate(viewId, data);
        return response(viewSerializer.serializeDetailedView(view));
    }

    @DELETE
    @Path("{viewId}")
    @Operation(
            summary = "Delete a view",
            description = "Delete a specific view",
            parameters = {
                    @Parameter(name = "viewId", in = ParameterIn.PATH, description = "Id of the view to query"),
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful deletion of view"),
                    @ApiResponse( responseCode = "404", description = "The view was not found"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam("viewId") String viewId
    ) {
        command.doDelete(viewId, viewService::getSharedByName);
        return success();
    }

    @GET
    @Path("{viewId}/print/{file}")
    @Operation(
            summary = "Print a view",
            description = "Print a view",
            parameters = {
                    @Parameter(name = "viewId", in = ParameterIn.PATH, description = "Id of the view to query"),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter to apply to the view"),
                    @Parameter(name = SORT, in = ParameterIn.QUERY, description = "How to order results", schema = @Schema(ref = "DefaultSortExample")),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = EXTENSION, in = ParameterIn.QUERY, description = "Extension to use for the file", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful print of view"),
                    @ApiResponse( responseCode = "404", description = "The view was not found"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    public DataHandler printView(
            @PathParam("viewId") String viewId,
            @QueryParam(FILTER) String filterStr,
            @QueryParam(SORT) @Parameter(description = "How to order results", schema = @Schema(ref = "DefaultSortExample")) String sort,
            @QueryParam(LIMIT) @Parameter(description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")) Long limit,
            @QueryParam(START) @Parameter(description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0") Long offset,
            @QueryParam(EXTENSION) String extension, @QueryParam("attributes") String attributes
    ) {
        return command.doPrintView(viewId, filterStr, sort, limit, offset, extension, attributes);
    }
}
