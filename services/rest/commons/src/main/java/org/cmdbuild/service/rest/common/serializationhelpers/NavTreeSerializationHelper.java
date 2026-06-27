/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.service.rest.common.serializationhelpers;


import com.google.common.base.Joiner;
import org.cmdbuild.ecql.EcqlBindingInfo;
import org.cmdbuild.ecql.inner.EcqlExpressionImpl;
import org.cmdbuild.ecql.utils.EcqlUtils;
import org.cmdbuild.navtree.NavTree;
import org.cmdbuild.navtree.NavTreeNode;
import org.cmdbuild.translation.ObjectTranslationService;
import org.cmdbuild.utils.lang.CmMapUtils;

import java.util.List;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.utils.lang.CmConvertUtils.serializeEnum;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 * @author ldare
 */
public class NavTreeSerializationHelper {

    public enum TreeMode {
        FLAT, TREE
    }

    public static CmMapUtils.FluentMap serializeTree(NavTree root, TreeMode mode, ObjectTranslationService translationService) {
        List nodes = switch (mode) {
            case FLAT ->
                    root.getData().getThisNodeAndAllDescendants().stream().map(n -> serializeNode(root, n, translationService)).collect(toList());
            case TREE -> singletonList(serializeNodeAndDescendants(root, root.getData(), translationService));
            default -> throw new IllegalArgumentException();
        };
        return map(
                "_id", root.getName(),
                "name", root.getName(),
                "description", root.getDescription(),
                "_description_translation", translationService.translateNavtreeDescription(root.getName(), root.getDescription()),
                "active", root.getActive(),
                "type", serializeEnum(root.getType()),
                "nodes", nodes
        );
    }

    public static CmMapUtils.FluentMap serializeNodeAndDescendants(NavTree tree, NavTreeNode node, ObjectTranslationService translationService) {
        return serializeNode(tree, node, translationService).with("nodes", node.getChildNodes().stream().map(n -> serializeNodeAndDescendants(tree, n, translationService)).collect(toList()));
    }

    public static CmMapUtils.FluentMap serializeNode(NavTree tree, NavTreeNode node, ObjectTranslationService translationService) {
        return map(
                "_id", node.getId(),
                "filter", node.getTargetFilter(),
                "targetClass", node.getTargetClassName(),
                "recursionEnabled", node.getEnableRecursion(),
                "domain", node.getDomainName(),
                "showOnlyOne", node.getShowOnlyOne(),
                "subclassViewMode", serializeEnum(node.getSubclassViewMode()),
                "subclassViewShowIntermediateNodes", node.getSubclassViewShowIntermediateNodes(),
                "description", node.getTargetClassDescription(),
                "_description_translation", translationService.translateNavtreeItemDescription(tree.getName(), node.getId(), node.getTargetClassDescription())
        ).accept(m -> {
            if (node.hasFilter()) {
                EcqlBindingInfo ecqlBindingInfo = EcqlUtils.getEcqlBindingInfoForExpr(new EcqlExpressionImpl(node.getTargetFilter()));
                m.put("ecqlFilter", map(
                        "id", EcqlUtils.buildNavTreeEcqlId(tree.getName(), node.getId()),
                        "bindings", map("server", ecqlBindingInfo.getServerBindings(), "client", ecqlBindingInfo.getClientBindings())
                ));
            }
            node.getSubclassDescriptions().forEach((k, v) -> {
                m.put(format("subclass_%s_description", k), nullToEmpty(v));
                m.put(format("_subclass_%s_description_translation", k), translationService.translateNavtreeItemSubclassDescription(tree.getName(), node.getId(), k, nullToEmpty(v)));
            });
        }).skipNullValues().with(
                "parent", node.getParentId(),
                "direction", isBlank(node.getDomainName()) ? null : (node.getDirect() ? "_1" : "_2"),
                "subclassFilter", emptyToNull(Joiner.on(",").join(node.getSubclassFilter()))
        ).then();
    }

    public static CmMapUtils.FluentMap serializeTreeSimple(NavTree tree, ObjectTranslationService translationService) {
        return map(
                "_id", tree.getName(),
                "description", tree.getDescription(),
                "_description_translation", translationService.translateNavtreeDescription(tree.getName(), tree.getDescription()),
                "active", tree.getActive(),
                "type", serializeEnum(tree.getType()));
    }
}
