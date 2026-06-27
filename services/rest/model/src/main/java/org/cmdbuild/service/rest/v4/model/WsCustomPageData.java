/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.cmdbuild.service.rest.v4.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author schursin
 */
public class WsCustomPageData {

    private final String description;

    public String getDescription() {
        return description;
    }

    public boolean isIsActive() {
        return isActive;
    }

    private final boolean isActive;

    public WsCustomPageData(
            @JsonProperty("description") String description,
            @JsonProperty("active") boolean active) {
        this.description = description;
        this.isActive = active;
    }

}
