package ru.abch.acceptgoods5;

public class WarehousesResult {
	public boolean success;
	public int counter;
	public Warehouse2[] Warehouses;
	public WarehousesResult(boolean success, int counter) {
		this.success = success;
		this.counter = counter;
		this.Warehouses = null;
	}
}
