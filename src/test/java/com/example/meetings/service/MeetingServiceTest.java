package com.example.meetings.service;

import com.example.meetings.discover.DiscoveredEvent;
import com.example.meetings.model.InviteStatus;
import com.example.meetings.model.Meeting;
import com.example.meetings.model.MeetingParticipant;
import com.example.meetings.model.User;
import com.example.meetings.repository.MeetingParticipantRepository;
import com.example.meetings.repository.MeetingRepository;
import com.example.meetings.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {
    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingParticipantRepository participantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MeetingService meetingService;

    // throw exception if create a meeting which has end before start
    @Test
    void endBeforeStart() {
        User organizer = new User("Oly", "123@gmail.com", "pwd");
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end = Instant.parse("2026-01-01T09:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.propose(organizer, "Meeting", "Description", start, end, List.of())
        );
    }

    // create meeting successfully
    @Test
    void newMeeting() {
        User organizer = new User("Oly", "123@gmail.com", "pwd");
        User u = new User("Winnie", "abc@gmail.com", "pwd");
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end = Instant.parse("2026-01-01T11:00:00Z");

        when(userRepository.findByUsername("Winnie")).thenReturn(Optional.of(u));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.propose(organizer, "Meeting", "Description", start, end, List.of("Winnie"));

        assertNotNull(result);
        assertEquals("Meeting", result.getTitle());
        assertEquals(2, result.getParticipants().size());

        verify(userRepository).findByUsername("Winnie");
        verify(meetingRepository).save(any(Meeting.class));
    }

    // throw exception if invite an inexisted user
    @Test
    void inviteeInexisted() {
        User organizer = new User("Oly", "123@gmail.com", "pwd");
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end = Instant.parse("2026-01-01T11:00:00Z");

        when(userRepository.findByUsername("Winnie")).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.propose(organizer, "Meeting", "Description", start, end, List.of("Winnie"))
        );

        verify(meetingRepository, never()).save(any());
    }

    // ignore repetitive invitee
    @Test
    void repetitiveInvitee() {
        User organizer = new User("Oly", "123@gmail.com", "pwd");
        User u = new User("Winnie", "abc@gmail.com", "pwd");
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end = Instant.parse("2026-01-01T11:00:00Z");

        when(userRepository.findByUsername("Winnie")).thenReturn(Optional.of(u));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.propose(organizer, "Meeting", "Description", start, end, List.of("Winnie", "Winnie", "Winnie"));
        assertEquals(2, result.getParticipants().size());
        verify(meetingRepository).save(any(Meeting.class));
    }

    // accept invite
    @Test
    void acceptInvite() {
        User u = new User("Winnie", "abc@gmail.com", "pwd");
        MeetingParticipant participant = mock(MeetingParticipant.class);

        when(participantRepository.findByMeetingIdAndUserId(anyLong(),any())).thenReturn(Optional.of(participant));

        meetingService.respond(1L, u, InviteStatus.ACCEPTED);

        verify(participant).setStatus(InviteStatus.ACCEPTED);
    }
    

    // throw exception if response is pending
    @Test
    void pendingResponse() {
        User u = new User("Winnie", "abc@gmail.com", "pwd");
        assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.respond(1L, u, InviteStatus.PENDING)
        );
    }

    // throw exception if invite is not found
    @Test
    void inviteNotFound() {
        User u = new User("Winnie", "abc@gmail.com", "pwd");

        when(participantRepository.findByMeetingIdAndUserId(anyLong(), any())).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.respond(1L, u, InviteStatus.ACCEPTED)
        );
    }

    // copy discovered event
    @Test
    void copyDiscoveredEvent() {
        User u = new User("Winnie", "abc@gmail.com", "pwd");
        Instant start = Instant.parse("2026-02-01T10:00:00Z");
        Instant end = Instant.parse("2026-02-01T12:00:00Z");
        DiscoveredEvent event =
                new DiscoveredEvent(
                        "Source",
                        "123",
                        "Meeting",
                        "Description",
                        start,
                        end,
                        "http://xxx.com",
                        "Venue"
                );

        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.copyFromDiscovered(u, event);

        assertEquals("Meeting", result.getTitle());
        assertEquals(u, result.getOrganizer());
        assertEquals(start, result.getStartTime());
        assertEquals(end, result.getEndTime());
        assertEquals(
            InviteStatus.ACCEPTED,
            result.getParticipants().iterator().next().getStatus()
        );

        verify(meetingRepository).save(any(Meeting.class));
    }

    // default no end event
    @Test
    void defaultNoEndEvent() {
        User u = new User("Winnie", "abc@gmail.com", "pwd");
        Instant start = Instant.parse("2026-02-01T10:00:00Z");

        DiscoveredEvent event =
                new DiscoveredEvent(
                        "Source",
                        "123",
                        "Meeting",
                        "Description",
                        start,
                        null,
                        "http://xxx.com",
                        "Venue"
                );

        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting result = meetingService.copyFromDiscovered(u, event);

        assertEquals(start.plus(Duration.ofHours(2)), result.getEndTime());
    }

    // return ical Token
    @Test
    void icalToken() {
        User u = new User("Winnie", "abc@gmail.com", "pwd");

        Meeting meeting = new Meeting("Meeting", "Description", Instant.now(), Instant.now().plusSeconds(3600), u);

        when(userRepository.findByIcalToken("token")).thenReturn(Optional.of(u));
        when(meetingRepository.findCalendarMeetings(u)).thenReturn(List.of(meeting));

        List<Meeting> result = meetingService.calendarForIcalToken("token");

        assertEquals(1, result.size());

        verify(userRepository).findByIcalToken("token");
        verify(meetingRepository).findCalendarMeetings(u);
    }

    // throw exception if invalid ical token
    @Test
    void invalidIcalToken() {
        when(userRepository.findByIcalToken("token")).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> meetingService.calendarForIcalToken("token")
        );
    }
}