package hsj.external.theatreofbloodstats;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public enum Boss
{
	MAIDEN("The Maiden of Sugadinti"),
	BLOAT("Pestilent Bloat"),
	NYLOCAS("Nylocas Vasilias"),
	SOTETSEG("Sotetseg"),
	XARPUS("Xarpus"),
	VERZIK("Verzik Vitur");

	private final String name;
	private static final Map<String, Boss> NAME_MAP = new HashMap<>();

	static
	{
		for (Boss boss : values())
		{
			NAME_MAP.put(boss.getName(), boss);
		}
	}

	Boss(String name)
	{
		this.name = name;
	}

	static Boss fromName(String name)
	{
		return NAME_MAP.get(name);
	}
}
