package org.cmdbuild.service.rest.v4.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.config.CoreConfiguration;
import org.cmdbuild.dao.beans.Card;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.email.template.EmailTemplateProcessorService;
import org.cmdbuild.gis.GisAttribute;
import jakarta.ws.rs.*;
import org.cmdbuild.gis.GisService;
import org.cmdbuild.gis.GisValue;
import org.cmdbuild.gis.GisValueType;
import org.cmdbuild.gis.model.GisValueImpl;
import org.cmdbuild.gis.model.LinestringImpl;
import org.cmdbuild.gis.model.PointImpl;
import org.cmdbuild.gis.model.PolygonImpl;
import org.cmdbuild.service.rest.common.utils.WsSerializationUtils;
import org.cmdbuild.template.SimpleExpressionInputData;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import org.cmdbuild.service.rest.common.serializationhelpers.GisValueSerializer;
import org.cmdbuild.service.rest.v4.command.CardGeoValueWsCommand;
import org.cmdbuild.service.rest.v4.model.WsGisValue;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.lang.CmConvertUtils.toLong;
import static org.cmdbuild.utils.lang.CmExceptionUtils.unsupported;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.CARD_ID;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.CLASS_ID;

@Path("{" + TYPE + ":classes|processes}/{" + CLASS_ID + "}/{" + TYPE_NAME + ":cards|instances}/{" + CARD_ID + "}/geovalues")
@Tag(name = "Geo values", description = "Operations related to geo values attached to cards")
@Produces(APPLICATION_JSON)
@Component
public class CardGeoValueWs {

    private final GisService gisService;
    private final GisValueSerializer gisValueSerializer;
    private final CardGeoValueWsCommand command;

    public CardGeoValueWs(GisService gisService, GisValueSerializer gisValueSerializer, CardGeoValueWsCommand command) {
        this.gisService = checkNotNull(gisService);
        this.gisValueSerializer = checkNotNull(gisValueSerializer);
        this.command = command;
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get geo values for a card",
            description = "Get geo values for a card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo values"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getAllForCard(
            @PathParam(CLASS_ID) String classId,
            @PathParam(CARD_ID) Long cardId
    ) {
        List<GisValue> gisValueList = command.doGetAllForCard(classId, cardId);
        return response(gisValueList.stream().map(e -> gisValueSerializer.serializeGisValueWithPanelInfo(e, gisService.getGisAttributeWithCurrentUserByClassAndNameOrId(classId, e.getLayerName()))).collect(toList()));
    }

    @GET
    @Path("/{attributeId}")
    @Operation(
            summary = "Get geo value for a card",
            description = "Get geo value for a card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query"),
                    @Parameter(name = ATTRIBUTE_ID, in = ParameterIn.PATH, description = "Name of the attribute to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo value"),
                    @ApiResponse(responseCode = "404", description = "Geo value not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object get(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(CARD_ID) Long cardId,
            @PathParam(ATTRIBUTE_ID) String attributeId
    ) {
        GisValue gisValue = command.doGet(classId, cardId, attributeId);
        return response(gisValueSerializer.serializeGisValueWithPanelInfo(gisValue, gisService.getGisAttributeWithCurrentUserByClassAndNameOrId(classId, attributeId)));
    }

    @PUT
    @Path("/{attributeId}")
    @Operation(
            summary = "Set geo value for a card",
            description = "Set geo value for a card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query"),
                    @Parameter(name = ATTRIBUTE_ID, in = ParameterIn.PATH, description = "Name of the attribute to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful setting of geo value"),
                    @ApiResponse(responseCode = "400", description = "Invalid geo value"),
                    @ApiResponse(responseCode = "404", description = "Geo value not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object set(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(CARD_ID) Long cardId,
            @PathParam(ATTRIBUTE_ID) String attributeId,
            WsGisValue data
    ) {
        GisValue value = command.doSet(classId, cardId, attributeId, data);
        return response(gisValueSerializer.serializeGisValueWithPanelInfo(value, gisService.getGisAttributeWithCurrentUserByClassAndNameOrId(classId, attributeId)));
    }

    @DELETE
    @Path("/{attributeId}")
    @Operation(
            summary = "Delete geo value for a card",
            description = "Delete geo value for a card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query"),
                    @Parameter(name = ATTRIBUTE_ID, in = ParameterIn.PATH, description = "Name of the attribute to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of geo value"),
                    @ApiResponse(responseCode = "404", description = "Geo value not found"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(CARD_ID) Long cardId,
            @PathParam(ATTRIBUTE_ID) String attributeId
    ) {
        command.doDelete(classId, cardId, attributeId);
        return success();
    }
}
