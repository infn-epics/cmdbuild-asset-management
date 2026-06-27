/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.services.rest.test.common;

import org.cmdbuild.auth.grant.GrantAttributePrivilege;
import org.cmdbuild.auth.grant.GrantData;
import org.cmdbuild.auth.grant.GrantDataImpl;
import org.cmdbuild.auth.role.GroupConfig;
import org.cmdbuild.auth.role.GroupConfigImpl;
import org.cmdbuild.auth.role.Role;
import org.cmdbuild.auth.role.RoleImpl;
import org.cmdbuild.auth.user.*;
import org.cmdbuild.calendar.CalendarTriggerInfo;
import org.cmdbuild.calendar.beans.CalendarTriggerImpl;
import org.cmdbuild.cardfilter.*;
import org.cmdbuild.classe.ExtendedClass;
import org.cmdbuild.classe.ExtendedClassImpl;
import org.cmdbuild.cleanup.ViewType;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.contextmenu.ContextMenuItem;
import org.cmdbuild.contextmenu.ContextMenuItemImpl;
import org.cmdbuild.corecomponents.CoreComponent;
import org.cmdbuild.corecomponents.CoreComponentImpl;
import org.cmdbuild.corecomponents.CoreComponentType;
import org.cmdbuild.dao.beans.*;
import org.cmdbuild.dao.entrytype.*;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dashboard.DashboardData;
import org.cmdbuild.dashboard.inner.DashboardDataImpl;
import org.cmdbuild.easyupload.EasyuploadItem;
import org.cmdbuild.easyupload.EasyuploadItemImpl;
import org.cmdbuild.email.EmailAccount;
import org.cmdbuild.email.EmailSignature;
import org.cmdbuild.email.beans.*;
import org.cmdbuild.email.template.EmailTemplate;
import org.cmdbuild.email.template.EmailTemplateService;
import org.cmdbuild.etl.config.inner.WaterwayDescriptorRecord;
import org.cmdbuild.etl.config.inner.WaterwayDescriptorRecordImpl;
import org.cmdbuild.etl.gate.inner.EtlGate;
import org.cmdbuild.etl.gate.inner.EtlGateHandler;
import org.cmdbuild.etl.gate.inner.EtlGateHandlerImpl;
import org.cmdbuild.etl.gate.inner.EtlGateImpl;
import org.cmdbuild.etl.loader.EtlProcessingResultDetailsImpl;
import org.cmdbuild.etl.loader.EtlTemplate;
import org.cmdbuild.etl.loader.EtlTemplateImpl;
import org.cmdbuild.etl.loader.inner.EtlProcessingResultErrorImpl;
import org.cmdbuild.etl.loader.inner.EtlProcessingResultImpl;
import org.cmdbuild.etl.webhook.WebhookConfig;
import org.cmdbuild.etl.webhook.WebhookConfigImpl;
import org.cmdbuild.etl.webhook.WebhookMethod;
import org.cmdbuild.formstructure.FormStructure;
import org.cmdbuild.formstructure.FormStructureImpl;
import org.cmdbuild.formtrigger.FormTrigger;
import org.cmdbuild.formtrigger.FormTriggerImpl;
import org.cmdbuild.gis.*;
import org.cmdbuild.gis.geoserver.GeoserverLayerImpl;
import org.cmdbuild.jobs.JobData;
import org.cmdbuild.jobs.beans.JobDataImpl;
import org.cmdbuild.lookup.*;
import org.cmdbuild.lookup.LookupValueImpl;
import org.cmdbuild.navtree.NavTree;
import org.cmdbuild.navtree.NavTreeImpl;
import org.cmdbuild.navtree.NavTreeNode;
import org.cmdbuild.navtree.NavTreeNodeImpl;
import org.cmdbuild.participant.ParticipantHeader;
import org.cmdbuild.report.ReportConfig;
import org.cmdbuild.report.ReportConfigImpl;
import org.cmdbuild.report.ReportInfo;
import org.cmdbuild.report.ReportInfoImpl;
import org.cmdbuild.report.dao.ReportDataImpl;
import org.cmdbuild.service.rest.common.beans.WsQueryOptions;
import org.cmdbuild.service.rest.common.serializationhelpers.WsLookupValue;
import org.cmdbuild.service.rest.v4.model.WsViewData;
import org.cmdbuild.services.serialization.EmailTemplateSerializationHelper;
import org.cmdbuild.template.TemplateBindings;
import org.cmdbuild.template.TemplateBindingsImpl;
import org.cmdbuild.translation.ObjectTranslationService;
import org.cmdbuild.ui.TargetDevice;
import org.cmdbuild.uicomponents.UiComponentInfo;
import org.cmdbuild.uicomponents.UiComponentInfoImpl;
import org.cmdbuild.uicomponents.UiComponentVersionInfo;
import org.cmdbuild.uicomponents.UiComponentVersionInfoImpl;
import org.cmdbuild.uicomponents.data.UiComponentData;
import org.cmdbuild.uicomponents.data.UiComponentDataImpl;
import org.cmdbuild.uicomponents.data.UiComponentType;
import org.cmdbuild.utils.cad.model.CadPoint;
import org.cmdbuild.utils.lang.CmMapUtils;
import org.cmdbuild.view.View;
import org.cmdbuild.view.ViewImpl;
import org.cmdbuild.widget.dao.WidgetDataFromDbImpl;
import org.cmdbuild.widget.model.*;
import org.cmdbuild.workflow.model.*;
import org.cmdbuild.workflow.model.ProcessImpl;
import org.jspecify.nullness.Nullable;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.cmdbuild.auth.grant.GrantAttributePrivilege.GAP_READ;
import static org.cmdbuild.auth.grant.GrantMode.GM_READ;
import static org.cmdbuild.auth.grant.PrivilegedObjectType.POT_CLASS;
import static org.cmdbuild.calendar.beans.CalendarEventType.CT_DATE;
import static org.cmdbuild.contextmenu.ContextMenuType.SEPARATOR;
import static org.cmdbuild.dao.beans.RelationDirection.RD_DIRECT;
import static org.cmdbuild.dao.entrytype.ClassMetadata.CLASS_SPECIALITY;
import static org.cmdbuild.dao.entrytype.ClassMetadata.DMS_CATEGORY;
import static org.cmdbuild.dao.entrytype.PermissionScope.PS_CORE;
import static org.cmdbuild.etl.gate.inner.EtlGate.ETL_GATE_CONFIG_SHOW_ON_CLASSES;
import static org.cmdbuild.etl.gate.inner.EtlGateHandler.ETL_HANDLER_CONFIG_TEMPLATES;
import static org.cmdbuild.etl.gate.inner.EtlGateHandler.ETL_HANDLER_CONFIG_TYPE;
import static org.cmdbuild.etl.gate.inner.EtlGateHandlerType.ETLHT_IFC;
import static org.cmdbuild.etl.gate.inner.EtlProcessingMode.PM_REALTIME;
import static org.cmdbuild.formtrigger.FormTriggerBinding.beforeInsert;
import static org.cmdbuild.gis.GisAttributeType.GAT_POINT;
import static org.cmdbuild.navtree.NavTreeType.NT_MENU;
import static org.cmdbuild.report.ReportFormat.PDF;
import static org.cmdbuild.ui.TargetDevice.TD_DEFAULT;
import static org.cmdbuild.uicomponents.data.UiComponentType.UCT_WIDGET;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmNullableUtils.isNotBlank;

