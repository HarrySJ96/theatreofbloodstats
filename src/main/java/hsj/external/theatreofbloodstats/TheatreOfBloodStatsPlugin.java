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

import com.google.inject.Provides;
import static hsj.external.theatreofbloodstats.TheatreOfBloodStatsConfig.CONFIG_GROUP;
import static hsj.external.theatreofbloodstats.TobConstants.SOTETSEG_MAZE_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.THRONE_ROOM_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.TOB_LOBBY_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.TOB_REGION_IDS;
import hsj.external.theatreofbloodstats.rooms.BloatTracker;
import hsj.external.theatreofbloodstats.rooms.MaidenTracker;
import hsj.external.theatreofbloodstats.rooms.NyloTracker;
import hsj.external.theatreofbloodstats.rooms.SoteTracker;
import hsj.external.theatreofbloodstats.rooms.VerzikTracker;
import hsj.external.theatreofbloodstats.rooms.XarpusTracker;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

@PluginDescriptor(
	name = "Theatre of Blood Stats",
	description = "Theatre of Blood room splits and damage",
	tags = {"combat", "raid", "pve", "pvm", "bosses", "tob"}
)
public class TheatreOfBloodStatsPlugin extends Plugin
{
	static final String TEST_INFO_BOXES_PROPERTY = "theatreofbloodstats.testInfoBoxes";
	private final Map<Boss, TheatreOfBloodStatsInfoBox> infoBoxes = new EnumMap<>(Boss.class);

	@Inject
	private Client client;

	@Inject
	private TheatreOfBloodStatsConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	private RoomTracker roomTracker;
	private List<RoomTracker> allTrackers;

	@Provides
	TheatreOfBloodStatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TheatreOfBloodStatsConfig.class);
	}

	@Override
	protected void startUp()
	{
		RoomStatsFormatter formatter = new RoomStatsFormatter(client, config, chatMessageManager);
		allTrackers = List.of(
			new MaidenTracker(client, formatter, this),
			new BloatTracker(client, formatter, this),
			new NyloTracker(client, formatter, this),
			new SoteTracker(client, formatter, this),
			new XarpusTracker(client, formatter, this),
			new VerzikTracker(client, formatter, this)
		);
	}

	@Override
	protected void shutDown()
	{
		resetAll();
		resetAllInfoBoxes();
		allTrackers = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()) || infoBoxes.isEmpty())
		{
			return;
		}

		switch (event.getKey())
		{
			case "infoBoxTooltip":
			case "infoBoxTooltipDmg":
			case "infoBoxTooltipHealed":
			case "infoBoxTooltipSplits":
			{
				for (TheatreOfBloodStatsInfoBox box : infoBoxes.values())
				{
					box.rebuildTooltip();
				}
				break;
			}
			default:
				break;
		}
	}

	@Subscribe
	public void onWorldViewLoaded(WorldViewLoaded event)
	{
		if (allTrackers == null || Boolean.getBoolean(TEST_INFO_BOXES_PROPERTY))
		{
			return;
		}

		int[] regions = event.getWorldView().getMapRegions();
		boolean isTobRegion = false;

		for (int[] tobRegion : TOB_REGION_IDS)
		{
			if (Arrays.equals(tobRegion, regions))
			{
				isTobRegion = true;
				break;
			}
		}

		if (!isTobRegion)
		{
			if (!infoBoxes.isEmpty())
			{
				resetAllInfoBoxes();
			}
			return;
		}

		if (Arrays.equals(TOB_LOBBY_REGION_IDS, regions) || Arrays.equals(THRONE_ROOM_REGION_IDS, regions))
		{
			resetAll();
			return;
		}

		if (Arrays.equals(SOTETSEG_MAZE_REGION_IDS, regions))
		{
			return;
		}

		for (RoomTracker tracker : allTrackers)
		{
			if (Arrays.equals(tracker.getRegionIds(), regions) && tracker != roomTracker)
			{
				tracker.reset();
				roomTracker = tracker;
				return;
			}
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (roomTracker == null)
		{
			return;
		}

		roomTracker.onVarbitChanged(event);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (roomTracker == null)
		{
			return;
		}

		roomTracker.onNpcSpawned(event);
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		if (roomTracker == null)
		{
			return;
		}

		roomTracker.onNpcChanged(event);
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (roomTracker == null)
		{
			return;
		}

		roomTracker.onNpcDespawned(event);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (roomTracker == null)
		{
			return;
		}

		roomTracker.onGameTick(event);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		if (roomTracker == null)
		{
			return;
		}

		roomTracker.onHitsplatApplied(event);
	}

	@Subscribe
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		Actor npc = event.getActor();
		if (!(npc instanceof NPC) || roomTracker == null)
		{
			return;
		}

		roomTracker.onOverheadTextChanged(event);
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		Actor npc = event.getActor();
		if (!(npc instanceof NPC) || roomTracker == null)
		{
			return;
		}

		roomTracker.onAnimationChanged(event);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (roomTracker == null || event.getType() != ChatMessageType.GAMEMESSAGE || !event.getMessage().contains("Wave"))
		{
			return;
		}

		roomTracker.onChatMessage(event);
	}

	public TheatreOfBloodStatsInfoBox createInfoBox(int itemId, String room, RoomResult result)
	{
		BufferedImage image = itemManager.getImage(itemId);
		return new TheatreOfBloodStatsInfoBox(image, config, this, room, result);
	}

	public void addInfoBox(Boss boss, TheatreOfBloodStatsInfoBox box)
	{
		TheatreOfBloodStatsInfoBox old = infoBoxes.get(boss);
		if (old != null)
		{
			infoBoxManager.removeInfoBox(old);
		}
		infoBoxManager.addInfoBox(box);
		infoBoxes.put(boss, box);
	}

	private void resetAll()
	{
		for (RoomTracker tracker : allTrackers)
		{
			tracker.reset();
		}
		roomTracker = null;
	}

	private void resetAllInfoBoxes()
	{
		for (TheatreOfBloodStatsInfoBox box : infoBoxes.values())
		{
			infoBoxManager.removeInfoBox(box);
		}
		infoBoxes.clear();
	}
}
