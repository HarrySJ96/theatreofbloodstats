package hsj.external.theatreofbloodstats;

import net.runelite.api.Actor;
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

public abstract class RoomTracker
{
	protected final Client client;
	protected final TheatreOfBloodStatsPlugin plugin;

	protected int personalDamage = 0;
	protected int totalDamage = 0;
	protected int totalHealing = 0;
	protected int startTick = -1;

	public abstract int[] getRegionIds();

	public RoomTracker(Client client, TheatreOfBloodStatsPlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;
	}

	public void onVarbitChanged(VarbitChanged event)
	{
	}

	public void onNpcSpawned(NpcSpawned event)
	{
	}

	public void onNpcChanged(NpcChanged event)
	{
	}

	public void onNpcDespawned(NpcDespawned event)
	{
	}

	public void onGameTick(GameTick event)
	{
	}

	public void onHitsplatApplied(HitsplatApplied event)
	{
		Actor actor = event.getActor();
		if (!(actor instanceof NPC) || actor.getName() == null)
		{
			return;
		}

		Boss boss = Boss.fromName(actor.getName());
		if (boss == null)
		{
			return;
		}

		Hitsplat hitsplat = event.getHitsplat();
		int amount = hitsplat.getAmount();

		if (hitsplat.isMine())
		{
			personalDamage += amount;
			totalDamage += amount;
		}
		else if (hitsplat.isOthers())
		{
			totalDamage += amount;
		}
		else if (hitsplat.getHitsplatType() == HitsplatID.HEAL)
		{
			totalHealing += amount;
		}
	}

	public void onOverheadTextChanged(OverheadTextChanged event)
	{
	}

	public void onAnimationChanged(AnimationChanged event)
	{
	}

	public void onChatMessage(ChatMessage event)
	{
	}

	public abstract void reset();
}
