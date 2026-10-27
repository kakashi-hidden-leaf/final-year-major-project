package com.campus360.service;

import com.campus360.entity.TeacherSchedule;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class TimetableExportService {

    private static final DateTimeFormatter ICS_DATE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    /**
     * Generates a pre-filled Google Calendar web URL for a single lecture slot.
     */
    public String generateGoogleCalendarLink(TeacherSchedule slot) {
        if (slot == null) return "#";

        LocalDate nextDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(slot.getDayOfWeek()));
        LocalDateTime startDt = LocalDateTime.of(nextDate, slot.getStartTime());
        LocalDateTime endDt = LocalDateTime.of(nextDate, slot.getEndTime());

        String datesParam = startDt.format(ICS_DATE_TIME) + "/" + endDt.format(ICS_DATE_TIME);
        String title = slot.getSubject().getDisplayName() + " - " + slot.getClassGroup().getDisplayName();
        String location = slot.getCampusLocation().getDisplayName();
        String details = "Campus 360 Lecture Slot for " + slot.getTeacher().getFullName() +
                "\nSubject: " + slot.getSubject().getSubjectName() + " (" + slot.getSubject().getSubjectCode() + ")" +
                "\nClass: " + slot.getClassGroup().getDisplayName() +
                "\nLocation: " + location;

        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String encodedDetails = URLEncoder.encode(details, StandardCharsets.UTF_8);
        String encodedLocation = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String rrule = URLEncoder.encode("RRULE:FREQ=WEEKLY", StandardCharsets.UTF_8);

        return "https://calendar.google.com/calendar/render?action=TEMPLATE" +
                "&text=" + encodedTitle +
                "&dates=" + datesParam +
                "&details=" + encodedDetails +
                "&location=" + encodedLocation +
                "&recur=" + rrule;
    }

    /**
     * Generates a complete RFC 5545 .ics iCalendar file for all weekly lecture slots of a teacher,
     * with VALARM trigger set to -PT15M (15 minutes prior reminder).
     */
    public String generateIcsContent(String teacherName, List<TeacherSchedule> schedules) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Campus360//Teacher Timetable//EN\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");
        sb.append("X-WR-CALNAME:Campus 360 - ").append(teacherName).append(" Timetable\r\n");
        sb.append("X-WR-TIMEZONE:Asia/Kolkata\r\n");

        String nowUtc = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));

        if (schedules != null) {
            for (TeacherSchedule slot : schedules) {
                LocalDate nextDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(slot.getDayOfWeek()));
                LocalDateTime startDt = LocalDateTime.of(nextDate, slot.getStartTime());
                LocalDateTime endDt = LocalDateTime.of(nextDate, slot.getEndTime());

                String byDay = slot.getDayOfWeek().name().substring(0, 2); // MO, TU, WE, TH, FR, SA, SU

                sb.append("BEGIN:VEVENT\r\n");
                sb.append("UID:campus360-slot-").append(slot.getScheduleId()).append("@campus360.edu\r\n");
                sb.append("DTSTAMP:").append(nowUtc).append("\r\n");
                sb.append("DTSTART;TZID=Asia/Kolkata:").append(startDt.format(ICS_DATE_TIME)).append("\r\n");
                sb.append("DTEND;TZID=Asia/Kolkata:").append(endDt.format(ICS_DATE_TIME)).append("\r\n");
                sb.append("RRULE:FREQ=WEEKLY;BYDAY=").append(byDay).append("\r\n");
                sb.append("SUMMARY:").append(escapeIcs(slot.getSubject().getDisplayName())).append("\r\n");
                sb.append("DESCRIPTION:Class: ").append(escapeIcs(slot.getClassGroup().getDisplayName()))
                        .append(" | Teacher: ").append(escapeIcs(slot.getTeacher().getFullName())).append("\r\n");
                sb.append("LOCATION:").append(escapeIcs(slot.getCampusLocation().getDisplayName())).append("\r\n");

                // VALARM - 15 minutes before lecture
                sb.append("BEGIN:VALARM\r\n");
                sb.append("TRIGGER:-PT15M\r\n");
                sb.append("ACTION:DISPLAY\r\n");
                sb.append("DESCRIPTION:Reminder: Lecture starts in 15 minutes for ")
                        .append(escapeIcs(slot.getSubject().getSubjectName())).append("\r\n");
                sb.append("END:VALARM\r\n");

                sb.append("END:VEVENT\r\n");
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcs(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\n", "\\n");
    }
}
