package com.campus360.repository;

import com.campus360.entity.Teacher;
import com.campus360.entity.TeacherSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TeacherScheduleRepository extends JpaRepository<TeacherSchedule, Long> {

    List<TeacherSchedule> findByTeacherOrderByDayOfWeekAscStartTimeAsc(Teacher teacher);

    List<TeacherSchedule> findByTeacher_TeacherIdOrderByDayOfWeekAscStartTimeAsc(Long teacherId);

    List<TeacherSchedule> findByTeacher_User_UsernameOrderByDayOfWeekAscStartTimeAsc(String username);

    List<TeacherSchedule> findByTeacher_TeacherIdAndDayOfWeekOrderByStartTimeAsc(Long teacherId, DayOfWeek dayOfWeek);

    @Query("SELECT s FROM TeacherSchedule s WHERE s.dayOfWeek = :dayOfWeek AND " +
           "s.startTime >= :windowStart AND s.startTime <= :windowEnd AND " +
           "(s.lastRemindedDate IS NULL OR s.lastRemindedDate != :today)")
    List<TeacherSchedule> findSchedulesNeedingReminder(
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("windowStart") LocalTime windowStart,
            @Param("windowEnd") LocalTime windowEnd,
            @Param("today") LocalDate today
    );

    void deleteByTeacher(Teacher teacher);
}
