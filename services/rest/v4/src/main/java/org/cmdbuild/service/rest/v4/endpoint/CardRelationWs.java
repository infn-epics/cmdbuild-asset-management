package org.cmdbuild.service.rest.v4.endpoint;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import org.cmdbuild.classe.access.UserDomainService;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.beans.CMRelation;
import org.cmdbuild.dao.beans.RelationImpl;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dao.driver.postgres.q3.DaoQueryOptionsImpl;
import org.cmdbuild.service.rest.common.serializationhelpers.card.CardWsSerializationHelperv3;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmNullableUtils.ltEqZeroToNull;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotNullAndGtZero;
import jakarta.ws.rs.*;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.beans.CMRelation;
import org.cmdbuild.service.rest.common.serializationhelpers.card.CardWsSerializationHelperv3;
import org.cmdbuild.service.rest.v4.command.CardRelationWsCommand;
import org.cmdbuild.service.rest.v4.model.WsRelationData;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;

@Path("{" + TYPE + ":classes|processes}/{" + CLASS_ID + "}/{" + TYPE_NAME + ":cards|instances}/{" + CARD_ID + "}/relations/")
@Tag(name = "Relations", description = "Operations about relations between cards")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class CardRelationWs {

    private final CardWsSerializationHelperv3 cardWsSerializationHelperv3;
    private final CardRelationWsCommand command;

    public CardRelationWs(CardWsSerializationHelperv3 cardWsSerializationHelperv3, CardRelationWsCommand command) {
        this.cardWsSerializationHelperv3 = checkNotNull(cardWsSerializationHelperv3);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get relations for a card",
            description = "Get relations for a card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query"),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Limit the number of relations returned"),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "Offset the number of relations returned"),
                    @Parameter(name = DETAILED, in = ParameterIn.QUERY, description = "Return detailed information about relations"),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter relations by attribute"),
                    @Parameter(name = SORT, in = ParameterIn.QUERY, description = "Sort relations by attribute")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of relations"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam(CLASS_ID) String className,
            @PathParam(CARD_ID) Long cardId,
            @QueryParam(LIMIT)Long limit,
            @QueryParam(START) Long offset,
            @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean detailed,
            @QueryParam(FILTER) String filterStr,
            @QueryParam(SORT) String sort) {
        PagedElements<CMRelation> relations = command.doRead(className, cardId, limit, offset, filterStr, sort);
        return response(relations.map(detailed ? cardWsSerializationHelperv3::serializeDetailedRelation : cardWsSerializationHelperv3::serializeMinimalRelation));
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create a new relation",
            description = "Create a new relation between two cards",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query")
            },
            requestBody = @RequestBody( content = @Content( schema = @Schema(implementation = WsRelationData.class, description = "Relation data"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of relation"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object create(
            @PathParam(CARD_ID) Long cardId,
            WsRelationData relationData
    ) {
        CMRelation relation = command.doCreate(cardId, relationData);

        return response(cardWsSerializationHelperv3.serializeDetailedRelation(relation));
    }

    @PUT
    @Path("{" + RELATION_ID + "}/")
    @Operation(
            summary = "Update an existing relation",
            description = "Update an existing relation between two cards",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = RELATION_ID, in = ParameterIn.PATH, description = "Id of the relation to update")
            },
            requestBody = @RequestBody( content = @Content( schema = @Schema(implementation = WsRelationData.class, description = "Relation data"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of relation"),
                    @ApiResponse(responseCode = "404", description = "Relation not found"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object update(
            @PathParam(CARD_ID) Long cardId,
            @PathParam(RELATION_ID) Long relationId,
            WsRelationData relationData
    ) {
        CMRelation relation = command.doUpdate(cardId, relationId, relationData);
        return response(cardWsSerializationHelperv3.serializeDetailedRelation(relation));
    }

    @DELETE
    @Path("{" + RELATION_ID + "}/")
    @Operation(
            summary = "Delete a relation",
            description = "Delete a specific relation between two cards",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = RELATION_ID, in = ParameterIn.PATH, description = "Id of the relation to delete")
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful deletion of relation"),
                    @ApiResponse( responseCode = "404", description = "Relation not found"),
                    @ApiResponse( responseCode = "403", description = "Forbidden"),
                    @ApiResponse( responseCode = "401", description = "Unauthorized"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam(RELATION_ID) Long relationId
    ) {
        command.doDelete(relationId);
        return success();
    }
}
