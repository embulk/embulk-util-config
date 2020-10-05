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
import java.util.Objects;
import org.embulk.config.ConfigSource;
import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.type.TimestampType;
import org.embulk.spi.type.Type;

public class ColumnConfig {
    @Deprecated
    public ColumnConfig(final String name, final Type type, final String format) {
        this.name = name;
        this.type = type;
        this.option = Exec.newConfigSource();  // only for backward compatibility
        if (format != null) {
            this.option.set("format", format);
        }
    }

    public ColumnConfig(final String name, final Type type, final ConfigSource option) {
        this.name = name;
        this.type = type;
        this.option = option;
    }

    @JsonCreator
    public ColumnConfig(final ConfigSource config) {
        this.name = config.get(String.class, "name");
        this.type = config.get(Type.class, "type");
        this.option = config.deepCopy();
        this.option.remove("name");
        this.option.remove("type");
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public ConfigSource getOption() {
        return this.option;
    }

    @JsonValue
    public ConfigSource getConfigSource() {
        final ConfigSource config = this.option.deepCopy();
        config.set("name", this.name);
        config.set("type", this.type);
        return config;
    }

    @Deprecated
    public String getFormat() {
        return this.option.get(String.class, "format", null);
    }

    // TODO: Stop using TimestampType.withFormat.
    @SuppressWarnings("deprecation")  // https://github.com/embulk/embulk/issues/935
    public Column toColumn(final int index) {
        final String format = this.option.get(String.class, "format", null);
        if (type instanceof TimestampType && format != null) {
            // this behavior is only for backward compatibility. TimestampType#getFormat is @Deprecated
            return new Column(index, this.name, ((TimestampType) this.type).withFormat(format));
        } else {
            return new Column(index, this.name, this.type);
        }
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (!(otherObject instanceof ColumnConfig)) {
            return false;
        }
        final ColumnConfig other = (ColumnConfig) otherObject;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.option, other.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.type);
    }

    @Override
    public String toString() {
        return String.format("ColumnConfig[%s, %s]", this.getName(), this.getType());
    }

    private final String name;
    private final Type type;
    private final ConfigSource option;
}
