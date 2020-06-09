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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import org.embulk.config.DataSource;
import org.embulk.util.config.rebuild.ObjectNodeRebuilder;

/**
 * Utility methods for compatibility before and after Embulk v0.10.2.
 */
final class Compat {
    private Compat() {
        // No instantiation.
    }

    /**
     * Rebuilds a stringified JSON representation from {@code org.embulk.config.DataSource}.
     */
    static String toJson(final DataSource source) throws IOException {
        final Optional<String> jsonString = callToJsonIfAvailable(source);
        if (jsonString.isPresent()) {
            // In case of newer Embulk versions since v0.10.3 -- `DataSource` has the `toJson` method.
            //
            // In this case, it uses the straightforward `toJson` method to convert into a JSON representation.
            return jsonString.get();
        }

        // In case of older Embulk versions than v0.10.3 -- the `toJson` method is not defined in `DataSource`.
        //
        // In this case, it exploits a hack -- it uses the `getObjectNode` method that is only in `DataSourceImpl`
        // since Embulk v0.10.2.
        final ObjectNode objectNode = callGetObjectNodeAndRebuildIfAvailable(source, SIMPLE_MAPPER);
        return SIMPLE_MAPPER.writeValueAsString(objectNode);  // It can throw JsonProcessingException extending IOException.
    }

    /**
     * Rebuilds a JSON {@link com.fasterxml.jackson.databind.ObjectMapper} from {@code org.embulk.config.DataSource}.
     *
     * @throws IOException  if failing in parsing a JSON from {@code DataSource#toJson}
     * @throws NullPointerException  if the parsed JSON is parsed to {@code null}, or receiving {@code null}
     * @throws ClassCastException  if the parsed JSON is not a JSON object, the rebuilt object cannot be casted to a corresponding
     *         Jackson class on plugin's side, or core-side Jackson's getter method does not return an object of an expected class
     * @throws UnsupportedOperationException  if {@code BinaryNode} or {@code POJONode} is contained in the core-side
     *         {@code ObjectNode}
     * @throws IllegalStateException  if {@code MissingNode} or unknown {@code JsonNode} is contained in the core-side
     *         {@code ObjectNode}, core-side Jackson's getter method throws an unexpected {@code Exception}, or core-side Jackson's
     *         getter method does not exist unexpectedly
     */
    static ObjectNode rebuildObjectNode(final DataSource source) throws IOException {
        final Optional<String> jsonString = callToJsonIfAvailable(source);
        if (jsonString.isPresent()) {
            // In case of newer Embulk versions since v0.10.3 -- `DataSource` has the `toJson` method.
            //
            // In this case, it uses the straightforward `toJson` method to rebuild `JsonNode` on the plugin side.
            final JsonNode jsonNode = SIMPLE_MAPPER.readTree(jsonString.get());

            if (jsonNode == null) {
                throw new NullPointerException("DataSource#toJson() returned null.");
            }
            if (!jsonNode.isObject()) {
                throw new ClassCastException(
                        "DataSource#toJson() returned not a JSON object: " + jsonNode.getClass().getCanonicalName());
            }
            return (ObjectNode) jsonNode;
        }

        // In case of older Embulk versions than v0.10.3 -- the `toJson` method is not defined in `DataSource`.
        //
        // In this case, it exploits a hack -- it uses the `getObjectNode` method that is only in `DataSourceImpl`
        // since Embulk v0.10.2.
        return callGetObjectNodeAndRebuildIfAvailable(source, SIMPLE_MAPPER);
    }

    private static Optional<String> callToJsonIfAvailable(final DataSource source) {
        final Method toJson = getToJsonMethod(source.getClass());
        if (toJson == null) {
            return Optional.empty();
        }

        final Object jsonStringObject;
        try {
            jsonStringObject = toJson.invoke(source);
        } catch (final InvocationTargetException ex) {
            final Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw new IllegalStateException("DataSource#toJson() threw unexpected Exception.", targetException);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException("DataSource#toJson() is not accessible.", ex);
        }

        if (jsonStringObject == null) {
            throw new NullPointerException("org.embulk.config.DataSource#toJson() returned null.");
        }
        if (!(jsonStringObject instanceof String)) {
            throw new ClassCastException(
                    "org.embulk.config.DataSource#toJson() returned not a String: "
                    + jsonStringObject.getClass().getCanonicalName());
        }

        return Optional.of((String) jsonStringObject);
    }

    private static ObjectNode callGetObjectNodeAndRebuildIfAvailable(final DataSource source, final ObjectMapper mapper) {
        final Class<? extends DataSource> coreDataSourceImplClass = source.getClass();
        if (!coreDataSourceImplClass.getCanonicalName().equals("org.embulk.config.DataSourceImpl")) {
            throw new ClassCastException("DataSource specified is not org.embulk.config.DataSourceImpl.");
        }
        final Method getObjectNode = getGetObjectNodeMethod(coreDataSourceImplClass);
        if (getObjectNode == null) {
            throw new IllegalStateException("org.embulk.config.DataSourceImpl does not implement getObjectNode().");
        }

        // NOTE: This ObjectNode instance is of Jackson on the core side while this library is for the plugin side.
        // It is planned to have different Jackson loadings both on the core side and the plugin side.
        final Object coreObjectNode;
        try {
            coreObjectNode = getObjectNode.invoke(source);
        } catch (final InvocationTargetException ex) {
            final Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw new IllegalStateException("DataSourceImpl#getObjectNode() threw unexpected Exception.", targetException);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException("DataSourceImpl#getObjectNode() is not accessible.", ex);
        }

        return ObjectNodeRebuilder.rebuild(coreObjectNode, mapper);
    }

    private static Method getToJsonMethod(final Class<? extends DataSource> coreDataSourceImplClass) {
        try {
            return coreDataSourceImplClass.getMethod("toJson");
        } catch (final NoSuchMethodException ex) {
            // Expected: toJson is not defined in Embulk v0.10.2 or earlier.
            return null;
        }
    }

    private static Method getGetObjectNodeMethod(final Class<? extends DataSource> coreDataSourceImplClass) {
        try {
            return coreDataSourceImplClass.getMethod("getObjectNode");
        } catch (final NoSuchMethodException ex) {
            // Expected; getObjectNode may be implemented only in org.embulk.config.DataSourceImpl of earlier Embulk.
            return null;
        }
    }

    private static final ObjectMapper SIMPLE_MAPPER = new ObjectMapper();
}
