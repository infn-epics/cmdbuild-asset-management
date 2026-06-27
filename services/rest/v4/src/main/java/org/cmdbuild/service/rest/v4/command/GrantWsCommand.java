/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.v4.command;


import org.cmdbuild.auth.grant.GrantData;
import org.cmdbuild.auth.grant.GrantDataRepository;
import org.cmdbuild.auth.grant.GrantService;
import org.cmdbuild.auth.grant.PrivilegedObjectType;
import org.cmdbuild.auth.role.Role;
import org.cmdbuild.auth.role.RoleRepository;
import org.cmdbuild.dao.utils.AttributeFilterProcessor;
import org.cmdbuild.dao.utils.CmFilterUtils;
import org.cmdbuild.data.filter.CmdbFilter;
import org.cmdbuild.data.filter.FilterType;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.cmdbuild.auth.role.RoleType.ADMIN;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.parseEnum;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;

/**
 * @author ldare
 */
@Component
public class GrantWsCommand {

    private final GrantDataRepository repository;
    private final GrantService grantService;
    private final RoleRepository roleRepository;

    public GrantWsCommand(GrantDataRepository repository, GrantService grantService, RoleRepository roleRepository) {
        this.repository = checkNotNull(repository);
        this.grantService = checkNotNull(grantService);
        this.roleRepository = checkNotNull(roleRepository);
    }

    public List<GrantData> doReadMany(String roleId, String filterStr, Boolean includeRecordsWithoutGrant) {
        Role role = roleRepository.getByNameOrId(roleId);
        CmdbFilter filter = CmFilterUtils.parseFilter(filterStr);
        List<GrantData> grants;
        if (includeRecordsWithoutGrant) {
            grants = grantService.getGrantsForRoleIncludeRecordsWithoutGrant(role.getId());
        } else {
            grants = repository.getGrantsForRole(role.getId());
        }
        if (!filter.isNoop()) {
            filter.checkHasOnlySupportedFilterTypes(FilterType.ATTRIBUTE);
            grants = AttributeFilterProcessor.<GrantData>builder()
                    .withKeyToValueFunction((key, grant) -> {
                        return switch (key) {
                            case "objectType" -> serializeEnum(grant.getType());
                            default -> throw new IllegalArgumentException("unsupported filter key = " + key);
                        };
                    }).withFilter(filter.getAttributeFilter())
                    .filter(grants);
        }
        return grants;
    }

    public List<GrantData> doReadOneByObject(String roleId, String objectTypeStr, String objectTypeName) {
        if (equal(roleId, "_ALL")) {
            return list(roleRepository.getAllGroups()).filter(g -> !equal(g.getType(), ADMIN)).map(g -> {
                Role role = roleRepository.getByNameOrId(g.getName());
                PrivilegedObjectType objectType = parseEnum(objectTypeStr, PrivilegedObjectType.class);
                return grantService.getGrantDataByRoleAndTypeAndName(role.getId(), objectType, objectTypeName);
            });
        } else {
            Role role = roleRepository.getByNameOrId(roleId);
            PrivilegedObjectType objectType = parseEnum(objectTypeStr, PrivilegedObjectType.class);
            return list(grantService.getGrantDataByRoleAndTypeAndName(role.getId(), objectType, objectTypeName));
        }
    }
}
