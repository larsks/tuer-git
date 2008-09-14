package main;

interface Collector {
    boolean collects(Collectable collectable);
    int increaseHealth(int amount);
}
