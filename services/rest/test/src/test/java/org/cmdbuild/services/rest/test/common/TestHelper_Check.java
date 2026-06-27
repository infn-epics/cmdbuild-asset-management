/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.services.rest.test.common;

import org.junit.Assert;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author ldare
 */
public class TestHelper_Check {

    public static void checkSuccess(Object resultObject) {
        Assert.assertTrue((boolean) ((Map) resultObject).get("success"));
    }

    public static <T> void checkId(T expId, Object resultObject) {
        checkSuccess(resultObject);
        Map result = (Map) ((Map) resultObject).get("data");
        Assert.assertEquals(expId, result.get("_id"));
    }

    public static void checkListIds(List<?> expListIds, Object resultObject) {
        checkSuccess(resultObject);
        List<Map> resultList = (List<Map>) ((Map) resultObject).get("data");
        Assert.assertEquals(
                expListIds,
                resultList.stream().map(a -> a.get("_id")).collect(toList())
        );
    }

    public static void checkName(String expName, Object resultObject) {
        checkSuccess(resultObject);
        Map result = (Map) ((Map) resultObject).get("data");
        Assert.assertEquals(expName, result.get("name"));
    }

    public static void checkListNames(List<String> expListNames, Object resultObject) {
        checkSuccess(resultObject);
        List<Map> resultList = (List<Map>) ((Map) resultObject).get("data");
        Assert.assertEquals(
                expListNames,
                resultList.stream().map(a -> a.get("name")).collect(toList())
        );
    }

    public static void checkCode(String expCode, Object resultObject) {
        checkSuccess(resultObject);
        Map result = (Map) ((Map) resultObject).get("data");
        Assert.assertEquals(expCode, result.get("code"));
    }

    public static void checkListCodes(List<String> expListCodes, Object resultObject) {
        checkSuccess(resultObject);
        List<Map> resultList = (List<Map>) ((Map) resultObject).get("data");
        Assert.assertEquals(
                expListCodes,
                resultList.stream().map(a -> a.get("code")).collect(toList())
        );
    }

    public static void checkListEtlGateTemplates(List<String> expTemplateNames, Object resultObject) {
        ((List<Map<String, Object>>) ((Map<String, Object>) resultObject).get("data"))
                .forEach(dataItem -> {
                    List<String> actualCodes = ((List<Map<String, Object>>) dataItem.get("_templates"))
                            .stream()
                            .map(t -> (String) t.get("code"))
                            .toList();

                    Assert.assertEquals(expTemplateNames, actualCodes);
                });
    }

    public static void checkEtlGateTemplates(List<String> expTemplateNames, Object resultObject) {
        List<Map<String, Object>> listTemplateSerializations = ((List<Map<String, Object>>) ((Map<String, Object>) ((Map<String, Object>) resultObject).get("data")).get("_templates"));

        Assert.assertEquals(
                expTemplateNames,
                listTemplateSerializations.stream().map(a -> a.get("code")).collect(toList())
        );
    }

    public static void checkNoTemplatesOnListEtlGate(Object resultObject) {
        ((List<Map<String, Object>>) ((Map<String, Object>) resultObject).get("data"))
                .forEach(etlGateSerialization -> Assert.assertNull(etlGateSerialization.get("_templates"))
                );
    }

    public static void checkNoTemplatesOnEtlGate(Object resultObject) {
        Assert.assertNull(((Map<String, Object>) ((Map<String, Object>) resultObject).get("data")).get("_templates"));
    }

    public static void checkDetailedList(Object resultObject, String paramToCheck) {
        ((List<Map<String, Object>>) ((Map<String, Object>) resultObject).get("data"))
                .forEach(m -> Assert.assertNotNull(m.get(paramToCheck)));
    }

    public static void checkDetailed(Object resultObject, String paramToCheck) {
        Assert.assertNotNull(((Map<String, Object>) ((Map<String, Object>) resultObject).get("data")).get(paramToCheck));
    }

    public static void checkNotDetailedList(Object resultObject, String paramToCheck) {
        ((List<Map<String, Object>>) ((Map<String, Object>) resultObject).get("data"))
                .forEach(m -> Assert.assertNull(m.get(paramToCheck)));
    }

    public static void checkNotDetailed(Object resultObject, String paramToCheck) {
        Assert.assertNull(((Map<String, Object>) ((Map<String, Object>) resultObject).get("data")).get(paramToCheck));
    }

    public static <T> void checkResult(T expValue, Object resultObject, String paramToCheck) {
        checkSuccess(resultObject);
        Map<String, Object> result = (Map<String, Object>) ((Map<String, Object>) resultObject).get("data");
        Assert.assertEquals(expValue, result.get(paramToCheck));
    }

    public static <T> void checkResultList(List<T> expValues, Object resultObject, String paramToCheck) {
        checkSuccess(resultObject);

        List<Map> resultList = (List<Map>) ((Map) resultObject).get("data");

        Assert.assertEquals(
                expValues,
                resultList.stream()
                        .map(a -> (T) a.get(paramToCheck))
                        .collect(toList())
        );
    }

    /**
     * Asserts that a REST-like response object indicates success and contains
     * the expected card serialization data.
     * <p>
     * This method checks that the {@code result} object (typically a
     * {@link Map}) has a {@code "success"} key set to {@code true} and that its
     * {@code "data"} key matches the expected card serialization map.
     * </p>
     *
     * @param result               the response object to check, expected to be a {@link Map}
     *                             with "success" and "data" keys
     * @param expCardSerialization the expected card serialization map to
     *                             compare against the "data" value
     * @throws AssertionError if the response does not indicate success or the
     *                        data does not match
     */
    public static void checkResponse(Object result, Map<String, Object> expCardSerialization) {
        Assert.assertTrue((boolean) ((Map) result).get("success"));
        Assert.assertEquals(expCardSerialization, ((Map) result).get("data"));
    }

    /**
     * Asserts that a REST-like response object indicates success and contains
     * the expected list of data.
     * <p>
     * This method checks that the {@code resultObj} (typically a {@link Map})
     * has a {@code "success"} key set to {@code true} and that its
     * {@code "data"} key matches the provided {@code list}.
     * </p>
     *
     * @param list      the expected list of data to compare against the "data" value
     *                  in the response
     * @param resultObj the response object to check, expected to be a
     *                  {@link Map} with "success" and "data" keys
     * @param <T>       the type of elements in the expected list
     * @throws AssertionError if the response does not indicate success or the
     *                        data does not match the expected list
     */
    public static <T> void checkResponse_List(List<T> list, Object resultObj) {
        //assert:
        Assert.assertTrue((boolean) ((Map) resultObj).get("success"));
        Assert.assertEquals(list, ((Map) resultObj).get("data"));
    }

    public static void checkResultSize(Object result, int expectedSize) {
        Map<String, Object> responseMap = (Map<String, Object>) result;
        List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
        Assert.assertEquals(expectedSize, data.size());
    }
}
