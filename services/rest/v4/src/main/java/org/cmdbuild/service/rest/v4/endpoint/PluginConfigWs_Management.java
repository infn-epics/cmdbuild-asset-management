package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.cmdbuild.plugin.config.PluginConfigService;

import static com.google.common.base.Preconditions.checkNotNull;
import jakarta.ws.rs.*;
import org.cmdbuild.service.rest.v4.command.PluginConfigWsCommand;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.PLUGIN;

@Path("plugin/{" + PLUGIN + "}/config/")
@Tag(name = "Plugin configuration", description = "Operations related to plugin configuration")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class PluginConfigWs_Management {

    private final PluginConfigWsCommand command;

    public PluginConfigWs_Management(PluginConfigWsCommand command) {
        this.command = checkNotNull(command);
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all public plugin configurations",
            description = "Get all public plugin configurations",
            parameters = {
                    @Parameter(name = PLUGIN, in = ParameterIn.PATH, description = "Name of the plugin to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of public plugin configurations"),
                    @ApiResponse(responseCode = "404", description = "Plugin not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object getAllPublicPluginConfigs(
            @PathParam(PLUGIN) String pluginName
    ) {
        return response(command.doGetAllPublicPluginConfigs(pluginName));
    }

    @GET
    @Path("{key}")
    @Operation(
            summary = "Get public plugin configuration value",
            description = "Get public plugin configuration value",
            parameters = {
                    @Parameter(name = PLUGIN, in = ParameterIn.PATH, description = "Name of the plugin to query"),
                    @Parameter(name = "key", in = ParameterIn.PATH, description = "Key of the configuration to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of public plugin configuration value"),
                    @ApiResponse(responseCode = "404", description = "Plugin or configuration not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    public Object getPublicPluginConfigValue(
            @PathParam(PLUGIN) String pluginName,
            @PathParam("key") String key
    ) {
        return response(command.doGetPublicPluginConfigValue(pluginName, key));
    }
}
