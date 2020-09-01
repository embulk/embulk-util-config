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

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.Validator;

/**
 * Creates {@link ConfigMapper} and {@link TaskMapper} with required and specified Jackson {@link com.fasterxml.jackson.databind.Module}s and {@link javax.validation.Validator}.
 */
public final class ConfigMapperFactory {
    private ConfigMapperFactory(final List<Module> additionalModules, final Validator validator) {
        this.additionalModules = Collections.unmodifiableList(new ArrayList<>(additionalModules));
        this.validator = validator;
    }

    /**
     * Builds {@link ConfigMapperFactory}.
     */
    public static class Builder {
        private Builder() {
            this.additionalModules = new ArrayList<>();
            this.validator = null;
        }

        /**
         * Builds {@link ConfigMapperFactory} with added Jackson {@link com.fasterxml.jackson.databind.Module}s and specified {@link javax.validation.Validator}.
         */
        public ConfigMapperFactory build() {
            return new ConfigMapperFactory(this.additionalModules, this.validator);
        }

        /**
         * Builds {@link ConfigMapperFactory} with Jackson {@link com.fasterxml.jackson.databind.Module}s.
         *
         * <p>For the time being, only {@link com.fasterxml.jackson.datatype.jdk8.Jdk8Module} is added.
         */
        public Builder addDefaultModules() {
            // TODO: Add those default Modules.
            // this.additionalModules.add(new DateTimeZoneModule());
            // this.additionalModules.add(new TimestampModule());
            // this.additionalModules.add(new CharsetModule());
            // this.additionalModules.add(new LocalFileModule());

            // com.fasterxml.jackson.datatype.guava.GuavaModule (jackson-datatype-guava) was here in embulk-core.
            this.additionalModules.add(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());  // jackson-datatype-jdk8
            // com.fasterxml.jackson.datatype.joda.JodaModule (jackson-datatype-joda) was here in embulk-core.
            return this;
        }

        /**
         * Adds a specified {@link com.fasterxml.jackson.databind.Module}.
         */
        public Builder addModule(final Module module) {
            if (module == null) {
                throw new NullPointerException("ConfigMapperFactory.Builder#addModule does not accept null.");
            }
            this.additionalModules.add(module);
            return this;
        }

        /**
         * Sets a {@link javax.validation.Validator}.
         */
        public Builder withValidator(final Validator validator) {
            if (validator == null) {
                throw new NullPointerException("ConfigMapperFactory.Builder#withValidator does not accept null.");
            }
            if (this.validator != null) {
                throw new IllegalStateException("ConfigMapperFactory.Builder accepts withValidator just once.");
            }
            this.validator = validator;
            return this;
        }

        private final ArrayList<Module> additionalModules;
        private Validator validator;
    }

    /**
     * Creates a {@link Builder} to build {@link ConfigMapperFactory}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a {@link ConfigMapperFactory} with required and specified Jackson {@link com.fasterxml.jackson.databind.Module}s and {@link javax.validation.Validator}.
     */
    public static ConfigMapperFactory with(final Validator validator, final Module... modules) {
        final Builder builder = builder();
        if (validator != null) {
            builder.withValidator(validator);
        }
        for (final Module module : modules) {
            builder.addModule(module);
        }
        return builder.build();
    }

    /**
     * Creates a {@link ConfigMapperFactory} with required and specified Jackson {@link com.fasterxml.jackson.databind.Module}s.
     */
    public static ConfigMapperFactory with(final Module... modules) {
        return with(null, modules);
    }

    /**
     * Creates a {@link ConfigMapperFactory} with required and default Jackson {@link com.fasterxml.jackson.databind.Module}s.
     */
    public static ConfigMapperFactory withDefault() {
        return builder().addDefaultModules().build();
    }

    /**
     * Creates a {@link ConfigMapper} to maps {@code org.embulk.config.ConfigSource} into a task-defining interface that inherits {@link Task}.
     */
    public ConfigMapper createConfigMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final Module module : this.additionalModules) {
            objectMapper.registerModule(module);
        }
        objectMapper.registerModule(new ConfigTaskSerializerModule(objectMapper));
        objectMapper.registerModule(new ConfigDeserializerModule(objectMapper, this.validator));  // Difference from TaskMapper.
        objectMapper.registerModule(new DataSourceModule(objectMapper));

        return new ConfigMapper(objectMapper, this.validator);
    }

    /**
     * Creates a {@link TaskMapper} to maps {@code org.embulk.config.TaskSource} into a task-defining interface that inherits {@link Task}.
     */
    public TaskMapper createTaskMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final Module module : this.additionalModules) {
            objectMapper.registerModule(module);
        }
        objectMapper.registerModule(new ConfigTaskSerializerModule(objectMapper));
        objectMapper.registerModule(new TaskDeserializerModule(objectMapper, this.validator));  // Difference from ConfigMapper.
        objectMapper.registerModule(new DataSourceModule(objectMapper));

        return new TaskMapper(objectMapper, this.validator);
    }

    private final List<Module> additionalModules;
    private final Validator validator;
}
