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

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

final class LegacyZones {
    private LegacyZones() {}

    static Optional<String> getSuggestion(final String shortName) {
        if (SUGGESTIONS_CASE_SENSITIVE.containsKey(shortName)) {
            return Optional.of(SUGGESTIONS_CASE_SENSITIVE.get(shortName));
        }
        final String shortNameUpperCase = shortName.toUpperCase(Locale.ROOT);
        if ("UTC".equals(shortNameUpperCase)) {
            return Optional.empty();
        }
        return Optional.ofNullable(SUGGESTIONS_CASE_INSENSITIVE.get(shortNameUpperCase));
    }

    static Optional<ZoneId> getAlternative(final String shortName) {
        if (ALTERNATIVES_CASE_SENSITIVE.containsKey(shortName)) {
            return Optional.of(ALTERNATIVES_CASE_SENSITIVE.get(shortName));
        }
        final String shortNameUpperCase = shortName.toUpperCase(Locale.ROOT);
        if ("UTC".equals(shortNameUpperCase)) {
            return Optional.of(ZoneOffset.UTC);
        }
        return Optional.ofNullable(ALTERNATIVES_CASE_INSENSITIVE.get(shortNameUpperCase));
    }

    private static final Map<String, ZoneId> ALTERNATIVES_CASE_SENSITIVE;

    private static final Map<String, ZoneId> ALTERNATIVES_CASE_INSENSITIVE;

    private static final Map<String, String> SUGGESTIONS_CASE_SENSITIVE;

    private static final Map<String, String> SUGGESTIONS_CASE_INSENSITIVE;

    private static final Object[] CASE_SENSITIVE_SHORT_ZONE_NAMES = {
        // All capital "CDT" considered as Central Standard Time (-06:00) in legacy Embulk.
        //
        // "CDT" is widely acknowledged as Central Daylight Time (-05:00).
        //
        // Java and Ruby have recognized "CDT" as Central Daylight Time (-05:00) normally.
        //
        // Embulk has recognized "CDT" wrongly as Central Standard Time (-06:00) because Embulk has calculated its offset by:
        //   org.joda.time.format.DateTimeFormat.forPattern("z").parseMillis("CDT")
        //
        // If it contains a non-uppercase character, it is considered as -05:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L16
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L445
        //
        // Central Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cst
        //
        // Central Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cdt
        "CDT", ZoneOffset.of("-06:00"),
        "Use -05:00, -06:00, America/Chicago, America/Mexico_City, or else as needed instead for true Central Daylight Time. "
            + "Embulk has recognized CDT wrongly as Central Standard Time, and keeps it for compatibility in the legacy mode.",

        // All capital "CET" considered as "CET" (Central European Time) in legacy Embulk.
        //
        // It is literally ZoneId.of("CET"), which is different from ZoneOffset.of("+01:00").
        //
        // If it contains a non-uppercase character, it is considered as +01:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L71
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Central European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cet
        "CET", ZoneId.of("CET"),  // "CET" is accepted as ZoneId as-is.
        "Use +01:00, +02:00, Europe/Paris, Europe/Berlin, Africa/Algiers, or else as needed instead for Central European Time.",

        // All capital "EDT" considered as Eastern Standard Time (-05:00) in legacy Embulk.
        //
        // "EDT" is widely acknowledged as Eastern Daylight Time (-04:00).
        //
        // Java and Ruby have recognized "EDT" as Eastern Daylight Time (-04:00) normally.
        //
        // Embulk has recognized "EDT" wrongly as Eastern Standard Time (-05:00) because Embulk has calculated its offset by:
        //   org.joda.time.format.DateTimeFormat.forPattern("z").parseMillis("EDT")
        //
        // If it contains a non-uppercase character, it is considered as -04:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L14
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L443
        //
        // Eastern Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/est
        //
        // Eastern Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/edt
        "EDT", ZoneOffset.of("-05:00"),
        "Use -05:00, -04:00, America/New_York, America/Toronto, or else as needed instead for true Eastern Daylight Time. "
            + "Embulk has recognized EDT wrongly as Eastern Standard Time, and keeps it for compatibility in the legacy mode.",

        // All capital "EET" considered as "EET" (Eastern European Time) in legacy Embulk.
        //
        // It is literally ZoneId.of("EET"), which is different from ZoneOffset.of("+02:00").
        //
        // If it contains a non-uppercase character, it is considered as +02:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L80
        // * Java: N/A
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1311
        //
        // Eastern European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/eet
        "EET", ZoneId.of("EET"),  // "EET" is accepted as ZoneId as-is.
        "Use +02:00, +03:00, Europe/Bucharest, Europe/Athens, Africa/Cairo, or else as needed instead for Eastern European Time.",

        // All capital "MDT" considered as Mountain Standard Time (-07:00) in legacy Embulk.
        //
        // "MDT" is widely acknowledged as Mountain Daylight Time (-06:00).
        //
        // Java and Ruby have recognized "MDT" as Mountain Daylight Time (-06:00) normally.
        //
        // Embulk has recognized "MDT" wrongly as Mountain Standard Time (-07:00) because Embulk has calculated its offset by:
        //   org.joda.time.format.DateTimeFormat.forPattern("z").parseMillis("MDT")
        //
        // If it contains a non-uppercase character, it is considered as -06:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L18
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L447
        //
        // Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Mountain_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/mst
        //
        // Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Mountain_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/mdt
        "MDT", ZoneOffset.of("-07:00"),
        "Use -07:00, -06:00, America/Denver, America/Edmonton, or else as needed instead for true Mountain Daylight Time. "
            + "Embulk has recognized MDT wrongly as Mountain Standard Time, and keeps it for compatibility in the legacy mode.",

        // All capital "MET" considered as "MET" (Middle European Time) in legacy Embulk.
        //
        // It is literally ZoneId.of("MET"), which is different from ZoneOffset.of("+01:00").
        //
        // If it contains a non-uppercase character, it is considered as +01:00.
        //
        // Middle European Time is usually acknowledged as Central European Time (CET).
        //
        // Java recognized "MET" as Middle East Time (+03:30) for historical reasons as of 1.1.6 at least.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L73
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1309
        //
        // Central European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cet
        "MET", ZoneId.of("MET"),  // "MET" is accepted as ZoneId as-is.
        "Use +01:00, +02:00, Europe/Paris, Europe/Berlin, Africa/Algiers, or else as needed instead for Middle (Central) European Time. "
            + "Or, use +03:30, Asia/Tehran, or else as needed instead for Middle East Time, historical Java standard.",

        // All capital "PDT" considered as Pacific Standard Time (-08:00) in legacy Embulk.
        //
        // "PDT" is widely acknowledged as Pacific Daylight Time (-07:00).
        //
        // Java and Ruby have recognized "PDT" as Pacific Daylight Time (-07:00) normally.
        //
        // Embulk has recognized "PDT" wrongly as Pacific Standard Time (-08:00) because Embulk has calculated its offset by:
        //   org.joda.time.format.DateTimeFormat.forPattern("z").parseMillis("PDT")
        //
        // If it contains a non-uppercase character, it is considered as -07:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L20
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L449
        //
        // Pacific Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Pacific_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/pst
        //
        // Pacific Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Pacific_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/pdt
        "PDT", ZoneOffset.of("-08:00"),
        "Use -08:00, -07:00, America/Los_Angeles, America/Vancouver, or else as needed instead for true Pacific Daylight Time. "
            + "Embulk has recognized PDT wrongly as Pacific Standard Time, and keeps it for compatibility in the legacy mode.",

        // All capital "WET" considered as "WET" (Western European Time) in legacy Embulk.
        //
        // It is literally ZoneId.of("WET"), which is different from ZoneOffset.of("+00:00").
        //
        // If it contains a non-uppercase character, it is considered as +00:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L47
        // * Java: N/A
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1307
        //
        // Western European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Western_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/wet
        "WET", ZoneId.of("WET"),  // "WET" is accepted as ZoneId as-is.
        "Use +00:00, +01:00, Europe/Lisbon, Africa/Casablanca, or else as needed instead for Western European Time.",
    };

