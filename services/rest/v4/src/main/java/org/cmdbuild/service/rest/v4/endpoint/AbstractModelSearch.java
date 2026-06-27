/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.endpoint;


import jakarta.annotation.Nullable;
import org.cmdbuild.auth.grant.PrivilegeSubjectWithInfo;
import org.cmdbuild.auth.role.Role;
import org.cmdbuild.cardfilter.StoredFilter;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dao.entrytype.Attribute;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.dao.entrytype.EntryType;
import org.cmdbuild.data.filter.CmdbFilter;
import org.cmdbuild.etl.config.inner.WaterwayDescriptorRecord;
import org.cmdbuild.etl.gate.inner.EtlGate;
import org.cmdbuild.etl.loader.EtlTemplate;
import org.cmdbuild.etl.loader.EtlTemplateService;
import org.cmdbuild.jobs.JobData;
import org.cmdbuild.lookup.LookupService;
import org.cmdbuild.lookup.LookupType;
import org.cmdbuild.lookup.LookupValue;
import org.cmdbuild.uicomponents.data.UiComponentData;
import org.cmdbuild.utils.lang.CmCollectionUtils;
import org.cmdbuild.utils.lang.CmConvertUtils;
import org.cmdbuild.utils.lang.CmMapUtils;
import org.cmdbuild.view.View;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.dao.constants.SystemAttributes.ALL_RESERVED_ATTRIBUTES;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmMapUtils.mapOf;

/**
 * @author ldare
 */
public abstract class AbstractModelSearch implements ModelSearch {


    protected <T> List<Map<String, Object>> doSearch(Stream<T> source, Function<T, Collection<String>> entryFiltrables, Function<T, CmMapUtils.FluentMap<String, Object>> mapper, CmdbFilter filter) {
        return doSearch(source, entryFiltrables, mapper, null, null, null, filter);
    }

    protected <T, O> List<Map<String, Object>> doSearch(Stream<T> source, Function<T, Collection<String>> entryFiltrables, Function<T, CmMapUtils.FluentMap<String, Object>> mapper, @Nullable Function<T, Collection<O>> helper, @Nullable Function<O, Collection<String>> itemFiltrables, @Nullable Function<O, Map<String, Object>> itemMapper, CmdbFilter filter) {
        return new TypedSearchHelperNew<>(entryFiltrables, mapper, helper, itemFiltrables, itemMapper, filter).search(source);
    }

    protected CmCollectionUtils.FluentList<Attribute> getPublicAttributes(EntryType entryType) {
        return list(entryType.getServiceAttributes()).without(a -> ALL_RESERVED_ATTRIBUTES.contains(a.getName()));
    }

    protected CmCollectionUtils.FluentList<LookupValue> getLookupValues(LookupType lookupType, LookupService lookupService) {
        return list(lookupService.getAllLookup(lookupType).elements());
    }

