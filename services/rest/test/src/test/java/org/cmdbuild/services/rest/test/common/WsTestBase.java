/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.services.rest.test.common;


import org.cmdbuild.asyncjob.AsyncRequestJob;
import org.cmdbuild.asyncjob.AsyncRequestJobService;
import org.cmdbuild.auth.grant.GrantDataRepository;
import org.cmdbuild.auth.grant.GrantService;
import org.cmdbuild.auth.multitenant.config.MultitenantConfiguration;
import org.cmdbuild.auth.role.RoleRepository;
import org.cmdbuild.auth.user.OperationUserSupplier;
import org.cmdbuild.auth.user.UserRepository;
import org.cmdbuild.auth.userrole.UserRoleRepository;
import org.cmdbuild.bim.BimService;
import org.cmdbuild.calendar.CalendarService;
import org.cmdbuild.cardfilter.CardFilterService;
import org.cmdbuild.classe.access.UserCardService;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.classe.access.UserDomainService;
import org.cmdbuild.config.CoreConfiguration;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.config.UiConfiguration;
import org.cmdbuild.contextmenu.ContextMenuService;
import org.cmdbuild.corecomponents.CoreComponentService;
import org.cmdbuild.dao.core.q3.DaoService;
import org.cmdbuild.dao.driver.repository.ClasseRepository;
import org.cmdbuild.dao.driver.repository.DomainRepository;
import org.cmdbuild.dao.driver.repository.FkDomainRepository;
import org.cmdbuild.dashboard.DashboardService;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.dao.DocumentInfoRepository;
import org.cmdbuild.easyupload.EasyuploadService;
import org.cmdbuild.email.EmailAccountService;
import org.cmdbuild.email.EmailSignatureService;
import org.cmdbuild.email.template.EmailTemplateService;
import org.cmdbuild.etl.config.WaterwayDescriptorService;
import org.cmdbuild.etl.gate.EtlGateService;
import org.cmdbuild.etl.loader.EtlTemplateInlineProcessorService;
import org.cmdbuild.etl.loader.EtlTemplateService;
import org.cmdbuild.etl.webhook.WebhookService;
import org.cmdbuild.formstructure.FormStructureService;
import org.cmdbuild.gis.GisService;
import org.cmdbuild.jobs.JobService;
import org.cmdbuild.lookup.LookupService;
import org.cmdbuild.navtree.NavTreeService;
import org.cmdbuild.participant.ParticipantService;
import org.cmdbuild.report.ReportService;
import org.cmdbuild.report.SysReportService;
import org.cmdbuild.service.rest.common.helpers.CardsForDomainFetcher;
import org.cmdbuild.service.rest.common.serializationhelpers.*;
import org.cmdbuild.service.rest.common.serializationhelpers.card.CardWsSerializationHelperv3;
import org.cmdbuild.services.serialization.EmailTemplateSerializationHelper;
import org.cmdbuild.services.serialization.attribute.file.CardAttributeFileHelper;
import org.cmdbuild.services.serialization.widget.WidgetHelper;
import org.cmdbuild.temp.TempService;
import org.cmdbuild.translation.ObjectTranslationService;
import org.cmdbuild.translation.TranslationService;
import org.cmdbuild.uicomponents.contextmenu.ContextMenuComponentService;
import org.cmdbuild.uicomponents.custompage.CustomPageService;
import org.cmdbuild.uicomponents.data.UiComponentRepository;
import org.cmdbuild.uicomponents.widget.WidgetComponentService;
import org.cmdbuild.view.ViewService;
import org.cmdbuild.widget.WidgetService;
import org.cmdbuild.workflow.WorkflowConfiguration;
import org.cmdbuild.workflow.WorkflowService;
import org.cmdbuild.workflow.dao.ExtendedRiverPlanRepository;

import static org.mockito.Mockito.mock;

/**
 * @author ldare
 */
public class WsTestBase {

    protected final WorkflowConfiguration workflowConfiguration = mock(WorkflowConfiguration.class);
    protected final MultitenantConfiguration multitenantConfiguration = mock(MultitenantConfiguration.class);
    protected final CoreConfiguration coreConfiguration = mock(CoreConfiguration.class);
    protected final UiConfiguration uiConfiguration = mock(UiConfiguration.class);
    protected final DmsConfiguration dmsConfiguration = mock(DmsConfiguration.class);
    protected final EmailConfiguration emailConfiguration = mock(EmailConfiguration.class);