    private static final Object[] CASE_INSENSITIVE_SHORT_ZONE_NAMES = {
        // "ADT" considered as Atlantic Daylight Time (-03:00) in legacy Embulk.
        //
        // "ADT" might sometimes mean Arabia Daylight Time (+04:00) historically in general.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L52
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Atlantic Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Atlantic_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/adt
        //
        // Arabia Daylight Time:
        // * timeanddate.com: https://www.timeanddate.com/time/zones/adt-arabia
        "ADT", ZoneOffset.of("-03:00"),
        "Use -03:00, America/Halifax, Canada/Atlantic, or else as needed instead for Atlantic Daylight Time.",

        // "AHST" considered as Alaska-Hawaii Standard Time (-10:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L64
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Alaska-Hawaii Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Alaska_Time_Zone
        // * timeanddate.com: N/A
        "AHST", ZoneOffset.of("-10:00"),
        "Use -10:00, -09:00, Pacific/Honolulu, America/Adak, or else as needed instead for Alaska-Hawaii Standard Time.",

        // "AKDT" considered as Alaska Daylight Time (-08:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L58
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Alaska Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Alaska_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/akdt
        "AKDT", ZoneOffset.of("-08:00"),
        "Use -08:00, America/Anchorage, or else as needed instead for Alaska Daylight Time.",

        // "AKST" considered as Alaska Standard Time (-09:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L60
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Alaska Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Alaska_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/akst
        "AKST", ZoneOffset.of("-09:00"),
        "Use -09:00, -08:00, America/Anchorage, or else as needed instead for Alaska Standard Time.",

        // "ART" considered as Argentina Time in legacy Embulk: -03:00 came from Ruby's zonetab.
        //
        // "ART" is widely acknowledged as Argentina Time (-03:00). The same in Ruby. The same in Embulk.
        //
        // Java has recognized "ART" as Egypt Time (+02:00) for historical reasons since 1.1.6 at the latest.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L51
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L223
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1325
        //
        // Argentina Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Argentina
        // * timeanddate.com: https://www.timeanddate.com/time/zones/art
        "ART", ZoneOffset.of("-03:00"),
        "Use -03:00, America/Buenos_Aires, America/Cordoba, or else as needed instead for Argentina Time. "
            + "Or, use +02:00, Africa/Cairo, or else as needed instead for Egypt Time, historical Java standard.",

        // "AST" considered as Atlantic Standard Time (-04:00) in legacy Embulk.
        //
        // "AST" is widely acknowledged as Atlantic Standard Time (-04:00). The same in Ruby. The same in Embulk.
        //
        // Java has recognized "AST" as Alaska Standard Time (-09:00) for historical reasons since 1.1.6 at the latest.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L56
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L224
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1314
        //
        // Atlantic Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Atlantic_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/ast
        "AST", ZoneOffset.of("-04:00"),
        "Use -04:00, -03:00, America/Halifax, America/Puerto_Rico, Atlantic/Bermuda, or else as needed instead for Atlantic Standard Time. "
            + "Or, use -09:00, -08:00, America/Anchorage, or else as needed instead for Alaska Standard Time, historical Java standard.",

        // "AT" considered as obsolete Azores Time (-02:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L97
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Azores Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Azores
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-at.html
        // * MHonArc: https://www.mhonarc.org/MHonArc/doc/resources/timezones.html
        "AT", ZoneOffset.of("-02:00"),
        "Use -02:00, Atlantic/Azores, or else as needed instead for obsolete Azores Time.",

        // "BRST" considered as Brasilia Summer Time (-02:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L49
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Brasilia Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Daylight_saving_time_in_Brazil
        // * timeanddate.com: https://www.timeanddate.com/time/zones/brst
        "BRST", ZoneOffset.of("-02:00"),
        "Use -02:00, America/Sao_Paulo, or else as needed instead for Brasilia Summer Time.",

        // "BRT" considered as Brasilia Time (-03:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L53
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Brasilia Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Brazil
        // * timeanddate.com: https://www.timeanddate.com/time/zones/brt
        "BRT", ZoneOffset.of("-03:00"),
        "Use -03:00, -02:00, America/Sao_Paulo, or else as needed instead for Brasilia Time.",

        // "BST" considered as British Summer Time (+01:00) in legacy Embulk.
        //
        // "BST" is widely acknowledged as British Summer Time (+01:00). The same in Ruby. The same in Embulk.
        //
        // Java has recognized "BST" as Bangladesh Standard Time (+06:00) for historical reasons since 1.1.6 at the latest.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L70
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L226
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1331
        //
        // British Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/British_Summer_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/bst
        //
        // Bangladesh Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Bangladesh_Standard_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/bst-bangladesh
        "BST", ZoneOffset.of("+01:00"),
        "Use +01:00, Europe/London, or else as needed instead for British Summer Time. "
            + "Or, use +06:00, Asia/Dhaka, or else as needed instead for Bangladesh Standard Time, historical Java standard.",

        // "BT" considered as Baghdad Time (+03:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L86
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Baghdad Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Iraq
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-bt.html
        // * MHonArc: https://www.mhonarc.org/MHonArc/doc/resources/timezones.html
        "BT", ZoneOffset.of("+03:00"),
        "Use +03:00, Asia/Baghdad, or else as needed instead for Baghdad Time.",

        // "CAT" considered as Central Alaska Standard Time (-10:00) in legacy Embulk.
        //
        // "CAT" is widely acknowledged as Central Africa Time (+02:00).
        //
        // Ruby has recognized "CAT" as Central Alaska Standard Time (-10:00) for historical reasons since 2002 at the latest.
        // Embulk has adopted it, too.
        //
        // Java has recognized "CAT" as Central Africa Time (+02:00) since 1.1.6.
        // Embulk has not adopted it, though.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L65
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L227
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1326
        //
        // Central Alaska Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Alaska_Time_Zone
        // * timeanddate.com: N/A
        //
        // Central Africa Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_Africa_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cat
        "CAT", ZoneOffset.of("-10:00"),
        "Use -10:00 instead for Central Alaska Standard Time. "
            + "Or, use +02:00, Africa/Harare, Atlantic/Cape_Verde, or else as needed instead for Central Africa Time.",

        // "CCT" considered as China Coastal Time (+08:00) in legacy Embulk.
        //
        // "CCT" is widely acknowledged as Cocos Islands Time (+06:30).
        //
        // Ruby has recognized "CCT" as China Coastal Time (+08:00) for historical reasons since 2002 at the latest.
        // PostgreSQL has recognized "CCT" as China Coastal Time (+08:00), too. Embulk has adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L96
        // * Java: N/A
        // * Joda-Time: N/A
        // * PostgreSQL: https://www.postgresql.org/docs/8.0/datetime-keywords.html
        //
        // China Coastal Time:
        // * Wikipedia: N/A
        // * timeanddate.com: N/A
        // * USGS: https://help.waterdata.usgs.gov/code/tz_query?fmt=html
        //
        // Cocos Islands Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Cocos_(Keeling)_Islands
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cct
        "CCT", ZoneOffset.of("+08:00"),
        "Use +08:00, Asia/Shanghai, Asia/Taipei, or else as needed instead for China Coastal Time. "
            + "Or, use +06:30, Indian/Cocos, or else as needed instead for Cocos Islands Time.",

        // "CDT", which contains a non-uppercase character, considered as Central Daylight Time (-05:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as -06:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L16
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L445
        //
        // Central Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cst
        //
        // Central Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cdt
        "CDT", ZoneOffset.of("-05:00"),
        "Use -05:00, -06:00, America/Chicago, America/Mexico_City, or else as needed instead for true Central Daylight Time. "
            + "Embulk has recognized CDT wrongly as Central Standard Time, and keeps it for compatibility in the legacy mode.",

        // "CEST" considered as Central European Summer Time (+02:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L79
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Central European Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Summer_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cest
        "CEST", ZoneOffset.of("+02:00"),
        "Use +02:00, Europe/Paris, Europe/Berlin, or else as needed instead for Central European Summer Time.",

        // "CET", which contains a non-uppercase character, considered as Central European Time (+01:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as ZoneId.of("CET").
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L71
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Central European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cet
        "CET", ZoneOffset.of("+01:00"),
        "Use +01:00, +02:00, Europe/Paris, Europe/Berlin, Africa/Algiers, or else as needed instead for Central European Time.",

        // "CLST" considered as Chile Summer Time (-03:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L54
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Chile Summer Time:
        // * Wikipedia:
        // * timeanddate.com: https://www.timeanddate.com/time/zones/clst
        "CLST", ZoneOffset.of("-03:00"),
        "Use -03:00, America/Santiago, or else as needed instead for Chile Summer Time.",

        // "CLT" considered as Chile Standard Time (-04:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L57
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Chile Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Chile
        // * timeanddate.com: https://www.timeanddate.com/time/zones/clt
        "CLT", ZoneOffset.of("-04:00"),
        "Use -04:00, -03:00, America/Santiago, or else as needed instead for Chile Standard Time.",

        // "CST" considered as Central Standard Time (-06:00) in legacy Embulk.
        //
        // "CST" might sometimes mean China Standard Time (+08:00) and Cuba Standard Time (-05:00) historically in general.
        // Embulk has not adopted them.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L15
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L229
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1318
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L444
        //
        // Central Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cst
        "CST", ZoneOffset.of("-06:00"),
        "Use -06:00, -05:00, America/Chicago, America/Mexico_City, America/Guatemala, or else as needed instead for Central Standard Time.",

        "CST6CDT", ZoneId.of("CST6CDT"),
        "Use -06:00, -05:00, America/Chicago, America/Mexico_City, America/Guatemala, or else as needed instead for Central Time.",

        // "EADT" considered as East Australian Daylight Time (+11:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L103
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // East Australian Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Australia
        // * timeanddate.com: https://www.timeanddate.com/time/zones/aedt
        "EADT", ZoneOffset.of("+11:00"),
        "Use +11:00, Australia/Sydney, or else as needed instead for East Australian Daylight Time.",

        // "EAST" considered as East Australian Standard Time (+10:00) in legacy Embulk.
        //
        // "EAST" is widely acknowledged as Easter Island Standard Time (-06:00).
        //
        // Ruby has recognized "EAST" as East Australian Standard Time (+10:00) for historical reasons since 2002 at the latest.
        // PostgreSQL has recognized "EAST" as East Australian Standard Time (+10:00), too. Embulk has adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L101
        // * Java: N/A
        // * Joda-Time: N/A
        // * PostgreSQL: https://www.postgresql.org/docs/8.0/datetime-keywords.html
        //
        // Australian Eastern Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Australia
        // * timeanddate.com: https://www.timeanddate.com/time/zones/aest
        //
        // Easter Island Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Chile
        // * timeanddate.com: https://www.timeanddate.com/time/zones/east
        "EAST", ZoneOffset.of("+10:00"),
        "Use +10:00, +11:00, Australia/Sydney, Australia/Brisbane, or else as needed instead for East Australian Standard Time. "
            + "Or, use -06:00, Pacific/Easter, or else as needed instead for Easter Island Standard Time.",

        // "EAT" considered as Eastern Africa Time (+03:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L87
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L231
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1327
        //
        // Eastern Africa Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/East_Africa_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/eat
        "EAT", ZoneOffset.of("+03:00"),
        "Use +03:00, Africa/Addis_Ababa, Asia/Riyadh, or else as needed instead for Eastern Africa Time.",

        // "EDT", which contains a non-uppercase character, considered as Eastern Daylight Time (-04:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as -05:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L14
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L443
        //
        // Eastern Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/est
        //
        // Eastern Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/edt
        "EDT", ZoneOffset.of("-04:00"),
        "Use -05:00, -04:00, America/New_York, America/Toronto, or else as needed instead for true Eastern Daylight Time. "
            + "Embulk has recognized EDT wrongly as Eastern Standard Time, and keeps it for compatibility in the legacy mode.",

        // "EEST" considered as Eastern European Summer Time (+03:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L88
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Eastern European Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_European_Summer_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/eest
        "EEST", ZoneOffset.of("+03:00"),
        "Use +03:00, Europe/Bucharest, Europe/Athens, or else as needed instead for Eastern European Summer Time.",

        // "EET", which contains a non-uppercase character, considered as Eastern European Time (+02:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as ZoneId.of("EET").
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L80
        // * Java: N/A
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1311
        //
        // Eastern European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/eet
        "EET", ZoneOffset.of("+02:00"),
        "Use +02:00, +03:00, Europe/Bucharest, Europe/Athens, Africa/Cairo, or else as needed instead for Eastern European Time.",

        // "EST" considered as Eastern Standard Time (-05:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L13
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L273
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1319
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L442
        //
        // Standard Time
        // * Wikipedia: https://en.wikipedia.org/wiki/Eastern_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/est
        "EST", ZoneOffset.of("-05:00"),
        "Use -05:00, -04:00, America/New_York, America/Toronto, America/Jamaica, or else as needed instead for Eastern Standard Time.",

        "EST5EDT", ZoneId.of("EST5EDT"),
        "Use -05:00, -04:00, America/New_York, America/Toronto, America/Jamaica, or else as needed instead for Eastern Time.",

        // "FST" considered as French Summer Time (+02:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L81
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // French Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_France
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-fst.html
        "FST", ZoneOffset.of("+02:00"),
        "Use +02:00, Europe/Paris instead for French Summer Time.",

        // "FWT" considered as French Winter Time (+01:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L72
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // French Winter Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_France
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-fwt.html
        "FWT", ZoneOffset.of("+01:00"),
        "Use +01:00, Europe/Paris instead for French Winter Time.",

        // "GMT" considered as Greenwich Mean Time (+00:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L12
        // * Java: N/A
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1306
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L441
        //
        // Greenwich Mean Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Greenwich_Mean_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/gmt
        "GMT", ZoneOffset.UTC,
        "Use +00:00, UTC, Europe/London, Atlantic/Reykjavik, or else as needed instead for Greenwich Mean Time.",

        // "GST" considered as Guam Standard Time (+10:00) in legacy Embulk.
        //
        // "GST" is widely acknowledged as Gulf Standard Time (+04:00).
        //
        // Ruby has recognized "GST" as Guam Standard Time (+10:00) for historical reasons since 2002 at the latest.
        // PostgreSQL has recognized "GST" as Guam Standard Time (+10:00), too. Embulk has adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L102
        // * Java: N/A
        // * Joda-Time: N/A
        // * PostgreSQL: https://www.postgresql.org/docs/8.0/datetime-keywords.html
        //
        // Guam Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Chamorro_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/chst
        //
        // Gulf Standard Time:
        // * Wikipedia: https://en.wikipedia.org/?title=Gulf_Standard_Time&redirect=no
        // * timeanddate.com: https://www.timeanddate.com/time/zones/gst
        "GST", ZoneOffset.of("+10:00"),
        "Use +10:00, Pacific/Guam, Pacific/Saipan, or else as needed instead for Guam Standard Time, also known as Chamorro Time Zone now. "
            + "Or, use +04:00, Asia/Dubai instead for Gulf Standard Time.",

        // "HADT" considered as Hawaii-Aleutian Daylight Time (-09:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L61
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Hawaii-Aleutian Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Hawaii%E2%80%93Aleutian_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/hadt
        "HADT", ZoneOffset.of("-09:00"),
        "Use -09:00, America/Adak instead for Hawaii-Aleutian Daylight Time.",

        // "HAST" considered as Hawaii-Aleutian Standard Time (-10:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L66
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Hawaii-Aleutian Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Hawaii%E2%80%93Aleutian_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/hast
        "HAST", ZoneOffset.of("-10:00"),
        "Use -10:00, -09:00, Pacific/Honolulu, America/Adak, or else as needed instead for Hawaii-Aleutian Standard Time.",

        // "HDT" considered as Hawaii Daylight Time (-09:00) in legacy Embulk.
        //
        // "HDT" is not widely acknowledged, but sometimes known as Hawaii Daylight Time (-09:00).
        // Hawaii has not observed daylight saving time since 1945.
        //
        // Aleutian Islands observe daylight saving time as a part of Hawaii-Aleutian Daylight Time.
        // The most popular abbreviation of Hawaii-Aleutian Daylight Time is "HADT"
        // although the US Government has changed it to "HDT" since 1983.
        // https://github.com/eggert/tz/blob/2020a/NEWS#L1879
        //
        // Ruby has recognized "HDT" as Hawaii Daylight Time (-09:00) for historical reasons since 2002 at the latest.
        // Embulk has adopted it, too.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L62
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Hawaii Daylight Time / Hawaii-Aleutian Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Hawaii%E2%80%93Aleutian_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/hadt
        "HDT", ZoneOffset.of("-09:00"),
        "Use -09:00, America/Adak instead for Hawaii(-Aleutian) Daylight Time.",

        // "HST" considered as Hawaii Standard Time (-10:00) in legacy Embulk.
        //
        // The most popular abbreviation of Hawaii-Aleutian Standard Time is "HAST"
        // although the US Government has changed it to "HST" since 1983.
        // https://github.com/eggert/tz/blob/2020a/NEWS#L1879
        //
        // "HST" is accepted by Joda-Time's DateTimeZone, the legacy Embulk has returned DateTimeZone.forID("HST").
        // On the other hand, ZoneId does not accept "HST". Because DateTimeZone.forID("HST").isFixed() is true,
        // it returns ZonfOffset.of("-10:00") instead.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L67
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L275
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1313
        //
        // Hawaii Standard Time / Hawaii-Aleutian Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Hawaii%E2%80%93Aleutian_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/hast
        "HST", ZoneOffset.of("-10:00"),  // "HST" is not accepted as ZoneId, then using "-10:00" instead.
        "Use -10:00, -09:00, Pacific/Honolulu, America/Adak, or else as needed instead for Hawaii(-Aleutian) Standard Time.",

        // "IDLE" considered as Internation Date Line East (+12:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L104
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Internation Date Line East:
        // * Wikipedia: https://en.wikipedia.org/wiki/International_Date_Line
        // * timeanddate.com: https://www.timeanddate.com/time/dateline.html
        "IDLE", ZoneOffset.of("+12:00"),
        "Use +12:00 instead for Internation Date Line East.",

        // "IDLW" considered as Internation Date Line West (-12:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L69
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Internation Date Line West:
        // * Wikipedia: https://en.wikipedia.org/wiki/International_Date_Line
        // * timeanddate.com: https://www.timeanddate.com/time/dateline.html
        "IDLW", ZoneOffset.of("-12:00"),
        "Use -12:00 instead for Internation Date Line West.",

        // "IST" considered as India Standard Time (+05:30) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L93
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L234
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1330
        //
        // India Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Indian_Standard_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/ist
        "IST", ZoneOffset.of("+05:30"),
        "Use +05:30, Asia/Kolkata, or else as needed instead for India Standard Time.",

        // "JST" considered as Japan Standard Time (+09:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L99
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L235
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1334
        //
        // Japan Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Japan_Standard_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/jst
        "JST", ZoneOffset.of("+09:00"),
        "Use +09:00, Asia/Tokyo, or else as needed instead for Japan Standard Time.",

        // "KST" considered as Korea Standard Time (+09:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L100
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Korea Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_South_Korea
        // * timeanddate.com: https://www.timeanddate.com/time/zones/kst
        "KST", ZoneOffset.of("+09:00"),
        "Use +09:00, Asia/Seoul, or else as needed instead for Korea Standard Time.",

        // "MDT", which contains a non-uppercase character, considered as Mountain Daylight Time (-06:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as -07:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L18
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L447
        //
        // Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Mountain_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/mst
        //
        // Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Mountain_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/mdt
        "MDT", ZoneOffset.of("-06:00"),
        "Use -07:00, -06:00, America/Denver, America/Edmonton, or else as needed instead for true Mountain Daylight Time. "
            + "Embulk has recognized MDT wrongly as Mountain Standard Time, and keeps it for compatibility in the legacy mode.",

        // "MEST" considered as Middle European Summer Time (+02:00) in legacy Embulk.
        //
        // Middle European Summer Time is usually acknowledged as Central European Summer Time (CEST).
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L82
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Middle European Summer Time / Central European Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Summer_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cest
        "MEST", ZoneOffset.of("+02:00"),
        "Use +02:00, Europe/Paris, Europe/Berlin, or else as needed instead for Middle (Central) European Summer Time.",

        // "MESZ" considered as Mitteleuropaeische Sommerzeit (+02:00) in legacy Embulk.
        //
        // Mitteleuropaeische Sommerzeit means Central European Summer Time in Germany.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L83
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Mitteleuropaeische Zeit:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Germany
        // * timeanddate.com: N/A
        "MESZ", ZoneOffset.of("+02:00"),
        "Use +02:00, Europe/Berlin, or else as needed instead for Mitteleuropaeische Sommerzeit.",

        // "MET", which contains a non-uppercase character, considered as Middle European Time (+01:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as ZoneId.of("MET").
        //
        // Java recognized "MET" as Middle East Time (+03:30) for historical reasons as of 1.1.6 at least.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L73
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1309
        //
        // Central European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/cet
        "MET", ZoneOffset.of("+01:00"),
        "Use +01:00, +02:00, Europe/Paris, Europe/Berlin, Africa/Algiers, or else as needed instead for Middle (Central) European Time. "
            + "Or, use +03:30, Asia/Tehran, or else as needed instead for Middle East Time, historical Java standard.",

        // "MEWT" considered as Middle European Winter Time (+01:00) in legacy Embulk.
        //
        // Middle European Winter Time is usually acknowledged as Central European Time (CET).
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L74
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Middle European Winter Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Central_European_Time
        // * timeanddate.com: N/A
        "MEWT", ZoneOffset.of("+01:00"),
        "Use +01:00, Europe/Paris, Europe/Berlin, Africa/Algiers, or else as needed instead for Middle European Winter Time.",

        // "MEZ" considered as Mitteleuropaeische Zeit (+01:00) in legacy Embulk.
        //
        // Mitteleuropaeische Zeit means Central European Time in Germany.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L75
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Mitteleuropaeische Zeit:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Germany
        // * timeanddate.com: N/A
        "MEZ", ZoneOffset.of("+01:00"),
        "Use +01:00, +02:00, Europe/Berlin, or else as needed instead for Mitteleuropaeische Zeit.",

        // "MSD" considered as Moscow Daylight Time (+04:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L90
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Moscow Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Moscow_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/msd
        "MSD", ZoneOffset.of("+04:00"),
        "Use +04:00, Europe/Moscow, or else as needed instead for Moscow Daylight Time.",

        // "MSK" considered as Moscow Standard Time (+03:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L89
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Moscow Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Moscow_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/msk
        "MSK", ZoneOffset.of("+03:00"),
        "Use +03:00, +04:00, Europe/Moscow, or else as needed instead for Moscow Standard Time.",

        // "MST" considered as Mountain Standard Time (-07:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L17
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L274
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1316
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L446
        //
        // Standard Time
        // * Wikipedia: https://en.wikipedia.org/wiki/Mountain_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/mst
        "MST", ZoneOffset.of("-07:00"),
        "Use -07:00, -06:00, America/Denver, America/Edmonton, America/Phoenix, or else as needed instead for Mountain Standard Time.",

        "MST7MDT", ZoneId.of("MST7MDT"),
        "Use -07:00, -06:00, America/Denver, America/Edmonton, America/Phoenix, or else as needed instead for Mountain Time.",

        // "NDT" considered as Newfoundland Daylight Time (-02:30) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L55
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Newfoundland Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Newfoundland_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/ndt
        "NDT", ZoneOffset.of("-02:30"),
        "Use -02:30, America/St_Johns, or else as needed instead for Newfoundland Daylight Time.",

        // "NST" considered as Newfoundland Standard Time (-03:30) in legacy Embulk.
        //
        // Java has recognized "NST" as New Zealand Standard Time (+12:00) for historical reasons since 1.1.6 at the latest.
        // Embulk has not adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L50
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L238
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1338
        //
        // Newfoundland Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Newfoundland_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/nst
        "NST", ZoneOffset.of("-03:30"),
        "Use -03:30, -02:30, America/St_Johns, or else as needed instead for Newfoundland Standard Time."
            + "Or, use +12:00, +13:00, Pacific/Auckland, or else instead for New Zealand Standard Time, historical Java standard.",

        // "NT" considered as obsolete Nome Time (-11:00) in legacy Embulk.
        //
        // Nome Time has no longer existed, and the areas have switched to Alaska Time.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L68
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Nome Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Nome,_Alaska
        // * timeanddate.com: https://www.timeanddate.com/time/zone/usa/nome
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-nt.html
        // * MHonArc: https://www.mhonarc.org/MHonArc/doc/resources/timezones.html
        "NT", ZoneOffset.of("-11:00"),
        "Use -11:00, America/Anchorage, or else as needed instead for obsolete Nome Time.",

        // "NZDT" considered as New Zealand Daylight Time (+13:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L107
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // New Zealand Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_New_Zealand
        // * timeanddate.com: https://www.timeanddate.com/time/zones/nzdt
        "NZDT", ZoneOffset.of("+13:00"),
        "Use +13:00 Pacific/Auckland, or else as needed instead for New Zealand Daylight Time.",

        // "NZST" considered as New Zealand Standard Time (+12:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L105
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // New Zealand Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_New_Zealand
        // * timeanddate.com: https://www.timeanddate.com/time/zones/nzst
        "NZST", ZoneOffset.of("+12:00"),
        "Use +12:00, +13:00, Pacific/Auckland, or else as needed instead for New Zealand Standard Time.",

        // "NZT" considered as New Zealand Time (+12:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L106
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // New Zealand Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_New_Zealand
        // * timeanddate.com: N/A
        "NZT", ZoneOffset.of("+12:00"),
        "Use +12:00, +13:00, Pacific/Auckland, or else as needed instead for New Zealand Time.",

        // "PDT", which contains a non-uppercase character, considered as Pacific Daylight Time (-07:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as -08:00.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L20
        // * Java: N/A
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L449
        //
        // Pacific Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Pacific_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/pst
        //
        // Pacific Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Pacific_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/pdt
        "PDT", ZoneOffset.of("-07:00"),
        "Use -08:00, -07:00, America/Los_Angeles, America/Vancouver, or else as needed instead for true Pacific Daylight Time. "
            + "Embulk has recognized PDT wrongly as Pacific Standard Time, and keeps it for compatibility in the legacy mode.",

        // "PST" considered as Pacific Standard Time (-08:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L19
        // * Java: https://github.com/openjdk/jdk/blob/jdk8-b120/jdk/src/share/classes/sun/util/calendar/ZoneInfoFile.java#L242
        // * Java 1.1.6: http://web.mit.edu/java_v1.1.6/distrib/sun4x_57/src/java/util/TimeZone.java
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1315
        // * Joda-Time DateTimeUtils: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeUtils.java#L448
        //
        // Standard Time
        // * Wikipedia: https://en.wikipedia.org/wiki/Pacific_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/pst
        "PST", ZoneOffset.of("-08:00"),
        "Use -08:00, -07:00, America/Los_Angeles, America/Vancouver, or else as needed instead for Pacific Standard Time.",

        "PST8PDT", ZoneId.of("PST8PDT"),
        "Use -08:00, -07:00, America/Los_Angeles, America/Vancouver, or else as needed instead for Pacific Time.",

        // "SAST" considered as South Africa Standard Time (+02:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L84
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // South Africa Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/South_African_Standard_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/sast
        "SAST", ZoneOffset.of("+02:00"),
        "Use +02:00, Africa/Johannesburg, or else as needed instead for South Africa Standard Time.",

        // "SGT" considered as Singapore Time (+08:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L97
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Singapore Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Singapore_Standard_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/sgt
        "SGT", ZoneOffset.of("+08:00"),
        "Use +08:00, Asia/Singapore, or else as needed instead for Singapore Time.",

        // "SST" considered as Swedish Summer Time (+02:00) in legacy Embulk.
        //
        // "SST" is widely acknowledged as Samoa Standard Time (-11:00).
        //
        // Ruby has recognized "SST" as Swedish Summer Time (+02:00) for historical reasons since 2002 at the latest.
        // PostgreSQL has recognized "SST" as Swedish Summer Time (+02:00), too. Embulk has adopted it.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L85
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Swedish Summer Time:
        // * Wikipedia: N/A
        // * timeanddate.com: https://www.timeanddate.com/time/zone/sweden
        //
        // Samoa Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Samoa_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/sst
        "SST", ZoneOffset.of("+02:00"),
        "Use +02:00, Europe/Stockholm, or else as needed instead for Swedish Summer Time. "
            + "Or, use -11:00, Pacific/Pago_Pago, or else as needed instead for Samoa Standard Time.",

        // "SWT" considered as Swedish Winter Time (+01:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L76
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Swedish Winter Time:
        // * Wikipedia: N/A
        // * timeanddate.com: https://www.timeanddate.com/time/zone/sweden
        "SWT", ZoneOffset.of("+01:00"),
        "Use +01:00, Europe/Stockholm, or else as needed instead for Swedish Winter Time.",

        // "UCT" considered as UTC in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: N/A
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // UTC:
        // * Wikipedia: https://en.wikipedia.org/wiki/Universal_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/utc
        "UCT", ZoneOffset.UTC,
        "Use UTC instead.",

        // "UT" considered as Universal Time in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L11
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Universal Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Universal_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/ut
        "UT", ZoneOffset.UTC,
        "Use UTC instead.",

        // "WADT" considered as obsolete and wrong West Australian Daylight Time (+08:00) in legacy Embulk.
        //
        // Ruby has recognized "WADT" as West Australian Daylight Time (+08:00) for historical reasons since 2002 at the latest.
        // It is wrong. Western Australia has not observed daylight saving time.
        //
        // PostgreSQL has recognized "WADT" as West Australian Daylight Time (+08:00), too.
        //
        // Embulk has adopted this wrong West Australian Daylight Time (+08:00).
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L98
        // * Java: N/A
        // * Joda-Time: N/A
        // * PostgreSQL: https://www.postgresql.org/docs/8.0/datetime-keywords.html
        //
        // West Australian Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Australia
        // * timeanddate.com: N/A
        "WADT", ZoneOffset.of("+08:00"),
        "Use +08:00, Australia/Perth, or else as needed instead for West Australian Daylight Time.",

        // "WAST" considered as obsolete and wrong West Australian Standard Time (+07:00) in legacy Embulk.
        //
        // "WAST" is widely acknowledged as West Africa Summer Time (+02:00).
        //
        // Ruby has recognized "WAST" as West Australian Standard Time (+07:00) for historical reasons since 2002 at the latest.
        // Furthermore, it is wrong. West Australian Standard Time has never been +07:00. It has been +08:00 in reality.
        //
        // PostgreSQL has recognized "WAST" as West Australian Standard Time (+07:00), too.
        //
        // Embulk has adopted this West Australian Standard Time (+07:00).
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L95
        // * Java: N/A
        // * Joda-Time: N/A
        // * PostgreSQL: https://www.postgresql.org/docs/8.0/datetime-keywords.html
        //
        // West Australian Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Australia
        // * timeanddate.com: https://www.timeanddate.com/time/zones/awst
        //
        // West Africa Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/West_Africa_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/wast
        "WAST", ZoneOffset.of("+07:00"),
        "Use +07:00, +08:00, Australia/Perth, or else as needed instead for West Australian Standard Time. "
            + "Or, use +02:00, Africa/Windhoek, or else as needed instead for West Africa Summer Time.",

        // "WAT" considered as West Africa Time (+01:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L77
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // West Africa Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/West_Africa_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/wat
        "WAT", ZoneOffset.of("+01:00"),
        "Use +01:00, +02:00, Africa/Lagos, Africa/Windhoek, or else as needed instead for West Africa Time.",

        // "WEST" considered as Western European Summer Time (+01:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L78
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Western European Summer Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Western_European_Summer_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/west
        "WEST", ZoneOffset.of("+01:00"),
        "Use +01:00, Europe/Lisbon, Africa/Casablanca, or else as needed instead for Western European Summer Time.",

        // "WET", which contains a non-uppercase character, considered as Western European Time (+00:00) in legacy Embulk.
        //
        // If it consists of all uppercase characters, it is considered as ZoneId.of("WET").
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L47
        // * Java: N/A
        // * Joda-Time DateTimeZone: https://github.com/JodaOrg/joda-time/blob/v2.10.6/src/main/java/org/joda/time/DateTimeZone.java#L1307
        //
        // Western European Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Western_European_Time
        // * timeanddate.com: https://www.timeanddate.com/time/zones/wet
        "WET", ZoneOffset.of("+00:00"),
        "Use +00:00, +01:00, Europe/Lisbon, Africa/Casablanca, or else as needed instead for Western European Time.",

        // "YDT" considered as obsolete Yukon Daylight Time (-08:00) in legacy Embulk.
        //
        // Yukon time zones have no longer existed, and the areas have switched to Pacific Time or Alaska Time.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L59
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Yukon Daylight Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Yukon_Time_Zone
        // * timeanddate.com: N/A
        "YDT", ZoneOffset.of("-08:00"),
        "Use -08:00, America/Whitehorse, America/Yakutat, or else as needed instead for obsolete Yukon Daylight Time.",

        // "YST" considered as obsolete Yukon Standard Time (-09:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L63
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Yukon Standard Time:
        // * Wikipedia: https://en.wikipedia.org/wiki/Yukon_Time_Zone
        // * timeanddate.com: https://www.timeanddate.com/time/zones/yst
        "YST", ZoneOffset.of("-09:00"),
        "Use -09:00, -08:00, America/Whitehorse, America/Yakutat, or else as needed instead for obsolete Yukon Daylight Time.",

        // "ZP4" considered as Zulu plus 4, USSR Zone 3, (+04:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L91
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Zulu plus 4, USSR Zone 3:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Russia#Soviet_Union
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-usz3.html
        "ZP4", ZoneOffset.of("+04:00"),
        "Use +04:00, Europe/Samara, or else as needed instead for USSR Zone 3.",

        // "ZP5" considered as Zulu plus 5, USSR Zone 4, (+05:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L92
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Zulu plus 5, USSR Zone 4:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Russia#Soviet_Union
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-usz4.html
        "ZP5", ZoneOffset.of("+05:00"),
        "Use +05:00, Asia/Yekaterinburg, or else as needed instead for USSR Zone 4.",

        // "ZP6" considered as Zulu plus 6, USSR Zone 5, (+06:00) in legacy Embulk.
        //
        // Abbreviations:
        // * Ruby: https://github.com/ruby/ruby/blob/v2_7_1/ext/date/zonetab.list#L94
        // * Java: N/A
        // * Joda-Time: N/A
        //
        // Zulu plus 6, USSR Zone 5:
        // * Wikipedia: https://en.wikipedia.org/wiki/Time_in_Russia#Soviet_Union
        // * timeanddate.com: N/A
        // * worldtimezone.com: https://www.worldtimezone.com/wtz-names/wtz-usz5.html
        "ZP6", ZoneOffset.of("+06:00"),
        "Use +06:00, Asia/Omsk, or else as needed instead for USSR Zone 5.",
    };

