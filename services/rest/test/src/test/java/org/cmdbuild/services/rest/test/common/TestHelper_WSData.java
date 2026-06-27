/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.services.rest.test.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.cmdbuild.auth.multitenant.config.MultitenantConfiguration;
import org.cmdbuild.auth.role.RoleRepository;
import org.cmdbuild.auth.user.OperationUserSupplier;
import org.cmdbuild.auth.user.UserRepository;
import org.cmdbuild.bim.BimService;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.config.CoreConfiguration;
import org.cmdbuild.config.MultitenantConfigurationImpl;
import org.cmdbuild.config.UiConfiguration;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dao.entrytype.AttributeGroupData;
import org.cmdbuild.dao.entrytype.AttributeGroupInfo;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.dao.entrytype.EntryType;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.dao.DocumentInfoRepository;
import org.cmdbuild.easyupload.EasyuploadService;
import org.cmdbuild.etl.webhook.WebhookMethod;
import org.cmdbuild.formstructure.FormStructureService;
import org.cmdbuild.lookup.LookupService;
import org.cmdbuild.service.rest.common.serializationhelpers.*;
import org.cmdbuild.service.rest.common.serializationhelpers.card.CardWsSerializationHelperv3;
import org.cmdbuild.service.rest.v4.model.*;
import org.cmdbuild.services.serialization.attribute.file.CardAttributeFileHelper;
import org.cmdbuild.services.serialization.widget.WidgetHelper;
import org.cmdbuild.translation.ObjectTranslationService;
import org.cmdbuild.uicomponents.contextmenu.ContextMenuComponentService;
import org.cmdbuild.utils.json.CmJsonUtils;
import org.cmdbuild.utils.lang.CmCollectionUtils;
import org.cmdbuild.widget.WidgetService;
import org.cmdbuild.widget.model.WidgetData;
import org.cmdbuild.workflow.WorkflowConfiguration;
import org.cmdbuild.workflow.WorkflowService;
import org.cmdbuild.workflow.dao.ExtendedRiverPlanRepository;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.cmdbuild.etl.gate.inner.EtlProcessingMode.PM_REALTIME;
import static org.cmdbuild.etl.loader.EtlMergeMode.EM_LEAVE_MISSING;
import static org.cmdbuild.etl.loader.EtlTemplateColumnMode.ETCM_DEFAULT;
import static org.cmdbuild.etl.loader.EtlTemplateConfig.EnableCreate.EC_TRUE;
import static org.cmdbuild.etl.loader.EtlTemplateTarget.ET_CLASS;
import static org.cmdbuild.etl.loader.EtlTemplateType.ETT_IMPORT;
import static org.cmdbuild.gis.GisAttributeType.GAT_POINT;
import static org.cmdbuild.navtree.NavTreeType.NT_MENU;
import static org.cmdbuild.report.ReportFormat.PDF;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/**
 *
 * @author schursin
 */
public class TestHelper_WSData {

    public static WsEmailSignatureData mockBuildWsEmailSignatureData() {
        return new WsEmailSignatureData(
                true, "exampCode", "exampDescription", "someContent_html"
        );
    }

