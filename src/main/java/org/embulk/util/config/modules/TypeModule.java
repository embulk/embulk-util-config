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

package org.embulk.util.config.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.embulk.spi.type.Type;
import org.embulk.spi.type.Types;

public final class TypeModule extends SimpleModule {
    public TypeModule() {
        this.addSerializer(Type.class, new TypeSerializer());
        this.addDeserializer(Type.class, new TypeDeserializer());
    }

    private static class TypeSerializer extends JsonSerializer<Type> {
        @Override
        public void serialize(
                final Type value,
                final JsonGenerator jsonGenerator,
                final SerializerProvider provider)
                throws IOException {
            jsonGenerator.writeString(value.getName());
        }
    }

    private static class TypeDeserializer extends FromStringDeserializer<Type> {
        public TypeDeserializer() {
            super(Type.class);
        }

        @Override
        protected Type _deserialize(final String value, final DeserializationContext context) throws IOException {
            final Type type = STRING_TO_TYPE.get(value);
            if (type == null) {
                throw new JsonMappingException(String.format("Unknown type name '%s'. Supported types are: %s",
                        value, String.join(", ", STRING_TO_TYPE.keySet())));
            }
            return type;
        }

        static {
            final HashMap<String, Type> builder = new HashMap<>();
            builder.put(Types.BOOLEAN.getName(), Types.BOOLEAN);
            builder.put(Types.LONG.getName(), Types.LONG);
            builder.put(Types.DOUBLE.getName(), Types.DOUBLE);
            builder.put(Types.STRING.getName(), Types.STRING);
            builder.put(Types.TIMESTAMP.getName(), Types.TIMESTAMP);
            builder.put(Types.JSON.getName(), Types.JSON);
            STRING_TO_TYPE = Collections.unmodifiableMap(builder);
        }

        private static final Map<String, Type> STRING_TO_TYPE;
    }
}
