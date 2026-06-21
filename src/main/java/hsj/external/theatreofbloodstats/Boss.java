package hsj.external.theatreofbloodstats;

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

	Boss(String name)
	{
		this.name = name;
	}

	static Boss fromName(String name)
	{
		for (Boss boss : values())
		{
			if (boss.getName().equals(name))
			{
				return boss;
			}
		}
		return null;
	}
}
