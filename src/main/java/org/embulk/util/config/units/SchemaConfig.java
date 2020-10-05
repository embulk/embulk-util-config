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

package org.embulk.util.config.units;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import org.embulk.spi.Column;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfigException;
import org.embulk.spi.type.Type;

public class SchemaConfig {
    private final List<ColumnConfig> columns;

    @JsonCreator
    public SchemaConfig(List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @JsonValue
    public List<ColumnConfig> getColumns() {
        return columns;
    }

    public int size() {
        return columns.size();
    }

    public int getColumnCount() {
        return columns.size();
    }

    public ColumnConfig getColumn(int index) {
        return columns.get(index);
    }

    public String getColumnName(int index) {
        return getColumn(index).getName();
    }

    public Type getColumnType(int index) {
        return getColumn(index).getType();
    }

    public boolean isEmpty() {
        return columns.isEmpty();
    }

    public ColumnConfig lookupColumn(String name) {
        for (ColumnConfig c : columns) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        throw new SchemaConfigException(String.format("Column '%s' is not found", name));
    }

    public Schema toSchema() {
        ImmutableList.Builder<Column> builder = ImmutableList.builder();
        for (int i = 0; i < columns.size(); i++) {
            builder.add(columns.get(i).toColumn(i));
        }
        return new Schema(builder.build());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SchemaConfig)) {
            return false;
        }
        SchemaConfig other = (SchemaConfig) obj;
        return Objects.equals(columns, other.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(columns);
    }
}
