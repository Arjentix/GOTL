package ru.arjentix.gotl.types;

import java.util.NoSuchElementException;

public class GotlHashMap {

  private class Item {
    public Object key;
    public Object value;

    public Item(Object key, Object value) {
      this.key = key;
      this.value = value;
    }
  }

  private class Pair<T, U> {
    public T first; 
    public U second; 
  }

  private final int bucketsCount = 5;
  private GotlList buckets[];
  private int size;

  public GotlHashMap() {
    this.buckets = new GotlList[bucketsCount];
    this.size = 0;
  }

  public void put(Object key, Object value) {
    Pair<GotlList, Integer> pair = find(key);
    if (pair == null) {
      int index = getIndex(key);
      if (buckets[index] == null) {
        buckets[index] = new GotlList();
      }

      buckets[index].add(new Item(key, value));
      ++size;
    }
    else {
      ((Item) pair.first.get(pair.second)).value = value;
    }
  }

  public Object get(Object key) throws NoSuchElementException {
    Pair<GotlList, Integer> pair = find(key);
    if (pair == null) {
      throw new NoSuchElementException("There is no key " + key);
    }

    return ((Item) pair.first.get(pair.second)).value;

  }

  public void remove(Object key) {
    Pair<GotlList, Integer> pair = find(key);
    if (pair != null) {
      pair.first.remove(pair.second);
      --size;
    }
  }

  public int size() {
    return size;
  }

  public void clear() {
    for (int i = 0; i < bucketsCount; ++i) {
      buckets[i].clear();
    }
    size = 0;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder("{");
    boolean first = true;

    for (int i = 0; i < bucketsCount; ++i) {
      GotlList bucket = buckets[i];
      for (int j = 0; j < bucket.size(); ++j) {
        if (!first) {
          builder.append(", ");
        }
        Item item = (Item) bucket.get(j);
        builder.append(item.key.toString() + " : " + item.value.toString());
        first = false;
      }
    }
    builder.append("}");

    return builder.toString();
  }

  private int getIndex(Object key) {
    return Math.abs(key.hashCode()) % bucketsCount;
  }

  private Pair<GotlList, Integer> find(Object key) {
    Pair<GotlList, Integer> res = null;
    GotlList bucket = buckets[getIndex(key)];

    if (bucket != null) {
      for (int i = 0; i < bucket.size(); ++i) {
        Item item = (Item) bucket.get(i);
        if (item.key.equals(key)) {
          res = new Pair<GotlList, Integer>();
          res.first = bucket;
          res.second = (Integer) i;
          break;
        }
      }
    }

    return res;
  }
}