    private static final Object[] MILITARY_ZONE_NAMES = {
        "A", ZoneOffset.ofHours(1),
        "B", ZoneOffset.ofHours(2),
        "C", ZoneOffset.ofHours(3),
        "D", ZoneOffset.ofHours(4),
        "E", ZoneOffset.ofHours(5),
        "F", ZoneOffset.ofHours(6),
        "G", ZoneOffset.ofHours(7),
        "H", ZoneOffset.ofHours(8),
        "I", ZoneOffset.ofHours(9),
        "K", ZoneOffset.ofHours(10),
        "L", ZoneOffset.ofHours(11),
        "M", ZoneOffset.ofHours(12),
        "N", ZoneOffset.ofHours(-1),
        "O", ZoneOffset.ofHours(-2),
        "P", ZoneOffset.ofHours(-3),
        "Q", ZoneOffset.ofHours(-4),
        "R", ZoneOffset.ofHours(-5),
        "S", ZoneOffset.ofHours(-6),
        "T", ZoneOffset.ofHours(-7),
        "U", ZoneOffset.ofHours(-8),
        "V", ZoneOffset.ofHours(-9),
        "W", ZoneOffset.ofHours(-10),
        "X", ZoneOffset.ofHours(-11),
        "Y", ZoneOffset.ofHours(-12),
        "Z", ZoneOffset.UTC,
    };

