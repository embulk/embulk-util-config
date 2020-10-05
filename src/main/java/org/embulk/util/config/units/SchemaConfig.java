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
    @JsonCreator
    public SchemaConfig(final List<ColumnConfig> columns) {
        this.columns = columns;
    }

    @JsonValue
    public List<ColumnConfig> getColumns() {
        return this.columns;
    }

    public int size() {
        return this.columns.size();
    }

    public int getColumnCount() {
        return this.columns.size();
    }

    public ColumnConfig getColumn(final int index) {
        return this.columns.get(index);
    }

    public String getColumnName(final int index) {
        return this.getColumn(index).getName();
    }

    public Type getColumnType(final int index) {
        return this.getColumn(index).getType();
    }

    public boolean isEmpty() {
        return this.columns.isEmpty();
    }

    public ColumnConfig lookupColumn(final String name) {
        for (final ColumnConfig column : this.columns) {
            if (column.getName().equals(name)) {
                return column;
            }
        }
        throw new SchemaConfigException(String.format("Column '%s' is not found", name));
    }

    public Schema toSchema() {
        final ImmutableList.Builder<Column> builder = ImmutableList.builder();
        for (int i = 0; i < this.columns.size(); i++) {
            builder.add(this.columns.get(i).toColumn(i));
        }
        return new Schema(builder.build());
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof SchemaConfig)) {
            return false;
        }
        final SchemaConfig other = (SchemaConfig) otherObject;
        return Objects.equals(this.columns, other.columns);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.columns);
    }

    private final List<ColumnConfig> columns;
}
