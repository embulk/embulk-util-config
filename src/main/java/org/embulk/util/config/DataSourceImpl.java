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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;
import org.embulk.config.DataSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;

/**
 * An implementation of {@code org.embulk.config.DataSource} for instances created mainly from {@link Task#dump()} and
 * {@link Task#toTaskSource()}.
 */
final class DataSourceImpl implements ConfigSource, TaskSource, TaskReport, ConfigDiff {
    DataSourceImpl(final ObjectNode data, final ObjectMapper objectMapper) {
        this.data = data;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> getAttributeNames() {
        final ArrayList<String> copy = new ArrayList<>();
        this.data.fieldNames().forEachRemaining(copy::add);
        return Collections.unmodifiableList(copy);
    }

    @Override
    public boolean isEmpty() {
        return !this.data.fieldNames().hasNext();
    }

    @Override
    public boolean has(final String attrName) {
        return this.data.has(attrName);
    }

    @Override
    public <E> E get(final Class<E> type, final String attrName) {
        final JsonNode json = this.data.get(attrName);
        if (json == null) {
            throw new ConfigException("Attribute " + attrName + " is required but not set.");
        }
        try {
            return this.objectMapper.readValue(json.traverse(), type);
        } catch (final IOException ex) {
            throw new ConfigException(ex);
        }
    }

    @Override
    public <E> E get(final Class<E> type, final String attrName, final E defaultValue) {
        final JsonNode json = this.data.get(attrName);
        if (json == null) {
            return defaultValue;
        }
        try {
            return this.objectMapper.readValue(json.traverse(), type);
        } catch (final IOException ex) {
            throw new ConfigException(ex);
        }
    }

    @Override
    public DataSourceImpl getNested(final String attrName) {
        final JsonNode json = this.data.get(attrName);
        if (json == null) {
            throw new ConfigException("Attribute " + attrName + " is required but not set.");
        }
        if (!json.isObject()) {
            throw new ConfigException("Attribute " + attrName + " must be an object.");
        }
        return new DataSourceImpl((ObjectNode) json, this.objectMapper);
    }

    @Override
    public DataSourceImpl getNestedOrSetEmpty(final String attrName) {
        final JsonNode json = this.data.get(attrName);
        if (json == null) {
            final ObjectNode object = this.data.objectNode();
            object.set(attrName, json);
            return new DataSourceImpl(object, this.objectMapper);
        }
        if (!json.isObject()) {
            throw new ConfigException("Attribute " + attrName + " must be an object.");
        }
        return new DataSourceImpl((ObjectNode) json, this.objectMapper);
    }

    @Override
    public DataSourceImpl getNestedOrGetEmpty(final String attrName) {
        final JsonNode json = this.data.get(attrName);
        if (json == null) {
            final ObjectNode object = this.data.objectNode();
            return new DataSourceImpl(object, this.objectMapper);
        }
        if (!json.isObject()) {
            throw new ConfigException("Attribute " + attrName + " must be an object.");
        }
        return new DataSourceImpl((ObjectNode) json, this.objectMapper);
    }

    @Override
    public DataSourceImpl set(final String attrName, final Object v) {
        if (v == null) {
            this.remove(attrName);
        } else {
            this.data.set(attrName, this.objectMapper.valueToTree(v));
        }
        return this;
    }

    @Override
    public DataSourceImpl setNested(final String attrName, final DataSource v) {
        if (v == null) {
            this.data.set(attrName, null);
        } else {
            final String vJsonStringified;
            try {
                vJsonStringified = Compat.toJson(v);  // TODO: DataSource#toJson
            } catch (final IOException ex) {
                throw new ConfigException("Unexpected failure in stringifying DataSource as JSON.", ex);
            }
            if (vJsonStringified == null) {
                throw new ConfigException(new NullPointerException("DataSource#setNested accepts only valid DataSource."));
            }
            final JsonNode vJsonNode;
            try {
                vJsonNode = this.objectMapper.readValue(vJsonStringified, JsonNode.class);
            } catch (final IOException ex) {
                throw new ConfigException(ex);
            }
            if (!vJsonNode.isObject()) {
                throw new ConfigException(new ClassCastException("DataSource#setNested accepts only valid JSON object."));
            }
            this.data.set(attrName, (ObjectNode) vJsonNode);
        }
        return this;
    }

    @Override
    public DataSourceImpl setAll(final DataSource other) {
        if (other == null) {
            throw new ConfigException(new NullPointerException("DataSource#setAll accepts only non-null value."));
        }
        final String otherJsonStringified;
        try {
            otherJsonStringified = Compat.toJson(other);  // TODO: DataSource#toJson
        } catch (final IOException ex) {
            throw new ConfigException("Unexpected failure in stringifying DataSource as JSON.", ex);
        }
        if (otherJsonStringified == null) {
            throw new ConfigException(new NullPointerException("DataSource#setAll accepts only valid DataSource."));
        }
        final JsonNode otherJsonNode;
        try {
            otherJsonNode = this.objectMapper.readValue(otherJsonStringified, JsonNode.class);
        } catch (final IOException ex) {
            throw new ConfigException(ex);
        }
        if (!otherJsonNode.isObject()) {
            throw new ConfigException(new ClassCastException("DataSource#setAll accepts only valid JSON object."));
        }
        final ObjectNode otherObjectNode = (ObjectNode) otherJsonNode;
        for (final Map.Entry<String, JsonNode> field : (Iterable<Map.Entry<String, JsonNode>>) () -> otherObjectNode.fields()) {
            this.data.set(field.getKey(), field.getValue());
        }
        return this;
    }

    @Override
    public DataSourceImpl remove(final String attrName) {
        this.data.remove(attrName);
        return this;
    }

    @Override
    public DataSourceImpl deepCopy() {
        return new DataSourceImpl(this.data.deepCopy(), this.objectMapper);
    }

    @Override
    public DataSourceImpl merge(final DataSource other) {
        if (other == null) {
            throw new ConfigException(new NullPointerException("DataSource#merge accepts only non-null value."));
        }
        final String otherJsonStringified;
        try {
            otherJsonStringified = Compat.toJson(other);  // DataSource#toJson
        } catch (final IOException ex) {
            throw new ConfigException("Unexpected failure in stringifying DataSource as JSON.", ex);
        }
        if (otherJsonStringified == null) {
            throw new ConfigException(new NullPointerException("DataSource#merge accepts only valid DataSource."));
        }
        final JsonNode otherJsonNode;
        try {
            otherJsonNode = this.objectMapper.readValue(otherJsonStringified, JsonNode.class);
        } catch (final IOException ex) {
            throw new ConfigException(ex);
        }
        if (!otherJsonNode.isObject()) {
            throw new ConfigException(new ClassCastException("DataSource#setAll accepts only valid JSON object."));
        }
        mergeJsonObject(data, (ObjectNode) otherJsonNode);
        return this;
    }

    /**
     * Implements {@code DataSource#toJson} for Embulk v0.10.3+.
     *
     * <p>Embulk v0.10.2 or earlier does not have {@code DataSource#toJson} -- it should not {@code @Override} for the time being.
     */
    // TODO: Enable @Override after most plugins start using embulk-util-config.
    // @Override
    public String toJson() {
        try {
            return this.objectMapper.writeValueAsString(this.data);
        } catch (final JsonProcessingException ex) {
            throw new ConfigException(ex);
        }
    }

    /**
     * Implemented for compatibility with older {@code ConfigSource#loadConfig}.
     */
    // TODO: Remove this implementation after most plugins start using embulk-util-config.
    @Deprecated
    // @Override
    public <T> T loadConfig(final Class<T> taskType) {
        throw new UnsupportedOperationException("org.embulk.util.config.DataSourceImpl does not implement loadConfig.");
    }

    /**
     * Implemented for compatibility with older {@code ConfigSource#loadTask}.
     */
    // TODO: Remove this implementation after most plugins start using embulk-util-config.
    @Deprecated
    // @Override
    public <T> T loadTask(final Class<T> taskType) {
        try {
            return this.objectMapper.readValue(this.data.traverse(), taskType);
        } catch (final IOException ex) {
            throw new ConfigException(ex);
        }
    }

    @Override
    public String toString() {
        return this.data.toString();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof DataSource)) {
            return false;
        }
        final DataSource otherDataSource = (DataSource) other;
        final String otherJsonStringified;
        try {
            otherJsonStringified = Compat.toJson(otherDataSource);  // TODO: DataSource#toJson
        } catch (final IOException ex) {
            throw new ConfigException("Unexpected failure in stringifying DataSource as JSON.", ex);
        }
        if (otherJsonStringified == null) {
            return false;
        }
        final JsonNode otherJsonNode;
        try {
            otherJsonNode = this.objectMapper.readValue(otherJsonStringified, JsonNode.class);
        } catch (final IOException ex) {
            return false;
        }
        if (!otherJsonNode.isObject()) {
            return false;
        }
        return this.data.equals((ObjectNode) otherJsonNode);
    }

    @Override
    public int hashCode() {
        return this.data.hashCode();
    }

    private static void mergeJsonObject(final ObjectNode src, final ObjectNode other) {
        final Iterator<Map.Entry<String, JsonNode>> it = other.fields();
        while (it.hasNext()) {
            final Map.Entry<String, JsonNode> pair = it.next();
            final JsonNode s = src.get(pair.getKey());
            final JsonNode v = pair.getValue();

            if (v.isObject() && s != null && s.isObject()) {
                mergeJsonObject((ObjectNode) s, (ObjectNode) v);
            } else if (v.isArray() && s != null && s.isArray()) {
                mergeJsonArray((ArrayNode) s, (ArrayNode) v);
            } else {
                src.replace(pair.getKey(), v);
            }
        }
    }

    private static void mergeJsonArray(final ArrayNode src, final ArrayNode other) {
        for (int i = 0; i < other.size(); i++) {
            JsonNode s = src.get(i);
            JsonNode v = other.get(i);
            if (s == null) {
                src.add(v);
            } else if (v.isObject() && s.isObject()) {
                mergeJsonObject((ObjectNode) s, (ObjectNode) v);
            } else if (v.isArray() && s.isArray()) {
                mergeJsonArray((ArrayNode) s, (ArrayNode) v);
            } else {
                src.remove(i);
                src.insert(i, v);
            }
        }
    }

    private final ObjectNode data;
    private final ObjectMapper objectMapper;
}
