/*
 * Copyright 2020 The Embulk project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.util.config;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents one task field defined by a getter method in a task-defining interface.
 *
 * <p>In the example below, it represents a field of {@code getConfigAsString()}, {@code getConfigAsInteger()},
 * or {@code getSomeone()}. Note that "a field" is not for {@code @Config("config")} (which is the same for
 * {@code getConfigAsString()} and {@code getConfigAsInteger()}).
 *
 * <pre>{@code interface PluginTask extends Task {
 *     @Config("config")
 *     String getConfigAsString();
 *
 *     @Config("config")
 *     int getConfigAsInteger();
 *
 *     @Config("someone")
 *     @ConfigDefault("\"any\"")
 *     String getSomeone();
 * }}</pre>
 *
 * <p>For the top field of the example above, {@code getterMethod} would be {@code getConfigAsString()},
 * {@code name} would be {@code "ConfigAsString"}, {@code returnType} would be {@code String}, and
 * {@code defaultValueInJsonString} would be {@code "any"}.
 *
 * <p>For your information, the value of {@code @Config} would be a key of {@code Map<String, TaskField>}
 * inside {@code TaskObjectsRetriever}.
 */
final class TaskField {
    private TaskField(
            final Method getterMethod,
            final String name,
            final Type returnType,
            final String defaultValueInJsonString) {
        this.getterMethod = getterMethod;
        this.name = name;
        this.returnType = returnType;
        this.defaultValueInJsonString = defaultValueInJsonString;
    }

    static TaskField of(final Method getterMethod) {
        if (getterMethod.getParameterTypes().length != 0) {
            return null;
        }
        if (getterMethod.isDefault() && getterMethod.getAnnotation(Config.class) == null) {
            return null;
        }

        final Optional<String> name = Tasks.getFieldNameFromGetter(getterMethod.getName());
        if (!name.isPresent()) {
            return null;
        }

        final Type returnType = getterMethod.getGenericReturnType();

        final ConfigDefault annotationConfigDefault = getterMethod.getAnnotation(ConfigDefault.class);
        if (annotationConfigDefault != null && !annotationConfigDefault.value().isEmpty()) {
            return new TaskField(getterMethod, name.get(), returnType, annotationConfigDefault.value());
        } else {
            return new TaskField(getterMethod, name.get(), returnType, null);
        }
    }

    Method getGetterMethod() {
        return this.getterMethod;
    }

    String getName() {
        return this.name;
    }

    Type getReturnType() {
        return this.returnType;
    }

    Optional<String> getDefaultValueInJsonString() {
        return Optional.ofNullable(this.defaultValueInJsonString);
    }

    String getFieldNameMappedFromConfigSourceJson() {
        final Config annotationConfig = this.getterMethod.getAnnotation(Config.class);
        if (annotationConfig != null) {
            return annotationConfig.value();
        }
        return null;
    }

    String getFieldNameMappedFromTaskSourceJson() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getterMethod, this.name, this.returnType, this.defaultValueInJsonString);
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (!(otherObject instanceof TaskField)) {
            return false;
        }
        final TaskField other = (TaskField) otherObject;
        return Objects.equals(this.getterMethod, other.getterMethod)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.returnType, other.returnType)
                && Objects.equals(this.defaultValueInJsonString, other.defaultValueInJsonString);
    }

    private final Method getterMethod;
    private final String name;
    private final Type returnType;
    private final String defaultValueInJsonString;
}
