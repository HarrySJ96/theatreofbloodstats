package hsj.external.theatreofbloodstats;

import static hsj.external.theatreofbloodstats.TobConstants.DECIMAL_FORMAT;
import java.awt.Color;
import java.awt.image.BufferedImage;
import lombok.Getter;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import org.apache.commons.lang3.StringUtils;

@Getter
public class TheatreOfBloodStatsInfoBox extends InfoBox
{

	private final String room;
	private final String time;
	private final double percent;
	private final String toolTipPercent;
	private final String infoBoxPercent;
	private final String damage;
	private final String splits;
	private final String healed;
	private String tooltipText;
	private final TheatreOfBloodStatsConfig config;


	TheatreOfBloodStatsInfoBox(
		BufferedImage image,
		TheatreOfBloodStatsConfig config,
		TheatreOfBloodStatsPlugin plugin,
		String room,
		String time,
		double percent,
		String damage,
		String splits,
		String healed
	)
	{
		super(image, plugin);
		this.config = config;
		this.room = room;
		this.time = StringUtils.substringBefore(time, ".");
		this.percent = percent;
		this.toolTipPercent = DECIMAL_FORMAT.format(percent) + "%";
		this.infoBoxPercent = Math.round(percent) + "%";
		this.damage = damage;
		this.splits = splits;
		this.healed = healed;
		this.tooltipText = buildTooltip();
		setPriority(InfoBoxPriority.LOW);
	}

	private String buildTooltip()
	{
		if (!config.infoBoxTooltip())
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(room);
		sb.append("</br>");

		if (config.infoBoxTooltipSplits() && !StringUtils.isEmpty(splits))
		{
			sb.append(splits);
			if (config.infoBoxTooltipDmg() || config.infoBoxTooltipHealed())
			{
				sb.append("</br>");
			}
		}

		if (config.infoBoxTooltipDmg() && !StringUtils.isEmpty(damage) && !damage.equals("0"))
		{
			sb.append(damage).append(" (").append(toolTipPercent).append(")");
			if (config.infoBoxTooltipHealed())
			{
				sb.append("</br>");
			}
		}

		if (config.infoBoxTooltipHealed() && !StringUtils.isEmpty(healed))
		{
			sb.append(healed);
		}

		return sb.toString();
	}

	public void rebuildTooltip()
	{
		tooltipText = buildTooltip();
	}

	@Override
	public String getText()
	{
		switch (config.infoBoxText())
		{
			case NONE:
				return "";
			case TIME:
				return time;
			case DAMAGE_PERCENT:
				return infoBoxPercent;
		}
		return "";
	}

	@Override
	public Color getTextColor()
	{
		return Color.GREEN;
	}

	@Override
	public String getTooltip()
	{
		return tooltipText;
	}

	@Override
	public boolean render()
	{
		return config.showInfoBoxes();
	}
}