    protected final WorkflowService workflowService = mock(WorkflowService.class);
    protected final UserClassService userClassService = mock(UserClassService.class);
    protected final UserCardService userCardService = mock(UserCardService.class);
    protected final WidgetService widgetService = mock(WidgetService.class);
    protected final FormStructureService formStructureService = mock(FormStructureService.class);
    protected final ObjectTranslationService objectTranslationService = mock(ObjectTranslationService.class);
    protected final ContextMenuComponentService contextMenuComponentService = mock(ContextMenuComponentService.class);
    protected final BimService bimService = mock(BimService.class);
    protected final EasyuploadService easyuploadService = mock(EasyuploadService.class);
    protected final LookupService lookupService = mock(LookupService.class);
    protected final DaoService daoService = mock(DaoService.class);
    protected final UserDomainService userDomainService = mock(UserDomainService.class);
    protected final DmsService dmsService = mock(DmsService.class);
    protected final ParticipantService participantService = mock(ParticipantService.class);
    protected final EmailTemplateService emailTemplateService = mock(EmailTemplateService.class);
    protected final CalendarService calendarService = mock(CalendarService.class);
    protected final AsyncRequestJobService asyncRequestJobService = mock(AsyncRequestJobService.class);
    protected final AsyncRequestJob asyncRequestJob = mock(AsyncRequestJob.class);
    protected final CardFilterService cardFilterService = mock(CardFilterService.class);
    protected final CoreComponentService coreComponentService = mock(CoreComponentService.class);
    protected final CustomPageService customPageService = mock(CustomPageService.class);
    protected final DashboardService dashboardService = mock(DashboardService.class);
    protected final SysReportService sysReportService = mock(SysReportService.class);
    protected final UserDomainService domainService = mock(UserDomainService.class);
    protected final EmailAccountService emailAccountService = mock(EmailAccountService.class);
    protected final EmailSignatureService emailSignatureService = mock(EmailSignatureService.class);
    protected final ReportService reportService = mock(ReportService.class);
    protected final EtlGateService etlGateService = mock(EtlGateService.class);
    protected final EtlTemplateService etlTemplateService = mock(EtlTemplateService.class);
    protected final EtlTemplateInlineProcessorService etlTemplateInlineProcessorService = mock(EtlTemplateInlineProcessorService.class);
    protected final CardsForDomainFetcher cardsForDomainFetcher = mock(CardsForDomainFetcher.class);
    protected final TempService tempService = mock(TempService.class);
    protected final WebhookService webhookService = mock(WebhookService.class);
    protected final GisService gisService = mock(GisService.class);
    protected final TranslationService translationService = mock(TranslationService.class);
    protected final NavTreeService navTreeService = mock(NavTreeService.class);
    protected final ViewService viewService = mock(ViewService.class);
    protected final ContextMenuService contextMenuService = mock(ContextMenuService.class);
    protected final WidgetComponentService widgetComponentService = mock(WidgetComponentService.class);
    protected final JobService jobService = mock(JobService.class);
    protected final GrantService grantService = mock(GrantService.class);

    protected final ClasseRepository classeRepository = mock(ClasseRepository.class);
    protected final FkDomainRepository fkDomainRepository = mock(FkDomainRepository.class);
    protected final RoleRepository roleRepository = mock(RoleRepository.class);
    protected final ExtendedRiverPlanRepository extendedRiverPlanRepository = mock(ExtendedRiverPlanRepository.class);
    protected final DocumentInfoRepository documentInfoRepository = mock(DocumentInfoRepository.class);
    protected final UserRepository userRepository = mock(UserRepository.class);
    protected final UserRoleRepository userRoleRepository = mock(UserRoleRepository.class);
    protected final DomainRepository domainRepository = mock(DomainRepository.class);
    protected final UiComponentRepository uiComponentRepository = mock(UiComponentRepository.class);
    protected final GrantDataRepository grantDataRepository = mock(GrantDataRepository.class);
    protected final WaterwayDescriptorService waterwayDescriptorService = mock(WaterwayDescriptorService.class);