    private static final Object[] RUBY_REGION_NAMES = {
        "afghanistan", ZoneOffset.ofTotalSeconds(16200),
        "alaskan", ZoneOffset.ofTotalSeconds(-32400),
        "arab", ZoneOffset.ofTotalSeconds(10800),
        "arabian", ZoneOffset.ofTotalSeconds(14400),
        "arabic", ZoneOffset.ofTotalSeconds(10800),
        "atlantic", ZoneOffset.ofTotalSeconds(-14400),
        "aus central", ZoneOffset.ofTotalSeconds(34200),
        "aus eastern", ZoneOffset.ofTotalSeconds(36000),
        "azores", ZoneOffset.ofTotalSeconds(-3600),
        "canada central", ZoneOffset.ofTotalSeconds(-21600),
        "cape verde", ZoneOffset.ofTotalSeconds(-3600),
        "caucasus", ZoneOffset.ofTotalSeconds(14400),
        "cen. australia", ZoneOffset.ofTotalSeconds(34200),
        "central america", ZoneOffset.ofTotalSeconds(-21600),
        "central asia", ZoneOffset.ofTotalSeconds(21600),
        "central europe", ZoneOffset.ofTotalSeconds(3600),
        "central european", ZoneOffset.ofTotalSeconds(3600),
        "central pacific", ZoneOffset.ofTotalSeconds(39600),
        "central", ZoneOffset.ofTotalSeconds(-21600),
        "china", ZoneOffset.ofTotalSeconds(28800),
        "dateline", ZoneOffset.ofTotalSeconds(-43200),
        "e. africa", ZoneOffset.ofTotalSeconds(10800),
        "e. australia", ZoneOffset.ofTotalSeconds(36000),
        "e. europe", ZoneOffset.ofTotalSeconds(7200),
        "e. south america", ZoneOffset.ofTotalSeconds(-10800),
        "eastern", ZoneOffset.ofTotalSeconds(-18000),
        "egypt", ZoneOffset.ofTotalSeconds(7200),
        "ekaterinburg", ZoneOffset.ofTotalSeconds(18000),
        "fiji", ZoneOffset.ofTotalSeconds(43200),
        "fle", ZoneOffset.ofTotalSeconds(7200),
        "greenland", ZoneOffset.ofTotalSeconds(-10800),
        "greenwich", ZoneOffset.ofTotalSeconds(0),
        "gtb", ZoneOffset.ofTotalSeconds(7200),
        "hawaiian", ZoneOffset.ofTotalSeconds(-36000),
        "india", ZoneOffset.ofTotalSeconds(19800),
        "iran", ZoneOffset.ofTotalSeconds(12600),
        "jerusalem", ZoneOffset.ofTotalSeconds(7200),
        "korea", ZoneOffset.ofTotalSeconds(32400),
        "mexico", ZoneOffset.ofTotalSeconds(-21600),
        "mid-atlantic", ZoneOffset.ofTotalSeconds(-7200),
        "mountain", ZoneOffset.ofTotalSeconds(-25200),
        "myanmar", ZoneOffset.ofTotalSeconds(23400),
        "n. central asia", ZoneOffset.ofTotalSeconds(21600),
        "nepal", ZoneOffset.ofTotalSeconds(20700),
        "new zealand", ZoneOffset.ofTotalSeconds(43200),
        "newfoundland", ZoneOffset.ofTotalSeconds(-12600),
        "north asia east", ZoneOffset.ofTotalSeconds(28800),
        "north asia", ZoneOffset.ofTotalSeconds(25200),
        "pacific sa", ZoneOffset.ofTotalSeconds(-14400),
        "pacific", ZoneOffset.ofTotalSeconds(-28800),
        "romance", ZoneOffset.ofTotalSeconds(3600),
        "russian", ZoneOffset.ofTotalSeconds(10800),
        "sa eastern", ZoneOffset.ofTotalSeconds(-10800),
        "sa pacific", ZoneOffset.ofTotalSeconds(-18000),
        "sa western", ZoneOffset.ofTotalSeconds(-14400),
        "samoa", ZoneOffset.ofTotalSeconds(-39600),
        "se asia", ZoneOffset.ofTotalSeconds(25200),
        "malay peninsula", ZoneOffset.ofTotalSeconds(28800),
        "south africa", ZoneOffset.ofTotalSeconds(7200),
        "sri lanka", ZoneOffset.ofTotalSeconds(21600),
        "taipei", ZoneOffset.ofTotalSeconds(28800),
        "tasmania", ZoneOffset.ofTotalSeconds(36000),
        "tokyo", ZoneOffset.ofTotalSeconds(32400),
        "tonga", ZoneOffset.ofTotalSeconds(46800),
        "us eastern", ZoneOffset.ofTotalSeconds(-18000),
        "us mountain", ZoneOffset.ofTotalSeconds(-25200),
        "vladivostok", ZoneOffset.ofTotalSeconds(36000),
        "w. australia", ZoneOffset.ofTotalSeconds(28800),
        "w. central africa", ZoneOffset.ofTotalSeconds(3600),
        "w. europe", ZoneOffset.ofTotalSeconds(3600),
        "west asia", ZoneOffset.ofTotalSeconds(18000),
        "west pacific", ZoneOffset.ofTotalSeconds(36000),
        "yakutsk", ZoneOffset.ofTotalSeconds(32400),
    };