    public static WsEmailAccountData mockBuildWsEmailAccountData() {
        return new WsEmailAccountData(
                "exampName", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }

    public static DomainSerializationHelper.WsDomainData mockBuildWsDomainData() {
        List<String> disabledSourceDescendants = list("disabledSourceDescendants");
        List<String> disabledDestinationDescendants = list("disabledDestinationDescendants");
        List<String> masterDetailAggregateAttrs = list("masterDetailAggregateAttrs");
        List<String> masterDetailDisabledCreateAttrs = list("masterDetailDisabledCreateAttrs");
        return new DomainSerializationHelper.WsDomainData(
                "exampSource", "exampName", "exampDescription", "exampDestination", "1:1", "exampDescriptionDirect",
                "exampDescriptionInverse", 2, 2, "exampDescriptionMasterDetail", "exampFilterMasterDetail",
                disabledSourceDescendants, disabledDestinationDescendants,
                masterDetailAggregateAttrs, masterDetailDisabledCreateAttrs,
                true, true, true, true, true, true, true, true, null,
                null, true, true, "exampSourceFilter",
                "exampTargetFilter"
        );
    }

    public static WsFilterData mockBuildWsFilterData() {
        return new WsFilterData(
                "UpdateFilterName",
                "UpdateDescription",
                "target",
                "{configuration}",
                true,
                false,
                null
        );
    }

    public static WsCoreComponentData mockBuildWsCoreComponentData() {
        return new WsCoreComponentData(
                "exampleCode",
                "This is a description",
                "Some data",
                true
        );
    }

    public static ClassSerializationHelper.WsClassData mockBuildWsClassData() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
                {
                  "name": "myName"
                }
                """;
        return mapper.readValue(json, ClassSerializationHelper.WsClassData.class);
    }

    public static WsAttributeData mockBuildWsAttributeData() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = """
                {
                  "formatPattern": "dd.MM.yyyy",
                  "unitOfMeasure": "kg",
                  "type": "string",
                  "name": "attrId",
                  "description": "Product weight"
                }
                """;
        return mapper.readValue(json, WsAttributeData.class);
    }

    public static WsAttributeData mockBuildWsAttributeData(String attributeName) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> attributeMap = map(
                "formatPattern", "dd.MM.yyyy",
                "unitOfMeasure", "kg",
                "type", "string",
                "name", attributeName,
                "description", "Product weight"
        );
        String json = CmJsonUtils.toJson(attributeMap);
        return mapper.readValue(json, WsAttributeData.class);
    }

    public static WsEmailTemplateData mockBuildWsEmailTemplateData() {
        return new WsEmailTemplateData(
                "exampName",
                "exampDescription",
                32L,
                "exampFrom",
                "exampTo",
                "exampCc",
                "exampBcc",
                "exampSubject",
                "exampContentType",
                "exampBody",
                222L,
                99L,
                true,
                true,
                "exampProvider",
                true,
                "exampShowOnClasses",
                map("data", "exampData"),
                list(mockBuildWsReportConfigData())
        );
    }

    public static WsReportConfigData mockBuildWsReportConfigData() {
        return new WsReportConfigData(
                "exampCode", PDF.name(), map("params", "exampFormat")
        );
    }

    //TODO delete after base test class
    public static CardWsSerializationHelperv3 mockBuildCardWsSerializationHelperv3() {
        DaoService dao = mock(DaoService.class);
        ObjectTranslationService translationService = mock(ObjectTranslationService.class);
        ClassSerializationHelper classSerializationHelper = mockBuildClassSerializationHelper();
        AttributeTypeConversionService attributeTypeConversionService = mock(AttributeTypeConversionService.class);
        WidgetService widgetService = mock(WidgetService.class);
        UserRepository userRepository = mock(UserRepository.class);
        CardAttributeFileHelper cardAttributeFile_helper = mockBuildCardAttributeFileHelper();
        WidgetHelper widgets_helper = mock(WidgetHelper.class);
        return new CardWsSerializationHelperv3(
                dao, translationService, classSerializationHelper,
                attributeTypeConversionService, widgetService,
                userRepository, cardAttributeFile_helper, widgets_helper
        );
    }

    //TODO delete after base test class
    public static CardAttributeFileHelper mockBuildCardAttributeFileHelper() {
        DaoService dao = mock(DaoService.class);
        DmsService dmsService = mock(DmsService.class);
        DocumentInfoRepository repository = mock(DocumentInfoRepository.class);
        UserClassService userClassService = mock(UserClassService.class);
        ObjectTranslationService translationService = mock(ObjectTranslationService.class);
        UserRepository userRepository = mock(UserRepository.class);
        return new CardAttributeFileHelper(
                dao, dmsService, repository,
                userClassService, translationService,
                userRepository, new PermissionsHandlerProxyImpl());
    }

    public static WsLayerData mockBuildWsLayerData() {
        return new WsLayerData(true);
    }

    public static WsTreeData mockBuildWsTreeData() {
        return new WsTreeData("exampName", "exampDescription", list(mockBuildWsTreeNodeData()), true, NT_MENU.name());
    }

    public static WsTreeNodeData mockBuildWsTreeNodeData() {
        Map<String, Object> nodeMap = map(
                "_id", 25,
                "targetClass", "exampTargetClass",
                "description", "exampDescription",
                "nodes", list()
        );
        return new WsTreeNodeData(nodeMap);
    }

    //TODO delete after base test class
    public static ClassSerializationHelper mockBuildClassSerializationHelper() {
        WidgetService mockWidgetService = mock(WidgetService.class);
        Mockito.when(mockWidgetService.widgetDataToWidget(any(WidgetData.class), any(Classe.class))).thenReturn(TestHelper_Model.mockBuildWidget());
        ObjectTranslationService mockTranslationService = mock(ObjectTranslationService.class);
        Mockito.when(mockTranslationService.translateViewAttributeGroupDescription(any(AttributeGroupData.class))).thenReturn("_description_translation");
        Mockito.when(mockTranslationService.translateAttributeGroupDescription(any(EntryType.class), any(AttributeGroupInfo.class))).thenReturn("_description_translation");
        Mockito.when(mockTranslationService.translateClassDescription(any(Classe.class))).thenReturn("examp_description_translation");
        Mockito.when(mockTranslationService.translateClassDescriptionPlural(any(Classe.class))).thenReturn("examp_description_plural_translation");
        BimService mockBimService = mock(BimService.class);
        EasyuploadService mockEasyuploadService = mock(EasyuploadService.class);
        Mockito.when(mockEasyuploadService.getByPathOrNull(Matchers.anyString())).thenReturn(TestHelper_Model.mockBuildEasyuploadItem());
        UserClassService mockClassService = mock(UserClassService.class);
        Mockito.when(mockClassService.getUserClass(Matchers.anyString())).thenReturn(TestHelper_Model.mockBuildClasse(TestHelper_Variable.A_KNOWN_CLASS_ID));
        MultitenantConfiguration mockMultitenantConfiguration = new MultitenantConfigurationImpl();
        WorkflowConfiguration mockWorkflowConfiguration = mock(WorkflowConfiguration.class);
        CoreConfiguration mockCoreConfiguration = mock(CoreConfiguration.class);
        UiConfiguration mockUiConfiguration = mock(UiConfiguration.class);
        ContextMenuSerializationHelper mockContextMenuSerializationHelper = mockBuildContextMenuSerializationHelper();
        DmsConfiguration mockDmsConfig = mock(DmsConfiguration.class);
        Mockito.when(mockDmsConfig.getDefaultDmsCategory()).thenReturn("AlfrescoCategory");
        LookupService mockLookupService = mock(LookupService.class);
        Mockito.when(mockLookupService.getAllLookup(Matchers.anyString())).thenReturn(TestHelper_Model.mockBuildPagedElementsLookupValue());
        OperationUserSupplier mockOperationUser = mock(OperationUserSupplier.class);
        Mockito.when(mockOperationUser.getCurrentGroup()).thenReturn("admin");
        RoleRepository mockRoleRepository = mock(RoleRepository.class);
        Mockito.when(mockRoleRepository.getByNameOrIdOrNull(Matchers.anyString())).thenReturn(TestHelper_Model.mockBuildRole(1L));
        return new ClassSerializationHelper(mockWidgetService, mockTranslationService, mockBimService, mockEasyuploadService, mockClassService, mockMultitenantConfiguration, mockWorkflowConfiguration, mockCoreConfiguration, mockUiConfiguration, mockContextMenuSerializationHelper, mockLookupService, mockDmsConfig, mockOperationUser, mockRoleRepository);
    }

    //TODO delete after base test class
    public static ContextMenuSerializationHelper mockBuildContextMenuSerializationHelper() {
        ContextMenuComponentService contextMenuComponentService = mock(ContextMenuComponentService.class);
        ObjectTranslationService translationService = mock(ObjectTranslationService.class);
        return new ContextMenuSerializationHelper(contextMenuComponentService, translationService);
    }

    //TODO delete after base test class
    public static ProcessWsSerializationHelper mockBuildProcessWsSerializationHelper() {
        WidgetService mockWidgetService = mock(WidgetService.class);
        AttributeTypeConversionService attributeDetailService = mock(AttributeTypeConversionService.class);
        ExtendedRiverPlanRepository planRepository = mock(ExtendedRiverPlanRepository.class);
        FormStructureService formStructureService = mock(FormStructureService.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        UserClassService classeService = mock(UserClassService.class);
        Mockito.when(classeService.getUserClass(Matchers.anyString())).thenReturn(TestHelper_Model.mockBuildClasse(TestHelper_Variable.A_KNOWN_CLASS_ID));
        ObjectTranslationService translationService = mock(ObjectTranslationService.class);
        WorkflowService workflowService = mock(WorkflowService.class);
        Mockito.when(workflowService.getTaskDefinitions(Matchers.anyString())).thenReturn(CmCollectionUtils.list(TestHelper_Model.mockBuildTaskDefinition()));
        WorkflowConfiguration workflowConfiguration = mock(WorkflowConfiguration.class);
        return new ProcessWsSerializationHelper(mockBuildCardWsSerializationHelperv3(), attributeDetailService, mockWidgetService, planRepository, workflowService, formStructureService, classeService, mockBuildClassSerializationHelper(), translationService, roleRepository, workflowConfiguration);
    }

    //TODO delete after base test class
    public static DomainSerializationHelper mockBuildDomainSerializationHelper() {
        DaoService dao = mock(DaoService.class);
        Mockito.when(dao.getClasse(Matchers.anyString())).thenReturn(TestHelper_Model.mockBuildClasse(0L, "classeName"));
        ObjectTranslationService translationService = mock(ObjectTranslationService.class);
        Mockito.when(translationService.translateDomainDirectDescription(Matchers.anyString(), Matchers.anyString())).thenReturn("examp_descriptionDirect_translation");
        Mockito.when(translationService.translateDomainInverseDescription(Matchers.anyString(), Matchers.anyString())).thenReturn("examp_descriptionInverse_translation");
        Mockito.when(translationService.translateDomainMasterDetailDescription(Matchers.anyString(), Matchers.anyString())).thenReturn("examp_descriptionMasterDetail_translation");
        return new DomainSerializationHelper(dao, translationService);
    }

    public static WsDashboardData mockBuildWsDashboardData() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode charts = mapper.createArrayNode();
        ObjectNode chart = mapper.createObjectNode();
        chart.put("_id", "1");
        chart.put("description", "desc");
        chart.put("_description_translation", (String) null);
        charts.add(chart);
        return new WsDashboardData("someCode", "someDescription", true, charts, null);
    }

    public static WsImportExportGateData mockBuildWsImportExportGateData() {
        return new WsImportExportGateData(
                TestHelper_Variable.A_KNOWN_ETL_GATE_NAME1,
                "exampleDescription",
                PM_REALTIME.toString(),
                true,
                true,
                emptyMap(),
                emptyList()
        );
    }

    public static WsEtlColumnData mockBuildWsEtlColumnData() {
        return new WsEtlColumnData(
                TestHelper_Variable.A_KNOWN_ATTR_NAME1,
                "columnName",
                "",
                "",
                "",
                "",
                "",
                "",
                ETCM_DEFAULT.toString()
        );
    }

    public static WsEtlTemplateData mockBuildWsEtlTemplateData() {
        return new WsEtlTemplateData(
                "",
                "",
                "",
                "",
                "",
                "wsEtlTemplateDataCode",
                "",
                "targetName",
                ET_CLASS.toString(),
                "",
                "",
                "",
                EM_LEAVE_MISSING.toString(),
                "",
                "",
                true,
                EC_TRUE.toString(),
                ETT_IMPORT.toString(),
                true,
                true,
                0,
                1,
                0,
                "",
                ",",
                CmCollectionUtils.list(TestHelper_Variable.A_KNOWN_ATTR_NAME1),
                null,
                list(mockBuildWsEtlColumnData()),
                "",
                "",
                "",
                "",
                "",
                true,
                null
        );
    }

    public static WsEtlWebhookData mockBuildWsEtlWebhookData() {
        return new WsEtlWebhookData(
                "exampCode",
                "exampDescription",
                "event",
                "target",
                WebhookMethod.WHM_GET.toString(),
                "url",
                null,
                null,
                "language",
                TestHelper_Variable.ACTIVE
        );
    }

    public static WsGeoAttributeData mockbuildWsGeoAttributeData(String name) {
        return new WsGeoAttributeData(
                null,
                name,
                "exampDescription",
                TestHelper_Variable.ACTIVE,
                GAT_POINT.toString(),
                null,
                0,
                0,
                0,
                0,
                map("visibility", true),
                emptyMap(),
                true,
                "",
                ""
        );
    }

}
