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

package org.embulk.spi;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Objects;
import org.embulk.config.ConfigSource;
import org.embulk.spi.type.TimestampType;
import org.embulk.spi.type.Type;

public class ColumnConfig {
    private final String name;
    private final Type type;
    private final ConfigSource option;

    @Deprecated
    public ColumnConfig(String name, Type type, String format) {
        this.name = name;
        this.type = type;
        this.option = Exec.newConfigSource();  // only for backward compatibility
        if (format != null) {
            option.set("format", format);
        }
    }

    public ColumnConfig(String name, Type type, ConfigSource option) {
        this.name = name;
        this.type = type;
        this.option = option;
    }

    @JsonCreator
    public ColumnConfig(ConfigSource conf) {
        this.name = conf.get(String.class, "name");
        this.type = conf.get(Type.class, "type");
        this.option = conf.deepCopy();
        option.remove("name");
        option.remove("type");
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public ConfigSource getOption() {
        return option;
    }

    @JsonValue
    public ConfigSource getConfigSource() {
        ConfigSource conf = option.deepCopy();
        conf.set("name", name);
        conf.set("type", type);
        return conf;
    }

    @Deprecated
    public String getFormat() {
        return option.get(String.class, "format", null);
    }

    // TODO: Stop using TimestampType.withFormat.
    @SuppressWarnings("deprecation")  // https://github.com/embulk/embulk/issues/935
    public Column toColumn(int index) {
        String format = option.get(String.class, "format", null);
        if (type instanceof TimestampType && format != null) {
            // this behavior is only for backward compatibility. TimestampType#getFormat is @Deprecated
            return new Column(index, name, ((TimestampType) type).withFormat(format));
        } else {
            return new Column(index, name, type);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ColumnConfig)) {
            return false;
        }
        ColumnConfig other = (ColumnConfig) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(type, other.type)
                && Objects.equals(option, other.option);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return String.format("ColumnConfig[%s, %s]",
                getName(), getType());
    }
}
