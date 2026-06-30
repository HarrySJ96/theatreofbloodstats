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

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("theatreofbloodstats")
public interface TheatreOfBloodStatsConfig extends Config
{
	@ConfigSection(
		name = "Chatbox Messages",
		description = "Settings for messages in the chatbox",
		position = 0
	)
	String chatSettings = "chatSettings";
	@ConfigSection(
		name = "Info Boxes",
		description = "Settings for the infoboxes",
		position = 1
	)
	String infoBoxSettings = "infoBoxSettings";

	@ConfigItem(
		keyName = "chatboxDmg",
		name = "Print Damage To Chat",
		description = "Print personal damage and percentage of total to chat",
		section = chatSettings,
		position = 0
	)
	default boolean chatboxDmg()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatboxHealed",
		name = "Print Heals To Chat",
		description = "Print amount healed to chat",
		section = chatSettings,
		position = 1
	)
	default boolean chatboxHealed()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatboxSplits",
		name = "Print Splits To Chat",
		description = "Print detailed room splits to chat",
		section = chatSettings,
		position = 2
	)
	default boolean chatboxSplits()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showInfoBoxes",
		name = "Info Boxes",
		description = "Show info boxes",
		section = infoBoxSettings,
		position = 1
	)
	default boolean showInfoBoxes()
	{
		return true;
	}

	@ConfigItem(
		keyName = "infoBoxText",
		name = "Info Box Text",
		description = "The text displayed on the info box",
		section = infoBoxSettings,
		position = 1
	)
	default InfoBoxText infoBoxText()
	{
		return InfoBoxText.TIME;
	}

	@ConfigItem(
		keyName = "infoBoxTooltip",
		name = "Info Box Tooltip",
		description = "Display info box tooltip",
		section = infoBoxSettings,
		position = 2
	)
	default boolean infoBoxTooltip()
	{
		return true;
	}

	@ConfigItem(
		keyName = "infoBoxTooltipDmg",
		name = "Info Box Tooltip Damage",
		description = "Display damage info in the info box tooltip",
		section = infoBoxSettings,
		position = 3
	)
	default boolean infoBoxTooltipDmg()
	{
		return true;
	}

	@ConfigItem(
		keyName = "infoBoxTooltipHealed",
		name = "Info Box Tooltip Healed",
		description = "Display amount healed in the info box tooltip",
		section = infoBoxSettings,
		position = 4
	)
	default boolean infoBoxTooltipHealed()
	{
		return true;
	}

	@ConfigItem(
		keyName = "infoBoxTooltipSplits",
		name = "Info Box Tooltip Splits",
		description = "Display splits in the info box tooltip",
		section = infoBoxSettings,
		position = 5
	)
	default boolean infoBoxTooltipSplits()
	{
		return true;
	}
}
