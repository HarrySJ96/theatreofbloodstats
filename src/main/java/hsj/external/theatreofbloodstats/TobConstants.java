package hsj.external.theatreofbloodstats;

import com.google.common.collect.ImmutableSet;
import java.util.regex.Pattern;
import net.runelite.api.Point;
import net.runelite.api.gameval.NpcID;
import java.text.DecimalFormat;
import java.util.Set;

public final class TobConstants
{
	public static final DecimalFormat DMG_FORMAT = new DecimalFormat("#,##0");
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0.0");
	public static final String MSG_PERSONAL_DAMAGE = "Personal Boss Damage";
	public static final String MSG_TOTAL_HEALING = "Total Healing";
	public static final String MSG_ROOM_COMPLETE = "Room Complete";
	public static final String XARPUS_SCREECH = "Screeeeech!";
	public static final Pattern MAIDEN_WAVE = Pattern.compile("Wave 'The Maiden of Sugadinti' \\(.*\\) complete!");
	public static final Pattern BLOAT_WAVE = Pattern.compile("Wave 'The Pestilent Bloat' \\(.*\\) complete!Duration: (\\d+):(\\d+)\\.?(\\d+)");
	public static final Pattern NYLOCAS_WAVE = Pattern.compile("Wave 'The Nylocas' \\(.*\\) complete!");
	public static final Pattern SOTETSEG_WAVE = Pattern.compile("Wave 'Sotetseg' \\(.*\\) complete!");
	public static final Pattern XARPUS_WAVE = Pattern.compile("Wave 'Xarpus' \\(.*\\) complete!");
	public static final Pattern VERZIK_WAVE = Pattern.compile("Wave 'The Final Challenge' \\(.*\\) complete!");
	public static final int[] TOB_LOBBY_REGION_IDS = {14642};
	public static final int[] MAIDEN_REGION_IDS = {12613, 12869};
	public static final int[] BLOAT_REGION_IDS = {13125};
	public static final int[] NYLOCAS_REGION_IDS = {13122};
	public static final int[] SOTE_REGION_IDS = {13123};
	public static final int[] SOTETSEG_MAZE_REGION_IDS = {13379};
	public static final int[] XARPUS_REGION_IDS = {12612};
	public static final int[] VERZIK_REGION_IDS = {12611};
	public static final int[] THRONE_ROOM_REGION_IDS = {12867};
	public static final Set<int[]> TOB_REGION_IDS = ImmutableSet.of(TOB_LOBBY_REGION_IDS, MAIDEN_REGION_IDS, BLOAT_REGION_IDS, NYLOCAS_REGION_IDS, SOTE_REGION_IDS, SOTETSEG_MAZE_REGION_IDS, XARPUS_REGION_IDS, VERZIK_REGION_IDS, THRONE_ROOM_REGION_IDS);
	public static final int NYLOCAS_WAVES_TOTAL = 31;
	public static final int TICK_LENGTH = 600;
	public static final int MAIDEN_IMAGE_ID = 25748;
	public static final int BLOAT_IMAGE_ID = 25749;
	public static final int NYLOCAS_IMAGE_ID = 25750;
	public static final int SOTETSEG_IMAGE_ID = 25751;
	public static final int XARPUS_IMAGE_ID = 25752;
	public static final int VERZIK_IMAGE_ID = 22473;
	public static final Set<Integer> NYLOCAS_IDS = ImmutableSet.of(
		NpcID.TOB_NYLOCAS_INCOMING_MAGIC, NpcID.TOB_NYLOCAS_BIG_INCOMING_MAGIC, NpcID.TOB_NYLOCAS_FIGHTING_MAGIC, NpcID.TOB_NYLOCAS_BIG_FIGHTING_MAGIC,
		NpcID.TOB_NYLOCAS_INCOMING_MAGIC_STORY, NpcID.TOB_NYLOCAS_BIG_INCOMING_MAGIC_STORY, NpcID.TOB_NYLOCAS_FIGHTING_MAGIC_STORY, NpcID.TOB_NYLOCAS_BIG_FIGHTING_MAGIC_STORY,
		NpcID.TOB_NYLOCAS_INCOMING_MAGIC_HARD, NpcID.TOB_NYLOCAS_BIG_INCOMING_MAGIC_HARD, NpcID.TOB_NYLOCAS_FIGHTING_MAGIC_HARD, NpcID.TOB_NYLOCAS_BIG_FIGHTING_MAGIC_HARD,
		NpcID.TOB_NYLOCAS_INCOMING_RANGED, NpcID.TOB_NYLOCAS_BIG_INCOMING_RANGED, NpcID.TOB_NYLOCAS_FIGHTING_RANGED, NpcID.TOB_NYLOCAS_BIG_FIGHTING_RANGED,
		NpcID.TOB_NYLOCAS_INCOMING_RANGED_STORY, NpcID.TOB_NYLOCAS_BIG_INCOMING_RANGED_STORY, NpcID.TOB_NYLOCAS_FIGHTING_RANGED_STORY, NpcID.TOB_NYLOCAS_BIG_FIGHTING_RANGED_STORY,
		NpcID.TOB_NYLOCAS_INCOMING_RANGED_HARD, NpcID.TOB_NYLOCAS_BIG_INCOMING_RANGED_HARD, NpcID.TOB_NYLOCAS_FIGHTING_RANGED_HARD, NpcID.TOB_NYLOCAS_BIG_FIGHTING_RANGED_HARD,
		NpcID.TOB_NYLOCAS_INCOMING_MELEE, NpcID.TOB_NYLOCAS_BIG_INCOMING_MELEE, NpcID.TOB_NYLOCAS_FIGHTING_MELEE, NpcID.TOB_NYLOCAS_BIG_FIGHTING_MELEE,
		NpcID.TOB_NYLOCAS_INCOMING_MELEE_STORY, NpcID.TOB_NYLOCAS_BIG_INCOMING_MELEE_STORY, NpcID.TOB_NYLOCAS_FIGHTING_MELEE_STORY, NpcID.TOB_NYLOCAS_BIG_FIGHTING_MELEE_STORY,
		NpcID.TOB_NYLOCAS_INCOMING_MELEE_HARD, NpcID.TOB_NYLOCAS_BIG_INCOMING_MELEE_HARD, NpcID.TOB_NYLOCAS_FIGHTING_MELEE_HARD, NpcID.TOB_NYLOCAS_BIG_FIGHTING_MELEE_HARD
	);
	public static final Set<Point> NYLOCAS_VALID_SPAWNS = ImmutableSet.of(
		new Point(17, 24), new Point(17, 25), new Point(18, 24), new Point(18, 25),
		new Point(31, 9), new Point(31, 10), new Point(32, 9), new Point(32, 10),
		new Point(46, 24), new Point(46, 25), new Point(47, 24), new Point(47, 25)
	);

	private TobConstants()
	{
	}
}
