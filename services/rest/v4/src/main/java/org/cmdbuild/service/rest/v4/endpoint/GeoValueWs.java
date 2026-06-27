package org.cmdbuild.service.rest.v4.endpoint;

import com.google.common.collect.Ordering;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.cmdbuild.data.filter.CmdbFilter;
import org.cmdbuild.gis.*;
import org.cmdbuild.service.rest.common.utils.WsSerializationUtils;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.dao.utils.CmFilterUtils.parseFilter;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import jakarta.ws.rs.*;
import org.cmdbuild.gis.Area;
import org.cmdbuild.gis.GisNavTreeNode;
import org.cmdbuild.gis.GisValue;
import org.cmdbuild.service.rest.v4.command.GeoValueWsCommand;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.serializationhelpers.GisValueSerializer.serializeGisValueList;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

@Path("{" + TYPE + ":classes|processes}/_ANY/{" + TYPE_NAME + ":cards|instances}/_ANY/geovalues/")
@Tag(name = "Geo values", description = "Operations related to geo values attached to cards")
@Produces(APPLICATION_JSON)
@Component
public class GeoValueWs {

    private final GeoValueWsCommand command;

    public GeoValueWs(GeoValueWsCommand command) {
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get geo values for a card",
            description = "Get geo values for a card",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = ATTRIBUTE, in = ParameterIn.QUERY, description = "Attribute ids", schema = @Schema(type = "Set", implementation = Long.class, example = "1,2,3")),
                    @Parameter(name = AREA, in = ParameterIn.QUERY, description = "Area to query geo values for"),
                    @Parameter(name = "attach_nav_tree", in = ParameterIn.QUERY, description = "Attach navigation tree to geo values", schema = @Schema(type = "boolean", defaultValue = FALSE)),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter to apply to the query"),
                    @Parameter(name = "forOwner", in = ParameterIn.QUERY, description = "Filter to apply to the query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo value data"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object query(
            @QueryParam(ATTRIBUTE) Set<Long> attrs,
            @QueryParam(AREA) String area,
            @QueryParam("attach_nav_tree") @DefaultValue(FALSE) Boolean attachNavTree,
            @QueryParam(FILTER) String filterStr,
            @QueryParam("forOwner") String forOwner
    ) {
        java.util.concurrent.atomic.AtomicReference<List<GisNavTreeNode>> navTreeRef = new java.util.concurrent.atomic.AtomicReference<>();
        List<GisValue> gisValueList = command.doQuery(attrs, area, filterStr, forOwner, attachNavTree, navTreeRef);

        if (attachNavTree) {
            return serializeGisValueList(gisValueList).accept((m) -> {
                FluentMap<String, Object> meta = (FluentMap<String, Object>) m.get("meta");
                List<GisNavTreeNode> navTree = navTreeRef.get();
                meta.put("nav_tree_items", navTree.stream()
                        .sorted(Ordering.natural().onResultOf(GisNavTreeNode::getClassId).thenComparing(GisNavTreeNode::getDescription))
                        .map((n) -> map(
                        "_id", n.getCardId(),
                        "type", n.getClassId(),
                        "description", n.getDescription(),
                        "parentid", n.getParentCardId(),
                        "parenttype", n.getParentClassId(),
                        "navTreeNodeId", n.getNavTreeNodeId()
                )).collect(toList()));
            });
        } else {
            return serializeGisValueList(gisValueList);
        }
    }

    @GET
    @Path("area")
    @Operation(
            summary = "Get area for geo values",
            description = "Get area for geo values",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = ATTRIBUTE, in = ParameterIn.QUERY, description = "Attribute ids", schema = @Schema(type = "Set", implementation = Long.class, example = "1,2,3")),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter to apply to the query"),
                    @Parameter(name = "forOwner", in = ParameterIn.QUERY, description = "Filter to apply to the query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo value area data"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object queryArea(
            @QueryParam(ATTRIBUTE) Set<Long> attrs,
            @QueryParam(FILTER) String filterStr,
            @QueryParam("forOwner") String forOwner
    ) {
        Area area = command.doQueryArea(attrs, filterStr, forOwner);
        if (area == null) {
            return success().with("found", false);
        } else {
            return response(map(
                    "x1", area.getX1(),
                    "y1", area.getY1(),
                    "x2", area.getX2(),
                    "y2", area.getY2()
            )).with("found", true);
        }
    }

    @GET
    @Path("center")
    @Operation(
            summary = "Get center for geo values",
            description = "Get center for geo values",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(allowableValues = {"processes", "classes"})),
                    @Parameter(name = TYPE_NAME, in = ParameterIn.PATH, description = "instances or cards", schema = @Schema(allowableValues = {"instances", "cards"})),
                    @Parameter(name = ATTRIBUTE, in = ParameterIn.QUERY, description = "Attribute ids", schema = @Schema(type = "Set", implementation = Long.class, example = "1,2,3")),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter to apply to the query"),
                    @Parameter(name = "forOwner", in = ParameterIn.QUERY, description = "Filter to apply to the query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of geo value center data"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object queryCenter(
            @QueryParam(ATTRIBUTE) Set<Long> attrs,
            @QueryParam(FILTER) String filterStr,
            @QueryParam("forOwner") String forOwner
    ) {
        Area area = command.doQueryArea(attrs, filterStr, forOwner);
        if (area == null) {
            return success().with("found", false);
        } else {
            return response(map(
                    "x", area.getCenter().getX(),
                    "y", area.getCenter().getY()
            )).with("found", true);
        }
    }
}
