/*
 * Copyright 2017 The Embulk project
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

import java.util.Collections;
import java.util.Set;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

/**
 * A utility class for emulating Embulk's legacy time zone handling with Joda-Time.
 *
 * <p>It is copied from {@code embulk-core} v0.10.18's {@code org.embulk.spi.time.TimeZoneIds}, and modified a little bit.
 */
class JodaDateTimeZones {
    private JodaDateTimeZones() {
        // No instantiation.
    }

    public static DateTimeZone parseJodaDateTimeZone(final String timeZoneName) {
        DateTimeZone jodaDateTimeZoneTemporary = null;
        try {
            // Use TimeZone#forID, not TimeZone#getTimeZone.
            // Because getTimeZone returns GMT even if given timezone id is not found.
            jodaDateTimeZoneTemporary = DateTimeZone.forID(timeZoneName);
        } catch (IllegalArgumentException ex) {
            jodaDateTimeZoneTemporary = null;
        }
        final DateTimeZone jodaDateTimeZone = jodaDateTimeZoneTemporary;

        // Embulk has accepted to parse Joda-Time's time zone IDs in Timestamps since v0.2.0
        // although the formats are based on Ruby's strptime. Joda-Time's time zone IDs are
        // continuously to be accepted with higher priority than Ruby's time zone IDs.
        if (jodaDateTimeZone != null && (timeZoneName.startsWith("+") || timeZoneName.startsWith("-"))) {
            return jodaDateTimeZone;

        } else if (timeZoneName.equals("Z")) {
            return DateTimeZone.UTC;

        } else {
            try {
                // DateTimeFormat.forPattern("z").parseMillis(s) is incorrect, but kept for compatibility as of now.
                //
                // The offset of PDT (Pacific Daylight Time) should be -07:00.
                // DateTimeFormat.forPattern("z").parseMillis("PDT") however returns 8 hours (-08:00).
                // DateTimeFormat.forPattern("z").parseMillis("PDT") == 28800000
                // https://github.com/JodaOrg/joda-time/blob/v2.9.2/src/main/java/org/joda/time/DateTimeUtils.java#L446
                //
                // Embulk has used it to parse time zones for a very long time since it was v0.1.
                // https://github.com/embulk/embulk/commit/b97954a5c78397e1269bbb6979d6225dfceb4e05
                //
                // It is kept as -08:00 for compatibility as of now.
                //
                // TODO: Make time zone parsing consistent.
                // @see <a href="https://github.com/embulk/embulk/issues/860">https://github.com/embulk/embulk/issues/860</a>
                int rawOffset = (int) DateTimeFormat.forPattern("z").parseMillis(timeZoneName);
                if (rawOffset == 0) {
                    return DateTimeZone.UTC;
                }
                int offset = rawOffset / -1000;
                int h = offset / 3600;
                int m = offset % 3600;
                return DateTimeZone.forOffsetHoursMinutes(h, m);
            } catch (IllegalArgumentException ex) {
                // parseMillis failed
            }

            if (jodaDateTimeZone != null && JODA_TIME_ZONES.contains(timeZoneName)) {
                return jodaDateTimeZone;
            }

            // Parsing Ruby-style time zones in lower priority than Joda-Time because
            // TimestampParser has parsed time zones with Joda-Time for a long time
            // since ancient. The behavior is kept for compatibility.
            //
            // The following time zone IDs are duplicated in Ruby and Joda-Time 2.9.2
            // while Ruby does not care summer time and Joda-Time cares summer time.
            // "CET", "EET", "Egypt", "Iran", "MET", "WET"
            //
            // Some zone IDs (ex. "PDT") are parsed by DateTimeFormat#parseMillis as shown above.
            final int rubyStyleTimeOffsetInSecond = RubyTimeZoneTab.dateZoneToDiff(timeZoneName);
            if (rubyStyleTimeOffsetInSecond != Integer.MIN_VALUE) {
                return DateTimeZone.forOffsetMillis(rubyStyleTimeOffsetInSecond * 1000);
            }

            return null;
        }
    }

    private static final Set<String> JODA_TIME_ZONES = Collections.unmodifiableSet(DateTimeZone.getAvailableIDs());
}
