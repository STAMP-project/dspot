package fr.inria.stamp.tavern;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 05/09/17
 */
public class Player {

	private int gold;
	private List<Item> items;
	private String name;

	public Player(String name, int gold) {
		this.name = name;
		this.gold = gold;
		this.items = new ArrayList<>();
	}

	public String getName() {
		return this.name;
	}

	public int getGold() {
		return gold;
	}

	public void giveGold(int amount) {
		this.gold = this.getGold() - amount;
	}

	public void buyItem(String name, Seller seller) {
		final Item item = seller.sellItem(name, this);
		if (item != null) {
			this.items.add(item);
		}
	}

	@Override
	public String toString() {
		return "Player{" +
				"gold=" + gold +
				", items=" + items +
				'}';
	}
}
