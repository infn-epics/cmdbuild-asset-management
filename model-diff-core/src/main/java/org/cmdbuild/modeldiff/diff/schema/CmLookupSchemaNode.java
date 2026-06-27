/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use CMDBuild according to the license
 */
package org.cmdbuild.modeldiff.diff.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.google.common.base.Strings.emptyToNull;
import java.util.Map;
import org.cmdbuild.lookup.LookupType;
import org.cmdbuild.lookup.LookupValue;
import static org.cmdbuild.modeldiff.core.CmSerializationHelper.ATTR_CODE_SERIALIZATION;
import static org.cmdbuild.modeldiff.core.CmSerializationHelper.ATTR_LOOKUP_VALUES_SERIALIZATION;
import static org.cmdbuild.modeldiff.core.CmSerializationHelper.ATTR_NAME_SERIALIZATION;
import static org.cmdbuild.modeldiff.core.CmSerializationHelper.ATTR_PARENT_SERIALIZATION;
import org.cmdbuild.modeldiff.schema.SchemaLookupConfiguration;
import static org.cmdbuild.utils.lang.CmNullableUtils.isBlank;
import static org.cmdbuild.utils.lang.CmStringUtils.toStringOrNull;

/**
 * <b>Concrete placeholder</b> for visitable (to engage polymorphism and choice
 * of visitor in related repository), wrap for a real <i>schema</i>
 * {@link LookupType} stuff, composed of {@link LookupValue} items.
 *
 * @author afelice
 */
public class CmLookupSchemaNode extends CmSchemaItemDataNode {

    /**
     * Computed because may be overwritten when calculating dependency order.
     */
    String parent;

    public CmLookupSchemaNode(CmSchemaItemAttributesData itemData) {
        super(itemData);

        this.parent = emptyToNull( // From UI can arrive "parent": "", see WsLookupType constructor
                toStringOrNull(getModelObj().getAttributesSerialization().get(ATTR_PARENT_SERIALIZATION))
        );
    }

    /**
     *
     * @return name of {@link LookupType}.
     */
    public String getTypeName() {
        return (String) (getModelObj().getAttributesSerialization().get(ATTR_NAME_SERIALIZATION));
    }

    /**
     *
     * @return name of parent; <code>null</code> if none.
     */
    public String getTypeParent() {
        return parent;
    }

    /**
     * Erase parent when calculating dependency order and this is an already
     * existing, untouched, {@link LookupType}: it don't concur to dependency calculation.
     */
    public void eraseTypeParent() {
        this.parent = null;
    }

    public boolean isInherited() {
        return isBlank(getModelObj().getAttributesSerialization().get(ATTR_PARENT_SERIALIZATION));
    }

    /**
     * Used converting from {@link GeneratedDiffSchema_Lookup} to a
     * {@link CmLookupSchemaNode}.
     *
     * @param props
     * @return
     */
    static public String fetchName(Map<String, Object> props) {
        return (String) props.get(ATTR_NAME_SERIALIZATION);
    }

    /**
     * Used converting from {@link GeneratedDiffSchema_Lookup} to a
     * {@link CmLookupSchemaNode}.
     *
     * @param props
     * @return
     */
    static public String fetchCode(Map<String, Object> props) {
        return (String) props.get(ATTR_CODE_SERIALIZATION);
    }

    /**
     * Used converting from {@link GeneratedDiffSchema_Lookup} to a
     * {@link CmLookupSchemaNode}.
     *
     * @param props
     * @return
     */
    static public String fetchTypeParent(Map<String, Object> props) {
        return toStringOrNull(props.get(ATTR_PARENT_SERIALIZATION));
    }

    /**
     * Used converting from {@link GeneratedDiffSchema_Lookup} to a
     * {@link CmLookupSchemaNode}.
     *
     * @param props
     * @return
     */
    static public String fetchValueCode(Map<String, Object> props) {
        return (String) props.get(ATTR_CODE_SERIALIZATION);
    }


    /**
     * Node used for <i>topological sorting</i>, coming from <i>diff</i>.
     *
     * @param lookupType
     * @return
     */
    static public CmLookupSchemaNode buildLookupSchemaNode(SchemaLookupConfiguration lookupType, ObjectMapper objectMapper) {
        CmLookupSchemaNode result = new CmLookupSchemaNode(
                CmSchemaItemAttributesData.from(lookupType.getName(),
                        fetchTypeCmdbSerialization(lookupType, objectMapper) // LookupType properties
                )
        );

        lookupType.getValues().forEach(valueConfiguration
                -> result.addComponent(
                        new CmSchemaItemAttributesDataNode(
                                CmSchemaItemAttributesData.from(valueConfiguration.getCode(), getCmdbSerialization(valueConfiguration, objectMapper)) // LookupValue properties
                        )
                )
        );

        return result;
    }

    static private Map<String, Object> fetchTypeCmdbSerialization(SchemaLookupConfiguration lookupType, ObjectMapper objectMapper) {
        Map<String, Object> cmdbSerialization = getCmdbSerialization(lookupType, objectMapper);
        cmdbSerialization.remove(ATTR_LOOKUP_VALUES_SERIALIZATION);

        return cmdbSerialization;
    }

    static private Map<String, Object> getCmdbSerialization(Object obj, ObjectMapper objectMapper) {
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }


}
