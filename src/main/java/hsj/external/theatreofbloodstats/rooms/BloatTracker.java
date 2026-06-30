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
package hsj.external.theatreofbloodstats.rooms;

import hsj.external.theatreofbloodstats.Boss;
import hsj.external.theatreofbloodstats.RoomTracker;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsInfoBox;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsPlugin;
import static hsj.external.theatreofbloodstats.TobConstants.BLOAT_IMAGE_ID;
import static hsj.external.theatreofbloodstats.TobConstants.BLOAT_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.BLOAT_WAVE;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_PERSONAL_DAMAGE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_ROOM_COMPLETE;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.WorldView;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.util.Text;

public class BloatTracker extends RoomTracker
{
	private final List<Integer> downTimes = new ArrayList<>();

	public BloatTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		super(client, plugin);
	}

	@Override
	public int[] getRegionIds()
	{
		return BLOAT_REGION_IDS;
	}

	@Override
	public void onGameTick(GameTick event)
	{
		if (bossNpc == null)
		{
			WorldView worldview = client.getTopLevelWorldView();
			if (worldview != null)
			{
				for (NPC npc : worldview.npcs())
				{
					if (npc != null && Boss.BLOAT.getName().equals(npc.getName()))
					{
						bossNpc = npc;
						break;
					}
				}
			}
		}
	}

	@Override
	public void onVarbitChanged(VarbitChanged event)
	{
		if (startTick != -1)
		{
			return;
		}

		if (event.getVarbitId() == VarbitID.TOB_CLIENT_WAVEPROGRESS_TYPE && client.getVarbitValue(VarbitID.TOB_CLIENT_WAVEPROGRESS_TYPE) == 1)
		{
			startTick = client.getTickCount();
		}
	}

	@Override
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor npc = event.getActor();
		String npcName = npc.getName();
		if (npcName == null || !npcName.equals(Boss.BLOAT.getName()))
		{
			return;
		}

		if (npc.getAnimation() == AnimationID.TOB_BLOAT_SLEEP)
		{
			downTimes.add(client.getTickCount() - startTick);
		}
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		if (!BLOAT_WAVE.matcher(Text.removeTags(event.getMessage())).find())
		{
			return;
		}

		List<String> messages = new ArrayList<>();
		double percent = plugin.percentOf(personalDamage, totalDamage);
		String roomTime = "";
		StringBuilder splits = new StringBuilder();
		String damage = (personalDamage > 0) ? MSG_PERSONAL_DAMAGE + " - " + DMG_FORMAT.format(personalDamage) : "";

		if (startTick > 0)
		{
			int roomTicks = client.getTickCount() - startTick;
			roomTime = plugin.formatTime(roomTicks);
			if (!downTimes.isEmpty())
			{
				for (int i = 0; i < downTimes.size(); i++)
				{
					if (i == 0)
					{
						splits.append("Down ").append(i + 1).append(" - ").append(plugin.formatTime(downTimes.get(i))).append("</br>");
					}
					else
					{
						splits.append("Down ").append(i + 1).append(" - ").append(plugin.formatTime(downTimes.get(i))).append(" (").append(plugin.formatTime(downTimes.get(i) - downTimes.get(i - 1))).append(")</br>");
					}
					plugin.buildSplitMessage(messages, "Down " + (i + 1), downTimes.get(i), i == 0 ? 0 : downTimes.get(i - 1));
				}
			}

			splits.append(MSG_ROOM_COMPLETE).append(" - ").append(roomTime).append(" (").append(plugin.formatTime(roomTicks - downTimes.get(downTimes.size() - 1))).append(")");
			plugin.buildSplitMessage(messages, MSG_ROOM_COMPLETE, roomTicks, downTimes.get(downTimes.size() - 1));
		}


		plugin.buildDamageMessage(messages, MSG_PERSONAL_DAMAGE, personalDamage, totalDamage);
		plugin.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(BLOAT_IMAGE_ID, "Bloat", roomTime, percent, damage, splits.toString(), "");
		plugin.addInfoBox(Boss.BLOAT, box);
		reset();
	}

	@Override
	public void reset()
	{
		startTick = -1;
		personalDamage = 0;
		totalDamage = 0;
		downTimes.clear();
		bossNpc = null;
	}
}
