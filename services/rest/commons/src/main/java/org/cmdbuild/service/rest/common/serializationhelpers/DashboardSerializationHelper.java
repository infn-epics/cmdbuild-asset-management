/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.common.serializationhelpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.cmdbuild.common.utils.PagedElements;
import static org.cmdbuild.common.utils.PagedElements.paged;
import org.cmdbuild.dashboard.DashboardData;
import static org.cmdbuild.dashboard.utils.DashboardUtils.getCqlExprsInOrder;
import org.cmdbuild.ecql.EcqlBindingInfo;
import org.cmdbuild.ecql.inner.EcqlExpressionImpl;
import org.cmdbuild.ecql.utils.EcqlUtils;
import org.cmdbuild.translation.ObjectTranslationService;
import static org.cmdbuild.utils.json.CmJsonUtils.fromJson;
import static org.cmdbuild.utils.lang.CmCollectionUtils.stream;
import org.cmdbuild.utils.lang.CmMapUtils.FluentMap;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import org.cmdbuild.utils.lang.CmStringUtils;

/**
 *
 * @author ldare
 */
public class DashboardSerializationHelper {

    private final ObjectTranslationService objectTranslationService;

    public DashboardSerializationHelper(ObjectTranslationService objectTranslationService) {
        this.objectTranslationService = objectTranslationService;
    }

    public FluentMap<String, Object> serializeDetailedDashboard(DashboardData dashboardData) {
        return serializeBasicDashboard(dashboardData).with(
                "charts", serializeCharts(dashboardData),
                "layout", fromJson(dashboardData.getConfig(), Map.class).get("layout")//TODO improve this
        );
    }

    public FluentMap<String, Object> serializeBasicDashboard(DashboardData dashboard) {
        return map(
                "_id", dashboard.getId(),
                "name", dashboard.getCode(),
                "description", dashboard.getDescription(),
                "_description_translation", objectTranslationService.translateDashboardDescription(dashboard.getCode(), dashboard.getDescription()),
                "active", dashboard.isActive()
        );
    }

    public PagedElements applySerializationToListDashboardData(List<DashboardData> listDashboardData, Boolean detailed, Integer limit, Integer offset) {
        return paged(listDashboardData, offset, limit).map(detailed ? this::serializeDetailedDashboard : this::serializeBasicDashboard);
    }

    private JsonNode serializeCharts(DashboardData dashboardData) { //note: this MUST match the order in DashboardUtils.getCqlExprsInOrder for ecql filter processing !!
        ListIterator<String> cqlExprsInOrder = getCqlExprsInOrder(dashboardData).listIterator();
        JsonNode charts = fromJson(dashboardData.getConfig(), JsonNode.class).get("charts");
        stream(charts.elements()).map(ObjectNode.class::cast).forEach(chart -> {
            if (chart.hasNonNull("description")) {
                chart.put("_description_translation",
                        objectTranslationService.translateDashboardChartDescription(dashboardData.getCode(), chart.get("_id").asText(), chart.get("description").asText()));
            }
            if (chart.hasNonNull("valueAxisLabel")) {
                chart.put("_valueAxisLabel_translation",
                        objectTranslationService.translateDashboardChartValueAxisLabel(dashboardData.getCode(), chart.get("_id").asText(), chart.get("valueAxisLabel").asText()));
            }
            if (chart.hasNonNull("categoryAxisLabel")) {
                chart.put("_categoryAxisLabel_translation",
                        objectTranslationService.translateDashboardChartCategoryAxisLabel(dashboardData.getCode(), chart.get("_id").asText(), chart.get("categoryAxisLabel").asText()));
            }
            if (chart.hasNonNull("labelField")) {
                chart.put("_labelField_translation",
                        objectTranslationService.translateDashboardChartLabelField(dashboardData.getCode(), chart.get("_id").asText(), chart.get("labelField").asText()));
            }
            if (chart.hasNonNull("dataSourceFilter") && !chart.get("dataSourceFilter").asText().isBlank()) {
                chart.set("ecqlDataSourceFilter", new ObjectMapper().valueToTree(createEcqlFilterData(dashboardData.getCode(), cqlExprsInOrder)));
            }
            if (chart.hasNonNull("dataSourceParameters")) {
                ArrayNode dataSourceParameters = (ArrayNode) chart.get("dataSourceParameters");
                int i = 0;
                for (JsonNode dataSourceParameter : dataSourceParameters) {
                    String name = Optional.ofNullable(dataSourceParameter.get("name")).map(JsonNode::textValue).orElse(null);
                    if (isNotBlank(name)) {
                        String description = Optional.ofNullable(dataSourceParameter.get("description")).map(JsonNode::textValue).map(CmStringUtils::emptyToNull).orElse(name);
                        ((ObjectNode) dataSourceParameter).put("_description_translation",
                                objectTranslationService.translateDashboardChartDataSourceParameter(dashboardData.getCode(), chart.get("_id").asText(), Integer.toString(i++), description));
                    }
                    if (dataSourceParameter.hasNonNull("filter")) {
                        if (dataSourceParameter.get("filter").hasNonNull("expression")) {
                            if (!dataSourceParameter.get("filter").get("expression").asText().isBlank()) {
                                ObjectMapper mapper = new ObjectMapper();
                                ((ObjectNode) dataSourceParameter).set("ecqlFilter", mapper.valueToTree(createEcqlFilterData(dashboardData.getCode(), cqlExprsInOrder)));
                            }
                        }
                    }
                }
            }
        });
        return charts;
    }

    private Map<String, Object> createEcqlFilterData(String dashboardCode, ListIterator<String> cqlExprsInOrder) {
        EcqlBindingInfo ecqlBindingInfo = EcqlUtils.getEcqlBindingInfoForExpr(new EcqlExpressionImpl(cqlExprsInOrder.next()));
        String ecqlId = EcqlUtils.buildDashboardEcqlId(dashboardCode, cqlExprsInOrder.previousIndex());
        Map<String, Object> ecqlData = map("id", ecqlId, "bindings", map("server", ecqlBindingInfo.getServerBindings(), "client", ecqlBindingInfo.getClientBindings()));
        return ecqlData;
    }
}