/**
 *
 * @author afelice
 */
public class TestHelper_Model {

//###########################################################################################
//############################## - ATTRIBUTE - ##############################################
//###########################################################################################

    /**
     * Create an AttributeWithoutOwner of type String with the given name.
     *
     * @param attributeName the attribute name to assign
     * @return an AttributeWithoutOwner instance using StringAttributeType
     */
    public static AttributeWithoutOwner mockBuildAttributeWithoutOwner(final String attributeName) {
        return AttributeWithoutOwnerImpl.builder()
                .withName(attributeName)
                .withType(new StringAttributeType())
                .build();
    }

    /**
     * Builds a mock {@link Attribute} of type String for the specified class.
     * <p>
     * The returned attribute will have the given name, be owned by the provided
     * {@link Classe}, and use a {@link StringAttributeType} with the default
     * (unbounded) length as its type.
     * </p>
     * <p>
     * The {@link StringAttributeType} is constructed with {@code null} length,
     * which means it will default to {@code Integer.MAX_VALUE} (unbounded
     * length).
     * </p>
     *
     * @param attributeName the name of the attribute to create
     * @param ownerClass    the {@link Classe} that owns this attribute
     * @return a mock {@link Attribute} instance with a String type and
     * unbounded length
     */
    public static Attribute mockBuildAttribute(final String attributeName, final Classe ownerClass) {
        return AttributeImpl.builder()
                .withOwner(ownerClass)
                .withName(attributeName)
                .withType(new StringAttributeType())
                .build();
    }

    public static Attribute mockBuildAttribute_ForeignKeyAttributeType(final String attributeName, final Classe ownerClass) {
        return AttributeImpl.builder()
                .withOwner(ownerClass)
                .withName(attributeName)
                .withType(new ForeignKeyAttributeType("exampDestinationClassName"))
                .build();
    }

    public static Attribute mockBuildAttributeInverse(final String attributeName, Domain floorRoomDomain) {
        // Create Floor reference
        // "classId": "Room1"
        // "domain": "FloorRoom1"
        // "targetClass": "Floor1"
        // "direction": "inverse"
        // "name": "Floor",
        // "type": "reference"
        Classe room = floorRoomDomain.getTargetClass();

        return AttributeImpl.builder()
                .withOwner(room)
                .withName(attributeName)
                .withType(new ReferenceAttributeType(floorRoomDomain, RelationDirection.RD_INVERSE))
                .build();
    }

    public static AttributeMetadataImpl mockBuildAttributeMetadata() {
        return AttributeMetadataImpl.builder()
                .build();
    }

//###########################################################################################
//################################# - CLASSE - ##############################################
//###########################################################################################

    /**
     * Create a simple classe.
     *
     * @param classeName
     * @return
     */
    public static Classe mockBuildClasse(String classeName) {
        final List<String> ancestors = emptyList();
        final List<AttributeWithoutOwner> attributes = emptyList();
        return mockBuildClasse(classeName, ancestors, attributes);
    }

    /**
     * Create a simple classe.
     *
     * @param classeName
     * @param attributes
     * @return
     */
    public static Classe mockBuildClasse(String classeName, List<AttributeWithoutOwner> attributes) {
        final List<String> ancestors = emptyList();
        return mockBuildClasse(classeName, ancestors, attributes);
    }

    /**
     * Builds a mock {@link Classe} instance with the specified name, ancestors,
     * and attributes.
     * <p>
     * The generated {@code Classe} will have a unique ID (using
     * {@link UniqueTestIdUtils#tuid()}), the provided name, and the given list
     * of attributes. If the {@code ancestors} list is not empty, it will be set
     * on the builder.
     * </p>
     * <p>
     * Note: The unique ID ensures that attribute-related filters are
     * categorized as {@code CLASS_ATTRIBUTE} instead of {@code EMBEDDED}.
     * </p>
     *
     * @param classeName the name of the class to create
     * @param ancestors  a list of ancestor class names (may be empty)
     * @param attributes the list of attributes for the class. Attributes of
     *                   <code>Card</code>s are derived from this
     * @return a mock {@link Classe} instance
     */
    public static Classe mockBuildClasse(String classeName, List<String> ancestors, List<AttributeWithoutOwner> attributes) {
        ClasseImpl.ClasseBuilder builder = ClasseImpl.builder()
                .withName(classeName)
                .withId(Long.valueOf(UniqueTestIdUtils.tuid())) // Without this the attribute related filter is categorized as EMBEDDED instead of CLASS_ATTRIBUTE
                .withAttributes(attributes);
        if (!ancestors.isEmpty()) {
            builder.withAncestors(ancestors);
        }
        return builder.build();
    }

    /**
     *
     * @param classeName
     * @param classSpeciality
     * @return
     */
    public static ClasseImpl mockBuildClasseWithMetadata(String classeName, ClassMetadata.@Nullable ClassSpeciality classSpeciality) {
        final List<String> ancestors = emptyList();
        List<AttributeWithoutOwner> attributes = list(
                mockBuildAttributeWithoutOwner(TestHelper_Variable.A_KNOWN_ATTR_NAME1),
                mockBuildAttributeWithoutOwner(TestHelper_Variable.A_KNOWN_ATTR_NAME2),
                mockBuildAttributeWithoutOwner(TestHelper_Variable.A_KNOWN_ATTR_NAME3)
        );
        ClasseImpl.ClasseBuilder classeBuilder = ClasseImpl.builder()
                .withId(Long.valueOf(UniqueTestIdUtils.tuid()))
                .withName(classeName)
                .withMetadata(mockBuildClassMetadata())
                .withAncestors(ancestors)
                .withAttributes(attributes);

        if (classSpeciality != null) {
            classeBuilder.withMetadata(new ClassMetadataImpl(map(CLASS_SPECIALITY, classSpeciality.toString())));
        }

        return classeBuilder.build();
    }

    public static Classe mockBuildClasseWithAttr(String classeName) {
        final List<String> ancestors = emptyList();
        List<AttributeWithoutOwner> attributes = list(
                mockBuildAttributeWithoutOwner(TestHelper_Variable.A_KNOWN_ATTR_NAME1),
                mockBuildAttributeWithoutOwner(TestHelper_Variable.A_KNOWN_ATTR_NAME2),
                mockBuildAttributeWithoutOwner(TestHelper_Variable.A_KNOWN_ATTR_NAME3)
        );
        return mockBuildClasse(classeName, ancestors, attributes);
    }

    public static ClasseImpl mockBuildClasse(Long id, String classeName) {
        return ClasseImpl.builder()
                .withId(id)
                .withName(classeName)
                .withMetadata(mockBuildClassMetadata())
                .build();
    }

