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
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Iterator;

final class ArrayNodeRebuilder {
    private ArrayNodeRebuilder() {
        // No instantiation.
    }

    static ArrayNode rebuild(final Object from, final ObjectMapper mapper) {
        if (mapper == null) {
            throw new NullPointerException(ArrayNodeRebuilder.class.getSimpleName() + " received null.");
        }

        final Iterator<Object> elementsValue = castToObject(Util.getThroughGetter(
                from,
                "com.fasterxml.jackson.databind.node.ArrayNode",
                "elements",
                Iterator.class,
                ArrayNodeRebuilder.class));

        final ArrayNode result = mapper.createArrayNode();
        for (final Object element : (Iterable<Object>) () -> elementsValue) {
            final Class<?> elementClass = element.getClass();
            final JsonNode rebuiltNode = Util.rebuildPerType(elementClass, element, mapper);
            result.add(rebuiltNode);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Iterator<Object> castToObject(final Iterator iterator) {
        return (Iterator<Object>) iterator;
    }
}
