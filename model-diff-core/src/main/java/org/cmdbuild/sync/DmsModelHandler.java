/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use CMDBuild according to the license
 */
package org.cmdbuild.sync;

import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.cmdbuild.classe.ExtendedClass;
import org.cmdbuild.classe.ExtendedClassImpl;
import org.cmdbuild.classe.access.UserClassService;
import static org.cmdbuild.classe.access.UserClassService.ClassQueryFeatures.CQ_INCLUDE_LOOKUP_VALUES;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.lookup.LookupType;
import org.cmdbuild.lookup.LookupValue;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmMapUtils.map;

/**
 * Contains data for:
 * <ol>
 * <li><i>dms model</i> (a special type of {@link Classe}) stuff;
 * <li><i>dms category</i> (a special type of {@link LookupType}) stuff.
 * </ol>
 *
 * @author afelice
 */
public class DmsModelHandler {

    protected static final String ATTRIBUTE_SERIALIZATION_DMS_CATEGORY_FIELDNAME = "dmsCategory";
    protected static final String ATTRIBUTE_SERIALIZATION_DMS_MODEL_FIELDNAME = "dmsModel";

    private final String defaultDmsCategoryName;

    private final DmsSync dmsSync;
    private final UserClassService classService;

    /**
     * Filled by {@link SchemaCollector}.
     */
    private final Map<String, ExtendedClass> dmsModelColl = map();


    /**
     * <dl>
     * <dt>key
     * <dd><i>dms category</i> {@link LookupType} name;
     * <dt>value
     * <dd>all <i>dms category</i> {@link LookupValue} values.
     * </dl>
     */
    private final Map<String, List<LookupValue>> allCategoriesValues = map();

    public DmsModelHandler(DmsSync dmsSync, UserClassService classService) {
        this.dmsSync = dmsSync;
        this.defaultDmsCategoryName = dmsSync.readDefaultDmsCategory();
        this.classService = classService;
    }

    /**
     * Used by <i>mobile offline</i> <code>ModelCollector</code> when a <i>DMS model</i> found.
     * 
     * @param dmsModel <i>Dms model</i> {@link Classe}.
     */
    public void addDmsModel(ExtendedClass dmsModel) {
        final String dmsModelName = dmsModel.getClasse().getName();
        dmsModelColl.put(dmsModelName, dmsModel);        
    }

    public boolean hasDmsModel(String dmsModelName) {
        return dmsModelColl.containsKey(dmsModelName);
    }

    /**
     * Used by <i>mobile offline</i> <code>ModelCollector</code> to fetch, for a 
     * given {@link Classe}:
     * <ol>
     * <li>the related <i>dms category</i> name (a {@link LookupType} name);
     * <li>for each <i>dms category</i> {@link LookupValue}, the related <i>DMS Model</i>.
     * </ol>
     * 
     * @param classe 
     * @return the {@link Classe} <i>dms category</i> name; <code>null</code> if
     *   no <i>dms category</i> set for the given {@link Classe} and <b>no default
     *   dms categgry</b> set in system. 
     * 
     */
    public String fetchDmsModels(Classe classe) {
        if (!classe.hasDmsCategory() && defaultDmsCategoryName == null) {
            // No default Dms category found, but trying to handle a Classe without Dms cagory set. 
            //Skip
            return null;
        } 
        
        Pair<String, List<LookupValue>> dmsCategory = dmsSync.readDmsCategoryValues(classe, defaultDmsCategoryName);
        
        String dmsCategoryTypeName = dmsCategory.getKey();
        List<LookupValue> allCategoryValues = dmsCategory.getValue();
        addLookupValues(dmsCategoryTypeName, allCategoryValues);
        allCategoryValues.stream().forEach( lv -> {
            // For each LookupValue, fetch related dms model
            String dmsModelName = lv.getDmsModelClass();
            

            // Manca    .withLookupValuesByAttr(allCategoriesValues) che veniva chiamata in buildExtendedClass(). Perché allCategoriesValues? Forse da prendere la serializzazione globale di "lookupValues" direttamente da allCategoriesValues invece che da ExtendedClass salvata dentro dmsModelColl?
            // o LookupValues per un DmsModel è ora vuoto perchè sono i LookupValues dei Dms category a puntare ai DmsModels, e nei dms model del 
            // test non ci sono LookupValue come attributi, quindi giusto che sia vuoto.
            
            // Fetch ExtendedClass, that contains formTriggers and other stuff
            if (!dmsModelColl.containsKey(dmsModelName)) {   
                // @tbc AFE prima si usavano allCategoryValues inseriti come LookupValues di questa extendedDmsModel, ma questo è errato:
                // - è un LookupValue dui una dms category che punta al DmsModel;
                // - se si sta serializzando un DmsModel, ha la property "lookupValues" valorizzata alla lista 
                //   delle LookupValue che contiene, se ha uno più attributi di tipo LOOKUP, ossia nella 
                //   "descrizione" di quel tipo di documenti, vi è uno devi valori che è una classica lookup.
                ExtendedClass extendedDmsModel = classService.getExtendedClass(dmsModelName, CQ_INCLUDE_LOOKUP_VALUES);
                dmsModelColl.put(dmsModelName, extendedDmsModel);
            }
        });
     
        return dmsCategoryTypeName;
    }

    public String getDefaultDmsCategory() {
        return defaultDmsCategoryName;
    }

    public List<ExtendedClass> getDmsModels() {
        return list(dmsModelColl.values());
    }

    /**
     *
     * @return <dl>
     * <dt>key
     * <dd><i>dms category</i> {@link LookupType} name;
     * <dt>value
     * <dd>all <i>dms category</i> {@link LookupValue} values.
     * </dl>
     */
    public Map<String, List<LookupValue>> getDmsCategoriesLookups() {
        return allCategoriesValues;
    }

    public void addLookupValues(String dmsCategoryType, List<LookupValue> allCategoryValues) {
        allCategoriesValues.put(dmsCategoryType, allCategoryValues);
    }

    private ExtendedClass buildExtendedClass(Classe dmsModel) {
        ExtendedClass extendedDmsModel = ExtendedClassImpl.builder()
                .withClasse(dmsModel)
                .withLookupValuesByAttr(allCategoriesValues)
                .withFormTriggers(emptyList())
                .withContextMenuItems(emptyList())
                .withWidgets(emptyList())
                .withCalendarTriggers(emptyList())
                .build();
        return extendedDmsModel;
    }

}