    public static Classe mockBuildClasse_Process(String classeName, List<AttributeWithoutOwner> processAttributes,
                                                 String dmsCategoryType) {
        CmMapUtils.FluentMap<String, String> metadata = map(CLASS_SPECIALITY, serializeEnum(ClassMetadata.ClassSpeciality.CS_PROCESS));

        if (isNotBlank(dmsCategoryType)) {
            metadata.with(DMS_CATEGORY, dmsCategoryType);
        }

        return ClasseImpl.builder()
                .withName(classeName)
                .withId(Long.valueOf(UniqueTestIdUtils.tuid())) // Without this the attribute related filter is categorized as EMBEDDED instead of CLASS_ATTRIBUTE
                .withAttributes(processAttributes)
                .withMetadata(new ClassMetadataImpl(metadata)) // Without this, the ProcessImpl constructor will complain
                .build();
    }

    public static ClassMetadata mockBuildClassMetadata() {
        return ClassMetadataImpl.builder()
                .withClassType(ClassType.CT_SIMPLE)
                .build();
    }

    public static Classe mockBuildClasseWithGisPermissions(String classeName, List<String> gisAttributesNames) {
        return ClasseImpl.builder()
                .withId(Long.valueOf(UniqueTestIdUtils.tuid()))
                .withName(classeName)
                .withMetadata(mockBuildClassMetadata())
                .withPermissions(mockBuildGisClassPermissions(gisAttributesNames))
                .build();
    }

    //###########################################################################################
//###################################### - WSVIEWDATA- ######################################
//###########################################################################################
    public static WsViewData mockBuildWsViewData(String name) {
        return new WsViewData(
                name,
                "view description",
                "source class name",
                "source function",
                "",
                true,
                true,
                "VT_SQL",
                "viewMasterclass",
                "viewMasterclassAlias",
                null,
                null,
                emptyList(),
                null,
                null,
                "JVPM_DEFAULT",
                emptyList());
    }

    //###########################################################################################
//###################################### - PROCESS - ########################################
//###########################################################################################
    public static org.cmdbuild.workflow.model.Process mockBuildProcess() {
        return ProcessImpl.builder()
                .withInner(mockBuildClasse_Process(TestHelper_Variable.A_KNOWN_CLASS_NAME1, emptyList(), "exampDmsCategoryType"))
                .withPlanId(TestHelper_Variable.A_KNOWN_PLAN_ID)
                .withEntryTasks(map("exampEntryTask", mockBuildTaskDefinition()))
                .withTasksById(map("examptasksById", mockBuildTaskDefinition()))
                .build();
    }

    public static org.cmdbuild.workflow.model.Process mockBuildProcess(Classe inner) {
        return ProcessImpl.builder()
                .withInner(inner)
                .withEntryTasks(emptyMap())
                .withTasksById(emptyMap())
                .withPlanId("planId")
                .build();
    }

    //###########################################################################################
//################################# - EXTENDEDCLASS - #######################################
//###########################################################################################
    public static ExtendedClass mockBuildExtendedClass(String name) {
        FormStructure formStructure = new FormStructureImpl("{ \"color\" : \"Black\", \"type\" : \"FIAT\" }");
        return ExtendedClassImpl.builder()
                .withClasse(mockBuildClasse(name))
                .withFormTriggers(list(mockBuildFormTrigger()))
                .withContextMenuItems(list(mockBuildContextMenuItem()))
                .withWidgets(list(mockBuildWidgetDbData()))
                .withFormStructure(formStructure)
                .withCalendarTriggers(list(mockBuildCalendarTriggerInfo()))
                .withLookupValuesByAttr(map())
                .build();
    }

    public static Classe toDeactivated(Classe classe) {
        return ClasseImpl.copyOf(classe).withMetadata(ClassMetadataImpl.copyOf(classe.getMetadata()).withActive(false).build()).build();
    }

    //###########################################################################################
//################################# - CLASSPERMISSION - #####################################
//###########################################################################################
    public static ClassPermissions mockBuildClassPermissions() {
        Set<ClassPermission> set = new HashSet<>();
        set.add(ClassPermission.CP_ALL);
        return ClassPermissionsImpl.builder()
                .withPermissions(map(PS_CORE, set))
                .build();
    }

    public static ClassPermissions mockBuildGisClassPermissions(List<String> gisAttributesNames) {
        Map<String, Set<GrantAttributePrivilege>> gisPermissions = gisAttributesNames.stream().collect(Collectors.toMap(k -> k, k -> Set.of(GAP_READ)));
        return ClassPermissionsImpl.builder()
                .withPermissions(map(PS_CORE, Set.of(ClassPermission.CP_ALL)))
                .withGisPermissions(gisPermissions)
                .build();
    }

//###########################################################################################
//######################################## - CARD - #########################################
//###########################################################################################

    /**
     * Generate a card.
     *
     * @param cardId
     * @param classe
     * @param attributes
     * @return
     */
    public static Card mockBuildCard(Long cardId, Classe classe, Map<String, Object> attributes) {
        return CardImpl.builder()
                .withId(cardId).withType(classe)
                .withAttributes(attributes)
                .build();
    }

    public static CardFilterAsDefaultForClass mockBuildCardFilterAsDefaultForClass() {
        return new CardFilterAsDefaultForClassImpl(mockBuildActiveStoredFilter(TestHelper_Variable.A_KNOWN_FILTER_ID1), "Owner Name", 777L);
    }

    //###########################################################################################
//######################################## - DOMAIN - #######################################
//###########################################################################################
    public static Domain mockBuildDomainDirect(String domainName, Classe sourceClass, Classe targetClass) {
        DomainImpl.DomainImplBuilder domainBuilder = DomainImpl.builder()
                .withName(domainName)
                .withClass1(sourceClass)
                .withClass2(targetClass);
        domainBuilder.withMetadata(b -> b.withCardinality(DomainCardinality.MANY_TO_ONE));

        Domain directDomain = domainBuilder.build();
        return directDomain;
    }

    public static Domain mockBuildDomainInverse(String domainName, Classe sourceClass, Classe targetClass) {
        DomainImpl.DomainImplBuilder domainBuilder = DomainImpl.builder()
                .withName(domainName)
                .withClass1(sourceClass)
                .withClass2(targetClass);

        domainBuilder.withMetadata(b -> b.withCardinality(DomainCardinality.ONE_TO_MANY));
        Domain domain = domainBuilder.build();

        return domain;
    }

    public static Domain mockBuildDomainWithAttributes(String domainName, String className1, String className2, String attrName) {
        Classe targetClass = mockBuildClasseWithAttr(className2);
        DomainImpl.DomainImplBuilder domainBuilder = DomainImpl.builder()
                .withName(domainName)
                .withClass1(mockBuildClasseWithAttr(className1)) // RequestForChange1
                .withClass2(targetClass) // Employee1
                .withAttribute(mockBuildAttributeWithoutOwner(attrName))
                .withAttribute(mockBuildAttribute_ForeignKeyAttributeType(domainName, targetClass));
        domainBuilder.withMetadata(b -> b.withCardinality(DomainCardinality.MANY_TO_ONE));

        Domain directDomain = domainBuilder.build();
        return directDomain;
    }

