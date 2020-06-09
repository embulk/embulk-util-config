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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;

final class DataSourceModule extends SimpleModule {
    DataSourceModule(final ObjectMapper nestedObjectMapper) {
        super(DataSourceModule.class.getCanonicalName(), Version.JACKSON_MODULE_VERSION);

        // DataSourceImpl -- disabled for now. Is it really needed?
        // TODO: Revisit it to reconsider its necessity.
        // addSerializer(DataSourceImpl.class, new DataSourceSerializer<DataSourceImpl>(nestedObjectMapper));
        // addDeserializer(DataSourceImpl.class, new DataSourceDeserializer<DataSourceImpl>(nestedObjectMapper));

        // ConfigSource
        this.addSerializer(ConfigSource.class, new DataSourceSerializer<ConfigSource>(nestedObjectMapper));
        this.addDeserializer(ConfigSource.class, new DataSourceDeserializer<ConfigSource>(nestedObjectMapper));

        // TaskSource
        this.addSerializer(TaskSource.class, new DataSourceSerializer<TaskSource>(nestedObjectMapper));
        this.addDeserializer(TaskSource.class, new DataSourceDeserializer<TaskSource>(nestedObjectMapper));

        // TaskReport
        this.addSerializer(TaskReport.class, new DataSourceSerializer<TaskReport>(nestedObjectMapper));
        this.addDeserializer(TaskReport.class, new DataSourceDeserializer<TaskReport>(nestedObjectMapper));

        // ConfigDiff
        this.addSerializer(ConfigDiff.class, new DataSourceSerializer<ConfigDiff>(nestedObjectMapper));
        this.addDeserializer(ConfigDiff.class, new DataSourceDeserializer<ConfigDiff>(nestedObjectMapper));
    }
}
