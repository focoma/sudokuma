package net.claves.games;

public abstract class Grid<T> {
    protected Item[][] itemsByRow;
    protected Item[][] itemsByColumn;
    protected int size;

    public Grid(int size) {
        this.size = size;
        itemsByRow = new Item[size][size];
        itemsByColumn = new Item[size][size];
    }

    protected abstract Item<T> getEmptyItem();

    public Item<T> put(Position position, Item<T> gridItem) {
        if (position.x >= size || position.y >= size || position.x < 0 || position.y < 0) {
            throw new IllegalArgumentException("Invalid position.");
        }
        itemsByRow[position.x][position.y] = gridItem;
        itemsByColumn[position.y][position.x] = gridItem;

        return gridItem;
    }

    public Item<T> get(Position position) {
        Item<T> item = itemsByRow[position.x][position.y];
        if (item == null) {
            return put(position, getEmptyItem());
        }
        return item;
    }

    public Item remove(Position position) {
        Item oldItem = get(position);
        put(position, getEmptyItem());
        return oldItem;
    }

    public int getSize() {
        return size;
    }

    public Item[] getRow(int row) {
        return itemsByRow[row];
    }

    public Item[] getColumn(int columne) {
        return itemsByColumn[columne];
    }

    public static class Item<T> {
        private T value;

        public Item(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Item item = (Item) o;

            return value != null && value.equals(item.getValue());
        }
    }
}
