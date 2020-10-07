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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TestLegacyZones {
    /**
     * Tests short time zone names included in Ruby's zonetab.list.
     *
     * @see <a href="https://svn.ruby-lang.org/cgi-bin/viewvc.cgi/tags/v2_5_0/ext/date/zonetab.list?view=markup">zonetab.list</a>
     */
    @ParameterizedTest
    @CsvSource({
            "ut,   0*3600",
            "gmt,  0*3600",
            "est, -5*3600",
            "edt, -4*3600",
            "cst, -6*3600",
            "cdt, -5*3600",
            "mst, -7*3600",
            "mdt, -6*3600",
            "pst, -8*3600",
            "pdt, -7*3600",

            "wet,  0*3600",
            "at,  -2*3600",
            "brst,-2*3600",
            "ndt, -(2*3600+1800)",
            "art, -3*3600",
            "adt, -3*3600",
            "brt, -3*3600",
            "clst,-3*3600",
            "nst, -(3*3600+1800)",
            "ast, -4*3600",
            "clt, -4*3600",
            "akdt,-8*3600",
            "ydt, -8*3600",
            "akst,-9*3600",
            "hadt,-9*3600",
            "hdt, -9*3600",
            "yst, -9*3600",
            "ahst,-10*3600",
            "cat,-10*3600",
            "hast,-10*3600",
            "hst,-10*3600",
            "nt,  -11*3600",
            "idlw,-12*3600",
            "bst,  1*3600",
            "cet,  1*3600",
            "fwt,  1*3600",
            "met,  1*3600",
            "mewt, 1*3600",
            "mez,  1*3600",
            "swt,  1*3600",
            "wat,  1*3600",
            "west, 1*3600",
            "cest, 2*3600",
            "eet,  2*3600",
            "fst,  2*3600",
            "mest, 2*3600",
            "mesz, 2*3600",
            "sast, 2*3600",
            "sst,  2*3600",
            "bt,   3*3600",
            "eat,  3*3600",
            "eest, 3*3600",
            "msk,  3*3600",
            "msd,  4*3600",
            "zp4,  4*3600",
            "zp5,  5*3600",
            "ist,  (5*3600+1800)",
            "zp6,  6*3600",
            "wast, 7*3600",
            "cct,  8*3600",
            "sgt,  8*3600",
            "wadt, 8*3600",
            "jst,  9*3600",
            "kst,  9*3600",
            "east,10*3600",
            "gst, 10*3600",
            "eadt,11*3600",
            "idle,12*3600",
            "nzst,12*3600",
            "nzt, 12*3600",
            "nzdt,13*3600",
    })
    public void testShortNamesFromRuby(final String name, final String expectedOffsetInString) {
        final Optional<ZoneId> javaId = LegacyZones.getAlternative(name);
        final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone(name.toUpperCase(Locale.ROOT));
        assertTrue(javaId.isPresent());
        assertZoneId(jodaId, javaId);
    }

    /**
     * Tests short time zone names included in ancient {@code java.util.TimeZone}.
     *
     * @see <a href="http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java">TimeZone</a>
     */
    @ParameterizedTest
    @CsvSource({
        /* "GMT Standard Time",              */  "GMT",
        /* "Romance Standard Time",          */  "ECT",
        /* "Egypt Standard Time",            */  "EET",
        /* "Saudi Arabia Standard Time",     */  "EAT",
        /* "Iran Standard Time",             */  "MET",
        /* "Arabian Standard Time",          */  "NET",
        /* "West Asia Standard Time",        */  "PLT",
        /* "India Standard Time",            */  "IST",
        /* "Central Asia Standard Time",     */  "BST",
        /* "Bangkok Standard Time",          */  "VST",
        /* "China Standard Time",            */  "CTT",
        /* "Tokyo Standard Time",            */  "JST",
        /* "Cen. Australia Standard Time",   */  "ACT",
        /* "Sydney Standard Time",           */  "AET",
        /* "Central Pacific Standard Time",  */  "SST",
        /* "New Zealand Standard Time",      */  "NST",
        /* "Samoa Standard Time",            */  "MIT",
        /* "Hawaiian Standard Time",         */  "HST",
        /* "Alaskan Standard Time",          */  "AST",
        /* "Pacific Standard Time",          */  "PST",
        /* "US Mountain Standard Time",      */  "MST",
        /* "Central Standard Time",          */  "CST",
        /* "Eastern Standard Time",          */  "EST",
        /* "Atlantic Standard Time",         */  "PRT",
        /* "Newfoundland Standard Time",     */  "CNT",
        /* "SA Eastern Standard Time",       */  "AGT",
        /* "E. South America Standard Time", */  "BET",
        /* "Azores Standard Time",           */  "CAT",

        // GMT is the ID for Greenwich Mean Time time zone.
        /*GMT+0*/ "GMT",
        // ECT is the ID for European Central Time time zone.
        /*GMT+1*/ "ECT",
        // EET is the ID for Eastern European Time time zone.
        /*GMT+2*/ "EET",
        // ART is the ID for (Arabic) Egypt Standard Time timezone.
        /*GMT+2*/ "ART",
        // EAT is the ID for Eastern African Time time zone.
        /*GMT+3*/ "EAT",
        // MET is the ID for Middle East Time time zone.
        /*GMT+0330*/ "MET",
        // NET is the ID for Near East Time time zone.
        /*GMT+4*/ "NET",
        // PLT is the ID for Pakistan Lahore Time time zone.
        /*GMT+5*/ "PLT",
        // IST is the ID for India Standard Time time zone.
        /*GMT+0550*/ "IST",
        // BST is the ID for Bangladesh Standard Time time zone.
        /*GMT+6*/ "BST",
        // VST is the ID for Vietnam Standard Time time zone.
        /*GMT+7*/ "VST",
        // CTT is the ID for China Taiwan Time time zone.
        /*GMT+8*/ "CTT",
        // JST is the ID for Japan Standard Time time zone.
        /*GMT+9*/ "JST",
        // ACT is the ID for Australia Central Time time zone.
        /*GMT+0930*/ "ACT",
        // AET is the ID for Australia Eastern Time time zone.
        /*GMT+10*/ "AET",
        // SST is the ID for Solomon Standard Time time zone.
        /*GMT+11*/ "SST",
        // NST is the ID for New Zealand Standard Time time zone.
        /*GMT+12*/ "NST",
        // MIT is the ID for Midway Islands Time time zone.
        /*GMT-11*/ "MIT",
        // HST is the ID for Hawaii Standard Time time zone.
        /*GMT-10*/ "HST",
        // AST is the ID for Alaska Standard Time time zone.
        /*GMT-9*/ "AST",
        // PST is the ID for Pacific Standard Time time zone.
        /*GMT-8*/ "PST",
        // PNT is the ID for Phoenix Standard Time time zone.
        /*GMT-7*/ "PNT",
        // MST is the ID for Mountain Standard Time time zone.
        /*GMT-7*/ "MST",
        // CST is the ID for Central Standard Time time zone.
        /*GMT-6*/ "CST",
        // EST is the ID for Eastern Standard Time time zone.
        /*GMT-5*/ "EST",
        // IET is the ID for Indiana Eastern Standard Time time zone.
        /*GMT-5*/ "IET",
        // PRT is the ID for Puerto Rico and US Virgin Islands Time time zone.
        /*GMT-4*/ "PRT",
        // CNT is the ID for Canada Newfoundland Time time zone.
        /*GMT-0330*/ "CNT",
        // AGT is the ID for Argentina Standard Time time zone.
        /*GMT-3*/ "AGT",
        // BET is the ID for Brazil Eastern Time time zone.
        /*GMT-3*/ "BET",
        // CAT is the ID for Central African Time time zone.
        /*GMT-1*/ "CAT",
    })
    public void testShortNamesFromJava1_1_6(final String name) {
        final Optional<ZoneId> javaId = LegacyZones.getAlternative(name);
        final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone(name);
        assertZoneId(jodaId, javaId);
    }

    /**
     * Tests short time zone names included in {@code java.util.calendar.ZoneInfoFile}.
     *
     * @see <a href="https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java">ZoneInfoFile</a>
     */
    @ParameterizedTest
    @CsvSource({
            "ACT",
            "AET",
            "AGT",
            "ART",
            "AST",
            "BET",
            "BST",
            "CAT",
            "CNT",
            "CST",
            "CTT",
            "EAT",
            "ECT",
            "IET",
            "IST",
            "JST",
            "MIT",
            "NET",
            "NST",
            "PLT",
            "PNT",
            "PRT",
            "PST",
            "SST",
            "VST",
            "EST",
            "MST",
            "HST",
    })
    public void testShortNamesFromJava(final String name) {
        final Optional<ZoneId> javaId = LegacyZones.getAlternative(name);
        final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone(name);
        assertZoneId(jodaId, javaId);
    }

    /**
     * Tests short time zone names included in tzdb.
     *
     * @see <a href="https://svn.ruby-lang.org/cgi-bin/viewvc.cgi/tags/v2_5_0/ext/date/zonetab.list?view=markup">zonetab.list</a>
     */
    @ParameterizedTest
    @CsvSource({
            "CST6CDT",
            "EST5EDT",
            "CET",
            "EET",
            "EST",
            "MET",
            "MST",
            "MST7MDT",
            "PST8PDT",
            "WET",
    })
    public void testShortNamesInTzdb(final String name) {
        final Optional<ZoneId> javaId = LegacyZones.getAlternative(name);
        final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone(name);
        assertTrue(javaId.isPresent());
        assertZoneId(jodaId, javaId);
    }

    @ParameterizedTest
    @CsvSource({
            "Cuba",
            "Egypt",
            "Eire",
            "GB",
            "GB-Eire",
            "GMT",
            "GMT+0",
            "GMT-0",
            "GMT0",
            "Greenwich",
            "HST",
            "Hongkong",
            "Iceland",
            "Iran",
            "Israel",
            "Jamaica",
            "Japan",
            "Kwajalein",
            "Libya",
            "NZ",
            "NZ-CHAT",
            "Navajo",
            "PRC",
            "Poland",
            "Portugal",
            "ROC",
            "ROK",
            "Singapore",
            "Turkey",
            "UCT",
            "Universal",
            "W-SU",
            "Zulu",
    })
    public void testBackwardRegionNamesWithoutSlash(final String name) {
        final Optional<ZoneId> javaId = LegacyZones.getAlternative(name);
        final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone(name);
        assertZoneId(jodaId, javaId);
    }

    /**
     * Tests military time zone names included in Ruby's zonetab.list.
     *
     * @see <a href="https://svn.ruby-lang.org/cgi-bin/viewvc.cgi/tags/v2_5_0/ext/date/zonetab.list?view=markup">zonetab.list</a>
     */
    @ParameterizedTest
    @CsvSource({
            "a,    1*3600",
            "b,    2*3600",
            "c,    3*3600",
            "d,    4*3600",
            "e,    5*3600",
            "f,    6*3600",
            "g,    7*3600",
            "h,    8*3600",
            "i,    9*3600",
            "k,   10*3600",
            "l,   11*3600",
            "m,   12*3600",
            "n,   -1*3600",
            "o,   -2*3600",
            "p,   -3*3600",
            "q,   -4*3600",
            "r,   -5*3600",
            "s,   -6*3600",
            "t,   -7*3600",
            "u,   -8*3600",
            "v,   -9*3600",
            "w,  -10*3600",
            "x,  -11*3600",
            "y,  -12*3600",
            "z,    0*3600",
    })
    public void testMilitaryZoneNamesFromRuby(final String name, final String expectedOffsetInString) {
        final Optional<ZoneId> javaId = LegacyZones.getAlternative(name);
        final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone(name.toUpperCase(Locale.ROOT));
        assertTrue(javaId.isPresent());
        assertZoneId(jodaId, javaId);
    }

    /**
     * Tests region names included in Ruby's zonetab.list.
     *
     * @see <a href="https://svn.ruby-lang.org/cgi-bin/viewvc.cgi/tags/v2_5_0/ext/date/zonetab.list?view=markup">zonetab.list</a>
     */
    @ParameterizedTest
    @CsvSource({
            "afghanistan,             16200",
            "alaskan,                -32400",
            "arab,                    10800",
            "arabian,                 14400",
            "arabic,                  10800",
            "atlantic,               -14400",
            "aus central,             34200",
            "aus eastern,             36000",
            "azores,                  -3600",
            "canada central,         -21600",
            "cape verde,              -3600",
            "caucasus,                14400",
            "cen. australia,          34200",
            "central america,        -21600",
            "central asia,            21600",
            "central europe,           3600",
            "central european,         3600",
            "central pacific,         39600",
            "central,                -21600",
            "china,                   28800",
            "dateline,               -43200",
            "e. africa,               10800",
            "e. australia,            36000",
            "e. europe,                7200",
            "e. south america,       -10800",
            "eastern,                -18000",
            "egypt,                    7200",
            "ekaterinburg,            18000",
            "fiji,                    43200",
            "fle,                      7200",
            "greenland,              -10800",
            "greenwich,                   0",
            "gtb,                      7200",
            "hawaiian,               -36000",
            "india,                   19800",
            "iran,                    12600",
            "jerusalem,                7200",
            "korea,                   32400",
            "mexico,                 -21600",
            "mid-atlantic,            -7200",
            "mountain,               -25200",
            "myanmar,                 23400",
            "n. central asia,         21600",
            "nepal,                   20700",
            "new zealand,             43200",
            "newfoundland,           -12600",
            "north asia east,         28800",
            "north asia,              25200",
            "pacific sa,             -14400",
            "pacific,                -28800",
            "romance,                  3600",
            "russian,                 10800",
            "sa eastern,             -10800",
            "sa pacific,             -18000",
            "sa western,             -14400",
            "samoa,                  -39600",
            "se asia,                 25200",
            "malay peninsula,         28800",
            "south africa,             7200",
            "sri lanka,               21600",
            "taipei,                  28800",
            "tasmania,                36000",
            "tokyo,                   32400",
            "tonga,                   46800",
            "us eastern,             -18000",
            "us mountain,            -25200",
            "vladivostok,             36000",
            "w. australia,            28800",
            "w. central africa,        3600",
            "w. europe,                3600",
            "west asia,               18000",
            "west pacific,            36000",
            "yakutsk,                 32400",
    })
    public void testRegionNamesFromRuby(final String name, final String expectedOffsetInString) {
        final String[] suffixes = { "", " standard time", " daylight time", " dst" };
        for (final String suffix : suffixes) {
            final Optional<ZoneId> javaId = LegacyZones.getAlternative(name + suffix);
            final DateTimeZone jodaId = JodaDateTimeZones.parseJodaDateTimeZone((name + suffix).toUpperCase(Locale.ROOT));
            assertTrue(javaId.isPresent());
            assertZoneId(jodaId, javaId);
        }
    }

    @Test
    public void testSuggestions() {
        assertEquals(Optional.of("CET is deprecated as a short time zone name. Use +01:00, +02:00, Europe/Paris, "
                                     + "Europe/Berlin, Africa/Algiers, or else as needed instead for Central European Time."),
                     LegacyZones.getSuggestion("CET"));
        assertEquals(Optional.of("F is deprecated as a military time zone name. Use +06:00 instead."),
                     LegacyZones.getSuggestion("F"));
        assertEquals(Optional.of("Z is deprecated as a military time zone name. Use UTC instead."),
                     LegacyZones.getSuggestion("Z"));
        assertEquals(Optional.of("TAIPEI STANDARD TIME is deprecated as a time zone name. Use +08:00 instead."),
                     LegacyZones.getSuggestion("Taipei Standard time"));
        assertEquals(Optional.empty(),
                     LegacyZones.getSuggestion("Japan Standard time"));
    }

    @Test
    public void testJodaHstIsFixed() {
        final DateTimeZone hst = DateTimeZone.forID("HST");
        assertTrue(hst.isFixed());
        assertEquals(hst.getOffset(new DateTime(2020, 3, 1, 0, 0, 0, DateTimeZone.UTC)),
                     hst.getOffset(new DateTime(2020, 9, 1, 0, 0, 0, DateTimeZone.UTC)));
        assertEquals(hst.getOffset(new DateTime(2020, 3, 1, 0, 0, 0, DateTimeZone.UTC)),
                     hst.getOffset(new DateTime(1946, 3, 1, 0, 0, 0, DateTimeZone.UTC)));
        assertEquals(hst.getOffset(new DateTime(2020, 3, 1, 0, 0, 0, DateTimeZone.UTC)),
                     hst.getOffset(new DateTime(1946, 9, 1, 0, 0, 0, DateTimeZone.UTC)));

        final DateTimeZone minusTen = DateTimeZone.forID("-10:00");
        assertEquals(hst.getOffset(new DateTime(2020, 3, 1, 0, 0, 0, DateTimeZone.UTC)),
                     minusTen.getOffset(new DateTime(2020, 9, 1, 0, 0, 0, DateTimeZone.UTC)));
        assertEquals(hst.getOffset(new DateTime(2020, 3, 1, 0, 0, 0, DateTimeZone.UTC)),
                     minusTen.getOffset(new DateTime(1946, 3, 1, 0, 0, 0, DateTimeZone.UTC)));
        assertEquals(hst.getOffset(new DateTime(2020, 3, 1, 0, 0, 0, DateTimeZone.UTC)),
                     minusTen.getOffset(new DateTime(1946, 9, 1, 0, 0, 0, DateTimeZone.UTC)));
    }

    @Test
    public void testJodaCetIsEqualToJavaCet() {
        final DateTimeZone jodaCet = DateTimeZone.forID("CET");
        assertFalse(jodaCet.isFixed());
        final ZoneId javaCet = ZoneId.of("CET");
        assertFalse(javaCet.getRules().isFixedOffset());

        for (long instant = OffsetDateTime.of(1950, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant().getEpochSecond();
                instant < OffsetDateTime.of(2020, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC).toInstant().getEpochSecond();
                instant += 1800) {
            final org.joda.time.Instant jodaInstant = new org.joda.time.Instant(instant * 1000);
            final java.time.Instant javaInstant = java.time.Instant.ofEpochSecond(instant);
            assertEquals(jodaCet.getOffset(jodaInstant),
                         javaCet.getRules().getOffset(javaInstant).getTotalSeconds() * 1000);
        }
    }

    private static void assertZoneId(final DateTimeZone jodaId, final Optional<ZoneId> javaId) {
        if (jodaId == null && !javaId.isPresent()) {
            return;
        }
        if (jodaId == null || !javaId.isPresent()) {
            assertEquals(jodaId, javaId.orElse(null));
        }
        if (isUtc(jodaId) && javaId.get().equals(ZoneOffset.UTC)) {
            return;
        }
        if (jodaId.equals(DateTimeZone.forID("HST")) && javaId.get().equals(ZoneOffset.of("-10:00"))) {
            // Joda:HST == JSR310:-10:00 is accepted as a special case.
            return;
        }
        assertEquals(jodaId.toString(), javaId.get().toString());
    }

    private static boolean isUtc(final DateTimeZone jodaId) {
        if (jodaId.equals(DateTimeZone.UTC)
                || jodaId.equals(DateTimeZone.forID("Etc/GMT"))
                || jodaId.equals(DateTimeZone.forID("Etc/UCT"))
                || jodaId.equals(DateTimeZone.forID("Etc/UTC"))) {
            return true;
        }
        return false;
    }
}
