/*
 * CMDBuild has been developed and is managed by PAT srl
 * You can use, distribute, edit CMDBuild according to the license
 */
package org.cmdbuild.plugin.config.utils;

import static java.lang.String.format;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cmdbuild.plugin.config.api.PluginConfigComponent;
import org.cmdbuild.plugin.config.api.PluginConfigValue;
import static org.cmdbuild.utils.lang.CmCollectionUtils.list;
import static org.cmdbuild.utils.lang.CmConvertUtils.getEnumParamsFromType;
import static org.cmdbuild.utils.lang.CmExecutorUtils.safe;
import static org.cmdbuild.utils.lang.CmMapUtils.map;
import static org.cmdbuild.utils.lang.CmMapUtils.mapOf;
import static org.cmdbuild.utils.lang.CmMapUtils.toMap;
import static org.cmdbuild.utils.lang.CmPreconditions.checkArgument;
import static org.cmdbuild.utils.lang.CmStringUtils.toStringOrNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ataboga
 */
public class PluginConfigUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Map<String, Object> getConfigs(Object configuration, String language) {
        checkArgument(configuration.getClass().getAnnotation(PluginConfigComponent.class) != null, "service =< %s > is not a plugin config component", configuration.getClass().getName());
        List<Field> declaredFields = asList(FieldUtils.getFieldsWithAnnotation(configuration.getClass(), PluginConfigValue.class));
        declaredFields.forEach(f -> f.setAccessible(true));
        Map<String, Object> configs = declaredFields.stream().collect(toMap(PluginConfigUtils::getPluginConfigExtendedKey, safe(f -> toStringOrNull(f.get(configuration)), null)));
        return map(configs).with("_model", map("attributes", list(declaredFields).map(PluginConfigUtils::generateAttributeConfig)));
    }

    public static String getPluginNameFromBean(Class<?> clazz) {
        return clazz.getAnnotation(PluginConfigComponent.class).plugin();
    }

    public static String getPluginConfigNamespace(Class<?> clazz) {
        return clazz.getAnnotation(PluginConfigComponent.class).value();
    }

    public static String getPluginConfigExtendedKey(Field field) {
        return format("%s.%s", getPluginConfigNamespace(field.getDeclaringClass()), field.getAnnotation(PluginConfigValue.class).key());
    }

    public static String getPluginConfigKey(Field field) {
        return field.getAnnotation(PluginConfigValue.class).key();
    }

    public static String getPluginConfigName(Field field) {
        return field.getAnnotation(PluginConfigValue.class).name();
    }

    public static String getPluginConfigDescription(Field field) {
        return field.getAnnotation(PluginConfigValue.class).description();
    }

    public static String getPluginConfigAccess(Field field) {
        return field.getAnnotation(PluginConfigValue.class).access();
    }

    private static Map<String, Object> generateAttributeConfig(Field field) {
        return mapOf(String.class, Object.class).with("_id", getPluginConfigExtendedKey(field),
                "name", getPluginConfigName(field),
                "description", getPluginConfigDescription(field),
                "_description_translation", getPluginConfigDescription(field),
                "type", getSimpleNameFromField(field),
                "access", getPluginConfigAccess(field)
        ).accept(m -> {
            if (field.isEnumConstant()) {
                m.put("options", getEnumParamsFromType(field.getType()));
            }
            if (Objects.equals(getSimpleNameFromField(field), "string") && getPluginConfigKey(field).contains("password")) {
                m.put("password", true);
            }
        });
    }

    private static String getSimpleNameFromField(Field field) {
        return field.getType().getSimpleName().toLowerCase();
    }
}
