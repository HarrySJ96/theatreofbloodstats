/*
 * Copyright (c) 2020, HSJ
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
import static hsj.external.theatreofbloodstats.TobConstants.DECIMAL_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.THRONE_ROOM_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.TICK_LENGTH;
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
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
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
	@Inject
	private Client client;

	@Inject
	private TheatreOfBloodStatsConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	public InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	public final Map<Boss, TheatreOfBloodStatsInfoBox> infoBoxes = new EnumMap<>(Boss.class);
	private RoomTracker roomTracker;
	private MaidenTracker maidenTracker;
	private BloatTracker bloatTracker;
	private NyloTracker nyloTracker;
	private SoteTracker soteTracker;
	private XarpusTracker xarpusTracker;
	private VerzikTracker verzikTracker;
	private List<RoomTracker> allTrackers;

	@Provides
	TheatreOfBloodStatsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TheatreOfBloodStatsConfig.class);
	}

	@Override
	protected void startUp()
	{
		maidenTracker = new MaidenTracker(client, this);
		bloatTracker = new BloatTracker(client, this);
		nyloTracker = new NyloTracker(client, this);
		soteTracker = new SoteTracker(client, this);
		xarpusTracker = new XarpusTracker(client, this);
		verzikTracker = new VerzikTracker(client, this);
		allTrackers = List.of(maidenTracker, bloatTracker, nyloTracker, soteTracker, xarpusTracker, verzikTracker);
	}

	@Override
	protected void shutDown()
	{
		resetAll();
		resetAllInfoBoxes();

		maidenTracker = null;
		bloatTracker = null;
		nyloTracker = null;
		soteTracker = null;
		xarpusTracker = null;
		verzikTracker = null;
		allTrackers = null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!"theatreofbloodstats".equals(event.getGroup()) || infoBoxes.isEmpty())
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
		}
	}

	@Subscribe
	public void onWorldViewLoaded(WorldViewLoaded event)
	{
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

		for (RoomTracker tracker : allTrackers)
		{
			if (Arrays.equals(tracker.getRegionIds(), regions))
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

		double percent = total > 0 ? (personal / total) * 100 : 0;
		messages.add(new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append(prefix)
			.append(" - ")
			.append(ChatColorType.HIGHLIGHT)
			.append(DMG_FORMAT.format(personal))
			.append(" (")
			.append(DECIMAL_FORMAT.format(percent))
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
			.append(DMG_FORMAT.format(healed))
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

	public TheatreOfBloodStatsInfoBox createInfoBox(int itemId, String room, String time, double percent, String damage, String splits, String healed)
	{
		BufferedImage image = itemManager.getImage(itemId);
		return new TheatreOfBloodStatsInfoBox(image, config, this, room, time, percent, damage, splits, healed);
	}

	public void addInfoBox(Boss boss, TheatreOfBloodStatsInfoBox box)
	{
		TheatreOfBloodStatsInfoBox old = infoBoxes.get(boss);
		if (old != null)
		{
			infoBoxManager.removeInfoBox(old);
		}
		infoBoxManager.addInfoBox(box);
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
		else
		{
			return String.format("%d:%02d", minutes, seconds);
		}
	}

	public String buildSplitString(String... lines)
	{
		return String.join("</br>", lines);
	}

	private void resetAll()
	{
		maidenTracker.reset();
		bloatTracker.reset();
		nyloTracker.reset();
		soteTracker.reset();
		xarpusTracker.reset();
		verzikTracker.reset();
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
