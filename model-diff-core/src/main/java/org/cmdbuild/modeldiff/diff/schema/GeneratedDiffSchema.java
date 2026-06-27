/*
 * CMDBuild has been developed and is managed by PAT srl
 You can use CMDBuild according to the license
 */
package org.cmdbuild.modeldiff.diff.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.lookup.LookupType;
import org.cmdbuild.modeldiff.core.CmSerializationHelper;
import static org.cmdbuild.modeldiff.core.CmSerializationHelper.ATTR_NAME_SERIALIZATION;
import static org.cmdbuild.modeldiff.diff.schema.CmSchemaHelper.fetchDefaultOrder;
import static org.cmdbuild.modeldiff.diff.schema.CmSchemaHelper.hasDefaultOrder;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmMapUtils.mapOf;

/**
 * Represents a <i>diff</i> on modified <i>schema</i>.
 *
 * @author afelice
 */
public class GeneratedDiffSchema {

    public String name;
    public String description;

    public List<GeneratedDiffSchema_Classe> insertedClasses = list();
    public List<GeneratedDiffSchema_Classe> removedClasses = list();
    public List<GeneratedDiffSchema_ChangedClasse> changedClasses = list();

    public List<GeneratedDiffSchema_Process> insertedProcesses = list();
    public List<GeneratedDiffSchema_Process> removedProcesses = list();
    public List<GeneratedDiffSchema_ChangedProcess> changedProcesses = list();

    public List<GeneratedDiffSchema_Domain> insertedDomains = list();
    public List<GeneratedDiffSchema_Domain> removedDomains = list();
    public List<GeneratedDiffSchema_ChangedDomain> changedDomains = list();

    public List<GeneratedDiffSchema_Lookup> insertedLookups = list();
    public List<GeneratedDiffSchema_Lookup> removedLookups = list();
    public List<GeneratedDiffSchema_ChangedLookup> changedLookups = list();

    public List<GeneratedDiffSchema_DmsModel> insertedDmsModels = list();
    public List<GeneratedDiffSchema_DmsModel> removedDmsModels = list();
    public List<GeneratedDiffSchema_ChangedDmsModel> changedDmsModels = list();

    public List<GeneratedDiffSchema_Lookup> insertedDmsCategories = list();
    public List<GeneratedDiffSchema_Lookup> removedDmsCategories = list();
    public List<GeneratedDiffSchema_ChangedLookup> changedDmsCategories = list();

    @JsonProperty("contextMenuItems")
    public GeneratedDiffSchema_ContextMenuItems changedContextMenuItems = new GeneratedDiffSchema_ContextMenuItems();

    public String xpdlZipFile = null;

    /**
     * Used internally by <i>apply schema diff</i> on {@link Classe} algorithm.
     */
    @JsonIgnore
    public List<String> classeInsertOrder = list();

    /**
     * Used internally by <i>apply schema diff</i> on {@link Process} algorithm.
     */
    @JsonIgnore
    public List<String> processInsertOrder = list();

    public boolean hasInsertedClasse(String curClasseName) {
        return findInserted(insertedClasses, curClasseName).isPresent();
    }

    public GeneratedDiffSchema_Classe getInsertedClasse(String curClasseName) {
        return findInserted(insertedClasses, curClasseName).get();
    }

    public synchronized boolean hasChangedClasse(String curClasseName) {
        return findChanged(changedClasses, curClasseName).isPresent();
    }

    public synchronized boolean hasChangedDomain(String curDomainName) {
        return findChanged(changedDomains, curDomainName).isPresent();
    }

    public synchronized GeneratedDiffSchema_ChangedClasse getChangedClasse(String curClasseName) {
        return findChanged(changedClasses, curClasseName).get();
    }

    public synchronized GeneratedDiffSchema_ChangedDomain getChangedDomain(String curDomainName) {
        return findChanged(changedDomains, curDomainName).get();
    }

    public GeneratedDiffSchema_Lookup getInsertedLookupType(String curLookupTypeName) {
        return findInserted(insertedLookups, curLookupTypeName).get();
    }

    public synchronized boolean hasChangedLookup(String curLookupName) {
        return findChanged(changedLookups, curLookupName).isPresent();
    }

    public synchronized GeneratedDiffSchema_ChangedLookup getChangedLookup(String curLookupName) {
        return findChanged(changedLookups, curLookupName).get();
    }

    public synchronized GeneratedDiffSchema_ChangedItemAttributes getChangedJSComponent(String curComponentId) {
        return changedContextMenuItems.components.getChangedAttributes().stream().filter(c -> curComponentId.equals(c.name)).findAny().get();
    }

