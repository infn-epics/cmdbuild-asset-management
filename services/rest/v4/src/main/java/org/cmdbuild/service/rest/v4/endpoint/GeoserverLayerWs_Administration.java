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
import jakarta.activation.DataHandler;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.gis.GeoserverLayer;
import org.cmdbuild.gis.GisService;
import org.cmdbuild.service.rest.v4.command.GeoserverLayerWsCommand;
import org.cmdbuild.service.rest.v4.model.WsLayerData;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.serializationhelpers.GeoserverLayerSerializationHelper.serializeLayer;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.service.rest.v4.command.WsUtils.filterSerializations;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;

/**
 *
 * @author schursin
 */
@Path("administration/{" + TYPE + ":classes|processes}/{" + CLASS_ID + "}/{" + TYPE_NAME + ":cards|instances}/{" + CARD_ID + "}/geolayers/")
@Tags({
        @Tag(name = "Geoserver Layers", description = "APIs to manage geoserver layers."),
        @Tag(name = "Administration")
})
@Produces(APPLICATION_JSON)
@Component
public class GeoserverLayerWs_Administration {

    private final GisService gisService;
    private final DaoService daoService;
    private final GeoserverLayerWsCommand command;

    public GeoserverLayerWs_Administration(GisService gisService, DaoService daoService, GeoserverLayerWsCommand command) {
        this.gisService = checkNotNull(gisService);
        this.daoService = checkNotNull(daoService);
        this.command = command;
    }

    @GET
    @Path("{attrName}/")
    @Operation(
            summary = "Get a geoserver layer for a given class and card",
            description = "Get a geoserver layer for a given class and card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"cards", "instances"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, required = true, description = "Id of the card to query"),
                    @Parameter(name = "attrName", in = ParameterIn.PATH, required = true, description = "Name of the attribute to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geoserver layer"),
                    @ApiResponse(responseCode = "404", description = "Geoserver layer not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object getOneForCard(
            @PathParam(CLASS_ID) String classId,
            @PathParam(CARD_ID) Long cardId,
            @PathParam("attrName") String layerCodeOrId
    ) {
        GeoserverLayer layer = command.doGetOneForCard(classId, cardId, layerCodeOrId);
        return response(serializeLayer(layer, gisService, daoService));
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get all geoserver layers for a given class and card",
            description = "Get all geoserver layers for a given class and card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"cards", "instances"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, required = true, description = "Id of the card to query"),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter geoserver layers by name"),
                    @Parameter(name = "visible", in = ParameterIn.QUERY, description = "Filter geoserver layers by visibility")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geoserver layers"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object getMany(
            @PathParam(CLASS_ID) String classId,
            @PathParam(CARD_ID) String cardId,
            @QueryParam(FILTER) String filterStr,
            @QueryParam("visible") @DefaultValue(FALSE) Boolean isVisible
    ) {
        List<GeoserverLayer> listGeoserverLayer = command.doGetMany(classId, cardId);
        List<FluentMap<String, Object>> listSerializedGeoserverLayer = list(listGeoserverLayer).map(m -> serializeLayer(m, gisService, daoService));
        listSerializedGeoserverLayer = filterSerializations(listSerializedGeoserverLayer, filterStr);
        return response(listSerializedGeoserverLayer);
    }

    @PUT
    @Path("{attrName}/")
    @Operation(
            summary = "Update a geoserver layer for a given class and card",
            description = "Update a geoserver layer for a given class and card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"cards", "instances"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, required = true, description = "Id of the card to query"),
                    @Parameter(name = "attrName", in = ParameterIn.PATH, required = true, description = "Name of the attribute to query"),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsLayerData.class)), required = true, description = "Geoserver layer data to update"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of geoserver layer"),
                    @ApiResponse(responseCode = "404", description = "Geoserver layer not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object update(
            @PathParam(CLASS_ID) String classId,
            @PathParam(CARD_ID) Long cardId,
            @PathParam("attrName") String attrName,
            @Multipart(value = FILE, required = false) DataHandler dataHandler,
            @Nullable WsLayerData data
    ) {
        GeoserverLayer layer = command.doUpdate(classId, cardId, attrName, dataHandler, data);
        return response(serializeLayer(layer, gisService, daoService));
    }

    @DELETE
    @Path("{attrName}/")
    @Operation(
            summary = "Delete a geoserver layer for a given class and card",
            description = "Delete a geoserver layer for a given class and card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"cards", "instances"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, required = true, description = "Id of the card to query"),
                    @Parameter(name = "attrName", in = ParameterIn.PATH, required = true, description = "Name of the attribute to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of geoserver layer"),
                    @ApiResponse(responseCode = "404", description = "Geoserver layer not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam(CLASS_ID) String classId,
            @PathParam(CARD_ID) Long cardId,
            @PathParam("attrName") String attrName
    ) {
        command.doDelete(classId, cardId, attrName);
        return success();
    }

}
