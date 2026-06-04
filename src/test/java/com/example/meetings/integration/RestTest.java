package com.example.meetings.integration;

import com.example.meetings.model.InviteStatus;
import com.example.meetings.model.Meeting;
import com.example.meetings.model.MeetingParticipant;
import com.example.meetings.model.User;
import com.example.meetings.repository.UserRepository;
import com.example.meetings.repository.MeetingRepository;
import com.example.meetings.repository.MeetingParticipantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RestTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingParticipantRepository meetingParticipantRepository;

    // ical
    @Test
    void icalTest() throws Exception {
        User u = userRepository.save(new User("Oly", "123@gmail.com", "pwd"));
        mockMvc.perform(
            get("/ical/" + u.getIcalToken() + ".ics")
        ).andExpect(status().isOk()).andExpect(content().string(org.hamcrest.Matchers.containsString("BEGIN:VCALENDAR")));
    }

    // respond
    @WithMockUser(username = "Oly")
    @Test
    void respondTest() throws Exception {
        User u = userRepository.save(new User("Oly", "123@gmail.com", "pwd"));
        Meeting meeting = meetingRepository.save(
            new Meeting(
                "Meeting",
                "Description",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                u)
        );

        MeetingParticipant p = new MeetingParticipant(meeting, u, InviteStatus.PENDING);
        meetingParticipantRepository.save(p);

        mockMvc.perform(
                post("/meetings/" + meeting.getId() + "/respond")
                        .with(csrf())
                        .param("action", "accept")
        ).andExpect(status().is3xxRedirection());

        MeetingParticipant updated = meetingParticipantRepository.findByMeetingIdAndUserId(meeting.getId(), u.getId()).orElseThrow();
        assertEquals(InviteStatus.ACCEPTED, updated.getStatus());
    }

    // copy
    @WithMockUser(username = "Oly")
    @Test
    void copyTest() throws Exception {
        User u = userRepository.save(new User("Oly", "123@gmail.com", "pwd"));

        mockMvc.perform(
                post("/discover/copy")
                        .with(csrf())
                        .param("source", "source")
                        .param("externalId", "123")
                        .param("title", "Meeting")
                        .param("start", "2026-01-01T10:00:00Z")
        ).andExpect(status().is3xxRedirection());
    }
}