    public static List<Domain> mockBuildDomainsList() {
        Classe sourceClass1 = mockBuildClasse(TestHelper_Variable.A_SOURCE_CLASS_NAME1);
        Classe targetClass1 = mockBuildClasse(TestHelper_Variable.A_TARGET_CLASS_NAME1);
        Domain domainA = mockBuildDomainDirect(TestHelper_Variable.A_SOURCE_CLASS_NAME1 + TestHelper_Variable.A_TARGET_CLASS_NAME1, sourceClass1, targetClass1);
        Classe sourceClass2 = mockBuildClasse(TestHelper_Variable.A_SOURCE_CLASS_NAME2);
        Classe targetClass2 = mockBuildClasse(TestHelper_Variable.A_TARGET_CLASS_NAME2);
        Domain domainB = mockBuildDomainDirect(TestHelper_Variable.A_SOURCE_CLASS_NAME2 + TestHelper_Variable.A_TARGET_CLASS_NAME2, sourceClass2, targetClass2);
        return list(domainA, domainB);
    }

    //###########################################################################################
//###################################### - WIDGET - #########################################
//###########################################################################################
    public static Widget mockBuildWidget() {
        Map<String, Object> data = map();
        data.put("key1", "value1");
        Map<String, Object> context = map();
        context.put("key1", "value1");
        return WidgetImpl.builder()
                .withType("exampleType")
                .withLabel("Example Widget")
                .withId("widget01")
                .withActive(true)
                .withData(data)
                .withContext(context)
                .build();
    }

    public static WidgetData mockBuildWidgetData() {
        return WidgetDataImpl.builder()
                .withType("exampleType")
                .withLabel("Example Widget")
                .withId("widget01")
                .withIsActive(true)
                .withData("key1", "value1")
                .withData("key2", 100)
                .build();
    }

    public static WidgetDbData mockBuildWidgetDbData() {
        Map<String, String> mapData = map();
        mapData.put("key1", "value1");
        mapData.put("key2", "value2");
        return WidgetDataFromDbImpl.builder()
                .withOwner("exampleOwner")
                .withLabel("Example Widget")
                .withType("exampleType")
                .withId("widget01")
                .withData(mapData)
                .withActive(true)
                .build();
    }

    //###########################################################################################
//################################## - FORMTRIGGER - ########################################
//###########################################################################################
    public static FormTrigger mockBuildFormTrigger() {
        return FormTriggerImpl.builder()
                .withJsScript("exampJsScript")
                .withActive(true)
                .withBindings(list(beforeInsert))
                .build();
    }

    //###########################################################################################
//############################## - CALENDARTRIGGER - ########################################
//###########################################################################################
    public static CalendarTriggerInfo mockBuildCalendarTriggerInfo() {
        return CalendarTriggerImpl.builder()
                .withCode("exampCode")
                .withOwnerClass("exampOwnerClass")
                .withOwnerClass("exampOwnerClass")
                .withOwnerAttr("exampOwnerAttr")
                .withActive(true)
                .withDescription("exampDescription")
                .withContent("exampContent")
                .withType(CT_DATE)
                .withCategory(mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME1, TestHelper_Variable.A_KNOWN_LOOKUP_ID1))
                .withPriority(mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME2, TestHelper_Variable.A_KNOWN_LOOKUP_ID2))
                .withParticipants(list(ParticipantHeader.USER.name() + ".123", ParticipantHeader.GROUP.name() + ".456"))
                .withNotifications(list(mockBuildEmailTemplateInlineData()))
                .build();
    }

    //###########################################################################################
//####################################### - EMAIL - #########################################
//###########################################################################################
    public static EmailAccount mockBuildEmailAccount(String name) {
        return EmailAccountImpl.builder()
                .withName(name)
                .withSmtpSsl(true)
                .withSmtpStartTls(true)
                .withImapSsl(true)
                .withImapStartTls(true)
                .withConfig("config", "config")
                .withActive(true)
                .withPassword("myPassword")
                .build();
    }

    public static List<EmailAccount> mockBuildListEmailAccount() {
        return list(
                mockBuildEmailAccount(TestHelper_Variable.A_KNOWN_EMAIL_NAME1),
                mockBuildEmailAccount(TestHelper_Variable.A_KNOWN_EMAIL_NAME2),
                mockBuildEmailAccount(TestHelper_Variable.A_KNOWN_EMAIL_NAME3)
        );
    }

    public static EmailTemplateInlineData mockBuildEmailTemplateInlineData() {
        return EmailTemplateInlineDataImpl.builder()
                .withId("exampId")
                .withTemplate("exampTemplate")
                .withContent("exampContent")
                .build();
    }

    public static EmailSignature mockBuildEmailSignature(Long id) {
        return EmailSignatureImpl.builder()
                .withId(id)
                .withActive(true)
                .withCode("exampCode")
                .withDescription("exampDescription")
                .withContentHtml("someContent_html")
                .build();
    }

    public static EmailSignature mockBuildEmailSignatureNotActive(Long id) {
        return EmailSignatureImpl.builder()
                .withId(id)
                .withActive(false)
                .withCode("exampCode")
                .withDescription("exampDescription")
                .withContentHtml("someContent_html")
                .build();
    }

    public static EmailTemplate mockBuildEmailTemplate(String code) {
        return EmailTemplateImpl.builder()
                .withNotificationProvider("exampType")
                .withCode(code)
                .withActive(true)
                .withContentType("exampContentType")
                .withKeepSynchronization(true)
                .withPromptSynchronization(true)
                .withMeta(map("meta", "exampMeta"))
                .withReports(list(mockBuildReportConfig()))
                .build();
    }

    public static List<EmailTemplate> mockBuildListEmailTemplate() {
        return list(
                mockBuildEmailTemplate(TestHelper_Variable.A_EMAIL_TEMPLATE_NAME1),
                mockBuildEmailTemplate(TestHelper_Variable.A_EMAIL_TEMPLATE_NAME2),
                mockBuildEmailTemplate(TestHelper_Variable.A_EMAIL_TEMPLATE_NAME3)
        );
    }

    public static TemplateBindings mockBuildTemplateBindings() {
        Collection<String> clientBindings = list("clientBindings1", "clientBindings2");
        Collection<String> serverBindings = list("serverBindings1", "serverBindings2");
        return new TemplateBindingsImpl(clientBindings, serverBindings);
    }

    //###########################################################################################
