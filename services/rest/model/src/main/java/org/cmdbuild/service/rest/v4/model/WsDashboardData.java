/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.cmdbuild.dashboard.inner.DashboardDataImpl;

import static org.cmdbuild.utils.json.CmJsonUtils.toJson;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 *
 * @author schursin
 */
public class WsDashboardData {

    private final String name, description;
    private final Boolean active;
    private final Object config;

    public WsDashboardData(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("active") Boolean active,
            @JsonProperty("charts") Object charts,
            @JsonProperty("layout") Object layout) {
        this.name = name;
        this.description = description;
        this.active = active;
        this.config = map("charts", charts, "layout", layout);//TODO improve this
    }

    public DashboardDataImpl.DashboardDataImplBuilder toDashboard() {
        return DashboardDataImpl.builder()
                .withCode(name)
                .withDescription(description)
                .withActive(active)
                .withConfig(toJson(config));
    }
}