    private <T extends GeneratedDiffSchema_SchemaItem> Optional<T> findInserted(List<T> insertedItems, String curItemName) {
        return insertedItems.stream().filter(c -> curItemName.equals(fetchName(c.getItemProperties()))).findAny();
    }

    private <T extends GeneratedDiffSchema_ChangedItem> Optional<T> findChanged(List<T> changedItems, String curItemName) {
        return changedItems.stream().filter(c -> curItemName.equals(c.getItemProps().getName())).findAny();
    }

    /**
     * Used when updating a parent {@link Classe} before a derived one: once
     * done, remove it to avoid processing this change again later on.
     *
     * @param curClasseName
     * @return
     */
    public synchronized GeneratedDiffSchema_ChangedClasse popChangedClasse(String curClasseName) {
        // Remove and return (pop) from changed classes
        return popChangedItem(changedClasses, curClasseName);
    }

    /**
     * Used when updating a parent {@link LookupType} before a derived one: once
     * done, remove it to avoid processing this change again later on.
     *
     * @param curLookupTypeName
     * @return
     */
    public synchronized GeneratedDiffSchema_ChangedLookup popChangedLookup(String curLookupTypeName) {
        // Remove and return (pop) from changed lookups
        return popChangedItem(changedLookups, curLookupTypeName);
    }

    public boolean hasInsertedProcess(String curProcessName) {
        return findInserted(insertedProcesses, curProcessName).isPresent();
    }

    public GeneratedDiffSchema_Process getInsertedProcess(String curProcessName) {
        return findInserted(insertedProcesses, curProcessName).get();
    }

    public synchronized boolean hasChangedProcess(String curProcessName) {
        return findChanged(changedProcesses, curProcessName).isPresent();
    }

    public synchronized GeneratedDiffSchema_ChangedProcess getChangedProcess(String curProcessName) {
        return findChanged(changedProcesses, curProcessName).get();
    }

    /**
     * Used when updating a parent {@link Process} before a derived one: once
     * done, remove it to avoid processing this change again later on.
     *
     * @param curProcessName
     * @return
     */
    public synchronized GeneratedDiffSchema_ChangedProcess popChangedProcess(String curProcessName) {
        // Remove and return (pop) from changed processes
        return popChangedItem(changedProcesses, curProcessName);
    }

    /**
     *
     * @return a map with
     * <dl>
     * <dt>key</dt>
     * <dd>the {@link Classe} name.
     * <dt>value</dt>
     * <dd>the <code>defaultOrder</code>
     * </dl>
     */
    @JsonIgnore
    public Map<String, List> getChangedDefaultOrderColl() {
        Map<String, List> result = mapOf(String.class, List.class);

        result.putAll(insertedClasses.stream()
                .filter(c -> hasDefaultOrder(c.getItemProperties()) && !(fetchDefaultOrder(c.getItemProperties())).isEmpty()) // While inserting a new Classe, a defaultOrder of [] means "nothing to do"
                .collect(Collectors.toMap(
                        GeneratedDiffSchema_Classe::getName,
                        c -> fetchDefaultOrder(c.getItemProperties())
                )
                ));
        result.putAll(changedClasses.stream()
                .filter(c -> hasDefaultOrder(c.getItemProps()))
                .collect(Collectors.toMap(
                        GeneratedDiffSchema_ChangedClasse::getName,
                        c -> fetchDefaultOrder(c.getItemProps())
                )
                ));
        return result;
    }

    /**
     * Workaround to fix <code>defaultOrder</code> with duplicated pairs coming
     * from unit-tests.
     */
    public void distinctDefaultOrder() {
        insertedClasses.stream()
                .filter(c -> hasDefaultOrder(c.getItemProperties()))
                .forEach(c -> {
                    List<Map> foundDefaultOrder = fetchDefaultOrder(c.getItemProperties());
                    List<Map> distinctDefaultOrder = foundDefaultOrder.stream().distinct().collect(Collectors.toList());
                    c.getItemProperties().put(CmSerializationHelper.ATTR_CLASSE_DEFAULT_ORDER_SERIALIZATION, distinctDefaultOrder);
                });
    }

    public boolean hasInsertedDmsModels(String curDmsModelName) {
        return findInserted(insertedDmsModels, curDmsModelName).isPresent();
    }

    public GeneratedDiffSchema_DmsModel getInsertedDmsModels(String curDmsModelName) {
        return findInserted(insertedDmsModels, curDmsModelName).get();
    }

