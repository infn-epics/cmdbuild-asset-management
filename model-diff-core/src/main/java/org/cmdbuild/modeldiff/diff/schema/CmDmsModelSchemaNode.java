/*
 * CMDBuild has been developed and is managed by PAT srl
 You can use CMDBuild according to the license
 */
package org.cmdbuild.modeldiff.diff.schema;

import org.cmdbuild.dao.entrytype.Attribute;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.modeldiff.schema.ClasseConfiguration;

/**
 * <b>Concrete placeholder</b> for visitable (to engage polymorphism and choice
 * of visitor in related repository), wrap for a real <i>schema</i>
 * <i>dms model</i> (a special type of {@link Classe}) stuff, composed of
 * {@link Attribute} items.
 *
 * @author afelice
 */
public class CmDmsModelSchemaNode extends CmClasseSchemaNode {

    public CmDmsModelSchemaNode(CmSchemaItemAttributesData itemData) {
        super(itemData);
    }

    /**
     * Node used for <i>topological sorting</i>.
     *
     * @param dmsModelClasse
     * @return
     */
    // @todo AFE TBC
    static public CmDmsModelSchemaNode buildDmsModelSchemaNode(ClasseConfiguration dmsModelClasse) {
        CmDmsModelSchemaNode result = new CmDmsModelSchemaNode(
                CmSchemaItemAttributesData.from(dmsModelClasse.name,
                        dmsModelClasse.getCmdbSerialization() // DMS Model properties
                )
        );

        dmsModelClasse.attributes.forEach(attribConfiguration
                -> result.addComponent(
                        new CmSchemaItemAttributesDataNode(
                                CmSchemaItemAttributesData.from(attribConfiguration.getName(), attribConfiguration.getCmdbSerialization()) // Attribute properties
                        ))
        );

        return result;
    }
}