//################################### - LOOKUPTYPE - ########################################
//###########################################################################################
    public static PagedElements<LookupType> mockBuildPagedElementsLookupType(LookupSpeciality lookupSpeciality) {
        List<LookupType> listLookUpType = mockBuildListOfLookupType(lookupSpeciality);
        return new PagedElements(listLookUpType, 1L, null);
    }

    public static LookupType mockBuildLookupType(String name) {
        return LookupTypeImpl.builder()
                .withId(0L)
                .withName(name)
                .withParent(1L)
                .withAccessType(LookupAccessType.LT_SYSTEM)
                .withSpeciality(LookupSpeciality.LS_DMSCATEGORY)
                .build();
    }

    public static LookupType mockBuildLookupType(String name, LookupSpeciality lookupSpeciality) {
        return LookupTypeImpl.builder()
                .withId(0L)
                .withName(name)
                .withParent(1L)
                .withAccessType(LookupAccessType.LT_DEFAULT)
                .withSpeciality(lookupSpeciality)
                .build();
    }

    public static List<LookupType> mockBuildListOfLookupType(LookupSpeciality lookupSpeciality) {
        List<LookupType> list = list(
                mockBuildLookupType(TestHelper_Variable.A_KNOWN_LOOKUP_NAME1, lookupSpeciality),
                mockBuildLookupType(TestHelper_Variable.A_KNOWN_LOOKUP_NAME2, lookupSpeciality),
                mockBuildLookupType(TestHelper_Variable.A_KNOWN_LOOKUP_NAME3, lookupSpeciality));
        return list;
    }

    //###########################################################################################
//################################## - LOOKUPVALUE - ########################################
//###########################################################################################
    public static LookupValue mockBuildLookupValue(String name, Long id) {
        return LookupValueImpl.builder()
                .withId(id)
                .withCode(name)
                .withDescription("myDescription")
                .withNotes("someNotes")
                .withActive(true)
                .withTypeName("typeName")
                .withParentId(Long.MIN_VALUE)
                .withType(mockBuildLookupType(TestHelper_Variable.A_KNOWN_LOOKUP_NAME1))
                .build();
    }

    public static List<LookupValue> mockBuildListLookupValue() {
        return list(
                mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME1, TestHelper_Variable.A_KNOWN_LOOKUP_ID1),
                mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME2, TestHelper_Variable.A_KNOWN_LOOKUP_ID2),
                mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME3, TestHelper_Variable.A_KNOWN_LOOKUP_ID3));
    }

    public static Iterable<LookupValue> convertWithStreamIterator(LookupValue[] array) {
        return () -> Arrays.stream(array).iterator();
    }

    public static PagedElements<LookupValue> mockBuildPagedElementsLookupValue() {
        List<LookupValue> listOfLookupValue = list(mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME1, TestHelper_Variable.A_KNOWN_LOOKUP_ID1));
        PagedElements<LookupValue> elements = new PagedElements(listOfLookupValue, 1L, null);
        return elements;
    }

    public static PagedElements<LookupValue> mockBuildPagedElementsLookupValue2() {
        List<LookupValue> listLookUpValue = mockBuildListLookupValue();
        return new PagedElements(listLookUpValue, 1L, null);
    }

    public static PagedElements<LookupValue> mockBuild2PagedElementsLookupValue() {
        List<LookupValue> listOfLookupValue = list(mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME1, TestHelper_Variable.A_KNOWN_LOOKUP_ID1), mockBuildLookupValue(TestHelper_Variable.A_KNOWN_LOOKUP_NAME2, TestHelper_Variable.A_KNOWN_LOOKUP_ID2));
        PagedElements<LookupValue> elements = new PagedElements(listOfLookupValue, 1L, null);
        return elements;
    }

    //###########################################################################################
//################################## - WSLOOKUPVALUE - ######################################
//###########################################################################################
    public static WsLookupValue mockBuildWsLookupValue() {
        return new WsLookupValue(null, null, null, null, "exampCode", null, null, null, null, null, null, null, null, null, null, null, null);
    }

    //###########################################################################################
//################################## - CORECOMPONENT - ######################################
//###########################################################################################
    public static CoreComponent mockBuildCoreComponent(String code) {
        return CoreComponentImpl.builder()
                .withActive(true)
                .withCode(code)
                .withDescription("This is a description")
                .withData("Some data")
                .withType(CoreComponentType.CCT_SCRIPT)
                .build();
    }

    public static List<CoreComponent> mockBuildListCoreComponent() {
        return list(
                mockBuildCoreComponent(TestHelper_Variable.A_KNOWN_COMPONENT_CODE1),
                mockBuildCoreComponent(TestHelper_Variable.A_KNOWN_COMPONENT_CODE2),
                mockBuildCoreComponent(TestHelper_Variable.A_KNOWN_COMPONENT_CODE3)
        );
    }

    //###########################################################################################
//################################## - STOREDFILTER - #######################################
//###########################################################################################
    public static StoredFilter mockBuildActiveStoredFilter(Long id) {
        StoredFilter filter = StoredFilterImpl.builder()
                .withId(id)
                .withName("Active Filter " + id)
                .withDescription("Description for active filter " + id)
                .withOwnerName("Owner Name")
                .withOwnerType(StoredFilterOwnerType.SFO_CLASS)
                .withActive(true)
                .withConfiguration("{}")
                .withShared(true)
                .build();
        return filter;
    }

    public static StoredFilter mockBuildInactiveStoredFilter(Long id) {
        StoredFilter filter = StoredFilterImpl.builder()
                .withId(id)
                .withName("Inactive Filter " + id)
                .withDescription("Description for inactive filter " + id)
                .withOwnerName("exampleOwnerName")
                .withOwnerType(StoredFilterOwnerType.SFO_CLASS)
                .withActive(false)
                .withConfiguration("{}")
                .withShared(true)
                .build();
        return filter;
    }

    public static List<StoredFilter> mockBuildTwoStoredFilters(Long idFirstFilter, Long idSecondFilter) {
        return list(
                mockBuildActiveStoredFilter(idFirstFilter),
                mockBuildInactiveStoredFilter(idSecondFilter)
        );
    }

    //###########################################################################################
//############################## - WATERWAYDESCRIPTORRECORD- ################################
//###########################################################################################
    public static WaterwayDescriptorRecord mockBuildWaterwayDescriptorRecord(Long id) {
        return WaterwayDescriptorRecordImpl.builder()
                .withId(id)
                .withCode("waterwayCode")
                .withData("data: waterwayData")
                .build();
    }

    public static List<WaterwayDescriptorRecord> mockBuildListWaterwayDescriptorRecord() {
        return list(
                mockBuildWaterwayDescriptorRecord(TestHelper_Variable.A_KNOWN_WATERWAY_DESCRIPTOR_ID1),
                mockBuildWaterwayDescriptorRecord(TestHelper_Variable.A_KNOWN_WATERWAY_DESCRIPTOR_ID2),
                mockBuildWaterwayDescriptorRecord(TestHelper_Variable.A_KNOWN_WATERWAY_DESCRIPTOR_ID3)
        );
    }

    //###########################################################################################
//######################################### - VIEW - ########################################
//###########################################################################################
    public static View mockBuildView(String name) {
        return ViewImpl.builder()
                .withName(name)
                .withId(1L)
                .withType(ViewType.VT_SQL)
                .withSourceFunction("sourceFunction")
                .build();
    }

    public static List<View> mockBuildListView() {
        return list(
                mockBuildView(TestHelper_Variable.A_KNOWN_VIEW_NAME1),
                mockBuildView(TestHelper_Variable.A_KNOWN_VIEW_NAME2),
                mockBuildView(TestHelper_Variable.A_KNOWN_VIEW_NAME3)
        );
    }

    //###########################################################################################
