package com.eventflow.catalog.controller.mcp;

import com.eventflow.catalog.dto.EventResponse;
import com.eventflow.catalog.service.EventService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Component
public class EventMcpTools {

    private final EventService eventService;

    public EventMcpTools(EventService eventService) {
        this.eventService = eventService;
    }

    @McpTool(
            name = "searchEvents",
            description = "Search published events by title text and/or date range. All parameters are optional. Returns published events ordered by date, soonest first.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true))
    public List<EventResponse> searchEvents(
            @McpToolParam(description = "Text to look for in the event title, case-insensitive. Leave empty to match all events.", required = false) String query,
            @McpToolParam(description = "Earliest event date-time, inclusive, ISO format such as 2026-12-01T00:00:00. Leave empty for no lower bound.", required = false) String dateFrom,
            @McpToolParam(description = "Latest event date-time, inclusive, ISO format such as 2026-12-31T23:59:59. Leave empty for no upper bound.", required = false) String dateTo) {
        return eventService.search(query, parseDate("dateFrom", dateFrom), parseDate("dateTo", dateTo));
    }

    @McpTool(
            name = "getEventDetails",
            description = "Get one published event by its id. Draft or unknown events are reported as not found.",
            annotations = @McpTool.McpAnnotations(readOnlyHint = true, destructiveHint = false, idempotentHint = true))
    public EventResponse getEventDetails(
            @McpToolParam(description = "The event id, a UUID.", required = true) String eventId) {
        return eventService.getById(parseUuid(eventId));
    }

    private static LocalDateTime parseDate(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " must be an ISO date-time such as 2026-12-01T20:00:00");
        }
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value == null ? "" : value.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("eventId must be a valid UUID");
        }
    }
}
