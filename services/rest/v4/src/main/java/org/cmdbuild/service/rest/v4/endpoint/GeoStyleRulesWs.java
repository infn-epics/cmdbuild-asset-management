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
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.gis.stylerules.GisStyleRuleset;
import org.cmdbuild.service.rest.v4.command.GeoStyleRulesWsCommand;
import org.cmdbuild.service.rest.v4.model.WsRulesetData;
import org.cmdbuild.service.rest.v4.serializationhelpers.GisStyleRulesetSerializer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_GIS_MODIFY_AUTHORITY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.service.rest.v4.serializationhelpers.GisStyleRulesetSerializer.serializeRulesResult;
import static org.cmdbuild.service.rest.v4.serializationhelpers.GisStyleRulesetSerializer.serializeRuleset;

@Path("{" + TYPE + ":classes|processes}/{" + CLASS_ID + "}/geostylerules/")
@Tag(name = "Geo style rules", description = "Operations related to geo style rules")
@Produces(APPLICATION_JSON)
@Component
public class GeoStyleRulesWs {

    private final GeoStyleRulesWsCommand command;

    public GeoStyleRulesWs(GeoStyleRulesWsCommand command) {
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get geo style rules for a class",
            description = "Get geo style rules for a class",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query", schema = @Schema(type = "string")),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter query to apply to the resultset", schema = @Schema(type = "string", example = "code:test")),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0")),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo style rules data"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style rules found for the given class"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            @PathParam(CLASS_ID) String classId,
            @QueryParam(FILTER) String filterStr,
            @QueryParam(LIMIT) Integer limit,
            @QueryParam(START) Integer offset
    ) {
        List<GisStyleRuleset> list = command.doReadAll(classId, filterStr);
        return response(paged(list, offset, limit).map(GisStyleRulesetSerializer::serializeRuleset));
    }

    @GET
    @Path("{rulesetId}/")
    @Operation(
            summary = "Get geo style rule details",
            description = "Get geo style rule details",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query", schema = @Schema(type = "string")),
                    @Parameter(name = "rulesetId", in = ParameterIn.PATH, description = "Id of the geo style rule to query", schema = @Schema(type = "integer")),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo style rule data"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style rule found for the given id"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam(CLASS_ID) String classId,
            @PathParam("rulesetId") Long rulesetId
    ) {
        GisStyleRuleset rulesetById = command.doRead(classId, rulesetId);
        return response(serializeRuleset(rulesetById));
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create geo style ruleset",
            description = "Create geo style ruleset",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsRulesetData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of geo style ruleset data"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style ruleset found for the given class"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object create(
            WsRulesetData data
    ) {
        GisStyleRuleset rules = command.doCreate(data);
        return response(serializeRuleset(rules));
    }

    @PUT
    @Path("{rulesetId}/")
    @Operation(
            summary = "Update geo style ruleset",
            description = "Update geo style ruleset",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = "rulesetId", in = ParameterIn.PATH, description = "Id of the geo style rule to query", schema = @Schema(type = "integer")),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsRulesetData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of geo style ruleset data"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style ruleset found for the given id"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object update(
            @PathParam("rulesetId") Long rulesetId,
            WsRulesetData data
    ) {
        GisStyleRuleset rules = command.doUpdate(rulesetId, data);
        return response(serializeRuleset(rules));
    }

    @DELETE
    @Path("{rulesetId}/")
    @Operation(
            summary = "Delete geo style ruleset",
            description = "Delete geo style ruleset",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = "rulesetId", in = ParameterIn.PATH, description = "Id of the geo style rule to query", schema = @Schema(type = "integer")),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of geo style ruleset data"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style ruleset found for the given id"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object delete(
            @PathParam("rulesetId") Long rulesetId
    ) {
        command.doDelete(rulesetId);
        return success();
    }

    @POST
    @Path("visibility")
    @RolesAllowed(ADMIN_GIS_MODIFY_AUTHORITY)
    @Operation(
            summary = "Bulk update visibility of geostyle rules",
            description = "Bulk update visibility from public to private and viceversa",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string")),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = Map.class, example = ""))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of visibility of geostyle rules"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style ruleset found for the given id"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object updateVisibility(
            @PathParam(CLASS_ID) String classId,
            Map<Long, Boolean> rulesets
    ) {
        rulesets = command.doUpdateVisibility(classId, rulesets);
        return response(rulesets);
    }

    @GET
    @Path("{rulesetId}/result")
    @Operation(
            summary = "Apply geo style rules to cards",
            description = "Apply geo style rules to cards",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = "rulesetId", in = ParameterIn.PATH, description = "Id of the geo style rule to query", schema = @Schema(type = "integer")),
                    @Parameter(name = "cards", in = ParameterIn.QUERY, description = "Comma separated list of card ids to apply the rules to", schema = @Schema(type = "string")),
            },
            responses = {@ApiResponse(responseCode = "200", description = "Successful retrieval of geo style rules application result data")},
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object applyRules(
            @PathParam("rulesetId") Long rulesetId,
            @QueryParam("cards") @Nullable String cards
    ) {
        return response(serializeRulesResult(command.doApplyRules(cards, rulesetId)));
    }

    @POST
    @Path("tryRules")
    @Operation(
            summary = "Test geo style rules on cards",
            description = "Test geo style rules on cards",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = "cards", in = ParameterIn.QUERY, description = "Comma separated list of card ids to apply the rules to", schema = @Schema(type = "string")),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsRulesetData.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo style rules test result data"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "401", description = "User not authenticated"),
                    @ApiResponse(responseCode = "403", description = "User not authorized to read the related geo attribute"),
                    @ApiResponse(responseCode = "404", description = "No geo style ruleset found for the given id"),
                    @ApiResponse(responseCode = "422", description = "Invalid filter query"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object testRules(
            WsRulesetData data,
            @QueryParam("cards") @Nullable String cards
    ) {
        return response(serializeRulesResult(command.doTestRules(cards, data)));
    }


}
