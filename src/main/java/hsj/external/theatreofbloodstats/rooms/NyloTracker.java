package hsj.external.theatreofbloodstats.rooms;

import hsj.external.theatreofbloodstats.Boss;
import hsj.external.theatreofbloodstats.RoomTracker;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsInfoBox;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsPlugin;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_PERSONAL_DAMAGE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_ROOM_COMPLETE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_TOTAL_HEALING;
import static hsj.external.theatreofbloodstats.TobConstants.NYLOCAS_IMAGE_ID;
import static hsj.external.theatreofbloodstats.TobConstants.NYLOCAS_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.NYLOCAS_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.NYLOCAS_VALID_SPAWNS;
import static hsj.external.theatreofbloodstats.TobConstants.NYLOCAS_WAVE;
import static hsj.external.theatreofbloodstats.TobConstants.NYLOCAS_WAVES_TOTAL;
import static hsj.external.theatreofbloodstats.TobConstants.packRegionCoords;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.Text;

public class NyloTracker extends RoomTracker
{
	private int currentNylos;
	private boolean nyloWavesFinished;
	private boolean nyloCleanupFinished;
	private boolean waveThisTick = false;
	private int waveTime;
	private int cleanupTime;
	private int bossSpawnTime;
	private int nyloWave = 0;

	public NyloTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		super(client, plugin);
	}

	@Override
	public int[] getRegionIds()
	{
		return NYLOCAS_REGION_IDS;
	}

	@Override
	public void onNpcSpawned(NpcSpawned event)
	{
		NPC npc = event.getNpc();
		int npcId = npc.getId();
		switch (npcId)
		{
			case NpcID.TOB_NYLOCAS_SUPPORT:
			case NpcID.TOB_NYLOCAS_SUPPORT_STORY:
			case NpcID.TOB_NYLOCAS_SUPPORT_HARD:
				startTick = client.getTickCount();
				break;
			case NpcID.NYLOCAS_BOSS_SPAWNING:
			case NpcID.NYLOCAS_BOSS_SPAWNING_STORY:
			case NpcID.NYLOCAS_BOSS_SPAWNING_HARD:
				bossSpawnTime = client.getTickCount() - startTick;
				break;
			case NpcID.NYLOCAS_BOSS_MELEE:
			case NpcID.NYLOCAS_BOSS_MELEE_STORY:
			case NpcID.NYLOCAS_BOSS_MELEE_HARD:
					bossNpc = event.getNpc();
				break;
		}

		if (Arrays.binarySearch(NYLOCAS_IDS, npcId) < 0)
		{
			return;
		}

		currentNylos++;
		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, npc.getLocalLocation());
		int packedLoc = packRegionCoords(worldPoint.getRegionX(), worldPoint.getRegionY());

		if (Arrays.binarySearch(NYLOCAS_VALID_SPAWNS, packedLoc) < 0)
		{
			return;
		}

		if (!waveThisTick)
		{
			nyloWave++;
			waveThisTick = true;
		}

		if (nyloWave == NYLOCAS_WAVES_TOTAL && !nyloWavesFinished)
		{
			waveTime = client.getTickCount() - startTick;
			nyloWavesFinished = true;
		}
	}

	@Override
	public void onNpcDespawned(NpcDespawned event)
	{
		int npcId = event.getNpc().getId();
		if (Arrays.binarySearch(NYLOCAS_IDS, npcId) < 0)
		{
			return;
		}

		currentNylos--;

		if (nyloWavesFinished && !nyloCleanupFinished && currentNylos == 0)
		{
			cleanupTime = client.getTickCount() - startTick;
			nyloCleanupFinished = true;
		}
	}

	@Override
	public void onGameTick(GameTick event)
	{
		if (waveThisTick)
		{
			waveThisTick = false;
		}
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		if (!NYLOCAS_WAVE.matcher(Text.removeTags(event.getMessage())).find())
		{
			return;
		}

		List<String> messages = new ArrayList<>();
		double percent = totalDamage > 0 ? ((double) personalDamage / totalDamage) * 100 : 0;
		String roomTime = "";
		String splits = "";
		String healing = MSG_TOTAL_HEALING + " - " + DMG_FORMAT.format(totalHealing);
		String damage = (personalDamage > 0) ? MSG_PERSONAL_DAMAGE + " - " + DMG_FORMAT.format(personalDamage) : "";

		if (startTick > 0)
		{
			int roomTicks = client.getTickCount() - startTick;
			roomTime = plugin.formatTime(roomTicks);

			splits = plugin.buildSplitString("Waves - " + plugin.formatTime(waveTime),
				"Cleanup - " + plugin.formatTime(cleanupTime) + " (" + plugin.formatTime(cleanupTime - waveTime) + ")",
				"Boss Spawn - " + plugin.formatTime(bossSpawnTime) + " (" + plugin.formatTime(bossSpawnTime - cleanupTime) + ")",
				MSG_ROOM_COMPLETE + " - " + roomTime + " (" + plugin.formatTime(roomTicks - bossSpawnTime) + ")");

			plugin.buildSplitMessage(messages, "Waves", waveTime, 0);
			plugin.buildSplitMessage(messages, "Cleanup", cleanupTime, waveTime);
			plugin.buildSplitMessage(messages, "Boss Spawn", bossSpawnTime, cleanupTime);
			plugin.buildSplitMessage(messages, MSG_ROOM_COMPLETE, roomTicks, bossSpawnTime);
		}

		plugin.buildDamageMessage(messages, MSG_PERSONAL_DAMAGE, personalDamage, totalDamage);
		plugin.buildHealedMessage(messages, MSG_TOTAL_HEALING, totalHealing);
		plugin.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(NYLOCAS_IMAGE_ID, "Nylocas", roomTime, percent, damage, splits, healing);
		plugin.infoBoxManager.addInfoBox(box);
		plugin.infoBoxes.put(Boss.NYLOCAS, box);
		reset();
	}

	@Override
	public void reset()
	{
		startTick = -1;
		currentNylos = 0;
		nyloWavesFinished = false;
		nyloCleanupFinished = false;
		waveTime = 0;
		cleanupTime = 0;
		bossSpawnTime = 0;
		waveThisTick = false;
		nyloWave = 0;
		personalDamage = 0;
		totalDamage = 0;
		bossNpc = null;
	}
}