    // We deprecate tzdb "backward" region names that do not have slash "/".
    // https://github.com/eggert/tz/blob/master/backward
    private static final Object[] NON_SLASH_BACKWARD_REGION_NAMES = {
        "Cuba", ZoneId.of("America/Havana"),
        "Egypt", ZoneId.of("Africa/Cairo"),
        "Eire", ZoneId.of("Europe/Dublin"),
        "GB", ZoneId.of("Europe/London"),
        "GB-Eire", ZoneId.of("Europe/London"),
        "GMT+0", ZoneId.of("Etc/GMT"),
        "GMT-0", ZoneId.of("Etc/GMT"),
        "GMT0", ZoneId.of("Etc/GMT"),
        "Greenwich", ZoneOffset.UTC,
        "Hongkong", ZoneId.of("Asia/Hong_Kong"),
        "Iceland", ZoneId.of("Atlantic/Reykjavik"),
        "Iran", ZoneId.of("Asia/Tehran"),
        "Israel", ZoneId.of("Asia/Jerusalem"),
        "Jamaica", ZoneId.of("America/Jamaica"),
        "Japan", ZoneId.of("Asia/Tokyo"),
        "Kwajalein", ZoneId.of("Pacific/Kwajalein"),
        "Libya", ZoneId.of("Africa/Tripoli"),
        "NZ", ZoneId.of("Pacific/Auckland"),
        "NZ-CHAT", ZoneId.of("Pacific/Chatham"),
        "Navajo", ZoneId.of("America/Denver"),
        "PRC", ZoneId.of("Asia/Shanghai"),
        "Poland", ZoneId.of("Europe/Warsaw"),
        "Portugal", ZoneId.of("Europe/Lisbon"),
        "ROC", ZoneId.of("Asia/Taipei"),
        "ROK", ZoneId.of("Asia/Seoul"),
        "Singapore", ZoneId.of("Asia/Singapore"),
        "Turkey", ZoneId.of("Europe/Istanbul"),
        "Universal", ZoneOffset.UTC,
        "W-SU", ZoneId.of("Europe/Moscow"),
        "Zulu", ZoneOffset.UTC,
    };

