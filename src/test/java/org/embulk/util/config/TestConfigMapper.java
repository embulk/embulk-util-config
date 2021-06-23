/*
 * Copyright 2021 The Embulk project
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestConfigMapper {
    @Test
    public void testBuildExceptionMessage() {
        assertEquals(
                "Failed to map Embulk's ConfigSource to org.embulk.util.config.TestConfigMapper: foobar",
                ConfigMapper.buildExceptionMessage(new RuntimeException("foobar"), TestConfigMapper.class));
        assertEquals(
                "Failed to map Embulk's ConfigSource to org.embulk.util.config.TestConfigMapper.",
                ConfigMapper.buildExceptionMessage(new RuntimeException(), TestConfigMapper.class));
    }
}
