/**
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.hawkbit.repository.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class Utils {

    private static final Map<FromTo, BiFunction<Object, Object, Boolean>> FROM_TO_BI_FUNCTION_MAP = new HashMap<>();

    // java:S4276 - it is intended to be generic - not specialized
    @SuppressWarnings("java:S4276")
    public static boolean copy(final Object from, final Object to) {
        final FromTo fromTo = new FromTo(from.getClass(), to.getClass());
        BiFunction<Object, Object, Boolean> fromToFunction;
        synchronized (FROM_TO_BI_FUNCTION_MAP) {
            fromToFunction = FROM_TO_BI_FUNCTION_MAP.get(fromTo);
            if (fromToFunction == null) {
                fromToFunction = fromToFunction(from.getClass(), to.getClass());
                FROM_TO_BI_FUNCTION_MAP.put(fromTo, fromToFunction);
            }
        }
        return fromToFunction.apply(from, to);
    }

    // java:S4276 - it is intended to be generic - not specialized
    // java:S3776 - complexity is due to reflection and dynamic method invocation
    // java:S1141 - better readable that way
    @SuppressWarnings({ "java:S4276", "java:S3776", "java:S1141" })
    private static BiFunction<Object, Object, Boolean> fromToFunction(final Class<?> fromClass, final Class<?> toClass) {
        final List<BiFunction<Object, Object, Boolean>> fieldSetters = new ArrayList<>();

        for (final Method fromMethod : fromClass.getMethods()) {
            if (fromMethod.getParameterCount() == 0 && fromMethod.getReturnType() != void.class ) {
                final String methodName = fromMethod.getName();
                if (!methodName.equals("getClass") && !methodName.equals("getId") &&
                        methodName.startsWith("get") && methodName.length() > 3 && Character.isUpperCase(methodName.charAt(3))) {
                    // getter method
                    final String setterName = "set" + methodName.substring(3);
                    Method toGetterMethod0;
                    try {
                        toGetterMethod0 = toClass.getMethod(fromMethod.getName(), fromMethod.getReturnType());
                    } catch (final NoSuchMethodException e) {
                        // no method to get current value of the target field
                        toGetterMethod0 = null;
                    }
                    final Method toGetterMethod = toGetterMethod0;
                    try {
                        final Method toSetterMethod = toClass.getMethod(setterName, fromMethod.getReturnType());
                        // method access
                        fieldSetters.add((from, to) -> {
                            try {
                                final Object value = fromMethod.invoke(from);
                                if (toGetterMethod != null) {
                                    final Object currentValue = toGetterMethod.invoke(to);
                                    if (Objects.equals(value, currentValue)) {
                                        return false; // no change
                                    }
                                }
                                toSetterMethod.invoke(to, value);
                                return true;
                            } catch (final Exception e) {
                                throw new IllegalStateException("Error invoking " + fromMethod + " or " + toSetterMethod, e);
                            }
                        });
                    } catch (final NoSuchMethodException e) {
                        final String fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
                        final Field field = getField(toClass, fieldName);
                        if (field == null) {
                            throw new IllegalStateException(
                                    "Setter method counterpart for " + fromMethod + " in " + toClass.getSimpleName() + " not found");
                        } else {
                            // field access
                            fieldSetters.add((from, to) -> {
                                try {
                                    final Object value = fromMethod.invoke(from);
                                    if (toGetterMethod != null) {
                                        final Object currentValue = toGetterMethod.invoke(to);
                                        if (Objects.equals(value, currentValue)) {
                                            return false; // no change
                                        }
                                    }
                                    field.set(to, value);
                                    return true;
                                } catch (final Exception e2) {
                                    throw new IllegalStateException("Error invoking " + fromMethod + " or setting " + field, e2);
                                }
                            });
                        }
                    }
                }
            }
        }

        return (from, to) -> {
            boolean updated = false;
            for (final BiFunction<Object, Object, Boolean> fieldSetter : fieldSetters) {
                updated = fieldSetter.apply(from, to) || updated;
            }
            return updated;
        };
    }

    private static Field getField(final Class<?> clazz, final String fieldName) {
        for (final Field field : clazz.getDeclaredFields()) {
            if (fieldName.equals(field.getName())) {
                field.setAccessible(true);
                return field;
            }
        }
        final Class<?> superClass = clazz.getSuperclass();
        if (superClass == null) {
            return null;
        } else {
            return getField(superClass, fieldName);
        }
    }

    private record FromTo(Class<?> from, Class<?> to) {}
}
