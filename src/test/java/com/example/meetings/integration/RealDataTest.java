package com.example.meetings.integration;

import com.example.meetings.model.Meeting;
import com.example.meetings.model.User;
import com.example.meetings.repository.MeetingRepository;
import com.example.meetings.repository.UserRepository;
import com.example.meetings.service.MeetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RealDataTest {
    @Autowired
    private MeetingService meetingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    // save data and search
    @Test
    void saveSearchTest() {
        User u = new User("Oly", "123@gmail.com", "pwd");
        u = userRepository.save(u);
        Meeting meeting = new Meeting(
            "Meeting",
            "Description",
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T11:00:00Z"),
            u
        );
        meetingRepository.save(meeting);
        List<Meeting> result = meetingRepository.findCalendarMeetings(u);

        assertEquals(1, result.size());
        assertEquals("Meeting", result.get(0).getTitle());
    }

    // meeting service
    @Test
    void meetingServiceTest() {
        User u = userRepository.save(new User("Oly", "123@gmail.com", "pwd"));
        Meeting meeting = meetingService.propose(
            u,
            "Meeting",
            "Description",
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T11:00:00Z"),
            List.of()
        );

        assertNotNull(meeting.getId());
        List<Meeting> result = meetingRepository.findCalendarMeetings(u);

        assertEquals(1, result.size());
    }

    // overlapping 
    @Test
    void overlappingTest() {
        User u = userRepository.save(new User("Oly", "123@gmail.com", "pwd"));
        Meeting meeting = new Meeting(
                "Meeting",
                "",
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T11:00:00Z"),
                u
        );

        meetingRepository.save(meeting);

        List<Meeting> result = meetingRepository.findOverlapping(
                u,
                Instant.parse("2026-01-01T10:30:00Z"),
                Instant.parse("2026-01-01T12:00:00Z")
        );

        assertEquals(1, result.size());
    }
}
