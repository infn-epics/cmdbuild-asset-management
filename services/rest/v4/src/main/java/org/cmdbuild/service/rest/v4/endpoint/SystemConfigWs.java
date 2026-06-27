package org.cmdbuild.service.rest.v4.endpoint;

import com.google.common.collect.Ordering;
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
import org.apache.commons.lang3.tuple.Pair;
import org.cmdbuild.config.CoreConfiguration;
import org.cmdbuild.config.api.ConfigDefinition;
import org.cmdbuild.config.api.GlobalConfigService;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.*;
import org.apache.commons.lang3.tuple.Pair;
import org.cmdbuild.config.api.ConfigDefinition;
import org.cmdbuild.config.api.GlobalConfigService;
import org.cmdbuild.service.rest.v4.command.SystemConfigWsCommand;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.*;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.*;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_SYSCONFIG_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_SYSCONFIG_VIEW_AUTHORITY;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.config.api.ConfigValue.TRUE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.DETAILED;
import static org.cmdbuild.utils.lang.CmCollectionUtils.set;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmMapUtils.toMap;

@Path("administration/system/config")
@Tag(name = "System configuration", description = "Operations related to system configuration")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RolesAllowed(ADMIN_SYSCONFIG_VIEW_AUTHORITY)
@Component
public class SystemConfigWs {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GlobalConfigService globalConfigService;
    private final SystemConfigWsCommand command;

    public SystemConfigWs(GlobalConfigService globalConfigService, SystemConfigWsCommand command) {
        this.globalConfigService = checkNotNull(globalConfigService);
        this.command = checkNotNull(command);
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get system configuration",
            description = "Get system configuration",
            parameters = {
                    @Parameter(name = DETAILED, in = ParameterIn.QUERY, description = "Detailed output", schema = @Schema(type = "boolean", defaultValue = "false"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of system configuration"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getSystemConfigAll(
            @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean detailed
    ) {
        logger.debug("get system config");
        Map<String, String> storedConfigs = globalConfigService.getStoredConfigAsMap();
        if (detailed) {
            Set<String> systemConfigs = command.doGetSystemConfigAll(storedConfigs);

            return response(systemConfigs.stream().map((k) -> {
                ConfigDefinition configDefinition = globalConfigService.getConfigDefinitionOrNull(k);
                boolean hasValue = storedConfigs.containsKey(k);
                FluentMap map = map("hasDefinition", configDefinition != null, "hasValue", hasValue);
                if (configDefinition != null) {
                    map.put("description", configDefinition.getDescription(),
                            "default", configDefinition.getDefaultValue(),
                            "oneof", configDefinition.getEnumValues(),
                            "category", serializeEnum(configDefinition.getCategory()),
                            "location", serializeEnum(configDefinition.getLocation()),
                            "modular", serializeEnum(configDefinition.getModular()),
                            "module", configDefinition.getModuleNamespace());
                }
                if (hasValue) {
                    map.put("value", storedConfigs.get(k));
                }
                return Pair.of(k, map);
            }).collect(toMap(Pair::getLeft, Pair::getRight)));
        } else {
            return response(map(storedConfigs));
        }
    }

    @GET
    @Path("/{key}")
    @Operation(
            summary = "Get system configuration value",
            description = "Get system configuration value",
            parameters = {
                    @Parameter(name = "key", in = ParameterIn.PATH, description = "Key of system configuration value", required = true),
                    @Parameter(name = "include_default", in = ParameterIn.QUERY, description = "Include default value", schema = @Schema(type = "boolean", defaultValue = "true"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of system configuration value"),
                    @ApiResponse(responseCode = "404", description = "System configuration value not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getSystemConfigValue(
            @PathParam("key") String key,
            @QueryParam("include_default") @DefaultValue(TRUE) Boolean includeDefault
    ) {
        String value = command.doGetSystemConfigValue(key, includeDefault);
        return response(value);
    }

    @PUT
    @Path("/{key}")
    @Operation(
            summary = "Update system configuration value",
            description = "Update system configuration value",
            parameters = {
                    @Parameter(name = "key", in = ParameterIn.PATH, description = "Key of system configuration value", required = true),
                    @Parameter(name = "value", in = ParameterIn.QUERY, description = "Value of system configuration value", required = true),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = String.class)), required = true, description = "Value of system configuration value"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of system configuration value"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @Consumes(TEXT_PLAIN)
    @RolesAllowed(ADMIN_SYSCONFIG_MODIFY_AUTHORITY)
    public Object updateSystemConfigValue(
            @PathParam("key") String key,
            @Nullable String value,
            @QueryParam("encrypt") Boolean encrypt
    ) {
        command.doUpdateSystemConfigValue(key, value, encrypt);
        return success();
    }

    @PUT
    @Path("/_MANY")
    @Operation(
            summary = "Update multiple system configuration values",
            description = "Update multiple system configuration values",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = Map.class)), required = true, description = "Map of key-value pairs representing system configuration values to update"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of multiple system configuration values"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_SYSCONFIG_MODIFY_AUTHORITY)
    public Object updateSystemConfigValues(Map<String, String> data) {
        command.doUpdateSystemConfigValues(data);
        return success();
    }

    @DELETE
    @Path("/{key}")
    @Operation(
            summary = "Delete system configuration value",
            description = "Delete system configuration value",
            parameters = {
                    @Parameter(name = "key", in = ParameterIn.PATH, description = "Key of system configuration value", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of system configuration value"),
                    @ApiResponse(responseCode = "404", description = "System configuration value not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_SYSCONFIG_MODIFY_AUTHORITY)
    public Object deleteSystemConfigValue(
            @PathParam("key") String key
    ) {
        command.doDeleteSystemConfigValue(key);
        return success();
    }

    @POST
    @Path("/reload")
    @Operation(
            summary = "Reload system configuration",
            description = "Reload system configuration",
            security = {@SecurityRequirement(name = "BasicAuth", scopes = {}), @SecurityRequirement(name = "BearerAuth", scopes = {})},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful reload of system configuration"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @Consumes(WILDCARD)
    @RolesAllowed(ADMIN_SYSCONFIG_MODIFY_AUTHORITY)
    public Object reloadConfig() {
        command.doReloadConfig();
        return success();
    }
}
