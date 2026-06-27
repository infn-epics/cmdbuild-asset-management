package org.cmdbuild.service.rest.v4.endpoint;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.activation.DataHandler;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.etl.config.WaterwayDescriptorService;
import org.cmdbuild.etl.config.WaterwayItem;
import org.cmdbuild.etl.config.inner.WaterwayDescriptorRecord;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.v4.command.EtlConfigWsCommand;
import org.cmdbuild.service.rest.v4.model.WsConfigMeta;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.etl.config.*;
import org.cmdbuild.etl.config.inner.WaterwayDescriptorRecord;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_ETL_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_ETL_VIEW_AUTHORITY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.dao.utils.CmFilterProcessingUtils.mapFilter;
import static org.cmdbuild.etl.config.utils.WaterwayDescriptorUtils.buildDescriptorDataAndParams;
import static org.cmdbuild.etl.config.utils.WaterwayDescriptorUtils.descriptorDataJsonToYaml;
import static org.cmdbuild.service.rest.common.serializationhelpers.WaterwaySerializer.*;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.CODE;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.FILE;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmConvertUtils.toListOfStrings;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

@Path("administration/etl/configs/")
@Tag(name = "ETL Configuration", description = "ETL Configuration")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@RolesAllowed(ADMIN_ETL_VIEW_AUTHORITY)
@Component
public class EtlConfigWs {

    private final WaterwayDescriptorService waterwayDescriptorService;
    private final EtlConfigWsCommand command;

