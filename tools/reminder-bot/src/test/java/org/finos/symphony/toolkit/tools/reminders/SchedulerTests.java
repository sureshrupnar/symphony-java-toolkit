package org.finos.symphony.toolkit.tools.reminders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.finos.springbot.symphony.content.SymphonyRoom;
import org.finos.springbot.symphony.stream.Participant;
import org.finos.springbot.symphony.stream.cluster.LeaderService;
import org.finos.springbot.tools.reminders.Reminder;
import org.finos.springbot.tools.reminders.ReminderList;
import org.finos.springbot.tools.reminders.alerter.Scheduler;
import org.finos.springbot.workflow.actions.Action;
import org.finos.springbot.workflow.actions.FormAction;
import org.finos.springbot.workflow.content.Addressable;
import org.finos.springbot.workflow.content.Chat;
import org.finos.springbot.workflow.content.User;
import org.finos.springbot.workflow.conversations.Conversations;
import org.finos.springbot.workflow.history.History;
import org.finos.springbot.workflow.response.WorkResponse;
import org.finos.springbot.workflow.response.handlers.ResponseHandlers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.junit.jupiter.MockitoExtension;

import com.symphony.api.model.StreamAttributes;
import com.symphony.api.model.StreamList;
import com.symphony.api.pod.StreamsApi;

@ExtendWith(MockitoExtension.class)
public class SchedulerTests {

	@Mock
	History<Addressable> history;

	@Mock
	ResponseHandlers responseHandlers;

	@Mock
	LeaderService leaderService;

	@Mock
	Conversations<Chat, User> rooms;

	@InjectMocks
	Scheduler scheduler = new Scheduler();

	LocalDateTime expectedTime = LocalDateTime.now();

	@SuppressWarnings("unchecked")
	@Test
	public void handleFeedLeaderTest() {
		when(history.getLastFromHistory(Mockito.any(Class.class), Mockito.any(Addressable.class)))
				.thenReturn(reminderList());

		when(leaderService.isLeader(Mockito.any())).thenReturn(true);
		when(rooms.getAllAddressables()).thenReturn(createStreams());
		scheduler.everyFiveMinutesWeekday();
		verify(responseHandlers).accept(Mockito.any(WorkResponse.class));
		ArgumentCaptor<WorkResponse> argumentCaptor = ArgumentCaptor.forClass(WorkResponse.class);
		verify(responseHandlers).accept(argumentCaptor.capture());
		WorkResponse fr = argumentCaptor.getValue();
		Reminder r = (Reminder) fr.getFormObject();
		Assertions.assertEquals(r.getLocalTime(), expectedTime);

		// reminder timefinder tests to chck formresponse

	}

	@Test
	public void handleFeedNonLeaderTest() {
		when(leaderService.isLeader(Mockito.any())).thenReturn(false);
		scheduler.everyFiveMinutesWeekday();
		verify(responseHandlers, VerificationModeFactory.noInteractions()).accept(Mockito.any(WorkResponse.class));

	}

	private Set<Addressable> createStreams() {
		return Collections.singleton(new SymphonyRoom("test", "1234"));
	}

	private Optional<ReminderList> reminderList() {
		Reminder reminder = new Reminder();
		reminder.setDescription("Check at 9 pm");
		reminder.setLocalTime(expectedTime);
		List<Reminder> reminders = new ArrayList<>();
		reminders.add(reminder);
		ReminderList rl = new ReminderList();
		rl.setTimeZone(ZoneId.of("Europe/London"));

		rl.setReminders(reminders);
		Optional<ReminderList> rrl = Optional.of(rl);
		return rrl;
	}
}
