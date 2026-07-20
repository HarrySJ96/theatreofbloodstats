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

import static hsj.external.theatreofbloodstats.TobConstants.LINE_BREAK;
import static hsj.external.theatreofbloodstats.TobConstants.formatPercent;
import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import org.apache.commons.lang3.StringUtils;

public class TheatreOfBloodStatsInfoBox extends InfoBox
{

	private final String room;
	private final String time;
	private final String toolTipPercent;
	private final String infoBoxPercent;
	private final String damage;
	private final String splits;
	private final String healed;
	private final TheatreOfBloodStatsConfig config;
	private String tooltipText;

	protected TheatreOfBloodStatsInfoBox(
		BufferedImage image,
		TheatreOfBloodStatsConfig config,
		TheatreOfBloodStatsPlugin plugin,
		String room,
		RoomResult result
	)
	{
		super(image, plugin);
		this.config = config;
		this.room = room;
		this.time = StringUtils.substringBefore(result.getRoomTime(), ".");
		this.toolTipPercent = formatPercent(result.getPercent()) + "%";
		this.infoBoxPercent = Math.round(result.getPercent()) + "%";
		this.damage = result.getDamage();
		this.splits = result.getSplits();
		this.healed = result.getHealing();
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
		sb.append(LINE_BREAK);

		if (config.infoBoxTooltipSplits() && !StringUtils.isEmpty(splits))
		{
			sb.append(splits);
			if (config.infoBoxTooltipDmg() || config.infoBoxTooltipHealed())
			{
				sb.append(LINE_BREAK);
			}
		}

		if (config.infoBoxTooltipDmg() && !StringUtils.isEmpty(damage))
		{
			sb.append(damage).append(" (").append(toolTipPercent).append(")");
			if (config.infoBoxTooltipHealed())
			{
				sb.append(LINE_BREAK);
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
	public boolean render()
	{
		return config.showInfoBoxes();
	}

	@Override
	public String getTooltip()
	{
		return tooltipText;
	}
}