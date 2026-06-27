/*
 * CMDBuild has been developed and is managed by PAT srl
 You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.api.SchemaCollectorApi;
import org.cmdbuild.api.SystemApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.ws.rs.*;
import org.cmdbuild.service.rest.v4.command.SchemaCollectorWsCommand;
import org.springframework.stereotype.Component;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_ALL_AUTHORITY;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotNull;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_ALL_AUTHORITY;

/**
 *
 * @author afelice
 */
@Path("administration/schema/collect")
@Tag(name = "Schema Collector", description = "Schema Collector")
@Consumes(TEXT_PLAIN)
@Produces(TEXT_PLAIN)
@RolesAllowed(ADMIN_ALL_AUTHORITY)
@Component
public class SchemaCollectorWs {

    /**
     * A <i>dummy implementation</i> (that throws
     * <code>UnsupportedOperationException</code> on all methods) if
     * schemaCollector module not available.
     */
    private final SchemaCollectorWsCommand command;

    public SchemaCollectorWs(SchemaCollectorWsCommand command) {
        this.command = checkNotNull(command);
    }

    @GET
    @Path("test")
    @Operation(
            summary = "Test Schema Collector",
            description = "Test Schema Collector",
            parameters = {
                    @Parameter(name = "msg", in = ParameterIn.QUERY, description = "Message to test")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of test data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object test(
            @QueryParam("msg") String msg
    ) {
        return command.doTest(msg);
    }

    @PUT
    @Path("collectSchema")
    @Operation(
            summary = "Collect Schema",
            description = "Collect Schema",
            parameters = {
                    @Parameter(name = "name", in = ParameterIn.QUERY, description = "System mnemonic name"),
                    @Parameter(name = "id", in = ParameterIn.QUERY, description = "System id")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of schema data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object collectSchema(@QueryParam("name") String curSystemMnemonicName, @QueryParam("id") String curSystemId) {
        return command.doCollectSchema(curSystemMnemonicName, curSystemId);
    }

    @PUT
    @Path("compareSchema")
    @Operation(
            summary = "Compare Schema",
            description = "Compare Schema",
            parameters = {
                    @Parameter(name = "other", in = ParameterIn.QUERY, description = "Other schema"),
                    @Parameter(name = "name", in = ParameterIn.QUERY, description = "System mnemonic name")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of schema comparison data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object compareSchema(
            @QueryParam("other") String otherSchemaSerialization,
            @QueryParam("name") String curSystemMnemonicName
    ) {
        return command.doCompareSchema(otherSchemaSerialization, curSystemMnemonicName);
    }

    @PUT
    @Path("compareSchemaBetween")
    @Operation(
            summary = "Compare Schema Between",
            description = "Compare Schema Between",
            parameters = {
                    @Parameter(name = "new", in = ParameterIn.QUERY, description = "New schema"),
                    @Parameter(name = "a", in = ParameterIn.QUERY, description = "Old schema")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of schema comparison data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object compareSchemaBetween(
            @QueryParam("new") String newSchemaSerialization,
            @QueryParam("a") String aSchemaSerialization
    ) {
        return command.doCompareSchemaBetween(newSchemaSerialization, aSchemaSerialization);
    }

    @PUT
    @Path("applySchemaDiff")
    @Operation(
            summary = "Apply Schema Diff",
            description = "Apply Schema Diff",
            parameters = {
                    @Parameter(name = "diff", in = ParameterIn.QUERY, description = "Schema diff")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful application of schema diff data"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object applySchemaDiff(
            @QueryParam("diff") String diffSchemaSerialization
    ) {
        return command.doApplySchemaDiff(diffSchemaSerialization);
    }
}
