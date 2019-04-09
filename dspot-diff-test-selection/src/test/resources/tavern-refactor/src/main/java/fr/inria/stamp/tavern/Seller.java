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

	public Item sellItem(String name, Player player) {
		final Item itemToSell = this.items.stream()
				.filter(item -> item.name.equals(name))
				.findFirst()
				.orElse(null);
		if (itemToSell != null &&
				player.getGold() > itemToSell.price) {
			this.gold = this.gold + itemToSell.price;
			player.giveGold(itemToSell.price);
			return itemToSell;
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
