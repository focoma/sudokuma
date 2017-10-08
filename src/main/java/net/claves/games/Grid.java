package net.claves.games;

import java.util.Iterator;

public abstract class Grid<T> implements Iterable<Grid.Item>{
    private Item[][] itemsByRow;
    private Item[][] itemsByColumn;
    private final int size;

    public Grid(int size) {
        this.size = size;
        itemsByRow = new Item[size][size];
        itemsByColumn = new Item[size][size];
    }

    protected void clear() {
        itemsByRow = new Item[size][size];
        itemsByColumn = new Item[size][size];
    }

    @Override
    public Iterator<Item> iterator() {
        return new GridIterator();
    }

    protected abstract Item<T> createEmptyItem(Position position);

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
            return put(position, createEmptyItem(position));
        }
        return item;
    }

    public Item<T> get(int x, int y) {
        return get(new Position(x, y));
    }

    protected Item remove(Position position) {
        Item oldItem = get(position);
        put(position, createEmptyItem(position));
        return oldItem;
    }

    public int getSize() {
        return size;
    }

    public Item[] getRow(int row) {
        return itemsByRow[row];
    }

    public Item[] getColumn(int column) {
        return itemsByColumn[column];
    }

    public Item[][] getItems() {
        return itemsByRow;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Grid) {
            Grid that = (Grid)object;
            int thatSize = that.getSize();
            if (thatSize == this.size) {
                for (Item item : this) {
                    Position position = item.getPosition();
                    if (!item.equals(that.get(position))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (Item item : this) {
            hashCode += item.hashCode();
        }
        return hashCode;
    }

    public static class Item<T> {
        private T value;
        private Position position;

        public Item(T value, Position position) {
            this.value = value;
            this.position = position;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public int getRowIndex() {
            return position.x;
        }

        public int getColumnIndex() {
            return position.y;
        }

        public Position getPosition() {
            return position;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Item)) return false;

            Item<?> item = (Item<?>) o;

            if (value != null ? !value.equals(item.value) : item.value != null) return false;
            return position.equals(item.position);
        }

        @Override
        public int hashCode() {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + position.hashCode();
            return result;
        }
    }

    private class GridIterator implements Iterator<Item> {
        private int x;
        private int y;

        @Override
        public boolean hasNext() {
            return x <= size - 1 && y <= size - 1;
        }

        @Override
        public Item next() {
            if (hasNext()) {
                Item<T> next = Grid.this.get(x, y);
                if (y < size - 1) {
                    y++;
                } else if (x < size) {
                    x++;
                    y = 0;
                }
                return next;
            }

            return null;
        }
    }
}
