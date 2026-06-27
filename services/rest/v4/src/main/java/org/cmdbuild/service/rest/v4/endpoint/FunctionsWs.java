package org.cmdbuild.service.rest.v4.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Ordering;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.ws.rs.*;
import org.apache.commons.lang3.math.NumberUtils;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dao.core.q3.ResultRow;
import org.cmdbuild.dao.entrytype.Attribute;
import org.cmdbuild.dao.function.StoredFunction;
import org.cmdbuild.service.rest.common.serializationhelpers.AttributeTypeConversionService;
import org.cmdbuild.service.rest.common.serializationhelpers.StoredFunctionSerializer;
import org.cmdbuild.service.rest.common.serializationhelpers.card.CardWsSerializationHelperv3;
import org.cmdbuild.service.rest.v4.command.FunctionsWsCommand;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.serializationhelpers.StoredFunctionSerializer.toDetailedResponse;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import org.cmdbuild.translation.ObjectTranslationService;
import org.cmdbuild.utils.json.CmJsonUtils;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.uniqueIndex;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.json.CmJsonUtils.MAP_OF_OBJECTS;
import static org.cmdbuild.utils.json.CmJsonUtils.fromJson;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmExceptionUtils.unsupported;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

@Path("functions/")
@Tag( name = "Functions", description = "Functions")
@Produces(APPLICATION_JSON)
@Component
public class FunctionsWs {

    private final CardWsSerializationHelperv3 cardWsSerializationHelperv3;
    private final AttributeTypeConversionService attributeTypeConversionService;
    private final FunctionsWsCommand command;

    public FunctionsWs(CardWsSerializationHelperv3 cardWsSerializationHelperv3, AttributeTypeConversionService attributeTypeConversionService, FunctionsWsCommand command) {
        this.cardWsSerializationHelperv3 = checkNotNull(cardWsSerializationHelperv3);
        this.attributeTypeConversionService = checkNotNull(attributeTypeConversionService);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get the list of stored functions",
            description = "Retrieve the list of stored functions defined in the CMDBuild system. Supports filtering, pagination, and detailed views.",
            parameters = {
                    @Parameter(name = FILTER, description = "Filter functions by attribute values"),
                    @Parameter(name = START, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = LIMIT, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = DETAILED, description = "Include or not full details in the response"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of function data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START)  Integer offset,
            @QueryParam(FILTER) String filterStr,
            @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean detailed
    ) {
        PagedElements<StoredFunction> paged = command.doReadAll(limit, offset, filterStr);

        return response(paged.stream().map(m -> detailed ? StoredFunctionSerializer.toDetailedResponse(m, attributeTypeConversionService) : StoredFunctionSerializer.toResponse(m)).collect(toList()), paged.totalSize());
    }

    @GET
    @Path("{" + FUNCTION_ID + "}/")
    @Operation(
            summary = "Get stored function details",
            description = "Retrieve detailed information about a specific stored function by its ID or name.",
            parameters = {
                    @Parameter(name = FUNCTION_ID, description = "ID or name of the function", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of function details"),
                    @ApiResponse(responseCode = "404", description = "Function not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam(FUNCTION_ID) String functionId
    ) {
        StoredFunction function = command.doRead(functionId);
        return response(toDetailedResponse(function, attributeTypeConversionService));
    }

    @GET
    @Path("{" + FUNCTION_ID + "}/parameters/")
    @Operation(
            summary = "Get stored function input parameters",
            description = "Retrieve the input parameters of a specific stored function by its ID or name.",
            parameters = {
                    @Parameter(name = FUNCTION_ID, description = "ID or name of the function", required = true),
                    @Parameter(name = LIMIT, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of function input parameters"),
                    @ApiResponse(responseCode = "404", description = "Function not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readInputParameters(
            @PathParam(FUNCTION_ID) String functionId,
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START) Integer offset
    ) {
        StoredFunction function = command.doRead(functionId);
        return attributeTypeConversionService.serializeResponse(function.getInputParameters(), function.getName(), limit, offset);
    }

    @GET
    @Path("{" + FUNCTION_ID + "}/attributes/")
    @Operation(
            summary = "Get stored function output parameters",
            description = "Retrieve the output parameters of a specific stored function by its ID or name.",
            parameters = {
                    @Parameter(name = FUNCTION_ID, description = "ID or name of the function", required = true),
                    @Parameter(name = LIMIT, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of function output parameters"),
                    @ApiResponse(responseCode = "404", description = "Function not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readOutputParameters(
            @PathParam(FUNCTION_ID) String functionId,
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START) Integer offset
    ) {
        StoredFunction function = command.doRead(functionId);
        return attributeTypeConversionService.serializeResponse(function.getOutputParameters(), function.getName(), limit, offset);
    }

    @GET
    @Path("{" + FUNCTION_ID + "}/outputs/")
    @Operation(
            summary = "Call stored function and get output parameters",
            description = "Invoke a specific stored function by its ID or name and retrieve the output parameters based on provided input parameters.",
            parameters = {
                    @Parameter(name = FUNCTION_ID, description = "ID or name of the function", required = true),
                    @Parameter(name = PARAMETERS, description = "JSON string containing input parameters for the function"),
                    @Parameter(name = "model", description = "JSON string containing output parameters model")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of function output parameters"),
                    @ApiResponse(responseCode = "404", description = "Function not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object call(
            @PathParam(FUNCTION_ID) String functionId,
            @QueryParam(PARAMETERS) String inputs,
            @QueryParam("model") String model
    ) {
        StoredFunction function = command.doRead(functionId);
        List<Object> inputParams = command.doGetInputParams(inputs, function);
        List<Attribute> outputParams = command.doGetOutputParams(model, function);
        List<ResultRow> results = command.doGetResults(function, inputParams, outputParams);

        List rows = results.stream().map((r) -> r.asMap())
                .map((source) -> map().accept((map) -> outputParams.forEach((p) -> cardWsSerializationHelperv3.addCardValuesAndDescriptionsAndExtras(p.getName(), p.getType(), source::get, map::put)))).collect(toList());

        return response(rows);
    }

    @POST
    @Path("{" + FUNCTION_ID + "}/outputs/")
    @Operation(
            summary = "Call stored function via POST and get output parameters",
            description = "Invoke a specific stored function by its ID or name using a POST request and retrieve the output parameters based on provided input parameters.",
            parameters = {
                    @Parameter(name = FUNCTION_ID, description = "ID or name of the function", required = true),
                    @Parameter(name = "model", description = "JSON string containing output parameters model"),
                    @Parameter(name = PARAMETERS, description = "JSON string containing input parameters for the function"),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of function output parameters"),
                    @ApiResponse(responseCode = "404", description = "Function not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object callAsPost(
            @PathParam(FUNCTION_ID) String functionId,
            @QueryParam("model") String model,
            @Multipart(value = PARAMETERS, required = false) @Nullable String parameters) {
        StoredFunction function = command.doRead(functionId);
        List<Object> inputParams = command.doGetInputParams(parameters, function);
        List<Attribute> outputParams = command.doGetOutputParams(model, function);
        List<ResultRow> results = command.doGetResults(function, inputParams, outputParams);

        List rows = results.stream().map((r) -> r.asMap())
                .map((source) -> map().accept((map) -> outputParams.forEach((p) -> cardWsSerializationHelperv3.addCardValuesAndDescriptionsAndExtras(p.getName(), p.getType(), source::get, map::put)))).collect(toList());

        return response(rows);
    }
}
