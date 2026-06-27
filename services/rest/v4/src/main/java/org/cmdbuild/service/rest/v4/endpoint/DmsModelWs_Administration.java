/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;
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
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import java.util.Collection;
import java.util.List;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_DMS_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_DMS_VIEW_AUTHORITY;
import org.cmdbuild.classe.ExtendedClass;
import static org.cmdbuild.classe.access.UserClassService.ClassQueryFeatures.CQ_FOR_USER;
import static org.cmdbuild.classe.access.UserClassService.ClassQueryFeatures.CQ_INCLUDE_INACTIVE_ELEMENTS;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import org.cmdbuild.dao.entrytype.Attribute;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.dao.entrytype.EntryType;
import org.cmdbuild.service.rest.common.serializationhelpers.AttributeTypeConversionService;
import org.cmdbuild.service.rest.common.serializationhelpers.ClassSerializationHelper;
import org.cmdbuild.service.rest.common.serializationhelpers.WsAttributeData;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import org.cmdbuild.service.rest.v4.command.DmsModelWsCommand;
import static org.cmdbuild.service.rest.v4.command.WsUtils.filterSerializations;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import org.springframework.stereotype.Component;

/**
 *
 * @author ldare
 */
@Path("administration/dms/models/")
@Tags({
    @Tag(name = "DMS Models", description = "APIs to manage DMS Models."),
    @Tag(name = "Administration")
})
@Produces(APPLICATION_JSON)
@Component
public class DmsModelWs_Administration {

    private static final Boolean ONLY_ACTIVE = Boolean.FALSE;

    private final DmsModelWsCommand command;
    private final ClassSerializationHelper classSerializationHelper;
    private final AttributeTypeConversionService attributeTypeConversionService;

