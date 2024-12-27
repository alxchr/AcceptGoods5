package ru.abch.acceptgoods5;

class GoodsResult {
    public boolean success;
    public int counter;
    public GoodsPosition[] Goods;
    public GoodsResult(boolean success, int counter) {
        this.success = success;
        this.counter = counter;
        this.Goods = null;
    }
    public int storeman;
}
