package com.pleasebookme.server.resource.resources.dto;

/**
 * Counts backing the resource dashboard tiles.
 *
 * <p>Utilization is deliberately absent: it needs booking data joined against
 * resource availability, and neither the query nor an agreed definition exists
 * yet. Returning a plausible-looking number computed from something else would
 * be worse than the client leaving that tile reserved.
 */
public record ResourceStatsResponse(
    long total,
    long active,
    long inactive,
    long maintenance,
    long retired
) {
}
