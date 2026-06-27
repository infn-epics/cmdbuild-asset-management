/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
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
import jakarta.activation.DataHandler;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.v4.command.ClassPrintWsCommand;
import org.springframework.stereotype.Component;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_CLASSES_VIEW_AUTHORITY;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.CLASS_ID;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.EXTENSION;
import static org.cmdbuild.report.utils.ReportExtUtils.reportExtFromString;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.json.CmJsonUtils.LIST_OF_STRINGS;
import static org.cmdbuild.utils.json.CmJsonUtils.fromJson;
import static org.cmdbuild.utils.lang.CmPreconditions.firstNotBlank;

/**
 * @author ldare
 */
@Path("administration/{" + TYPE + ":classes|processes}/")
@Tag(name = "Class print", description = "Operations related to printing of classes")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class ClassPrintWs_Administration {

    private final ClassPrintWsCommand command;

    public ClassPrintWs_Administration(ClassPrintWsCommand command) {
        this.command = command;
    }

    @GET
    @Path("{" + CLASS_ID + "}/print/{file}")
    @Operation(
            summary = "Print a report for a class",
            description = "Print a report for a class",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = EXTENSION, in = ParameterIn.QUERY, description = "Extension of the report to generate"),
                    @Parameter(name = ATTRIBUTES, in = ParameterIn.QUERY, description = "List of attributes to include in the report"),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsQueryOptions.class)), required = false, description = "Query options"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful generation of report"),
                    @ApiResponse(responseCode = "400", description = "Invalid query parameters"),
                    @ApiResponse(responseCode = "404", description = "Class not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    @RolesAllowed(ADMIN_CLASSES_VIEW_AUTHORITY)
    public DataHandler printClassReport(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            WsQueryOptions wsQueryOptions,
            @QueryParam(EXTENSION) String extension,
            @QueryParam(ATTRIBUTES) String attributes
    ) {
        return command.doPrintClassReport(classId, wsQueryOptions, extension, attributes);
    }

    @GET
    @Path("{" + CLASS_ID + "}/print_schema/{" + FILE + "}")
    @Operation(
            summary = "Print class schema report",
            description = "Print class schema report",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter(name = FILE, in = ParameterIn.PATH, description = "Name of the file to generate"),
                    @Parameter(name = EXTENSION, in = ParameterIn.QUERY, description = "Extension of the report to generate")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful generation of report"),
                    @ApiResponse(responseCode = "404", description = "Class not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    @RolesAllowed(ADMIN_CLASSES_VIEW_AUTHORITY)
    public DataHandler printClassSchemaReport(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(FILE) String fileName,
            @QueryParam(EXTENSION) String extension
    ) {
        return command.doPrintClassSchemaReport(classId, fileName, extension);
    }

    @GET
    @Path("print_schema/{" + FILE + "}")
    @Operation(
            summary = "Print system schema report",
            description = "Print system schema report",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, description = "processes or classes", schema = @Schema(type = "string", allowableValues = {"processes", "classes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.QUERY, description = "Name of the class to query"),
                    @Parameter(name = FILE, in = ParameterIn.PATH, description = "Name of the file to generate"),
                    @Parameter(name = EXTENSION, in = ParameterIn.QUERY, description = "Extension of the report to generate")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Successful generation of report")},
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    @RolesAllowed(ADMIN_CLASSES_VIEW_AUTHORITY)
    public DataHandler printSchemaReport(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(FILE) String fileName,
            @QueryParam(EXTENSION) String extension
    ) {
        return command.doPrintSchemaReport(classId, fileName, extension);

    }
}
