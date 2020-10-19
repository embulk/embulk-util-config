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

package org.embulk.util.config.modules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZoneId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestZoneIdModule {
    @ParameterizedTest
    @CsvSource({
            "UTC,UTC",
            "Etc/UTC,Etc/UTC",
            "+00:00,+00:00",
            "-00:00,-00:00",
            "+12:30,+12:30",
            "+12:30:39,+12:30:39",
            "America/Los_Angeles,America/Los_Angeles",
            "Asia/Ho_Chi_Minh,Asia/Ho_Chi_Minh",
            "Asia/Tokyo,Asia/Tokyo",
            "Europe/London,Europe/London",
            "Pacific/Apia,Pacific/Apia",
    })
    public void testSafe(final String expected, final String value) throws IOException {
        assertEquals(ZoneId.of(expected), LEGACY.readValue("\"" + value + "\"", ZoneId.class));
        assertEquals(ZoneId.of(expected), STRICT.readValue("\"" + value + "\"", ZoneId.class));
    }

    @ParameterizedTest
    @CsvSource({
            "Z,Z",
            "Z,UT",
            "Z,UCT",
            "Z,GMT",
            "+06:00,F",
            "+09:00,JST",
            "CET,CET",
            "+02:00,CEST",
            "-08:00,PST",
            "-08:00,Pst",
            "-08:00,pst",
            "-08:00,PDT",
            "-08:00,Pdt",
            "-08:00,pdt",
            "+03:30,iran",
            "Asia/Tehran,Iran",
            "+03:30,IRAN",
            "+02:00,egypt",
            "Africa/Cairo,Egypt",
            "+02:00,EGYPT",
            "+02:00,Egypt Standard Time",
            "+03:00,Egypt Daylight Time",
            "+03:00,Egypt DST",
            "Asia/Tokyo,Japan",
            "Asia/Taipei,ROC",
            "Asia/Seoul,ROK",
            "-10:00,HST",
    })
    public void testLegacy(final String expected, final String value) throws IOException {
        assertEquals(ZoneId.of(expected), LEGACY.readValue("\"" + value + "\"", ZoneId.class));
        assertThrows(JsonMappingException.class, () -> STRICT.readValue("\"" + value + "\"", ZoneId.class));
    }

    @ParameterizedTest
    @CsvSource({
            "+12:70",
            "+23:59",
            "J",
            "VST",
            "JAPAN",
            "Asia/Osaka",
            "unknown",
    })
    public void testBad(final String value) throws IOException {
        assertThrows(JsonMappingException.class, () -> LEGACY.readValue("\"" + value + "\"", ZoneId.class));
        assertThrows(JsonMappingException.class, () -> STRICT.readValue("\"" + value + "\"", ZoneId.class));
    }

    private static final ObjectMapper LEGACY;

    private static final ObjectMapper STRICT;

    static {
        LEGACY = new ObjectMapper();
        LEGACY.registerModule(ZoneIdModule.withLegacyNames());

        STRICT = new ObjectMapper();
        STRICT.registerModule(new ZoneIdModule());
    }
}