    protected CmCollectionUtils.FluentList<EtlTemplate> getEtlTemplates(EtlGate etlGate, EtlTemplateService etlTemplateService) {
        return list(etlGate.getAllTemplates()).distinct().map(etlTemplateService::getTemplateByName);
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(Attribute attribute) {
        return list(attribute.getName(), attribute.getDescription());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(EntryType entryType) {
        return list(entryType.getName(), entryType.getDescription());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(UiComponentData uiComponentData) {
        return list(uiComponentData.getName(), uiComponentData.getDescription());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(PrivilegeSubjectWithInfo privilegeSubjectWithInfo) {
        return list(privilegeSubjectWithInfo.getName(), privilegeSubjectWithInfo.getDescription());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(LookupValue lookupValue) {
        return list(lookupValue.getCode(), lookupValue.getDescription());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(LookupType lookupType) {
        return list(lookupType.getName());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(Role role) {
        return list(role.getName(), role.getDescription(), role.getEmail());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(JobData jobData) {
        return list(jobData.getCode(), jobData.getDescription());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(EtlTemplate etlTemplate, DaoService daoService) {
        return list(etlTemplate.getCode(),
                etlTemplate.getDescription(),
                serializeEnum(etlTemplate.getFileFormat()),
                etlTemplate.getTargetName(),
                (etlTemplate.isTargetClass() || etlTemplate.isTargetProcess()) ?
                        getClassDescriptionIfExists(etlTemplate.getTargetName(), daoService) : null);
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(EtlGate etlGate) {
        return list(etlGate.getCode(), etlGate.getDescription()).with(etlGate.getShowOnClasses());
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(StoredFilter storedFilter, DaoService daoService) {
        return list(storedFilter.getName(), storedFilter.getDescription(), storedFilter.getOwnerName(), getClassDescriptionIfExists(storedFilter.getOwnerName(), daoService));
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(View view, DaoService daoService) {
        return list(view.getName(), view.getDescription(), view.getSourceClass(), view.getSourceFunction(), getClassDescriptionIfExists(view.getSourceClass(), daoService));
    }

    protected CmCollectionUtils.FluentList<String> getSummaryFields(WaterwayDescriptorRecord waterwayDescriptorRecord) {
        return CmCollectionUtils.list(waterwayDescriptorRecord.getCode(), waterwayDescriptorRecord.getDescription());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(Attribute attribute) {
        return map("_id", attribute.getName(),
                "name", attribute.getName(),
                "description", attribute.getDescription());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(EntryType entryType) {
        return map("_id", entryType.getName(),
                "name", entryType.getName(),
                "description", entryType.getDescription());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(LookupType lookupType) {
        return map("_id", lookupType.getId(),
                "name", lookupType.getName(),
                "description", lookupType.getName());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(LookupValue lookupValue) {
        return map("_id", lookupValue.getId(),
                "name", lookupValue.getCode(),
                "description", lookupValue.getDescription());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(EtlTemplate etlTemplate, DaoService daoService) {
        return map("_id", etlTemplate.getCode(),
                "name", etlTemplate.getCode(),
                "fileFormat", serializeEnum(etlTemplate.getFileFormat()),
                "description", etlTemplate.getDescription(),
                "type", serializeEnum(etlTemplate.getType()),
                "target", etlTemplate.getTargetName(),
                "target_description", (etlTemplate.isTargetClass() || etlTemplate.isTargetProcess()) ? getClassDescriptionIfExists(etlTemplate.getTargetName(), daoService) : null);
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(PrivilegeSubjectWithInfo privilegeSubjectWithInfo) {
        return map("_id", privilegeSubjectWithInfo.getId(),
                "name", privilegeSubjectWithInfo.getName(),
                "description", privilegeSubjectWithInfo.getDescription());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(UiComponentData uiComponentData) {
        return map("_id", uiComponentData.getId(),
                "name", uiComponentData.getName(),
                "description", uiComponentData.getDescription(),
                "devices", list(uiComponentData.getTargetDevices()).map(CmConvertUtils::serializeEnum));
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(Role role) {
        return map("_id", role.getId(),
                "name", role.getName(),
                "description", role.getDescription(),
                "email", role.getEmail());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(JobData jobData) {
        return map("_id", jobData.getId(),
                "name", jobData.getCode(),
                "description", jobData.getDescription(),
                "type", jobData.getType());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(EtlGate etlGate) {
        return map("_id", etlGate.getCode(), "name", etlGate.getCode(), "description", etlGate.getDescription(), "type", etlGate.getSingleHandlerType());
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(StoredFilter storedFilter, DaoService daoService) {
        return map("_id", storedFilter.getId(), "name", storedFilter.getName(), "description", storedFilter.getDescription(), "target", storedFilter.getOwnerName(), "target_description", getClassDescriptionIfExists(storedFilter.getOwnerName(), daoService));
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(View view, DaoService daoService) {
        return mapOf(String.class, Object.class).with("_id", view.getId(), "name", view.getName(), "description", view.getDescription(), "type", serializeEnum(view.getType())).accept(m -> {
            switch (view.getType()) {
                case VT_FILTER, VT_JOIN ->
                        m.put("target", view.getSourceClass(), "target_description", getClassDescriptionIfExists(view.getSourceClass(), daoService));
                case VT_SQL -> m.put("target", view.getSourceFunction());
            }
        });
    }

    protected CmMapUtils.FluentMap<String, Object> applyMapping(WaterwayDescriptorRecord waterwayDescriptorRecord) {
        return map("_id", waterwayDescriptorRecord.getId(), "name", waterwayDescriptorRecord.getCode(), "description", waterwayDescriptorRecord.getDescription());
    }

    @Nullable
    protected String getClassDescriptionIfExists(@Nullable String maybeClassName, DaoService daoService) {
        return isBlank(maybeClassName) ? null : Optional.ofNullable(daoService.getClasseOrNull(maybeClassName)).map(Classe::getDescription).orElse(null);
    }

}
