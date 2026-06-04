package com.example.meetings.integration;

import com.example.meetings.discover.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ThirdPartyTest {
    // AgendaLx
    @Test
    void agendaLxTest() {
        AgendaLxProvider provider = new AgendaLxProvider();
        List<DiscoveredEvent> result = provider.search("music");

        assertNotNull(result);
        assertTrue(result.size() >= 0);
        if (!result.isEmpty()) {
            DiscoveredEvent e = result.get(0);

            assertNotNull(e.title());
            assertNotNull(e.source());
            assertNotNull(e.start());
        }
    }

    // SeatGeek
    @Test
    void seatGeekWithoutKey() {
        SeatGeekProvider provider = new SeatGeekProvider("");
        List<DiscoveredEvent> result = provider.search("concert");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void seatGeekWithKey() {
        String key = System.getenv("SEATGEEK_KEY");
        if (key == null || key.isBlank()) {
            return;
        }

        SeatGeekProvider provider = new SeatGeekProvider(key);
        var result = provider.search("music");

        assertNotNull(result);
    }

    // Ticketmaster
    @Test
    void ticketmasterWithoutKey() {
        TicketmasterProvider provider = new TicketmasterProvider("", "PT");
        List<DiscoveredEvent> result = provider.search("music");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void ticketmasterWithKey() {
        String key = System.getenv("TICKETMASTER_KEY");
        if (key == null || key.isBlank()) {
            return;
        }

        TicketmasterProvider provider = new TicketmasterProvider(key, "PT");
        List<DiscoveredEvent> result = provider.search("music");

        assertNotNull(result);

        if (!result.isEmpty()) {
            DiscoveredEvent e = result.get(0);

            assertNotNull(e.title());
            assertNotNull(e.source());
            assertNotNull(e.start());
        }
    }
}