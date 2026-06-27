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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.dao.entrytype.Attribute;
import org.cmdbuild.service.rest.common.serializationhelpers.AttributeTypeConversionService;
import org.cmdbuild.service.rest.v4.command.ClassAttributeWsCommand;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;

/**
 *
 * @author schursin
 */
@Path("{" + TYPE + ":classes|processes}/{" + CLASS_ID + "}/attributes/")
@Tag(
        name = "Class attributes"
)
@Produces(APPLICATION_JSON)
@Component
public class ClassAttributeWs_Management {

    private final UserClassService userClassService;
    private final AttributeTypeConversionService attributeTypeConversionService;
    private final ClassAttributeWsCommand command;

    public ClassAttributeWs_Management(UserClassService userClassService, AttributeTypeConversionService attributeTypeConversionService, ClassAttributeWsCommand command) {
        this.userClassService = checkNotNull(userClassService);
        this.attributeTypeConversionService = checkNotNull(attributeTypeConversionService);
        this.command = command;
    }

    @GET
    @Path("{attrId}/")
    @Operation(
            summary = "Get all data for the chosen attribute",
            description = "Obtain the details of a specific attribute",
            parameters = {
                    @Parameter( name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, description = "Name of the class to query"),
                    @Parameter(name = "attrId", in = ParameterIn.PATH, required = true, description = "Name of the attribute to query")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(ref = "APIAttributesResponse"))),
                    @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(ref = "Default401Example"))),
                    @ApiResponse(responseCode = "404", description = "Attribute not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(ref = "DefaultError500Example")))
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object read(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query")  String classId,
            @PathParam("attrId") @Parameter(description = "Name of the attribute to query") String attrId
    ) {
        Attribute attribute = command.doRead(classId, attrId);
        return response(attributeTypeConversionService.serializeAttributeType(attribute));
    }

    @GET
    @Path(EMPTY)
    @Operation(
            summary = "Get all the attributes for the chosen class",
            description = "Obtain a list of every available attribute for the specified class",
            parameters = {
                    @Parameter(name = TYPE, in = ParameterIn.PATH, required = true, schema = @Schema(type = "string", allowableValues = {"classes", "processes"})),
                    @Parameter(name = CLASS_ID, in = ParameterIn.PATH, required = true, description = "Name of the class to query"),
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of attributes to return", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(ref = "APIAttributesResponse"))),
                    @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(ref = "Default401Example"))),
                    @ApiResponse(responseCode = "404", description = "Attribute not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = APPLICATION_JSON, schema = @Schema(ref = "DefaultError500Example")))
            },
            security = { @SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {} )}
    )
    public Object readAll(
            @PathParam(CLASS_ID) @Parameter(description = "Name of the class to query") String classId,
            @QueryParam(LIMIT) @Parameter(description = "Number of attributes to return", schema = @Schema(ref = "DefaultLimitExample")) Integer limit,
            @QueryParam(START) @Parameter(description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0") Integer offset
    ) {
        List<Attribute> listAttribute = command.doReadAll(classId, userClassService::getActiveUserAttributes);
        return response(attributeTypeConversionService.serializeAndSort(listAttribute, limit, offset, false));
    }
}