    public EtlConfigWs(WaterwayDescriptorService waterwayDescriptorService, EtlConfigWsCommand command) {
        this.waterwayDescriptorService = checkNotNull(waterwayDescriptorService);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all ETL configuration files",
            description = "Get all ETL configuration files",
            parameters = {
                    @Parameter(name = "includeMeta", in = ParameterIn.QUERY, description = "Include meta data in the response", schema = @io.swagger.v3.oas.annotations.media.Schema(type = "boolean", defaultValue = "false"))
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsQueryOptions.class)), description = "Query options", required = false),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of ETL configuration files data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readAll(
            WsQueryOptions wsQueryOptions,
            @QueryParam("includeMeta") @DefaultValue(FALSE) Boolean includeMeta
    ) {
        List<WaterwayDescriptorRecord> waterwayDescriptorRecordList = command.doReadAll();
        return response(paged(list(waterwayDescriptorRecordList).map(e -> wsQueryOptions.isDetailed() ? serializeDetailedConfigFile(e, includeMeta, waterwayDescriptorService) : serializeBasicConfigFile(e)).withOnly(mapFilter(wsQueryOptions.getQuery().getFilter())), wsQueryOptions.getQuery()));
    }

    @GET
    @Path("{code}/")
    @Operation(
            summary = "Get ETL configuration file by code",
            description = "Get ETL configuration file by code",
            parameters = {
                    @Parameter(name = CODE, in = ParameterIn.PATH, description = "ETL configuration file code", required = true, schema = @Schema(type = "string")),
                    @Parameter(name = "if_exists", in = ParameterIn.QUERY, description = "Check if the configuration file exists", schema = @Schema(type = "boolean", defaultValue = "false")),
                    @Parameter(name = "includeMeta", in = ParameterIn.QUERY, description = "Include meta data in the response", schema = @Schema(type = "boolean", defaultValue = "false")),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of ETL configuration file data"),
                    @ApiResponse(responseCode = "404", description = "ETL configuration file not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object read(
            @PathParam(CODE) String code,
            @QueryParam("includeMeta") @DefaultValue(FALSE) Boolean includeMeta,
            @QueryParam("if_exists") @DefaultValue(FALSE) Boolean checkIfExists
    ) {
        WaterwayDescriptorRecord configFile = command.doRead(code, checkIfExists);
        if (checkIfExists) {
            return response(configFile == null ? map("exists", false) : map(serializeDetailedConfigFile(configFile, includeMeta, waterwayDescriptorService)).with("exists", true));
        } else {
            return response(serializeDetailedConfigFile(configFile, includeMeta, waterwayDescriptorService));
        }
    }

    @GET
    @Path("{code}/items")
    @Operation(
            summary = "Get ETL configuration items by configuration code",
            description = "Get ETL configuration items by configuration code",
            parameters = {@Parameter(name = CODE, in = ParameterIn.PATH, description = "ETL configuration file code", required = true, schema = @Schema(type = "string"))},
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsQueryOptions.class)), description = "Query options", required = false),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of ETL configuration items data"),
                    @ApiResponse(responseCode = "404", description = "ETL configuration file not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object readItems(
            WsQueryOptions wsQueryOptions,
            @PathParam(CODE) String code
    ) {
        List<WaterwayItem> waterwayItemList = command.doReadItems(code);
        return response(paged(list(waterwayItemList).map(e -> serializeItem(e)).withOnly(mapFilter(wsQueryOptions.getQuery().getFilter())), wsQueryOptions.getQuery()));
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create ETL configuration file",
            description = "Create ETL configuration file",
            parameters = {
                    @Parameter(name = FILE, in = ParameterIn.QUERY, description = "ETL configuration file data"),
                    @Parameter(name = "overwriteIfExists", in = ParameterIn.QUERY, description = "Overwrite existing ETL configuration file", schema = @Schema(type = "boolean", defaultValue = "false"))
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsConfigMeta.class)), description = "ETL configuration file meta data", required = false),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful creation of ETL configuration file data"),
                    @ApiResponse(responseCode = "409", description = "ETL configuration file already exists"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_ETL_MODIFY_AUTHORITY)
    public Object create(
            @Multipart(value = FILE, required = false) DataHandler dataHandler,
            @Nullable WsConfigMeta meta,
            @QueryParam("overwriteIfExists") @DefaultValue(FALSE) Boolean overwriteIfExists
    ) {
        WaterwayDescriptorRecord waterwayDescriptorRecord = command.doCreate(dataHandler, meta, overwriteIfExists);
        return response(serializeDetailedConfigFile(waterwayDescriptorRecord, false, waterwayDescriptorService));
    }

    @PUT
    @Path("{code}/")
    @Operation(
            summary = "Update ETL configuration file",
            description = "Update ETL configuration file",
            parameters = {
                    @Parameter(name = CODE, in = ParameterIn.PATH, description = "ETL configuration file code", required = true, schema = @Schema(type = "string"))
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsConfigMeta.class)), description = "ETL configuration file meta data", required = false),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful update of ETL configuration file data"),
                    @ApiResponse(responseCode = "404", description = "ETL configuration file not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_ETL_MODIFY_AUTHORITY)
    public Object update(
            @PathParam(CODE) String code,
            @Multipart(value = FILE, required = false) DataHandler dataHandler,
            @Nullable WsConfigMeta meta) {
        WaterwayDescriptorRecord waterwayDescriptorRecord = command.doUpdate(code, dataHandler, meta);
        if (waterwayDescriptorRecord == null) {
            return read(code, false, false);
        } else {
            return response(serializeDetailedConfigFile(waterwayDescriptorRecord, false, waterwayDescriptorService));
        }
    }

    @DELETE
    @Path("{code}/")
    @Operation(
            summary = "Delete ETL configuration file",
            description = "Delete ETL configuration file",
            parameters = {
                    @Parameter(name = CODE, in = ParameterIn.PATH, description = "ETL configuration file code", required = true, schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful deletion of ETL configuration file data"),
                    @ApiResponse(responseCode = "404", description = "ETL configuration file not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_ETL_MODIFY_AUTHORITY)
    public Object delete(
            @PathParam(CODE) String code
    ) {
        command.doDelete(code);
        return success();
    }
}