    protected final OperationUserSupplier operationUserSupplier = mock(OperationUserSupplier.class);
    protected final WidgetHelper widgetHelper = mock(WidgetHelper.class);

    public DomainSerializationHelper mockBuildDomainSerializationHelper() {
        return new DomainSerializationHelper(daoService, objectTranslationService);
    }

    public CardWsSerializationHelperv3 mockBuildCardWsSerializationHelperv3() {
        ClassSerializationHelper classSerializationHelper = mockBuildClassSerializationHelper();
        AttributeTypeConversionService attributeTypeConversionService = mockBuildAttributeTypeConversionService();
        CardAttributeFileHelper cardAttributeFileHelper = mockBuildCardAttributeFileHelper();
        return new CardWsSerializationHelperv3(
                daoService, objectTranslationService, classSerializationHelper,
                attributeTypeConversionService, widgetService,
                userRepository, cardAttributeFileHelper, widgetHelper
        );
    }

    public ReportSerializationHelper mockBuildReportSerializationHelper() {
        return new ReportSerializationHelper(objectTranslationService, reportService);
    }

    public EmailTemplateSerializationHelper mockBuildEmailTemplateSerializationHelper() {
        return new EmailTemplateSerializationHelper(emailTemplateService, objectTranslationService);
    }

    public CardAttributeFileHelper mockBuildCardAttributeFileHelper() {
        return new CardAttributeFileHelper(daoService, dmsService, documentInfoRepository, userClassService, objectTranslationService, userRepository, new PermissionsHandlerProxyImpl());
    }

    public LookupSerializationHelper mockBuildLookupSerializationHelper() {
        return new LookupSerializationHelper(objectTranslationService, lookupService);
    }

    public CalendarWsSerializationHelper mockBuildCalendarWsSerializationHelper() {
        LookupSerializationHelper lookupSerializationHelper = mockBuildLookupSerializationHelper();
        return new CalendarWsSerializationHelper(lookupSerializationHelper, objectTranslationService, daoService, participantService, emailTemplateService, calendarService);
    }

    public AttributeTypeConversionService mockBuildAttributeTypeConversionService() {
        CalendarWsSerializationHelper calendarWsSerializationHelper = mockBuildCalendarWsSerializationHelper();
        return new AttributeTypeConversionService(daoService, objectTranslationService, userClassService, userDomainService, calendarWsSerializationHelper, lookupService, dmsService);
    }

    public ContextMenuSerializationHelper mockBuildContextMenuSerializationHelper() {
        return new ContextMenuSerializationHelper(contextMenuComponentService, objectTranslationService);
    }

    public ClassSerializationHelper mockBuildClassSerializationHelper() {
        ContextMenuSerializationHelper contextMenuSerializationHelper = mockBuildContextMenuSerializationHelper();
        return new ClassSerializationHelper(widgetService, objectTranslationService, bimService, easyuploadService, userClassService, multitenantConfiguration, workflowConfiguration, coreConfiguration, uiConfiguration, contextMenuSerializationHelper, lookupService, dmsConfiguration, operationUserSupplier, roleRepository);
    }

    public ProcessWsSerializationHelper mockBuildProcessWsSerializationHelper() {
        CardWsSerializationHelperv3 cardWsSerializationHelperv3 = mockBuildCardWsSerializationHelperv3();
        AttributeTypeConversionService attributeTypeConversionService = mockBuildAttributeTypeConversionService();
        ClassSerializationHelper classSerializationHelper = mockBuildClassSerializationHelper();
        return new ProcessWsSerializationHelper(cardWsSerializationHelperv3, attributeTypeConversionService, widgetService, extendedRiverPlanRepository, workflowService, formStructureService, userClassService, classSerializationHelper, objectTranslationService, roleRepository, workflowConfiguration);
    }

    public ViewSerializer mockBuildViewSerializer() {
        ContextMenuSerializationHelper contextMenuSerializationHelper = mockBuildContextMenuSerializationHelper();
        return new ViewSerializer(viewService, objectTranslationService, roleRepository, formStructureService, operationUserSupplier, contextMenuService, contextMenuSerializationHelper, uiConfiguration);
    }
}
