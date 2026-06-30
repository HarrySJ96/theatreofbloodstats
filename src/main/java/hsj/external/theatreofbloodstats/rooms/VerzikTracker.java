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
import static hsj.external.theatreofbloodstats.TobConstants.DECIMAL_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.VERZIK_IMAGE_ID;
import static hsj.external.theatreofbloodstats.TobConstants.VERZIK_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.VERZIK_WAVE;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.Text;

public class VerzikTracker extends RoomTracker
{
	private int verzikP1time;
	private int verzikP2time;
	private int verzikP1personal;
	private int verzikP1total;
	private int verzikP2personal;
	private int verzikP2total;
	private int verzikP2healed;

	public VerzikTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		super(client, plugin);
	}

	@Override
	public int[] getRegionIds()
	{
		return VERZIK_REGION_IDS;
	}

	@Override
	public void onNpcSpawned(NpcSpawned event)
	{
		switch (event.getNpc().getId())
		{
			case NpcID.VERZIK_PHASE1_TO2_TRANSITION:
			case NpcID.VERZIK_PHASE1_TO2_TRANSITION_STORY:
			case NpcID.VERZIK_PHASE1_TO2_TRANSITION_HARD:
				bossNpc = event.getNpc();
				verzikP1time = client.getTickCount() - startTick;
				verzikP1personal = personalDamage;
				verzikP1total = totalDamage;
				break;
			case NpcID.VERZIK_PHASE2_TO3_TRANSITION:
			case NpcID.VERZIK_PHASE2_TO3_TRANSITION_STORY:
			case NpcID.VERZIK_PHASE2_TO3_TRANSITION_HARD:
				bossNpc = event.getNpc();
				verzikP2time = client.getTickCount() - startTick;
				verzikP2personal = personalDamage - verzikP1personal;
				verzikP2total = totalDamage - verzikP1total;
				verzikP2healed = totalHealing;
				break;
			case NpcID.VERZIK_PHASE3:
			case NpcID.VERZIK_PHASE3_STORY:
			case NpcID.VERZIK_PHASE3_HARD:
				bossNpc = event.getNpc();
				break;
		}
	}

	@Override
	public void onNpcChanged(NpcChanged event)
	{
		switch (event.getNpc().getId())
		{
			case NpcID.VERZIK_PHASE1:
			case NpcID.VERZIK_PHASE1_STORY:
			case NpcID.VERZIK_PHASE1_HARD:
				bossNpc = event.getNpc();
				startTick = client.getTickCount();
				break;
		}
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		if (!VERZIK_WAVE.matcher(Text.removeTags(event.getMessage())).find())
		{
			return;
		}

		List<String> messages = new ArrayList<>();
		double p3personal = personalDamage - (verzikP1personal + verzikP2personal);
		double p3total = totalDamage - (verzikP1total + verzikP2total);
		int p3healed = totalHealing - verzikP2healed;

		double p1percent = plugin.percentOf(verzikP1personal, verzikP1total);
		double p2percent = plugin.percentOf(verzikP2personal, verzikP2total);
		double p3percent = plugin.percentOf(p3personal, p3total);
		double percent = plugin.percentOf(personalDamage, totalDamage);

		String roomTime = "";
		String splits = "";
		String damage = "";
		String healing = plugin.buildSplitString("P2 Healed - " + DMG_FORMAT.format(verzikP2healed),
			"P3 Healed - " + DMG_FORMAT.format(p3healed),
			"Total Healed - " + DMG_FORMAT.format(totalHealing));

		if (startTick > 0)
		{
			int roomTicks = client.getTickCount() - startTick;
			roomTime = plugin.formatTime(roomTicks);

			splits = plugin.buildSplitString("P1 - " + plugin.formatTime(verzikP1time),
				"P2 - " + plugin.formatTime(verzikP2time) + " (" + plugin.formatTime(verzikP2time - verzikP1time) + ")",
				"P3 - " + roomTime + " (" + plugin.formatTime(roomTicks - verzikP2time) + ")");

			plugin.buildSplitMessage(messages, "P1", verzikP1time, 0);
			plugin.buildSplitMessage(messages, "P2", verzikP2time, verzikP1time);
			plugin.buildSplitMessage(messages, "P3", roomTicks, verzikP2time);
		}

		if (verzikP1personal > 0)
		{
			damage += "P1 Personal Damage - " + DMG_FORMAT.format(verzikP1personal) + " (" + DECIMAL_FORMAT.format(p1percent) + "%)</br>";
			plugin.buildDamageMessage(messages, "P1 Personal Damage", verzikP1personal, verzikP1total);
		}
		if (verzikP2personal > 0)
		{
			damage += "P2 Personal Damage - " + DMG_FORMAT.format(verzikP2personal) + " (" + DECIMAL_FORMAT.format(p2percent) + "%)</br>";
			plugin.buildDamageMessage(messages, "P2 Personal Damage", verzikP2personal, verzikP2total);
		}
		if (p3personal > 0)
		{
			damage += "P3 Personal Damage - " + DMG_FORMAT.format(p3personal) + " (" + DECIMAL_FORMAT.format(p3percent) + "%)</br>";
			plugin.buildDamageMessage(messages, "P3 Personal Damage", p3personal, p3total);
		}
		if (personalDamage > 0)
		{
			damage += "Total Personal Damage - " + DMG_FORMAT.format(personalDamage);
			plugin.buildDamageMessage(messages, "Total Personal Damage", personalDamage, totalDamage);
		}

		plugin.buildHealedMessage(messages, "P2 Healed", verzikP2healed);
		plugin.buildHealedMessage(messages, "P3 Healed", p3healed);
		plugin.buildHealedMessage(messages, "Total Healed", totalHealing);
		plugin.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(VERZIK_IMAGE_ID, "Verzik", roomTime, percent, damage, splits, healing);
		plugin.addInfoBox(Boss.VERZIK, box);
		reset();
	}

	@Override
	public void reset()
	{
		startTick = -1;
		verzikP1time = 0;
		verzikP2time = 0;
		verzikP1personal = 0;
		verzikP1total = 0;
		verzikP2personal = 0;
		verzikP2total = 0;
		verzikP2healed = 0;
		personalDamage = 0;
		totalDamage = 0;
		totalHealing = 0;
		bossNpc = null;
	}
}
