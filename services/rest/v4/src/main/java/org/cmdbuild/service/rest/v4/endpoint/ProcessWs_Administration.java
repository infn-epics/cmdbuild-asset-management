/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.activation.DataHandler;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.classe.ExtendedClass;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.service.rest.common.serializationhelpers.ClassSerializationHelper;
import org.cmdbuild.service.rest.common.serializationhelpers.ProcessWsSerializationHelper;
import org.cmdbuild.service.rest.v4.command.ProcessWsCommand;
import org.cmdbuild.workflow.WorkflowService;
import org.cmdbuild.workflow.model.Process;
import org.cmdbuild.workflow.model.XpdlInfo;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_PROCESSES_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_PROCESSES_VIEW_AUTHORITY;
import static org.cmdbuild.classe.access.UserClassService.ClassQueryFeatures.CQ_FOR_USER;
import static org.cmdbuild.classe.access.UserClassService.ClassQueryFeatures.CQ_INCLUDE_INACTIVE_ELEMENTS;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.serializationhelpers.ProcessWsSerializationHelper.serializeXpdlInfo;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;

/**
 *
 * @author schursin
 */
@Path("administration/processes/")
@Tags({
        @Tag( name = "Processes", description = "APIs to manage processes."),
        @Tag( name = "Administration" )
})
@Produces(APPLICATION_JSON)
@Component
public class ProcessWs_Administration {

    private final WorkflowService workflowService;//TODO replace with user wf service
    private final UserClassService userClassService;
    private final ClassSerializationHelper classSerializationHelper;
    private final ProcessWsSerializationHelper processWsSerializationHelper;
    private final ProcessWsCommand command;

