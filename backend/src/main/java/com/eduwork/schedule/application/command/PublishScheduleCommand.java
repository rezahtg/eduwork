package com.eduwork.schedule.application.command;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Command for publishing a schedule.
 *
 * @param scheduleId ID of the schedule to publish
 * @param mentorId   ID of the mentor (for authorization)
 */
@Value
@Builder
public class PublishScheduleCommand {
    UUID scheduleId;
    UUID mentorId;
}
