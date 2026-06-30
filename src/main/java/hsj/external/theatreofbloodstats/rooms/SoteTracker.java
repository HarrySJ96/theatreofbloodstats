package hsj.external.theatreofbloodstats.rooms;

import hsj.external.theatreofbloodstats.Boss;
import hsj.external.theatreofbloodstats.RoomTracker;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsInfoBox;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsPlugin;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_PERSONAL_DAMAGE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_ROOM_COMPLETE;
import static hsj.external.theatreofbloodstats.TobConstants.SOTETSEG_IMAGE_ID;
import static hsj.external.theatreofbloodstats.TobConstants.SOTETSEG_WAVE;
import static hsj.external.theatreofbloodstats.TobConstants.SOTE_REGION_IDS;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.Text;

public class SoteTracker extends RoomTracker
{
	private boolean sote66;
	private int sote66time;
	private boolean sote33;
	private int sote33time;

	public SoteTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		super(client, plugin);
	}

	@Override
	public int[] getRegionIds()
	{
		return SOTE_REGION_IDS;
	}

	@Override
	public void onNpcSpawned(NpcSpawned event)
	{
		switch (event.getNpc().getId())
		{
			case NpcID.TOB_SOTETSEG_COMBAT:
			case NpcID.TOB_SOTETSEG_COMBAT_STORY:
			case NpcID.TOB_SOTETSEG_COMBAT_HARD:
				bossNpc = event.getNpc();
				break;
		}
	}

	@Override
	public void onNpcChanged(NpcChanged event)
	{
		int npcId = event.getNpc().getId();
		switch (npcId)
		{
			case NpcID.TOB_SOTETSEG_COMBAT:
			case NpcID.TOB_SOTETSEG_COMBAT_STORY:
			case NpcID.TOB_SOTETSEG_COMBAT_HARD:
				bossNpc = event.getNpc();
				if (startTick == -1)
				{
					startTick = client.getTickCount();
				}
				break;
			case NpcID.TOB_SOTETSEG_NONCOMBAT:
			case NpcID.TOB_SOTETSEG_NONCOMBAT_STORY:
			case NpcID.TOB_SOTETSEG_NONCOMBAT_HARD:
				if (startTick != -1)
				{
					if (!sote66)
					{
						sote66time = client.getTickCount() - startTick;
						sote66 = true;
					}
					else if (!sote33)
					{
						sote33time = client.getTickCount() - startTick;
						sote33 = true;
					}
				}
				break;
		}
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		if (!SOTETSEG_WAVE.matcher(Text.removeTags(event.getMessage())).find())
		{
			return;
		}

		List<String> messages = new ArrayList<>();
		double percent = plugin.percentOf(personalDamage, totalDamage);
		String roomTime = "";
		String splits = "";
		String damage = (personalDamage > 0) ? MSG_PERSONAL_DAMAGE + " - " + DMG_FORMAT.format(personalDamage) : "";

		if (startTick > 0)
		{
			int roomTicks = client.getTickCount() - startTick;
			roomTime = plugin.formatTime(roomTicks);

			splits = plugin.buildSplitString("66% - " + plugin.formatTime(sote66time),
				"33% - " + plugin.formatTime(sote33time) + " (" + plugin.formatTime(sote33time - sote66time) + ")",
				MSG_ROOM_COMPLETE + " - " + roomTime + " (" + plugin.formatTime(roomTicks - sote33time) + ")");

			plugin.buildSplitMessage(messages, "66%", sote66time, 0);
			plugin.buildSplitMessage(messages, "33%", sote33time, sote66time);
			plugin.buildSplitMessage(messages, MSG_ROOM_COMPLETE, roomTicks, sote33time);
		}

		plugin.buildDamageMessage(messages, MSG_PERSONAL_DAMAGE, personalDamage, totalDamage);
		plugin.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(SOTETSEG_IMAGE_ID, "Sotetseg", roomTime, percent, damage, splits, "");
		plugin.addInfoBox(Boss.SOTETSEG,box);
		reset();
	}

	@Override
	public void reset()
	{
		startTick = -1;
		sote66 = false;
		sote66time = 0;
		sote33 = false;
		sote33time = 0;
		personalDamage = 0;
		totalDamage = 0;
		bossNpc = null;
	}
}
