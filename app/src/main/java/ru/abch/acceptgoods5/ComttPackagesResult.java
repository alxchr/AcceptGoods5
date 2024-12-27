package ru.abch.acceptgoods5;

public class ComttPackagesResult {
	public boolean success;
	public int counter;
	public ComttPackage[] packages;
	public ComttPackagesResult(boolean success, int counter) {
		this.success = success;
		this.counter = counter;
		this.packages = null;
	}
}
