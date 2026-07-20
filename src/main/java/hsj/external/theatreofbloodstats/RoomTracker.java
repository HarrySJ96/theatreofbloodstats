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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.util.Text;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class RoomTracker
{
	protected final Client client;
	protected final RoomStatsFormatter formatter;
	protected final TheatreOfBloodStatsPlugin plugin;
	@Getter(AccessLevel.PROTECTED)
	private final String roomName = getBoss().getName();

	protected int personalDamage = 0;
	protected int totalDamage = 0;
	protected int totalHealing = 0;
	protected int startTick = -1;
	protected NPC bossNpc = null;

	protected abstract int[] getRegionIds();

	protected abstract Pattern getCompletionPattern();

	protected abstract Boss getBoss();

	protected abstract int getImageId();

	protected abstract RoomResult buildResult(List<String> messages);

	protected void onVarbitChanged(VarbitChanged event)
	{
	}

	protected void onNpcSpawned(NpcSpawned event)
	{
	}

	protected void onNpcChanged(NpcChanged event)
	{
	}

	protected void onNpcDespawned(NpcDespawned event)
	{
	}

	protected void onGameTick(GameTick event)
	{
	}

	protected void onHitsplatApplied(HitsplatApplied event)
	{
		if (bossNpc == null || event.getActor() != bossNpc)
		{
			return;
		}

		Hitsplat hitsplat = event.getHitsplat();
		int amount = hitsplat.getAmount();

		if (hitsplat.getHitsplatType() == HitsplatID.HEAL)
		{
			totalHealing += amount;
		}
		else if (hitsplat.isMine())
		{
			personalDamage += amount;
			totalDamage += amount;
		}
		else if (hitsplat.isOthers())
		{
			totalDamage += amount;
		}
	}

	protected void onOverheadTextChanged(OverheadTextChanged event)
	{
	}

	protected void onAnimationChanged(AnimationChanged event)
	{
	}

	protected final void onChatMessage(ChatMessage event)
	{
		if (!getCompletionPattern().matcher(Text.removeTags(event.getMessage())).find())
		{
			return;
		}

		List<String> messages = new ArrayList<>();
		RoomResult result = buildResult(messages);

		formatter.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(getImageId(), getRoomName(), result);
		plugin.addInfoBox(getBoss(), box);
		reset();
	}

	protected final String buildSplitSummary(List<String> messages, List<Split> splits)
	{
		StringBuilder sb = new StringBuilder();
		int previousTime = 0;

		for (int i = 0; i < splits.size(); i++)
		{
			Split split = splits.get(i);

			sb.append(split.getLabel())
				.append(" - ")
				.append(formatter.formatTime(split.getTick()));

			if (i > 0)
			{
				sb.append(" (")
					.append(formatter.formatTime(split.getTick() - previousTime))
					.append(")");
			}

			if (i < splits.size() - 1)
			{
				sb.append(LINE_BREAK);
			}

			formatter.buildSplitMessage(messages, split.getLabel(), split.getTick(), previousTime);
			previousTime = split.getTick();
		}

		return sb.toString();
	}

	protected void resetBaseState()
	{
		startTick = -1;
		personalDamage = 0;
		totalDamage = 0;
		totalHealing = 0;
		bossNpc = null;
	}

	protected abstract void resetRoomState();

	protected final void reset()
	{
		resetBaseState();
		resetRoomState();
	}
}