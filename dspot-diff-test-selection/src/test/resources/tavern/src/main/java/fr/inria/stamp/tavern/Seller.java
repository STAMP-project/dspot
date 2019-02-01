package fr.inria.stamp.tavern;

import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class Seller {

	private int gold;

	private List<Item> items;

	public Seller(int gold, List<Item> itemsToSell) {
		this.gold = gold;
		this.items = itemsToSell;
	}

	public Item sellItem(String s, Player p) {
		Item i = null;
		for (int i2 = 0; i2 < this.items.size(); i2++) {
			final Item i3 = this.items.get(i2);
			if (i3.name.equals(s)) {
				i = i3;
			}
		}
		if (i != null) {
			final Integer g_p = p.getGold();
			final int value = g_p.compareTo(i.price);
			if (value >= 0) {
				this.gold = this.gold + i.price;
				p.giveGold(i.price);
				return i;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Seller{" +
				"gold=" + gold +
				", items=" + items +
				'}';
	}
}
