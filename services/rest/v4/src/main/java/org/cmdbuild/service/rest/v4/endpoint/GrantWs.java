package org.cmdbuild.service.rest.v4.endpoint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.auth.grant.*;
import org.cmdbuild.auth.role.Role;
import org.cmdbuild.auth.role.RoleRepository;
import org.cmdbuild.dao.utils.AttributeFilterProcessor;
import org.cmdbuild.dao.utils.CmFilterUtils;
import org.cmdbuild.data.filter.CmdbFilter;
import org.cmdbuild.data.filter.FilterType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import jakarta.ws.rs.*;
import org.cmdbuild.auth.grant.*;
import org.cmdbuild.auth.role.Role;
import org.cmdbuild.auth.role.RoleRepository;
import org.cmdbuild.service.rest.v4.command.GrantWsCommand;
import org.cmdbuild.service.rest.v4.model.WsGrantData;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.util.stream.Collectors.toList;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.cmdbuild.auth.grant.GrantConstants.*;
import static org.cmdbuild.auth.grant.GrantData.*;
import static org.cmdbuild.auth.grant.GrantMode.GM_NONE;
import static org.cmdbuild.auth.grant.PrivilegedObjectType.POT_PROCESS;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_ROLES_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_ROLES_VIEW_AUTHORITY;
import static org.cmdbuild.auth.role.RoleType.ADMIN;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.services.serialization.GrantSerializer.serializeGrant;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.json.CmJsonUtils.*;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.parseEnum;
import static org.cmdbuild.utils.lang.CmConvertUtils.*;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;

@Path("administration/roles/{roleId}/grants/")
@Tag(name = "Grants", description = "Grants management")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RolesAllowed(ADMIN_ROLES_VIEW_AUTHORITY)
@Component
public class GrantWs {

    private final GrantDataRepository repository;
    private final GrantService grantService;
    private final RoleRepository roleRepository;
    private final GrantWsCommand command;