//##################################### - USERDATA - ########################################
//###########################################################################################
    public static UserDataImpl mockBuildUserDataImpl(Long id) {
        return UserDataImpl.builder()
                .withId(id)
                .withUsername("testUsername")
                .build();
    }

    //###########################################################################################
//################################## - OPERATIONUSER - ######################################
//###########################################################################################
    public static OperationUser mockBuildOperationUser() {
        return OperationUserImpl.builder()
                .withAuthenticatedUser(mockBuildLoginUser())
                .build();
    }

    //###########################################################################################
//##################################### - LOGINUSER - #######################################
//###########################################################################################
    public static LoginUser mockBuildLoginUser() {
        return LoginUserImpl.builder()
                .withId(1L)
                .withUsername("username")
                .withActiveStatus(false)
                .withServiceStatus(false)
                .build();
    }

    //###########################################################################################
//##################################### - UICOMPONENT- ######################################
//###########################################################################################
    public static UiComponentData mockBuildUiComponent(String name, UiComponentType uiComponentType) {
        byte[] dataDefault = "dataDefault".getBytes();
        byte[] dataMobile = "dataMobile".getBytes();
        return UiComponentDataImpl.builder()
                .withName(name)
                .withId(1L)
                .withActive(true)
                .withType(uiComponentType)
                .withDataDefault(dataDefault)
                .withDataMobile(dataMobile)
                .withSynthesized(true)
                .build();
    }

    public static List<UiComponentData> mockBuildListUiComponent(UiComponentType uiComponentType) {
        UiComponentData uiComponentData1 = mockBuildUiComponent(TestHelper_Variable.A_KNOWN_UI_COMPONENT_DATA_NAME1, uiComponentType);
        UiComponentData uiComponentData2 = mockBuildUiComponent(TestHelper_Variable.A_KNOWN_UI_COMPONENT_DATA_NAME2, uiComponentType);
        UiComponentData uiComponentData3 = mockBuildUiComponent(TestHelper_Variable.A_KNOWN_UI_COMPONENT_DATA_NAME3, uiComponentType);
        return list(uiComponentData1, uiComponentData2, uiComponentData3);
    }

    public static UiComponentInfo mockBuildUiComponentInfo(String name) {
        UiComponentType type = UCT_WIDGET;
        TargetDevice targetDevice = TD_DEFAULT;
        UiComponentVersionInfo uiComponentVersionInfo = new UiComponentVersionInfoImpl(targetDevice);
        List<UiComponentVersionInfo> versions = list(uiComponentVersionInfo);
        return UiComponentInfoImpl.builder()
                .withName(name)
                .withActive(true)
                .withDescription("exampleDescription")
                .withExtjsComponentId("someExtjsComponentId")
                .withExtjsAlias("someExtjsAlias")
                .withType(type)
                .withVersions(versions)
                .build();
    }

    public static List<UiComponentInfo> mockBuildListUiComponentInfo() {
        return list(
                mockBuildUiComponentInfo(TestHelper_Variable.A_KNOWN_UI_COMPONENT_NAME1),
                mockBuildUiComponentInfo(TestHelper_Variable.A_KNOWN_UI_COMPONENT_NAME2),
                mockBuildUiComponentInfo(TestHelper_Variable.A_KNOWN_UI_COMPONENT_NAME3)
        );
    }

    //###########################################################################################
//################################## - DASHBOARDDATA - ######################################
//###########################################################################################
    public static DashboardData mockBuildDashboardData(String name) {
        return DashboardDataImpl.builder()
                .withCode(name)
                .withDescription("someDescription")
                .withConfig("{\"charts\":[{\"_id\":\"1\",\"description\":\"desc\"}]}")
                .withId(5L)
                .withActive(true)
                .build();
    }

    public static List<DashboardData> mockBuildListDashBoardData() {
        DashboardData dashboardData1 = mockBuildDashboardData(TestHelper_Variable.A_KNOWN_DASHBOARD_NAME1);
        DashboardData dashboardData2 = mockBuildDashboardData(TestHelper_Variable.A_KNOWN_DASHBOARD_NAME2);
        DashboardData dashboardData3 = mockBuildDashboardData(TestHelper_Variable.A_KNOWN_DASHBOARD_NAME3);
        return list(dashboardData1, dashboardData2, dashboardData3);
    }

    //###########################################################################################
//################################## - EASYUPLOADITEM - #####################################
//###########################################################################################
    public static EasyuploadItem mockBuildEasyuploadItem() {
        return EasyuploadItemImpl.builder()
                .withPath("exampPath")
                .withFileName("exampFileName")
                .withHash("someHash")
                .withMimeType("exampMimeType")
                .withSize(10)
                .withContent("randomText".getBytes())
                .withDescription("exampDescription")
                .build();
    }

    //###########################################################################################
//######################################## - REPORT - #######################################
//###########################################################################################
    public static ReportConfig mockBuildReportConfig() {
        return ReportConfigImpl.builder()
                .withCode(TestHelper_Variable.A_KNOWN_REPORT_CONFIG_NAME)
                .withFormat(PDF)
                .withParams(map("params", "exampParams"))
                .build();
    }

    public static ReportInfo mockBuildReportInfo(String name) {
        return ReportInfoImpl.builder()
                .withCode(name)
                .withActive(true)
                .build();
    }

    public static List<ReportInfo> mockBuildListReportInfo() {
        ReportInfo reportInfo1 = mockBuildReportInfo(TestHelper_Variable.A_KNOWN_REPORT_INFO_NAME1);
        ReportInfo reportInfo2 = mockBuildReportInfo(TestHelper_Variable.A_KNOWN_REPORT_INFO_NAME2);
        ReportInfo reportInfo3 = mockBuildReportInfo(TestHelper_Variable.A_KNOWN_REPORT_INFO_NAME3);
        return list(reportInfo1, reportInfo2, reportInfo3);
    }

    public static ReportDataImpl mockBuildReportData(Long id) {
        return ReportDataImpl.builder()
                .withId(id)
                .withCode("sample code")
                .withSourceReports(list("rep1", "rep2", "rep3"))
                .build();
    }

    //###########################################################################################