    public ProcessWs_Administration(WorkflowService workflowService, UserClassService userClassService, ClassSerializationHelper classSerializationHelper, ProcessWsSerializationHelper processWsSerializationHelper, ProcessWsCommand command) {
        this.workflowService = checkNotNull(workflowService);
        this.userClassService = checkNotNull(userClassService);
        this.classSerializationHelper = checkNotNull(classSerializationHelper);
        this.processWsSerializationHelper = checkNotNull(processWsSerializationHelper);
        this.command = checkNotNull(command);
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all processes",
            description = "Get all processes",
            parameters = {
                    @Parameter( name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter( name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter( name = DETAILED, in = ParameterIn.QUERY, description = "Include or not full details in the response", schema = @Schema(type = "boolean"))
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful operation"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_PROCESSES_VIEW_AUTHORITY)
    public Object readAll(
            @QueryParam(LIMIT) Long limit,
            @QueryParam(START) Long offset,
            @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean detailed
    ) {
        PagedElements<Process> processes = command.doReadAll(workflowService::getAllProcessClasses, limit, offset);
        return response(processes.map(detailed ?
                p -> classSerializationHelper.buildFullDetailExtendedResponse(userClassService.getExtendedClass(p.getName(), CQ_INCLUDE_INACTIVE_ELEMENTS, CQ_FOR_USER)).accept(processWsSerializationHelper.processSpecificDataMapConsumer(p, true)) : processWsSerializationHelper::minimalResponse));
    }

    @GET
    @Path("{processId}/")
    @Operation(
            summary = "Get a specific process",
            description = "Get a specific process",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful retrieval of process data"),
                    @ApiResponse( responseCode = "404", description = "The process was not found"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_PROCESSES_VIEW_AUTHORITY)
    public Object read(
            @PathParam(PROCESS_ID) String processId
    ) {
        Process process = command.doRead(processId);
        return response(classSerializationHelper.buildFullDetailExtendedResponse(userClassService.getExtendedClass(process.getName(), CQ_INCLUDE_INACTIVE_ELEMENTS, CQ_FOR_USER)).accept(processWsSerializationHelper.processSpecificDataMapConsumer(process, true)));
    }

    @POST
    @Path("{processId}/versions")
    @Operation(
            summary = "Upload a new version of a process",
            description = "Upload a new version of a process",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter( name = "replace", in = ParameterIn.QUERY, description = "Replace existing version", schema = @Schema(type = "boolean"))
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = DataHandler.class)), required = true, description = "The XPDL file to upload"),
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful upload of process version"),
                    @ApiResponse( responseCode = "400", description = "Bad request"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Consumes(MULTIPART_FORM_DATA)
    public Object uploadNewXpdlVersion(
            @PathParam(PROCESS_ID) String processId,
            @Multipart(FILE) DataHandler dataHandler,
            @QueryParam("replace") @DefaultValue(FALSE) Boolean replace
    ) {
        XpdlInfo xpdlInfo = command.doUploadNewXpdlVersion(processId, dataHandler, replace);
        return response(serializeXpdlInfo(xpdlInfo));
    }

    @GET
    @Path("{processId}/versions")
    @Operation(
            summary = "Get all versions of a process",
            description = "Get all versions of a process",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful retrieval of process versions"),
                    @ApiResponse( responseCode = "404", description = "The process was not found"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    public Object getAllXpdlVersions(
            @PathParam(PROCESS_ID) String processId
    ) {
        List<XpdlInfo> xpdlInfoList = command.doGetAllXpdlVersions(processId);
        return response(xpdlInfoList.stream().map(ProcessWsSerializationHelper::serializeXpdlInfo).collect(toList()));
    }

    @GET
    @Path("{processId}/versions/{planId}/file")
    @Operation(
            summary = "Get the XPDL file of a specific process version",
            description = "Get the XPDL file of a specific process version",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                    @Parameter( name = "planId", in = ParameterIn.PATH, description = "Id of the process version to query")
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful retrieval of XPDL file"),
                    @ApiResponse( responseCode = "404", description = "The process version was not found"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    public DataHandler getXpdlVersionFile(
            @PathParam(PROCESS_ID) String processId,
            @PathParam("planId") String planId
    ) {
        return command.doGetXpdlVersionFile(processId, planId);
    }

    @GET
    @Path("{processId}/template")
    @Operation(
            summary = "Get the XPDL template file of a process",
            description = "Get the XPDL template file of a process",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful retrieval of XPDL template file"),
                    @ApiResponse( responseCode = "404", description = "The process was not found"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    public DataHandler getXpdlTemplateFile(
            @PathParam(PROCESS_ID) String processId
    ) {
        return command.doGetXpdlTemplateFile(processId);
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create a new process",
            description = "Create a new process",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ClassSerializationHelper.WsClassData.class)), required = true, description = "The process data to create"),
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful creation of process"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_PROCESSES_MODIFY_AUTHORITY)
    public Object create(
            ClassSerializationHelper.WsClassData data
    ) {
        ExtendedClass classe = command.doCreate(data);
        return read(classe.getClasse().getName());
    }

    @PUT
    @Path("{processId}/")
    @Operation(
            summary = "Update an existing process",
            description = "Update an existing process",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ClassSerializationHelper.WsClassData.class)), required = true, description = "The process data to update"),
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful update of process"),
                    @ApiResponse( responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_PROCESSES_MODIFY_AUTHORITY)
    public Object update(
            @PathParam(PROCESS_ID) String classId,
            ClassSerializationHelper.WsClassData data
    ) {
        ExtendedClass classe = command.doUpdate(classId, data);
        return read(classe.getClasse().getName());
    }

    @DELETE
    @Path("{processId}/")
    @Operation(
            summary = "Delete a process",
            description = "Delete a specific process",
            parameters = {
                    @Parameter( name = PROCESS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
            },
            responses = {
                    @ApiResponse( responseCode = "200", description = "Successful deletion of process"),
                    @ApiResponse(responseCode = "404", description = "The process was not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_PROCESSES_MODIFY_AUTHORITY)
    public Object delete(
            @PathParam(PROCESS_ID) String classId
    ) {
        command.doDelete(classId);
        return success();
    }
}
