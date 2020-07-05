package ru.abch.acceptgoods5;

class GoodsRow {
    String id, description, article, brand, cellId, cell;
    int qnt;
    GoodsRow(String id, int qnt, String description, String article, String brand, String cellId, String cell) {
        this.cell = cell;
        this.cellId = cellId;
        this.qnt = qnt;
        this.id = id;
        this.article = article;
        this.brand = brand;
        this.description = description;
    }
}