    static {
        final HashMap<String, ZoneId> alternativesCaseSensitive = new HashMap<>();
        final HashMap<String, String> suggestionsCaseSensitive = new HashMap<>();

        final HashMap<String, ZoneId> alternativesCaseInsensitive = new HashMap<>();
        final HashMap<String, String> suggestionsCaseInsensitive = new HashMap<>();

        for (int i = 0; i < CASE_SENSITIVE_SHORT_ZONE_NAMES.length; i += 3) {
            alternativesCaseSensitive.put(
                    (String) CASE_SENSITIVE_SHORT_ZONE_NAMES[i],
                    (ZoneId) CASE_SENSITIVE_SHORT_ZONE_NAMES[i + 1]);
            suggestionsCaseSensitive.put(
                    (String) CASE_SENSITIVE_SHORT_ZONE_NAMES[i],
                    (String) CASE_SENSITIVE_SHORT_ZONE_NAMES[i] + " is deprecated as a short time zone name. "
                    + (String) CASE_SENSITIVE_SHORT_ZONE_NAMES[i + 2]);
        }

        for (int i = 0; i < CASE_INSENSITIVE_SHORT_ZONE_NAMES.length; i += 3) {
            alternativesCaseInsensitive.put(
                    (String) CASE_INSENSITIVE_SHORT_ZONE_NAMES[i],
                    (ZoneId) CASE_INSENSITIVE_SHORT_ZONE_NAMES[i + 1]);
            suggestionsCaseInsensitive.put(
                    (String) CASE_INSENSITIVE_SHORT_ZONE_NAMES[i],
                    (String) CASE_INSENSITIVE_SHORT_ZONE_NAMES[i] + " is deprecated as a short time zone name. "
                    + (String) CASE_INSENSITIVE_SHORT_ZONE_NAMES[i + 2]);
        }

        for (int i = 0; i < MILITARY_ZONE_NAMES.length; i += 2) {
            alternativesCaseInsensitive.put((String) MILITARY_ZONE_NAMES[i], (ZoneOffset) MILITARY_ZONE_NAMES[i + 1]);
            suggestionsCaseInsensitive.put(
                    (String) MILITARY_ZONE_NAMES[i],
                    (String) MILITARY_ZONE_NAMES[i] + " is deprecated as a military time zone name. "
                        + "Use " + zoneToString((ZoneOffset) MILITARY_ZONE_NAMES[i + 1]) + " instead.");
        }

        for (int i = 0; i < RUBY_REGION_NAMES.length; i += 2) {
            final String declaredRegion = ((String) RUBY_REGION_NAMES[i]).toUpperCase(Locale.ROOT);
            final ZoneOffset declaredStandardOffset = (ZoneOffset) RUBY_REGION_NAMES[i + 1];
            final ZoneOffset declaredDaylightOffset = ZoneOffset.ofTotalSeconds(declaredStandardOffset.getTotalSeconds() + 3600);

            final Object[] expandedRegionNames = {
                declaredRegion, declaredStandardOffset,
                declaredRegion + " STANDARD TIME", declaredStandardOffset,
                declaredRegion + " DAYLIGHT TIME", declaredDaylightOffset,
                declaredRegion + " DST", declaredDaylightOffset,
            };

            for (int j = 0; j < expandedRegionNames.length; j += 2) {
                final String region = (String) expandedRegionNames[j];
                final ZoneOffset offset = (ZoneOffset) expandedRegionNames[j + 1];
                alternativesCaseInsensitive.put(region, offset);
                suggestionsCaseInsensitive.put(
                        region, region + " is deprecated as a time zone name. Use " + zoneToString(offset) + " instead.");
            }
        }

        for (int i = 0; i < NON_SLASH_BACKWARD_REGION_NAMES.length; i += 2) {
            final String region = ((String) NON_SLASH_BACKWARD_REGION_NAMES[i]);
            final ZoneId zoneId = (ZoneId) NON_SLASH_BACKWARD_REGION_NAMES[i + 1];
            alternativesCaseSensitive.put(region, zoneId);
            suggestionsCaseSensitive.put(
                    region,
                    region + " is deprecated as a tzdb region name without a slash ('/'). "
                        + "Use " + zoneToString(zoneId) + " instead.");
        }

        ALTERNATIVES_CASE_INSENSITIVE = Collections.unmodifiableMap(alternativesCaseInsensitive);
        SUGGESTIONS_CASE_INSENSITIVE = Collections.unmodifiableMap(suggestionsCaseInsensitive);

        ALTERNATIVES_CASE_SENSITIVE = Collections.unmodifiableMap(alternativesCaseSensitive);
        SUGGESTIONS_CASE_SENSITIVE = Collections.unmodifiableMap(suggestionsCaseSensitive);
    }

    private static String zoneToString(final ZoneId zone) {
        if (zone.equals(ZoneOffset.UTC)) {
            return "UTC";
        }
        return zone.toString();
    }
}