    public GrantWs(GrantDataRepository repository, GrantService grantService, RoleRepository roleRepository, GrantWsCommand command) {
        this.repository = checkNotNull(repository);
        this.grantService = checkNotNull(grantService);
        this.roleRepository = checkNotNull(roleRepository);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all grants for role",
            description = "Returns all grants for role. If role is `_ALL`, returns all groups with grants.",
            parameters = {
                    @Parameter(name = ROLE_ID, description = "Role ID or name", required = true, in = ParameterIn.PATH),
                    @Parameter(name = FILTER, description = "Filter to apply to the resultset", in = ParameterIn.QUERY),
                    @Parameter(name = LIMIT, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = "includeObjectDescription", description = "Include object description in the response", in = ParameterIn.QUERY, schema = @Schema(defaultValue = FALSE)),
                    @Parameter(name = "includeRecordsWithoutGrant", description = "Include records without grant in the response", in = ParameterIn.QUERY, schema = @Schema(defaultValue = FALSE))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of grants list"),
                    @ApiResponse(responseCode = "404", description = "No grants found for the role"),
                    @ApiResponse(responseCode = "400", description = "Invalid filter"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "409", description = "Conflict"),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readMany(
            @PathParam(ROLE_ID) String roleId,
            @QueryParam(FILTER) String filterStr,
            @QueryParam(LIMIT) Long limit,
            @QueryParam(START) Long offset,
            @QueryParam("includeObjectDescription") @DefaultValue(FALSE) Boolean includeObjectDescription,
            @QueryParam("includeRecordsWithoutGrant") @DefaultValue(FALSE) Boolean includeRecordsWithoutGrant
    ) {
        List<GrantData> grants = command.doReadMany(roleId, filterStr, includeRecordsWithoutGrant);
        return response(paged(grants, offset, limit).map((g) -> serializeGrant(g, includeObjectDescription, grantService)));
    }

    @GET
    @Path("/by-target/{objectType}/{objectTypeName}")
    @Operation(
            summary = "Get grant for role by target object",
            description = "Returns grant for role by target object. If role is `_ALL`, returns all groups with grants for the target object.",
            parameters = {
                @Parameter(name = ROLE_ID, description = "Role ID or name", required = true, in = ParameterIn.PATH),
                @Parameter(name = "objectType", description = "Type of the privileged object", required = true, in = ParameterIn.PATH),
                @Parameter(name = "objectTypeName", description = "Name/ID of the privileged object", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of grant data"),
                    @ApiResponse(responseCode = "404", description = "No grant found for the role and object"),
                    @ApiResponse(responseCode = "400", description = "Invalid object type or name"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "409", description = "Conflict"),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    // The type of value returned by this endpoint has slightly changed its structure in v4, now the content of the field "data" of the map
    // returned is always a list
    public Object readOneByObject(
            @PathParam(ROLE_ID) String roleId,
            @PathParam("objectType") String objectTypeStr,
            @PathParam("objectTypeName") String objectTypeName
    ) {
//        List<GrantData> grantDataList = command.doReadOneByObject(roleId, objectTypeStr, objectTypeName);
//        return response(list(grantDataList).map(g -> serializeGrant(g, grantService)));
        if (equal(roleId, "_ALL")) {
            return response(list(roleRepository.getAllGroups()).filter(g -> !equal(g.getType(), ADMIN)).map(g -> {
                Role role = roleRepository.getByNameOrId(g.getName());
                PrivilegedObjectType objectType = parseEnum(objectTypeStr, PrivilegedObjectType.class);
                GrantData grant = grantService.getGrantDataByRoleAndTypeAndName(role.getId(), objectType, objectTypeName);
                return serializeGrant(grant, grantService);
            }));
        } else {
            Role role = roleRepository.getByNameOrId(roleId);
            PrivilegedObjectType objectType = parseEnum(objectTypeStr, PrivilegedObjectType.class);
            GrantData grant = grantService.getGrantDataByRoleAndTypeAndName(role.getId(), objectType, objectTypeName);
            return response(serializeGrant(grant, grantService));
        }
    }

    @POST
    @Path("_ANY")
    @Operation(
            summary = "Update grants for role",
            description = "Updates grants for role. If role is `_ALL`, updates grants for multiple roles.",
            parameters = {
                @Parameter(name = ROLE_ID, description = "Role ID or name", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of grants"),
                    @ApiResponse(responseCode = "400", description = "Invalid request body"),
                    @ApiResponse(responseCode = "404", description = "Role not found"),
                    @ApiResponse(responseCode = "403", description = "Access denied"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "409", description = "Conflict"),
                    @ApiResponse(responseCode = "422", description = "Unprocessable Entity"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_ROLES_MODIFY_AUTHORITY)
    public Object update(
            @PathParam(ROLE_ID) String roleId,
            List<WsGrantData> data
    ) {
        if (equal(roleId, "_ALL")) {
            return response(list(updateMultiRoleGrants(data)).map(m -> serializeGrant(m, grantService)));
        } else {
            Role role = roleRepository.getByNameOrId(roleId);
            return response(list(updateRoleGrants(role, data)).map(m -> serializeGrant(m, grantService)));
        }
    }

    private List<GrantData> updateRoleGrants(Role role, List<WsGrantData> data) {
        return repository.updateGrantsForRole(role.getId(), data.stream().map(d -> GrantDataImpl.builder()
                .withAttributePrivileges(d.getAttributePrivileges())
                .withDmsPrivileges(d.getDmsPrivileges())
                .withGisPrivileges(d.getGisPrivileges())
                .withCustomPrivileges(d.getCustomPrivileges())
                .withMode(d.getMode())
                .withObjectIdOrClassName(d.getClassNameOrObjectId())
                .withPrivilegeFilter(d.getFilter())
                .withType(d.getType())
                .withRoleId(role.getId())
                .build()).collect(toList()));
    }

    private List<GrantData> updateMultiRoleGrants(List<WsGrantData> data) {
        checkArgument(data.size() == data.stream().map(d -> d.getRole()).distinct().toList().size(), "list of grants has duplicate of same group");
        List<GrantData> grants = list();
        list(data).forEach(d -> {
            grants.addAll(updateRoleGrants(roleRepository.getByNameOrId(checkNotBlank(d.getRole())), list(d)));
        });
        return grants;
    }
}
