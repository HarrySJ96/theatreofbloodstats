package hsj.external.theatreofbloodstats.rooms;

import hsj.external.theatreofbloodstats.Boss;
import hsj.external.theatreofbloodstats.RoomTracker;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsInfoBox;
import hsj.external.theatreofbloodstats.TheatreOfBloodStatsPlugin;
import static hsj.external.theatreofbloodstats.TobConstants.DECIMAL_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.DMG_FORMAT;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_PERSONAL_DAMAGE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_ROOM_COMPLETE;
import static hsj.external.theatreofbloodstats.TobConstants.MSG_TOTAL_HEALING;
import static hsj.external.theatreofbloodstats.TobConstants.XARPUS_IMAGE_ID;
import static hsj.external.theatreofbloodstats.TobConstants.XARPUS_REGION_IDS;
import static hsj.external.theatreofbloodstats.TobConstants.XARPUS_SCREECH;
import static hsj.external.theatreofbloodstats.TobConstants.XARPUS_WAVE;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.util.Text;

public class XarpusTracker extends RoomTracker
{
	private int xarpusAcidTime;
	private int xarpusRecoveryTime;
	private int xarpusPreScreech;
	private int xarpusPreScreechTotal;

	public XarpusTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		super(client, plugin);
	}

	@Override
	public int[] getRegionIds()
	{
		return XARPUS_REGION_IDS;
	}

	@Override
	public void onNpcChanged(NpcChanged event)
	{
		int npcId = event.getNpc().getId();
		switch (npcId)
		{
			case NpcID.TOB_XARPUS_FEEDING:
			case NpcID.TOB_XARPUS_FEEDING_STORY:
			case NpcID.TOB_XARPUS_FEEDING_HARD:
				startTick = client.getTickCount();
				bossNpc = event.getNpc();
				break;
			case NpcID.TOB_XARPUS_COMBAT:
			case NpcID.TOB_XARPUS_COMBAT_STORY:
			case NpcID.TOB_XARPUS_COMBAT_HARD:
				xarpusRecoveryTime = client.getTickCount() - startTick;
				break;
		}
	}

	@Override
	public void onOverheadTextChanged(OverheadTextChanged event)
	{
		String overheadText = event.getOverheadText();
		String npcName = event.getActor().getName();
		if (npcName != null && npcName.equals(Boss.XARPUS.getName()) && overheadText.equals(XARPUS_SCREECH))
		{
			xarpusAcidTime = client.getTickCount() - startTick;
			xarpusPreScreech = personalDamage;
			xarpusPreScreechTotal = totalDamage;
		}
	}

	@Override
	public void onChatMessage(ChatMessage event)
	{
		if (!XARPUS_WAVE.matcher(Text.removeTags(event.getMessage())).find())
		{
			return;
		}

		List<String> messages = new ArrayList<>();
		double xarpusPostScreech = personalDamage - xarpusPreScreech;
		double percent = Math.round(totalDamage > 0 ? ((double) personalDamage / totalDamage) * 100 : 0);
		double preScreechPercent = xarpusPreScreechTotal > 0 ? ((double) xarpusPreScreech / xarpusPreScreechTotal) * 100 : 0;

		double xarpusPostTotal = totalDamage - xarpusPreScreechTotal;
		double postScreechPercent = xarpusPostTotal > 0 ? (xarpusPostScreech / xarpusPostTotal) * 100 : 0;

		String roomTime = "";
		String splits = "";
		String damage = "";
		String healing = MSG_TOTAL_HEALING + " - " + DMG_FORMAT.format(totalHealing);

		if (startTick > 0)
		{
			int roomTicks = client.getTickCount() - startTick;
			roomTime = plugin.formatTime(roomTicks);

			splits = plugin.buildSplitString("Recovery Phase - " + plugin.formatTime(xarpusRecoveryTime),
				"Screech Time - " + plugin.formatTime(xarpusAcidTime) + " (" + plugin.formatTime(xarpusAcidTime - xarpusRecoveryTime) + ")",
				MSG_ROOM_COMPLETE + " - " + roomTime + " (" + plugin.formatTime(roomTicks - xarpusAcidTime) + ")");

			plugin.buildSplitMessage(messages, "Recovery Phase", xarpusRecoveryTime, 0);
			plugin.buildSplitMessage(messages, "Screech Time", xarpusAcidTime, xarpusRecoveryTime);
			plugin.buildSplitMessage(messages, MSG_ROOM_COMPLETE, roomTicks, xarpusAcidTime);
		}

		if (xarpusPreScreech > 0)
		{
			damage += "Pre Screech Damage - " + DMG_FORMAT.format(xarpusPreScreech) + " (" + DECIMAL_FORMAT.format(preScreechPercent) + "%)</br>";
			plugin.buildDamageMessage(messages, "Pre Screech Damage", xarpusPreScreech, xarpusPreScreechTotal);
		}
		if (xarpusPostScreech > 0)
		{
			damage += "Post Screech Damage - " + DMG_FORMAT.format(xarpusPostScreech) + " (" + DECIMAL_FORMAT.format(postScreechPercent) + "%)</br>";
			plugin.buildDamageMessage(messages, "Post Screech Damage", xarpusPostScreech, xarpusPostTotal);
		}
		if (personalDamage > 0)
		{
			damage += MSG_PERSONAL_DAMAGE + " - " + DMG_FORMAT.format(personalDamage);
			plugin.buildDamageMessage(messages, MSG_PERSONAL_DAMAGE, personalDamage, totalDamage);
		}

		plugin.buildHealedMessage(messages, "Total Healed", totalHealing);
		plugin.sendChatMessage(messages);

		TheatreOfBloodStatsInfoBox box = plugin.createInfoBox(XARPUS_IMAGE_ID, "Xarpus", roomTime, DECIMAL_FORMAT.format(percent) + "%", damage, splits, healing);
		plugin.infoBoxManager.addInfoBox(box);
		plugin.infoBoxes.put(Boss.XARPUS, box);
		reset();
	}

	@Override
	public void reset()
	{
		startTick = -1;
		xarpusRecoveryTime = 0;
		xarpusAcidTime = 0;
		xarpusPreScreech = 0;
		xarpusPreScreechTotal = 0;
		personalDamage = 0;
		totalDamage = 0;
		totalHealing = 0;
		bossNpc = null;
	}
}
