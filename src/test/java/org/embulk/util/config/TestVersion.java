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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class TestVersion {
    @Test
    public void testParseVersion() throws Exception {
        assertEquals(new com.fasterxml.jackson.core.Version(1, 2, 3, "SNAPSHOT", "org.embulk", "embulk-util-config"),
                     Version.parseVersionForTesting("1.2.3-SNAPSHOT"));
        assertEquals(new com.fasterxml.jackson.core.Version(14, 139, 0, "SNAPSHOT", "org.embulk", "embulk-util-config"),
                     Version.parseVersionForTesting("14.139-SNAPSHOT"));
        assertEquals(new com.fasterxml.jackson.core.Version(103, 0, 0, "SNAPSHOT-A", "org.embulk", "embulk-util-config"),
                     Version.parseVersionForTesting("103-SNAPSHOT-A"));
        assertEquals(new com.fasterxml.jackson.core.Version(5, 3, 1, "", "org.embulk", "embulk-util-config"),
                     Version.parseVersionForTesting("5.3.1"));
        assertEquals(new com.fasterxml.jackson.core.Version(9, 8, 0, "", "org.embulk", "embulk-util-config"),
                     Version.parseVersionForTesting("9.8"));
        assertEquals(new com.fasterxml.jackson.core.Version(4, 0, 0, "", "org.embulk", "embulk-util-config"),
                     Version.parseVersionForTesting("4"));

        assertThrows(IllegalArgumentException.class, () -> Version.parseVersionForTesting(""));
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersionForTesting("-SNAPSHOT"));
        assertThrows(NumberFormatException.class, () -> Version.parseVersionForTesting("12a.1.3"));
        assertThrows(IllegalArgumentException.class, () -> Version.parseVersionForTesting("9.7.5.3"));
    }

    @Test
    public void testValidVersion() throws Exception {
        // The version string (in build.gradle) is tested valid through
        // the system property "org.embulk.embulk_util_config.version"
        // that is set in Gradle's "test" task.
        assertFalse(Version.JACKSON_MODULE_VERSION.isUnknownVersion());
        System.out.println(Version.JACKSON_MODULE_VERSION.toString());
    }
}
