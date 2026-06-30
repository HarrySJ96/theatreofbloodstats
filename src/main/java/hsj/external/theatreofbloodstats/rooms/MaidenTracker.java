package hsj.external.theatreofbloodstats.rooms;

import hsj.external.theatreofbloodstats.Boss;
import hsj.external.theatreofbloodstats.RoomTracker;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsInfoBox;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsPlugin;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.MAIDEN_IMAGE_ID;
import static hsj.external.theatreofbloodstats.TobConstants.MAIDEN_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.MAIDEN_WAVE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_PERSONAL_DAMAGE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_ROOM_COMPLETE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_TOTAL_HEALING;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.Text;

public class MaidenTracker extends RoomTracker
{
	private int maiden70time = 0;
	private int maiden50time = 0;
	private int maiden30time = 0;

	public MaidenTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		super(client, plugin);
	}

	@Override
	public int[] getRegionIds()
	{
		return MAIDEN_REGION_IDS;
	}

	@Override
	public void onNpcSpawned(NpcSpawned event)
	{
		switch (event.getNpc().getId())
		{
			case NpcID.TOB_MAIDEN_100:
			case NpcID.TOB_MAIDEN_100_STORY:
			case NpcID.TOB_MAIDEN_100_HARD:
				bossNpc = event.getNpc();
				startTick = client.getTickCount();
				break;
		}
	}

	@Override
	public void onNpcChanged(NpcChanged event)
	{
		int npcId = event.getNpc().getId();
		switch (npcId)
		{
			case NpcID.TOB_MAIDEN_70:
			case NpcID.TOB_MAIDEN_70_STORY:
			case NpcID.TOB_MAIDEN_70_HARD:
				if (startTick != -1)
				{
					maiden70time = client.getTickCount() - startTick;
				}
				break;
			case NpcID.TOB_MAIDEN_50:
			case NpcID.TOB_MAIDEN_50_STORY:
			case NpcID.TOB_MAIDEN_50_HARD:
				if (startTick != -1)
				{
					maiden50time = client.getTickCount() - startTick;
				}
				break;
			case NpcID.TOB_MAIDEN_30:
			case NpcID.TOB_MAIDEN_30_STORY:
			case NpcID.TOB_MAIDEN_30_HARD:
				if (startTick != -1)
				{
					maiden30time = client.getTickCount() - startTick;
				}
				break;
		}
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		if (!MAIDEN_WAVE.matcher(Text.removeTags(event.getMessage())).find())
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

			splits = plugin.buildSplitString(
				"70% - " + plugin.formatTime(maiden70time),
				"50% - " + plugin.formatTime(maiden50time) + " (" + plugin.formatTime(maiden50time - maiden70time) + ")",
				"30% - " + plugin.formatTime(maiden30time) + " (" + plugin.formatTime(maiden30time - maiden50time) + ")",
				MSG_ROOM_COMPLETE + " - " + roomTime + " (" + plugin.formatTime(roomTicks - maiden30time) + ")"
			);

			plugin.buildSplitMessage(messages, "70%", maiden70time, 0);
			plugin.buildSplitMessage(messages, "50%", maiden50time, maiden70time);
			plugin.buildSplitMessage(messages, "30%", maiden30time, maiden50time);
			plugin.buildSplitMessage(messages, MSG_ROOM_COMPLETE, roomTicks, maiden30time);
		}

		plugin.buildDamageMessage(messages, MSG_PERSONAL_DAMAGE, personalDamage, totalDamage);
		plugin.buildHealedMessage(messages, MSG_TOTAL_HEALING, totalHealing);
		plugin.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(MAIDEN_IMAGE_ID, "Maiden", roomTime, percent, damage, splits, healing);
		plugin.addInfoBox(Boss.MAIDEN, box);
		plugin.infoBoxes.put(Boss.MAIDEN, box);
		reset();
	}

	@Override
	public void reset()
	{
		startTick = -1;
		maiden70time = 0;
		maiden50time = 0;
		maiden30time = 0;
		personalDamage = 0;
		totalDamage = 0;
		totalHealing = 0;
		bossNpc = null;
	}
}
