/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.endpoint;

import com.google.common.base.Strings;
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
import jakarta.activation.DataHandler;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.preload.PreloadService;
import org.cmdbuild.service.rest.v4.command.TranslationsWsCommand;
import org.cmdbuild.translation.ExportRecord;
import org.cmdbuild.translation.dao.Translation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_LOCALIZATION_MODIFY_AUTHORITY;
import static org.cmdbuild.auth.role.RolePrivilegeAuthority.ADMIN_LOCALIZATION_VIEW_AUTHORITY;
import static org.cmdbuild.common.utils.PagedElements.paged;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.success;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.*;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 * @author ldare
 */
@Path("administration/translations/")
@Tag(name = "Translations", description = "Operations related to translations")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
@Component
public class TranslationsWs_Administration {


    private final TranslationsWsCommand command;

    public TranslationsWs_Administration(TranslationsWsCommand command) {
        this.command = checkNotNull(command);
    }

    @GET
    @Path("loadTranslations")
    @Operation(
            summary = "Load translations for languages",
            description = "Load translations for languages",
            parameters = {
                    @Parameter(name = "lang", in = ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_VIEW_AUTHORITY)
    public Object loadAllTranslationsForLanguages(
            @QueryParam("lang") String languages
    ) {
        command.doLoadAllTranslationsForLanguages(languages);
        return response(success());
    }

    @GET
    @Path("")
    @Operation(
            summary = "Get all translations",
            description = "Get all translations",
            parameters = {
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter translations by code", schema = @Schema(ref = "DefaultFilterExample"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_VIEW_AUTHORITY)
    public Object getAll(
            @Nullable @QueryParam(LIMIT) Integer limit,
            @Nullable @QueryParam(START) Integer offset,
            @Nullable @QueryParam(FILTER) String filter
    ) {
        PagedElements<Translation> translations = command.doGetAll(limit, offset, filter);
        return response(translations.map((t) -> map("code", t.getCode(), "lang", t.getLang(), "value", t.getValue())));
    }

    @GET
    @Path("by-code")
    @Operation(
            summary = "Get all translations aggregated by code",
            description = "Get all translations aggregated by code",
            parameters = {
                    @Parameter(name = LIMIT, in = ParameterIn.QUERY, description = "Number of items to return in the response", schema = @Schema(ref = "DefaultLimitExample")),
                    @Parameter(name = START, in = ParameterIn.QUERY, description = "A long value to set an offset in the resultset", schema = @Schema(minimum = "0"), example = "0"),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter translations by code", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = "lang", in = ParameterIn.QUERY, description = "Filter translations by language", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = "includeRecordsWithoutTranslation", in = ParameterIn.QUERY, description = "Include records without translation", schema = @Schema(type = "boolean")),
                    @Parameter(name = "section", in = ParameterIn.QUERY, description = "Filter translations by section", schema = @Schema(ref = "DefaultFilterExample"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_VIEW_AUTHORITY)
    public Object getAllAggregateByCode(
            @Nullable @QueryParam(LIMIT) Integer limit,
            @Nullable @QueryParam(START) Integer offset,
            @Nullable @QueryParam(FILTER) String filter,
            @Nullable @QueryParam("lang") String languages,
            @QueryParam("includeRecordsWithoutTranslation") @DefaultValue(FALSE) Boolean includeRecordsWithoutTranslation,
            @Nullable @QueryParam("section") String section
    ) {
        List<ExportRecord> exportRecordList = command.doGetAllAggregateByCode(section, languages, includeRecordsWithoutTranslation, filter);
        return response(paged(exportRecordList.stream().sorted(Ordering.natural().onResultOf(ExportRecord::getCode)).collect(toList()), offset, limit).map((t) -> map(
                "code", t.getCode(),
                "default", t.getDefault(),
                "values", map(t.getTranslationsByLanguage()).withoutValues(Strings::isNullOrEmpty))));
    }

    @GET
    @Path("{code}/")
    @Operation(
            summary = "Get translation for code",
            description = "Get translation for code",
            parameters = {
                    @Parameter(name = "code", in = ParameterIn.PATH, description = "Code of the translation to query", required = true),
                    @Parameter(name = "lang", in = ParameterIn.QUERY, description = "Filter translations by language", schema = @Schema(ref = "DefaultFilterExample"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "404", description = "Translation not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_VIEW_AUTHORITY)
    public Object getTranslationForKeyAndLang(
            @PathParam("code") String code,
            @Nullable @QueryParam("lang") String lang
    ) {
        Map<String, String> translationValueMap = command.doGetTranslationForKeyAndLang(code, lang);
        if (isNotBlank(lang)) {
            return response(map("code", code, "lang", lang).with(translationValueMap));
        } else {
            return serializeResponse(code, translationValueMap);
        }
    }

    @PUT
    @Path("{code}/")
    @Operation(
            summary = "Set translation for code",
            description = "Set translation for code",
            parameters = {
                    @Parameter(name = "code", in = ParameterIn.PATH, description = "Code of the translation to query", required = true),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = Map.class)), required = true, description = "Translation data"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "404", description = "Translation not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_MODIFY_AUTHORITY)
    public Object setTranslation(
            @PathParam("code") String code,
            Map<String, String> data
    ) {
        return serializeResponse(code, command.doSetTranslation(code, data));
    }

    @DELETE
    @Path("{code}/")
    @Operation(
            summary = "Delete translation for code",
            description = "Delete translation for code",
            parameters = {
                    @Parameter(name = "code", in = ParameterIn.PATH, description = "Code of the translation to query", required = true),
                    @Parameter(name = "lang", in = ParameterIn.QUERY, description = "Filter translations by language", schema = @Schema(ref = "DefaultFilterExample"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "404", description = "Translation not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_MODIFY_AUTHORITY)
    public Object deleteTranslation(
            @PathParam("code") String code,
            @Nullable @QueryParam("lang") String lang
    ) {
        command.doDeleteTranslation(code, lang);
        return success();
    }

    @GET
    @Path("export")
    @Operation(
            summary = "Export translations",
            description = "Export translations",
            parameters = {
                    @Parameter(name = "lang", in = ParameterIn.QUERY, description = "Filter translations by language", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = "format", in = ParameterIn.QUERY, description = "Format of the export", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = FILTER, in = ParameterIn.QUERY, description = "Filter translations by code", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = "separator", in = ParameterIn.QUERY, description = "Separator to use in the export", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = "section", in = ParameterIn.QUERY, description = "Filter translations by section", schema = @Schema(ref = "DefaultFilterExample")),
                    @Parameter(name = "includeRecordsWithoutTranslation", in = ParameterIn.QUERY, description = "Include records without translation", schema = @Schema(type = "boolean"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @RolesAllowed(ADMIN_LOCALIZATION_VIEW_AUTHORITY)
    public DataHandler export(
            @QueryParam("lang") String languages,
            @QueryParam("format") String format,
            @QueryParam(FILTER) String filter,
            @QueryParam("separator") String separator,
            @QueryParam("section") String section,
            @QueryParam("includeRecordsWithoutTranslation") @DefaultValue(FALSE) Boolean includeRecordsWithoutTranslation
    ) {
        checkArgument(isBlank(format) || equal(format.toLowerCase(), "csv"), "invalid format = %s", format);
        //TODO filter
        return command.doExport(languages, format, separator, section, includeRecordsWithoutTranslation);
    }

    @POST
    @Path("import")
    @Operation(
            summary = "Import translations",
            description = ",Import translations",
            parameters = {
                    @Parameter(name = "separator", in = ParameterIn.QUERY, description = "Separator to use in the import", schema = @Schema(ref = "DefaultFilterExample"))
            },
            requestBody = @RequestBody(content = @Content(mediaType = MULTIPART_FORM_DATA, schema = @Schema(implementation = DataHandler.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful operation"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            },
            security = {@SecurityRequirement( name = "BasicAuth", scopes = {} ), @SecurityRequirement( name = "BearerAuth", scopes = {})}
    )
    @Consumes(MULTIPART_FORM_DATA)
    @RolesAllowed(ADMIN_LOCALIZATION_MODIFY_AUTHORITY)
    public Object importTranslations(
            @QueryParam("separator") String separator,
            @Multipart(FILE) DataHandler dataHandler
    ) {
        command.doImportTranslations(separator, dataHandler);
        return success();
    }

    private Object serializeResponse(String code, Map<String, String> map) {
        return response(map("_id", code).with(map));
    }
}
