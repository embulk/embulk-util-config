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
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.Validator;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.util.config.modules.CharsetModule;
import org.embulk.util.config.modules.ColumnModule;
import org.embulk.util.config.modules.LocalFileModule;
import org.embulk.util.config.modules.SchemaModule;
import org.embulk.util.config.modules.TypeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
         * <p>Six Modules, {@link org.embulk.util.config.modules.ColumnModule}, {@link org.embulk.util.config.modules.SchemaModule},
         * {@link org.embulk.util.config.modules.TypeModule}, {@link org.embulk.util.config.modules.CharsetModule},
         * {@link org.embulk.util.config.modules.LocalFileModule}, and {@link com.fasterxml.jackson.datatype.jdk8.Jdk8Module}
         * are added since {@code embulk-util-config} v0.3.0.
         */
        public Builder addDefaultModules() {
            // embulk-core's ModelManager had DateTimeZoneModule here for Joda-Time's DateTimeZone.
            // DateTimeZoneModule is no longer with embulk-util-config because Joda-Time is not here.
            // embulk-util-config has ZoneIdModule for java.time.ZoneId instead of DateTimeZoneModule.
            //
            // But, the alternative ZoneIdModule is not added by default because it has variations.
            //   new ZoneId(): not using "legacy" timezone names
            //   ZoneId.withLegacyZoneNames(): using "legacy" timezone names
            //
            // Developers need to choose one of them by themselves.

            // embulk-core's ModelManager had TimestampModule here for org.embulk.spi.time.Timestamp.
            //
            // TimestampModule is implemented as org.embulk.util.config.modules.TimestampModule, but
            // it is not added by default because org.embulk.spi.time.Timestamp is deprecated.

            this.addModule(new ColumnModule());
            this.addModule(new SchemaModule());
            this.addModule(new TypeModule());

            this.addModule(new CharsetModule());
            this.addModule(new LocalFileModule());

            // com.fasterxml.jackson.datatype.guava.GuavaModule (jackson-datatype-guava) was here in embulk-core.
            this.addModule(new com.fasterxml.jackson.datatype.jdk8.Jdk8Module());  // jackson-datatype-jdk8
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
            for (final Module m : this.additionalModules) {
                if (m.getClass().equals(module.getClass())) {
                    logger.warn("Jackson Module {} is already added. It may be duplicated.", module.getClass());
                }
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
        assertJacksonCoreVersion();
        assertJacksonDataBindVersion();
        assertJacksonDataTypeJdk8Version();
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
        final ObjectMapper objectMapper = this.mapperForConfig();
        return new ConfigMapper(objectMapper, this.validator);
    }

    /**
     * Creates a {@link TaskMapper} to maps {@code org.embulk.config.TaskSource} into a task-defining interface that inherits {@link Task}.
     */
    public TaskMapper createTaskMapper() {
        final ObjectMapper objectMapper = this.mapperForTask();
        return new TaskMapper(objectMapper);
    }

    /**
     * Creates an empty {@link org.embulk.config.ConfigDiff} instance.
     *
     * <p>It is to replace {@link org.embulk.spi.Exec#newConfigDiff}.
     */
    public ConfigDiff newConfigDiff() {
        final ObjectMapper objectMapper = this.mapperForOthers();
        return (ConfigDiff) new DataSourceImpl(objectMapper.createObjectNode(), objectMapper);
    }

    /**
     * Creates an empty {@link org.embulk.config.ConfigSource} instance.
     *
     * <p>It is to replace {@link org.embulk.spi.Exec#newConfigSource}.
     */
    public ConfigSource newConfigSource() {
        final ObjectMapper objectMapper = this.mapperForConfig();
        return (ConfigSource) new DataSourceImpl(objectMapper.createObjectNode(), objectMapper);
    }

    /**
     * Creates an empty {@link org.embulk.config.TaskReport} instance.
     *
     * <p>It is to replace {@link org.embulk.spi.Exec#newTaskReport}.
     */
    public TaskReport newTaskReport() {
        final ObjectMapper objectMapper = this.mapperForOthers();
        return (TaskReport) new DataSourceImpl(objectMapper.createObjectNode(), objectMapper);
    }

    /**
     * Creates an empty {@link org.embulk.config.TaskSource} instance.
     *
     * <p>It is to replace {@link org.embulk.spi.Exec#newTaskSource}.
     */
    public TaskSource newTaskSource() {
        final ObjectMapper objectMapper = this.mapperForTask();
        return (TaskSource) new DataSourceImpl(objectMapper.createObjectNode(), objectMapper);
    }

    /**
     * Rebuilds {@link org.embulk.config.ConfigDiff} with the context of this {@code embulk-util-config}.
     *
     * <p>{@link org.embulk.config.ConfigDiff} passed from {@code embulk-core} is built with the context of
     * {@code embulk-core}. It is usually fine, but sometimes, it could be problematic because the given
     * {@link org.embulk.config.ConfigDiff} is built with Jackson on the {@code embulk-core} side.
     *
     * <p>For example, when converting a {@link org.embulk.config.ConfigDiff} instance to a Jackson object,
     * such as {@link com.fasterxml.jackson.databind.JsonNode} like below, it fails.
     *
     * <pre>{@code  configDiff.get(JsonNode.class, "field");}</pre>
     *
     * <p>Rebuilding the given {@link org.embulk.config.ConfigDiff} instance would mitigate such a problem.
     *
     * @param configDiff  a {@link org.embulk.config.ConfigDiff} instance to rebuild
     * @return the new {@link org.embulk.config.ConfigDiff} instance rebuilt
     */
    public ConfigDiff rebuildConfigDiff(final ConfigDiff configDiff) {
        final ObjectNode objectNode;
        try {
            objectNode = Compat.rebuildObjectNode(configDiff);
        } catch (final IOException ex) {
            // It should happen only from DataSource#toJson(), not from rebuilding ObjectNode.
            throw new ConfigException("org.embulk.config.ConfigDiff#toJson() returned an invalid JSON.", ex);
        } catch (final RuntimeException ex) {
            throw new ConfigException("Unexpected failure in reinterpreting ObjectNode from org.embulk.config.ConfigDiff.", ex);
        }

        return (ConfigDiff) new DataSourceImpl(objectNode, this.mapperForConfig());
    }

    /**
     * Rebuilds {@link org.embulk.config.TaskReport} with the context of this {@code embulk-util-config}.
     *
     * <p>{@link org.embulk.config.TaskReport} passed from {@code embulk-core} is built with the context of
     * {@code embulk-core}. It is usually fine, but sometimes, it could be problematic because the given
     * {@link org.embulk.config.TaskReport} is built with Jackson on the {@code embulk-core} side.
     *
     * <p>For example, when converting a {@link org.embulk.config.TaskReport} instance to a Jackson object,
     * such as {@link com.fasterxml.jackson.databind.JsonNode} like below, it fails.
     *
     * <pre>{@code  taskReport.get(JsonNode.class, "field");}</pre>
     *
     * <p>Rebuilding the given {@link org.embulk.config.TaskReport} instance would mitigate such a problem.
     *
     * @param taskReport  a {@link org.embulk.config.TaskReport} instance to rebuild
     * @return the new {@link org.embulk.config.TaskReport} instance rebuilt
     */
    public TaskReport rebuildTaskReport(final TaskReport taskReport) {
        final ObjectNode objectNode;
        try {
            objectNode = Compat.rebuildObjectNode(taskReport);
        } catch (final IOException ex) {
            // It should happen only from DataSource#toJson(), not from rebuilding ObjectNode.
            throw new ConfigException("org.embulk.config.TaskReport#toJson() returned an invalid JSON.", ex);
        } catch (final RuntimeException ex) {
            throw new ConfigException("Unexpected failure in reinterpreting ObjectNode from org.embulk.config.TaskReport.", ex);
        }

        return (TaskReport) new DataSourceImpl(objectNode, this.mapperForConfig());
    }

    private ObjectMapper mapperForConfig() {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final Module module : this.additionalModules) {
            objectMapper.registerModule(module);
        }
        objectMapper.registerModule(new ConfigTaskSerializerModule(objectMapper));
        objectMapper.registerModule(new ConfigDeserializerModule(objectMapper, this.validator));  // Difference from TaskMapper.
        objectMapper.registerModule(new DataSourceModule(objectMapper));

        return objectMapper;
    }

    private ObjectMapper mapperForTask() {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final Module module : this.additionalModules) {
            objectMapper.registerModule(module);
        }
        objectMapper.registerModule(new ConfigTaskSerializerModule(objectMapper));
        objectMapper.registerModule(new TaskDeserializerModule(objectMapper, this.validator));  // Difference from ConfigMapper.
        objectMapper.registerModule(new DataSourceModule(objectMapper));

        return objectMapper;
    }

    private ObjectMapper mapperForOthers() {
        final ObjectMapper objectMapper = new ObjectMapper();
        for (final Module module : this.additionalModules) {
            objectMapper.registerModule(module);
        }
        objectMapper.registerModule(new DataSourceModule(objectMapper));

        return objectMapper;
    }

    private static void assertJacksonCoreVersion() {
        if (com.fasterxml.jackson.core.json.PackageVersion.VERSION.getMajorVersion() != 2) {
            throw new UnsupportedOperationException("embulk-util-config is not used with Jackson 2.");
        }

        final int minor = com.fasterxml.jackson.core.json.PackageVersion.VERSION.getMinorVersion();
        if (minor < 14 || (minor == 15 && com.fasterxml.jackson.core.json.PackageVersion.VERSION.getPatchLevel() <= 2)) {
            throw new UnsupportedOperationException("embulk-util-config is not used with Jackson (jackson-core) 2.15.3 or later.");
        }
    }

    private static void assertJacksonDataBindVersion() {
        if (com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION.getMajorVersion() != 2) {
            throw new UnsupportedOperationException("embulk-util-config is not used with Jackson 2.");
        }

        final int minor = com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION.getMinorVersion();
        if (minor < 14 || (minor == 15 && com.fasterxml.jackson.databind.cfg.PackageVersion.VERSION.getPatchLevel() <= 2)) {
            throw new UnsupportedOperationException("embulk-util-config is not used with Jackson (jackson-databind) 2.15.3 or later.");
        }
    }

    private static void assertJacksonDataTypeJdk8Version() {
        if (com.fasterxml.jackson.datatype.jdk8.PackageVersion.VERSION.getMajorVersion() != 2) {
            throw new UnsupportedOperationException("embulk-util-config is not used with Jackson 2.");
        }

        final int minor = com.fasterxml.jackson.datatype.jdk8.PackageVersion.VERSION.getMinorVersion();
        if (minor < 14 || (minor == 15 && com.fasterxml.jackson.datatype.jdk8.PackageVersion.VERSION.getPatchLevel() <= 2)) {
            throw new UnsupportedOperationException("embulk-util-config is not used with Jackson (jackson-datatype-jdk8) 2.15.3 or later.");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ConfigMapperFactory.class);

    private final List<Module> additionalModules;
    private final Validator validator;
}
