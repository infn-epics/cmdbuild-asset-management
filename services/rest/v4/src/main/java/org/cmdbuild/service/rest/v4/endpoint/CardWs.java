package org.cmdbuild.service.rest.v4.endpoint;

import com.google.common.base.Function;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
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
import org.cmdbuild.classe.access.UserCardQueryForDomain;
import org.cmdbuild.classe.access.UserCardService;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.beans.Card;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dao.core.q3.QueryBuilder;
import org.cmdbuild.dao.driver.postgres.q3.DaoQueryOptions;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.dao.function.StoredFunction;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.common.helpers.CardsForDomainFetcher;
import org.cmdbuild.service.rest.common.serializationhelpers.card.*;
import org.cmdbuild.service.rest.v4.command.CardWsCommand;
import org.cmdbuild.service.rest.v4.model.WsCardData;
import org.cmdbuild.service.rest.v4.model.WsForDomainOptions;
import org.cmdbuild.services.serialization.CompositeDataSerializer;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.dao.core.q3.DaoService.COUNT;
import static org.cmdbuild.dao.core.q3.WhereOperator.ISNOTNULL;
import static org.cmdbuild.dao.postgres.utils.SqlQueryUtils.quoteSqlIdentifier;
import static org.cmdbuild.dao.utils.SorterProcessor.sorted;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.service.rest.v4.utils.PositionOfUtils.handlePositionOfAndGetMeta;

