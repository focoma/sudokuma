package net.claves.games;

public abstract class Grid<T> {
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
        if (object instanceof Grid) {
            Grid that = (Grid)object;
            int thatSize = that.getSize();
            if (thatSize == this.size) {
                for (int rowIndex = 0; rowIndex < thatSize; rowIndex++) {
                    for (int columnIndex = 0; columnIndex < thatSize; columnIndex++) {
                        if (!this.get(rowIndex, columnIndex).equals(that.get(rowIndex, columnIndex))) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
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
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof Item)) {
                return false;
            }

            Item that = (Item) o;

            if (this.getValue() == null && that.getValue() == null) {
                return true;
            }

            return value != null && this.getValue().equals(that.getValue());
        }
    }
}