    public synchronized boolean hasChangedDmsModels(String curDmsModelName) {
        return findChanged(changedDmsModels, curDmsModelName).isPresent();
    }

    public synchronized GeneratedDiffSchema_ChangedDmsModel getChangedDmsModels(String curDmsModelName) {
        return findChanged(changedDmsModels, curDmsModelName).get();
    }

    /**
     * Used when updating a parent <i>dms model</i> (a {@link Classe}) before a
     * derived one: once done, remove it to avoid processing this change again
     * later on.
     *
     * @param curDmsModelName
     * @return
     */
    public synchronized GeneratedDiffSchema_ChangedDmsModel popChangedDmsModels(String curDmsModelName) {
        // Remove and return (pop) from changed dms model
        return popChangedItem(changedDmsModels, curDmsModelName);
    }

    public GeneratedDiffSchema_Lookup getInsertedDmsCategory(String curDmsCategoryName) {
        return findInserted(insertedDmsCategories, curDmsCategoryName).get();
    }

    public synchronized boolean hasChangedDmsCategory(String curDmsCategoryName) {
        return findChanged(changedDmsCategories, curDmsCategoryName).isPresent();
    }

    public synchronized GeneratedDiffSchema_ChangedLookup getChangedDmsCategory(String curDmsCategoryName) {
        return findChanged(changedDmsCategories, curDmsCategoryName).get();
    }

    /**
     * Used when updating a parent <i>dms category</i> (a {@link LookupType})
     * before a derived one: once done, remove it to avoid processing this
     * change again later on.
     *
     * @param curDmsCategoryName
     * @return
     */
    public synchronized GeneratedDiffSchema_ChangedLookup popChangedDmsCategory(String curDmsCategoryName) {
        // Remove and return (pop) from changed dms categories
        return popChangedItem(changedDmsCategories, curDmsCategoryName);
    }

    /**
     * Used when updating a parent {@link Classe} before a derived one: once
     * done, remove it to avoid processing this change again later on.
     *
     * @param curClasseName
     * @return
     */
    private synchronized <T extends GeneratedDiffSchema_ChangedItem> T popChangedItem(List<T> changedItems, String curItemName) {
        // Return removing from changed items
        T result = null;

        int curPos = 0;
        for (; curPos < changedItems.size(); curPos++) {
            if (changedItems.get(curPos).getName().equals(curItemName)) {
                result = changedItems.get(curPos);
                changedItems.remove(curPos);
                break; // Esci dal loop dopo la riomozione
            }
        }

        return result;
    }

    private String fetchName(Map<String, Object> props) {
        return (String) props.get(ATTR_NAME_SERIALIZATION);
    }

    // @todo AFE non dovrebbe servire, con lo schema, visto che il file di diff non è deserializzato in più parti
//    /**
//     * Used if adding incrementally {@link Classe} deserialized <i>diff data</i>.
//     *
//     * @param curClasseDiffSchema
//     * @throws IOException if found <i>diff</i> name is mismatching.
//     */
//    public void add(GeneratedDiffSchema curClasseDiffSchema) throws IOException {
//        checkDataset(curClasseDiffSchema);
//
//        insertedClasses = addComponentData(curClasseDiffSchema, insertedCards, curClasseDiffSchema.insertedCards);
//        removedClasses = addComponentData(curClasseDiffSchema, removedCards, curClasseDiffSchema.removedCards);
//        changedClasses = addComponentData(curClasseDiffSchema, changedCards, curClasseDiffSchema.changedCards);
//    }
//
//    /**
//     * Removes changes that are now empty for some sanitizing operation (like removing
//     * newly to add document metadata.
//     */
//    public void removeEmptyChanges() {
//        changedCards = list(changedCards).without(c -> c.changedAttribs.isEmpty() && c.addedAttribs.isEmpty());
//    }
//
//    protected List addComponentData(GeneratedDiffSchema curClasseData, List thisComponentData, List otherComponentData) {
//        List result = thisComponentData;
//        if (result == null) {
//            result = list();
//        }
//
//        if (otherComponentData != null) {
//            result.addAll(otherComponentData);
//        }
//
//        return result;
//    }
//
//    protected void checkDataset(GeneratedDiffSchema curClasseData) throws IOException {
//        if (CmNullableUtils.isBlank(name)) {
//            this.name = curClasseData.name;
//        } else {
//            if (!this.name.equals(curClasseData.name)) {
//                throw new IOException("mismatching related dataset, found =< %s > but expected =< %s >".formatted(curClasseData.name, this.name));
//            }
//        }
//    }
}