@Path("classes/{" + CLASS_ID + "}/cards/")
@Tag(name = "Card", description = "Operations related to cards")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class CardWs {

    private final UserClassService userClassService;
    private final UserCardService userCardService;
    private final DaoService daoService;
    private final CardWsSerializationHelperv3 cardWsSerializationHelperv3;
    private final DmsService dmsService;
    private final CardsForDomainFetcher cardsForDomainFetcher;
    private final CardWsCommand command;

    public CardWs(UserClassService userClassService, UserCardService userCardService, DaoService daoService, CardWsSerializationHelperv3 cardWsSerializationHelperv3, DmsService dmsService, CardsForDomainFetcher cardsForDomainFetcher, CardWsCommand command) {
        this.userClassService = checkNotNull(userClassService);
        this.userCardService = checkNotNull(userCardService);
        this.daoService = checkNotNull(daoService);
        this.cardWsSerializationHelperv3 = checkNotNull(cardWsSerializationHelperv3);
        this.dmsService = checkNotNull(dmsService);
        this.cardsForDomainFetcher = checkNotNull(cardsForDomainFetcher);
        this.command = command;
    }

    @GET
    @Path("{" + CARD_ID + "}/")
    @Operation(
            summary = "Get a card",
            description = "Get a card",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query"),
                    @Parameter(name = "includeModel", in = ParameterIn.QUERY, description = "Include model in the response"),
                    @Parameter(name = "includeWidgets", in = ParameterIn.QUERY, description = "Include widgets in the response"),
                    @Parameter(name = "includeStats", in = ParameterIn.QUERY, description = "Include stats in the response"),
                    @Parameter(name = "infoOnly", in = ParameterIn.QUERY, description = "Only return card info")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of card data"),
                    @ApiResponse(responseCode = "404", description = "Card not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readOne(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(CARD_ID) Long cardId,
            @QueryParam("includeModel") @DefaultValue(FALSE) Boolean includeModel,
            @QueryParam("includeWidgets") @DefaultValue(FALSE) Boolean includeWidgets,
            @QueryParam("includeStats") @DefaultValue(FALSE) Boolean includeStats,
            @QueryParam("infoOnly") @DefaultValue(FALSE) Boolean infoOnly) {
        Card card = command.doReadOne(classId, cardId, infoOnly);
        CardSerializer cardSerializer = new CardSerializer(cardWsSerializationHelperv3);
        if (infoOnly) {
            return response(cardSerializer.serialize(card));
        }
        if (includeModel) {
            cardSerializer = new CardSerializer_WithModel(cardWsSerializationHelperv3);
        }
        FluentMap<String, Object> serialization = new CompositeDataSerializer(cardSerializer)
                .addEnhancer(new CardSerializerEnhancer_Widgets(includeWidgets, cardWsSerializationHelperv3))
                .addEnhancer(new CardSerializerEnhancer_Stats(includeStats, dmsService, daoService))
                .serialize(card);

        return response(serialization);
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Query cards",
            description = "Query cards",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = "functionValue", in = ParameterIn.QUERY, description = "Function to use to compute the value of the attribute"),
                    @Parameter(name = "distinctIncludeNull", in = ParameterIn.QUERY, description = "Include null values in the distinct list"),
                    @Parameter(name = DISTINCT, in = ParameterIn.QUERY, description = "Attribute to use to compute the distinct list"),
                    @Parameter(name = COUNT, in = ParameterIn.QUERY, description = "Attribute to use to compute the count")
            },
            requestBody = @RequestBody(description = "Query options", content = @Content(schema = @Schema(implementation = WsQueryOptions.class, name = "WsQueryOptions"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of card data"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readMany(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            WsQueryOptions wsQueryOptions,
            WsForDomainOptions wsForDomainOptions,
            @QueryParam("functionValue") String selectFunctionValue,
            @QueryParam("distinctIncludeNull") @DefaultValue(FALSE) Boolean distinctIncludeNull,
            @QueryParam(DISTINCT) String distinctAttribute,
            @QueryParam(COUNT) String countAttribute
    ) {
        Classe classe = userClassService.getUserClass(classId);

        DaoQueryOptions queryOptions = wsQueryOptions.getQuery();

        if (isNotBlank(distinctAttribute)) {//TODO move count, distinct etc to /statistics endpoint (3.3)
            DaoQueryOptions tqueryOptions = queryOptions.mapAttrNames(daoService.getClasse(classId).getAliasToAttributeMap()).expandFulltextFilter(classe);
            List<Map<String, Object>> list = loadDistinctAttributeValues(classe, tqueryOptions, selectFunctionValue, distinctIncludeNull, distinctAttribute, countAttribute);

            return response(paged(list, tqueryOptions.getOffset(), tqueryOptions.getLimit()));
        }

        UserCardQueryForDomain forDomain = wsForDomainOptions.getForDomain();
        PagedElements<Card> cards = cardsForDomainFetcher.fetchCards(forDomain, classId, queryOptions, selectFunctionValue);
        final List<Map<String, Object>> cardsForDomain = cardsForDomainFetcher.fetchCardsForDomain(forDomain, cards, queryOptions, selectFunctionValue);

        return response(cardsForDomain, cards.totalSize(), handlePositionOfAndGetMeta(queryOptions, cards));
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create a new card",
            description = "Create a new card",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            requestBody = @RequestBody(description = "Card data", content = @Content(schema = @Schema(implementation = WsCardData.class, name = "WsCardData"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of card data"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object create(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            WsCardData data
    ) {
        Card card = command.doCreate(classId, data);
        return response(cardWsSerializationHelperv3.serializeCard(card));
    }

    @PUT
    @Path("{" + CARD_ID + "}/")
    @Operation(
            summary = "Update a card",
            description = "Update a card",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query")
            },
            requestBody = @RequestBody(description = "Card data", content = @Content(schema = @Schema(implementation = WsCardData.class, name = "WsCardData"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of card data"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object update(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(CARD_ID) Long cardId,
            WsCardData data
    ) {
        Card card = command.doUpdate(classId, cardId, data);
        return response(cardWsSerializationHelperv3.serializeCard(card));
    }

    @DELETE
    @Path("{" + CARD_ID + "}/")
    @Operation(
            summary = "Delete a card",
            description = "Delete a card",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = CARD_ID, in = ParameterIn.PATH, description = "Id of the card to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of card data"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(CARD_ID) Long cardId
    ) {
        command.doDelete(classId, cardId);
        return success();
    }

    @PUT
    @Path("")
    @Operation(
            summary = "Update multiple cards",
            description = "Update multiple cards",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            requestBody = @RequestBody(description = "Card data", content = @Content(schema = @Schema(implementation = WsCardData.class, name = "WsCardData"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of card data"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object updateMany(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            WsCardData data,
            WsQueryOptions wsQueryOptions
    ) {
        command.doUpdateMany(classId, data, wsQueryOptions);
        return success();
    }

    @DELETE
    @Path("")
    @Operation(
            summary = "Delete multiple cards",
            description = "Delete multiple cards",
            parameters = {
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            requestBody = @RequestBody(description = "Query options", content = @Content(schema = @Schema(implementation = WsQueryOptions.class, name = "WsQueryOptions"))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of card data"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "403", description = "Access denied to the requested card"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object deleteMany(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to delete") String classId,
            WsQueryOptions wsQueryOptions
    ) {
        userCardService.deleteCards(classId, wsQueryOptions.getQuery().getFilter());
        return success();
    }

    @Nullable
    public static String getFilterOrNull(@Nullable String filter, Function<Long, String> filterRepo) {
        if (isBlank(filter)) {
            return null;
        } else {
            JsonPrimitive filterId = JsonParser.parseString(filter).getAsJsonObject().getAsJsonPrimitive("_id");
            if (filterId != null && !filterId.isJsonNull()) {
                return filterRepo.apply(filterId.getAsLong());
            } else {
                return filter;
            }
        }
    }

    private StoredFunction getSelectFunctionValueStoredFunction(String selectFunctionValue) {//TODO duplicate code
        StoredFunction storedFunction = daoService.getFunctionByName(selectFunctionValue);//TODO check fun permission
        checkArgument(storedFunction.hasOnlyOneOutputParameter());//TODO
        return storedFunction;
    }

    private List<Map<String, Object>> loadDistinctAttributeValues(Classe classe, DaoQueryOptions queryOptions, String selectFunctionValue, Boolean distinctIncludeNull, String distinctAttribute, String countAttribute) {
        //TODO order, card level permissions
        boolean count;
        QueryBuilder query;
        if (isNotBlank(countAttribute)) {
            checkArgument(equal(countAttribute, distinctAttribute), "count attribute must match distinct attribute");
            count = true;
        } else {
            count = false;
        }
        boolean distinctOnFunctionValue = isNotBlank(selectFunctionValue) && equal(getSelectFunctionValueStoredFunction(selectFunctionValue).getOnlyOutputParameter().getName(), distinctAttribute);
        if (distinctOnFunctionValue) {
            query = daoService.selectDistinctExpr(getSelectFunctionValueStoredFunction(selectFunctionValue).getOnlyOutputParameter().getName(), format("%s(\"Id\")", quoteSqlIdentifier(getSelectFunctionValueStoredFunction(selectFunctionValue).getName())));//TODO duplicate code, improve this
        } else {
            query = daoService.selectDistinct(distinctAttribute);
        }
        List<Map<String, Object>> list = query.accept(q -> {
            if (count) {
                q.selectCount();
            }
            if (!distinctIncludeNull) {
                q.where(distinctAttribute, ISNOTNULL);
            }
        }).from(classe.getName()).where(queryOptions.getFilter()).run().stream().map(r -> (distinctOnFunctionValue ? cardWsSerializationHelperv3.serializeAttributeValue(getSelectFunctionValueStoredFunction(selectFunctionValue).getOnlyOutputParameter(), r.asMap()) : cardWsSerializationHelperv3.serializeAttributeValue(classe, distinctAttribute, r.asMap())).accept(m -> {
            if (count) {
                m.put("_count", r.get(COUNT, Long.class));
            }
        })).collect(toList());
        list = sorted(list, queryOptions.getSorter());
        return list;
    }
}
