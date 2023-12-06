package dev.melncat.est.util

import org.bukkit.entity.Player


fun setTotalExperience(player: Player, exp: Int) {
		require(exp >= 0) { "Experience is negative!" }
		player.setExp(0f)
		player.setLevel(0)
		player.setTotalExperience(0)

		//This following code is technically redundant now, as bukkit now calulcates levels more or less correctly
		//At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
		var amount = exp
		while (amount > 0) {
			val expToLevel: Int = getExpAtLevel(player)
			amount -= expToLevel
			if (amount >= 0) {
				// give until next level
				player.giveExp(expToLevel)
			} else {
				// give the rest
				amount += expToLevel
				player.giveExp(amount)
				amount = 0
			}
		}
	}

	private fun getExpAtLevel(player: Player): Int {
		return getExpAtLevel(player.getLevel())
	}

	//new Exp Math from 1.8
	fun getExpAtLevel(level: Int): Int {
		if (level <= 15) {
			return 2 * level + 7
		}
		return if (level >= 16 && level <= 30) {
			5 * level - 38
		} else 9 * level - 158
	}

	fun getExpToLevel(level: Int): Int {
		var currentLevel = 0
		var exp = 0
		while (currentLevel < level) {
			exp += getExpAtLevel(currentLevel)
			currentLevel++
		}
		if (exp < 0) {
			exp = Int.MAX_VALUE
		}
		return exp
	}

	//This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
	//Without this people would be able to use exp and then still sell it.
	fun getTotalExperience(player: Player): Int {
		var exp = Math.round(getExpAtLevel(player) * player.getExp()).toInt()
		var currentLevel: Int = player.getLevel()
		while (currentLevel > 0) {
			currentLevel--
			exp += getExpAtLevel(currentLevel)
		}
		if (exp < 0) {
			exp = Int.MAX_VALUE
		}
		return exp
	}

	fun getExpUntilNextLevel(player: Player): Int {
		val exp = Math.round(getExpAtLevel(player) * player.getExp()).toInt()
		val nextLevel: Int = player.getLevel()
		return getExpAtLevel(nextLevel) - exp
	}
