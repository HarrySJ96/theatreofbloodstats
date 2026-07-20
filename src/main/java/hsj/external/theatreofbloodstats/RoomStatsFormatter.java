/*
 * Copyright (c) 2026, HSJ (https://github.com/HarrySJ96)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package hsj.external.theatreofbloodstats;

import static hsj.external.theatreofbloodstats.TobConstants.LINE_BREAK;
import static hsj.external.theatreofbloodstats.TobConstants.TICK_LENGTH;
import static hsj.external.theatreofbloodstats.TobConstants.formatDamage;
import static hsj.external.theatreofbloodstats.TobConstants.formatPercent;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomStatsFormatter
{
	private final Client client;
	private final TheatreOfBloodStatsConfig config;
	private final ChatMessageManager chatMessageManager;

	public void buildSplitMessage(List<String> messages, String phaseName, int currentPhaseTime, int previousPhaseTime)
	{
		if (!config.chatboxSplits())
		{
			return;
		}

		String timeString = formatTime(currentPhaseTime);
		if (previousPhaseTime > 0)
		{
			timeString += " (" + formatTime(currentPhaseTime - previousPhaseTime) + ")";
		}

		messages.add(new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append(phaseName)
			.append(" - ")
			.append(ChatColorType.HIGHLIGHT)
			.append(timeString)
			.build());
	}

	public void buildDamageMessage(List<String> messages, String prefix, double personal, double total)
	{
		if (!config.chatboxDmg() || personal <= 0)
		{
			return;
		}

		double percent = percentOf(personal, total);
		messages.add(new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append(prefix)
			.append(" - ")
			.append(ChatColorType.HIGHLIGHT)
			.append(formatDamage(personal))
			.append(" (")
			.append(formatPercent(percent))
			.append("%)")
			.build());
	}

	public void buildHealedMessage(List<String> messages, String prefix, int healed)
	{
		if (!config.chatboxHealed() || healed <= 0)
		{
			return;
		}

		messages.add(new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append(prefix)
			.append(" - ")
			.append(ChatColorType.HIGHLIGHT)
			.append(formatDamage(healed))
			.build());
	}

	public void sendChatMessage(List<String> messages)
	{
		if (messages.isEmpty())
		{
			return;
		}

		for (String m : messages)
		{
			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.GAMEMESSAGE)
				.runeLiteFormattedMessage(m)
				.build());
		}
	}

	public String formatTime(int ticks)
	{
		int millis = ticks * TICK_LENGTH;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1);
		long hundredths = (millis % 1000) / 10;

		if (client.getVarbitValue(VarbitID.OPTION_PRECISE_TIMING) == 1)
		{
			return String.format("%d:%02d.%02d", minutes, seconds, hundredths);
		}

		return String.format("%d:%02d", minutes, seconds);
	}

	public double percentOf(double personal, double total)
	{
		return total > 0 ? (personal / total) * 100 : 0;
	}

	public String buildSplitString(String... lines)
	{
		return String.join(LINE_BREAK, lines);
	}
}
