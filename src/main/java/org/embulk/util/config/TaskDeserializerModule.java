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
import javax.validation.Validator;

final class TaskDeserializerModule extends Module {  // can't use just SimpleModule, due to generic types
    TaskDeserializerModule(final ObjectMapper nestedObjectMapper, final Validator validator) {
        this.nestedObjectMapper = nestedObjectMapper;
        this.validator = validator;
    }

    @Override
    public String getModuleName() {
        return TaskDeserializerModule.class.getCanonicalName();
    }

    @Override
    public void setupModule(final SetupContext context) {
        context.addDeserializers(ConfigTaskDeserializers.forTask(this.nestedObjectMapper, this.validator));
    }

    @Override
    public com.fasterxml.jackson.core.Version version() {
        return Version.JACKSON_MODULE_VERSION;
    }

    private final ObjectMapper nestedObjectMapper;
    private final Validator validator;
}
