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

package org.embulk.util.config.rebuild;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map;

/**
 * Rebuilds Jackson {@code ObjectNode} from another {@code ObjectNode} loaded by another {@link java.lang.ClassLoader}.
 *
 * <p>This rebuilder is designed to rebuild Embulk plugin's own {@code ObjectNode} under {@code PluginClassLoader}
 * from Embulk core's internal {@code ObjectNode}.
 *
 * <p>The rebuilder would run on plugin's side under {@code PluginClassLoader}. It would not have direct access to
 * Embulk core's Jackson classes which is loaded by another {@link java.lang.ClassLoader} under the Embulk core.
 * Then, it is accessing the given {@link java.lang.Object} (expected to be Embulk core's Jackson object) through
 * reflection.
 */
public final class ObjectNodeRebuilder {
    private ObjectNodeRebuilder() {
        // No instantiation.
    }

    /**
     * Rebuilds Jackson {@code ObjectNode} on plugin's side from core-side {@code ObjectNode}.
     *
     * @throws NullPointerException  if receiving {@code null}
     * @throws ClassCastException  if the rebuilt object cannot be casted to a corresponding Jackson class on plugin's side,
     *         or core-side Jackson's getter method does not return an object of an expected class
     * @throws UnsupportedOperationException  if {@code BinaryNode} or {@code POJONode} is contained in the core-side
     *         {@code ObjectNode}
     * @throws IllegalStateException  if {@code MissingNode} or unknown {@code JsonNode} is contained in the core-side
     *         {@code ObjectNode}, core-side Jackson's getter method throws an unexpected {@code Exception}, or core-side Jackson's
     *         getter method does not exist unexpectedly
     */
    public static ObjectNode rebuild(final Object from, final ObjectMapper mapper) {
        if (mapper == null) {
            throw new NullPointerException(ObjectNodeRebuilder.class.getSimpleName() + " received null.");
        }

        final Iterator<Map.Entry<String, Object>> fieldsValue = castToMapEntry(Util.getThroughGetter(
                from,
                "com.fasterxml.jackson.databind.node.ObjectNode",
                "fields",
                Iterator.class,
                ObjectNodeRebuilder.class));

        final ObjectNode result = mapper.createObjectNode();
        for (final Map.Entry<String, Object> field : (Iterable<Map.Entry<String, Object>>) () -> fieldsValue) {
            final String key = field.getKey();
            final Object value = field.getValue();
            final Class<?> valueClazz = value.getClass();
            final JsonNode rebuiltNode = Util.rebuildPerType(valueClazz, value, mapper);
            result.set(key, rebuiltNode);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Iterator<Map.Entry<String, Object>> castToMapEntry(final Iterator iterator) {
        return (Iterator<Map.Entry<String, Object>>) iterator;
    }
}