//########################################## - ETL - ########################################
//###########################################################################################
    public static EtlGate mockBuildEtlGate(String code) {
        return EtlGateImpl.builder()
                .withCode(code)
                .withDescription("exampDescription")
                .withAllowPublicAccess(true)
                .withEnabled(true)
                .withProcessingMode(PM_REALTIME)
                .withConfig(
                        map(ETL_GATE_CONFIG_SHOW_ON_CLASSES, TestHelper_Variable.A_KNOWN_CLASS_ID,
                                ETL_HANDLER_CONFIG_TEMPLATES, TestHelper_Variable.A_KNOWN_ETL_TEMPLATE_NAME1)
                )
                .addHandler(mockBuildEtlGateHandler())
                .build();
    }

    public static List<EtlGate> mockBuildListEtlGate() {
        return list(
                mockBuildEtlGate(TestHelper_Variable.A_KNOWN_ETL_GATE_NAME1),
                mockBuildEtlGate(TestHelper_Variable.A_KNOWN_ETL_GATE_NAME2),
                mockBuildEtlGate(TestHelper_Variable.A_KNOWN_ETL_GATE_NAME3));
    }

    public static EtlTemplate mockBuildEtlTemplate(String code, Boolean active) {
        return EtlTemplateImpl.builder()
                .withCode(code)
                .withDescription("exampDescription")
                .withActive(active)
                .build();
    }

    public static List<EtlTemplate> mockBuildListEtlTemplate() {
        return list(
                mockBuildEtlTemplate(TestHelper_Variable.A_KNOWN_ETL_TEMPLATE_NAME1, TestHelper_Variable.ACTIVE),
                mockBuildEtlTemplate(TestHelper_Variable.A_KNOWN_ETL_TEMPLATE_NAME2, TestHelper_Variable.ACTIVE),
                mockBuildEtlTemplate(TestHelper_Variable.A_KNOWN_ETL_TEMPLATE_NAME3, TestHelper_Variable.NOT_ACTIVE)
        );
    }

    public static EtlGateHandler mockBuildEtlGateHandler() {
        return new EtlGateHandlerImpl(map(
                ETL_HANDLER_CONFIG_TYPE, ETLHT_IFC
        ));
    }

    public static EtlProcessingResultErrorImpl mockBuildEtlProcessingResultErrorImpl() {
        return new EtlProcessingResultErrorImpl(
                1L,
                1L,
                emptyMap(),
                "userErrorMessage",
                "techErrorMessage"
        );
    }

    public static EtlProcessingResultDetailsImpl mockBuildEtlProcessingResultDetailsImpl() {
        return new EtlProcessingResultDetailsImpl(emptyList(), emptyList(), emptyList());
    }

    public static EtlProcessingResultImpl mockBuildEtlProcessingResult() {
        DateTimeFormatter formatter
                = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return new EtlProcessingResultImpl(
                ZonedDateTime.parse("2018-12-16T20:28:33.213+05:30", formatter),
                ZonedDateTime.parse("2026-12-16T20:28:33.213+05:30", formatter),
                100L,
                15L,
                85L,
                5L,
                30L,
                list(mockBuildEtlProcessingResultErrorImpl()),
                mockBuildEtlProcessingResultDetailsImpl()
        );
    }

    //###########################################################################################
//################################### - WEBHOOKCONFIG - #####################################
//###########################################################################################
    public static WebhookConfig mockBuildWebhookConfig(String code) {
        return WebhookConfigImpl.builder()
                .withCode(code)
                .withMethod(WebhookMethod.WHM_GET)
                .withUrl("exampUrl")
                .withEvents("exampEvents")
                .withActive(true)
                .withFilter("")
                .build();
    }

    //###########################################################################################
//################################### - GISATTRIBUTE - ######################################
//###########################################################################################
    public static GisAttribute mockBuildGisAttribute(String name, Boolean isPostgis, Boolean active, Boolean withIcon) {
        GisAttributeImpl.GisAttributeImplBuilder builder = GisAttributeImpl.builder()
                .withLayerName(name)
                .withDescription("exampDescription")
                .withActive(active)
                .withType(GisAttributeType.GAT_SHAPE)
                .withIndex(2)
                .withMinimumZoom(1)
                .withDefaultZoom(2)
                .withMaximumZoom(3)
                .withVisibility(map("visibility", true))
                .withOwnerClassName("exampOwnerClass")
                .withBeginDate(ZonedDateTime.now())
                .withConfig(mockBuildGisAttributeConfig());

        if (withIcon) {
            builder.withMapStyle(map("mapStyle", true, "externalGraphic", "exampIcon"));
        } else {
            builder.withMapStyle(map("mapStyle", true));
        }

        if (isPostgis) {
            builder.withType(GAT_POINT);
        }

        return builder.build();
    }

    public static GisAttribute mockBuildGisAttribute_RIP(String name, Boolean isPostgis, Boolean active) {
        GisAttributeImpl.GisAttributeImplBuilder builder = GisAttributeImpl.builder()
                .withLayerName(name)
                .withDescription("exampDescription")
                .withActive(active)
                .withMapStyle(map("mapStyle", true))
                .withType(GisAttributeType.GAT_SHAPE)
                .withIndex(2)
                .withMinimumZoom(1)
                .withDefaultZoom(2)
                .withMaximumZoom(3)
                .withVisibility(map("visibility", true))
                .withOwnerClassName("exampOwnerClass")
                .withBeginDate(ZonedDateTime.now())
                .withConfig(mockBuildGisAttributeConfig());

        if (isPostgis) {
            builder.withType(GAT_POINT);
        }

        return builder.build();
    }

    public static GisAttribute mockBuildInactiveGisAttribute(String name, Boolean isPostgis) {
        GisAttributeImpl.GisAttributeImplBuilder builder = GisAttributeImpl.builder()
                .withLayerName(name)
                .withDescription("exampDescription")
                .withActive(false)
                .withMapStyle(map("mapStyle", true))
                .withType(GisAttributeType.GAT_SHAPE)
                .withIndex(2)
                .withMinimumZoom(1)
                .withDefaultZoom(2)
                .withMaximumZoom(3)
                .withVisibility(map("visibility", true))
                .withOwnerClassName("exampOwnerClass")
                .withBeginDate(ZonedDateTime.now())
                .withConfig(mockBuildGisAttributeConfig());

        if (isPostgis) {
            builder.withType(GAT_POINT);
        }

        return builder.build();
    }

    public static GisAttributeConfig mockBuildGisAttributeConfig() {
        return GisAttributeConfigImpl.builder()
                .withInfoWindowEnabled(true)
                .build();
    }

    //###########################################################################################
//################################# - GEOSERVERLAYER - ######################################
//###########################################################################################
    public static GeoserverLayer mockBuildGeoserverLayer() {
        DateTimeFormatter formatter
                = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return GeoserverLayerImpl.builder()
                .withAttributeName("exampAttributeName")
                .withOwnerClass("exampOwnerClass")
                .withGeoserverStore("exampGeoserverStore")
                .withGeoserverLayer("exampGeoserverLayer")
                .withOwnerCard(3L)
                .withBeginDate(ZonedDateTime.parse("2018-12-16T20:28:33.213+05:30", formatter))
                .withActive(true)
                .withCenter(new CadPoint(2, 3))
                .build();
    }

    //###########################################################################################
//###################################### - NAVTREE - ########################################
//###########################################################################################
    public static NavTree mockBuildNavTree(String name) {
        return NavTreeImpl.builder()
                .withName(name)
                .withDescription("exampDescription")
                .withData(mockBuildNavTreeNode())
                .withActive(true)
                .withType(NT_MENU)
                .build();
    }

    public static List<NavTree> mockBuildListNavTree() {
        return list(
                mockBuildNavTree(TestHelper_Variable.A_KNOWN_NAVTREE_NAME1),
                mockBuildNavTree(TestHelper_Variable.A_KNOWN_NAVTREE_NAME2),
                mockBuildNavTree(TestHelper_Variable.A_KNOWN_NAVTREE_NAME3));
    }

    public static NavTreeNode mockBuildNavTreeNode() {
        return NavTreeNodeImpl.builder()
                .withTargetClassName("exampTargetClassName")
                .withTargetClassDescription("exampTargetClassDescription")
                .withId("exampId")
                .withShowOnlyOne(true)
                .withEnableRecursion(true)
                .withDirection(RD_DIRECT.name())
                .build();
    }

    //###########################################################################################