    public DmsModelWs_Administration(ClassSerializationHelper classSerializationHelper, AttributeTypeConversionService attributeTypeConversionService, DmsModelWsCommand command) {
        this.classSerializationHelper = checkNotNull(classSerializationHelper);
        this.attributeTypeConversionService = checkNotNull(attributeTypeConversionService);
        this.command = command;
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all classes",
            description = "Get all classes",
            parameters = {
                @Parameter(name = DETAILED, in = ParameterIn.QUERY, description = "Include or not full details in the response"),
                @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter classes by name")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful retrieval of classes"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_VIEW_AUTHORITY)
    public Object readAll(
            @QueryParam(DETAILED) @DefaultValue(FALSE) @Parameter(description = "Include or not full details in the response") Boolean detailed,
            @QueryParam(LIMIT) @Parameter(description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")) Integer limit,
            @QueryParam(START) @Parameter(description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0") Integer offset,
            @QueryParam(FILTER) String filterStr
    ) {
        List<Classe> listClasse = command.doReadAll(ONLY_ACTIVE);

        List<FluentMap<String, Object>> listDmsSerialization = classSerializationHelper.serialize(listClasse, detailed);
        listDmsSerialization = filterSerializations(listDmsSerialization, filterStr);
        return response(paged(listDmsSerialization, offset, limit));
    }

    @GET
    @Path("{" + CLASS_ID + "}/")
    @Operation(
            summary = "Get a specific class",
            description = "Get a specific class",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),},
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful retrieval of class"),
                @ApiResponse(responseCode = "404", description = "Class not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_VIEW_AUTHORITY)
    public Object read(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId
    ) {
        ExtendedClass extendedClass = command.doRead(classId, CQ_INCLUDE_INACTIVE_ELEMENTS, CQ_FOR_USER);
        return response(classSerializationHelper.buildFullDetailExtendedResponse(extendedClass));
    }

    @GET
    @Path("{" + CLASS_ID + "}/attributes/{attrId}/")
    @Operation(
            summary = "Get all data for the chosen attribute",
            description = "Obtain the details of a specific attribute",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                @Parameter(name = "attrId", in = ParameterIn.PATH, description = "Id of the attribute to query")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful operation"),
                @ApiResponse(responseCode = "404", description = "Attribute not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_VIEW_AUTHORITY)
    public Object readAttribute(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam("attrId") String attrId
    ) {
        Attribute attribute = command.doReadAttribute(classId, attrId);
        return response(attributeTypeConversionService.serializeAttributeType(attribute));
    }

    @GET
    @Path("{" + CLASS_ID + "}/attributes/")
    @Operation(
            summary = "Get all the attributes for the chosen class",
            description = "Obtain a list of every available attribute for the specified class",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful operation"),
                @ApiResponse(responseCode = "404", description = "Attribute not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_VIEW_AUTHORITY)
    public Object readAllAttributes(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @QueryParam(LIMIT) @Parameter(description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")) Integer limit,
            @QueryParam(START) @Parameter(description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0") Integer offset
    ) {
        Collection<Attribute> attributeCollection = command.doReadAllAttributes(classId, EntryType::getServiceAttributes);
        List<FluentMap<String, Object>> attributeSerializations = attributeCollection.stream().map(attributeTypeConversionService::serializeAttributeType).collect(toList());
        return response(paged(attributeSerializations, offset, limit));
    }

    @POST
    @Path(EMPTY)
    @Operation(
            summary = "Create a new class",
            description = "Create a new class",
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ClassSerializationHelper.WsClassData.class))),
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful creation of class"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object create(
            ClassSerializationHelper.WsClassData data
    ) {
        ExtendedClass extendedClass = command.doCreate(data);
        return response(classSerializationHelper.buildFullDetailExtendedResponse(extendedClass));
    }

    @PUT
    @Path("{" + CLASS_ID + "}/")
    @Operation(
            summary = "Update an existing class",
            description = "Update an existing class",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ClassSerializationHelper.WsClassData.class))),
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful update of class"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object update(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            ClassSerializationHelper.WsClassData data
    ) {
        ExtendedClass extendedClass = command.doUpdate(classId, data);
        return response(classSerializationHelper.buildFullDetailExtendedResponse(extendedClass));
    }

    @DELETE
    @Path("{" + CLASS_ID + "}/")
    @Operation(
            summary = "Delete a class",
            description = "Delete a class",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful deletion of class"),
                @ApiResponse(responseCode = "404", description = "The class was not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object delete(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId
    ) {
        command.doDelete(classId);
        return success();
    }

    @POST
    @Path("{" + CLASS_ID + "}/attributes/")
    @Operation(
            summary = "Create a new attribute",
            description = "Create a new attribute",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsAttributeData.class))),
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful creation of attribute"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object createAttribute(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            WsAttributeData data
    ) {
        Attribute attribute = command.doCreateAttribute(classId, data);
        return response(attributeTypeConversionService.serializeAttributeType(attribute));
    }

    @PUT
    @Path("{" + CLASS_ID + "}/attributes/{attrId}/")
    @Operation(
            summary = "Update an existing attribute",
            description = "Update an existing attribute",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                @Parameter(name = "attrId", in = ParameterIn.PATH, description = "Id of the attribute to query")
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = WsAttributeData.class))),
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful update of attribute"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object updateAttributes(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam("attrId") String attrId,
            WsAttributeData data
    ) {
        Attribute attribute = command.doUpdateAttribute(classId, data);
        return response(attributeTypeConversionService.serializeAttributeType(attribute));
    }

    @DELETE
    @Path("{" + CLASS_ID + "}/attributes/{attrId}/")
    @Operation(
            summary = "Delete an attribute",
            description = "Delete an attribute",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                @Parameter(name = "attrId", in = ParameterIn.PATH, description = "Id of the attribute to query")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful deletion of attribute"),
                @ApiResponse(responseCode = "404", description = "The attribute was not found"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object deleteAttributes(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam("attrId") String attrId
    ) {
        command.doDeleteAttribute(classId, attrId);
        return success();
    }

    @POST
    @Path("{" + CLASS_ID + "}/attributes/order")
    @Operation(
            summary = "Reorder attributes",
            description = "Reorder attributes",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query")
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = List.class))),
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful reordering of attributes"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_DMS_MODIFY_AUTHORITY)
    public Object reorderAttributes(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            List<String> attrOrder
    ) {
        Classe classe = command.doReorderAttributes(classId, attrOrder);
        return response(attrOrder.stream().map(classe::getAttribute).map(attributeTypeConversionService::serializeAttributeType).collect(toList()), attrOrder.size());
    }

    @GET
    @Path("{" + CLASS_ID + "}/print_schema/{file}")
    @Operation(
            summary = "Print model schema report",
            description = "Print model schema report",
            parameters = {
                @Parameter(name = CLASS_ID, in = ParameterIn.PATH, description = "Name of the class to query"),
                @Parameter(name = FILE, in = ParameterIn.PATH, description = "Name of the file to print"),
                @Parameter(name = EXTENSION, in = ParameterIn.QUERY, description = "Extension of the file to print")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Successful retrieval of model schema report"),
                @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {
                @SecurityRequirement(name = "BasicAuth", scopes = {}),
                @SecurityRequirement(name = "BearerAuth", scopes = {})}
    )
    @Produces(APPLICATION_OCTET_STREAM)
    @RolesAllowed(ADMIN_DMS_VIEW_AUTHORITY)
    public DataHandler printModelSchemaReport(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @PathParam(FILE) String fileName,
            @QueryParam(EXTENSION) String extension
    ) {
        return command.doPrintModelSchemaReport(classId, fileName, extension);
    }
}
