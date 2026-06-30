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