//######################################## - TASK - #########################################
//###########################################################################################
    public static TaskDefinition mockBuildTaskDefinition() {
        return TaskDefinitionImpl.builder()
                .withId("exampId")
                .withDescription("exampDescription")
                .withInstructions("exampInstructions")
                .withMetadata(mockBuildTaskMetadata())
                .build();
    }

    public static Iterable<TaskMetadata> mockBuildTaskMetadata() {
        TaskMetadata[] taskMetadataArr = new TaskMetadata[1];
        taskMetadataArr[0] = new TaskMetadata("exampName", "exampValue");
        return () -> Arrays.stream(taskMetadataArr).iterator();
    }

    //###########################################################################################
//################################## - CONTEXTMENU - ########################################
//###########################################################################################
    public static ContextMenuItem mockBuildContextMenuItem() {
        return ContextMenuItemImpl.builder()
                .withType(SEPARATOR)
                .withActive(true)
                .build();
    }

    //###########################################################################################
//######################################### - JOB - #########################################
//###########################################################################################
    public static JobData mockBuildJobData(Long id) {
        return JobDataImpl.builder()
                .withId(id)
                .withCode("sampleCode")
                .withType("test")
                .withConfig(map("test", "test"))
                .build();
    }

    public static List<JobData> mockBuildListJobData() {
        return list(
                mockBuildJobData(TestHelper_Variable.A_KNOWN_JOBDATA_ID1),
                mockBuildJobData(TestHelper_Variable.A_KNOWN_JOBDATA_ID2),
                mockBuildJobData(TestHelper_Variable.A_KNOWN_JOBDATA_ID3)
        );
    }

    //###########################################################################################
//######################################## - ROLE - #########################################
//###########################################################################################
    public static Role mockBuildRole(Long id) {
        return RoleImpl.builder()
                .withId(id)
                .withName(TestHelper_Variable.A_KNOWN_ROLE_NAME1)
                .build();
    }

    public static Role mockBuildRole(Long id, String name) {
        return RoleImpl.builder()
                .withId(id)
                .withName(name)
                .build();
    }

    public static List<Role> mockBuildListRole() {
        return list(
                mockBuildRole(TestHelper_Variable.A_KNOWN_ROLE_ID1),
                mockBuildRole(TestHelper_Variable.A_KNOWN_ROLE_ID2),
                mockBuildRole(TestHelper_Variable.A_KNOWN_ROLE_ID3));
    }

    public static List<Role> mockBuildListRoleWithDifferentNames() {
        return list(
                mockBuildRole(TestHelper_Variable.A_KNOWN_ROLE_ID1, TestHelper_Variable.A_KNOWN_ROLE_NAME1),
                mockBuildRole(TestHelper_Variable.A_KNOWN_ROLE_ID2, TestHelper_Variable.A_KNOWN_ROLE_NAME2),
                mockBuildRole(TestHelper_Variable.A_KNOWN_ROLE_ID3, TestHelper_Variable.A_KNOWN_ROLE_NAME3)
        );
    }

    //###########################################################################################
//################################### - GROUPCONFIG - #######################################
//###########################################################################################
    public static GroupConfig mockBuildGroupConfig() {
        return GroupConfigImpl.builder()
                .withProcessWidgetAlwaysEnabled(true)
                .build();
    }

    //###########################################################################################
//##################################### - XPDILNFO - ########################################
//###########################################################################################
    public static XpdlInfo mockBuildXpdlInfo() {
        DateTimeFormatter formatter
                = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        return XpdlInfoImpl.builder()
                .withVersion("exampVersion")
                .withPlanId(TestHelper_Variable.A_KNOWN_XPDL_ID)
                .withProvider("exampProvider")
                .withLastUpdate(ZonedDateTime.parse("2018-12-16T20:28:33.213+05:30", formatter))
                .withDefault(true)
                .build();
    }

    //###########################################################################################
//##################################### - EMIAILTEMPLATE - ##################################
//###########################################################################################
    public static EmailTemplateSerializationHelper mockBuildEmailTemplateSerializationHelper(EmailTemplateService service) {
        ObjectTranslationService translationService = Mockito.mock(ObjectTranslationService.class);
        Mockito.when(translationService.translateEmailTemplateDescription(Matchers.anyString(), Matchers.anyString())).thenReturn("examp_description_translation");
        return new EmailTemplateSerializationHelper(service, translationService);
    }
//###########################################################################################
//##################################### - WSQUERYOPTIONS - ##################################
//###########################################################################################
public static WsQueryOptions mockBuildWsQueryOptions(Boolean detailed) {
    return new WsQueryOptions(
            list("attr1"),
            true,
            "",
            "",
            "",
            10L,
            0L,
            detailed,
            5L,
            true
    );
    }

    public static WsQueryOptions mockBuildWsQueryOptions2() {
        return new WsQueryOptions(
                list("attr1", "attr2"),
                true,
                "",
                "",
                "exampSort",
                10L,
                0L,
                true,
                2L,
                true
        );
    }

//###########################################################################################
//####################################### - DATASOURCE - ####################################
//###########################################################################################

    public static TempDataSource mockBuildTempDataSource() {
        return (TempDataSource) TempDataSource.newInstance()
                .withName(TestHelper_Variable.A_KNOWN_DATASOURCE_NAME)
                .build();
    }

    //###########################################################################################
    //####################################### - FKDOMAIN - ######################################
    //###########################################################################################
    public static FkDomain mockBuildFkDomain() {
        return new FkDomainImpl(mockBuildAttribute_ForeignKeyAttributeType("exampAttributeName", mockBuildClasse("exampClasseName")));
    }

    //###########################################################################################
    //####################################### - GRANTDATA - #####################################
    //###########################################################################################
    public static GrantData mockBuildGrantData(Long grantId, Long roleId) {
        return GrantDataImpl.builder()
                .withId(grantId)
                .withRoleId(roleId)
                .withType(POT_CLASS)
                .withMode(GM_READ)
                .withClassName(TestHelper_Variable.A_KNOWN_CLASS_NAME1)
                .build();
    }

}




/**
 * Duplicated here from module cmdbuild-test-framework to not import all that
 * module only to use this class.
 *
 * @author afelice
 */
class UniqueTestIdUtils {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static int i = 0;

    public static void prepareTuid() {
        i++;
    }

    /**
     * @return test unique id (to prefix names and stuff)
     */
    public static String tuid() {
        return Integer.toString(i, 32);
    }

    /**
     * @param id
     * @return param + test unique id
     */
    public static String tuid(String id) {
        return id + tuid();//StringUtils.capitalizeFirstLetter(tuid());
    }

} // end UniqueTestIdUtils class
