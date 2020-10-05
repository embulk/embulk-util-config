/*
 * Copyright 2014 The Embulk project
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

package org.embulk.util.config.modules;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.io.IOException;
import org.embulk.spi.type.Type;
import org.embulk.spi.type.Types;
import org.junit.Test;

public class TestTypeModule {

    private static class HasType {
        private Type type;
        // TODO test TimestampType

        @JsonCreator
        public HasType(
                @JsonProperty("type") Type type) {
            this.type = type;
        }

        @JsonProperty("type")
        public Type getType() {
            return type;
        }
    }

    @Test
    public void testGetType() throws IOException {
        HasType type = new HasType(Types.STRING);
        String json = MAPPER.writeValueAsString(type);
        HasType decoded = MAPPER.readValue(json, HasType.class);
        assertTrue(Types.STRING == decoded.getType());
    }

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.registerModule(new Jdk8Module());
        MAPPER.registerModule(new TypeModule());
    }
}
