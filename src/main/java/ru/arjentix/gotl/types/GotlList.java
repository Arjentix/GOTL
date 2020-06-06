package ru.arjentix.gotl.types;

public class GotlList {

  private class Node {
    Object value;
    Node prev;
    Node next;

    public Node(Object value, Node prev, Node next) {
      this.value = value;
      this.prev = prev;
      this.next = next;
    }
  }

  private Node head;
  private Node tail;
  private int size;
  
  public GotlList() {
    this.head = null;
    this.tail = null;
    this.size = 0;
  }

  public void add(Object value) {
    Node node = new Node(value, null, tail);

    if (tail != null) {
      tail.prev = node;
    }
    tail = node;

    if (head == null) {
      head = tail;
    }
    
    ++size;
  }

  public void insert(int index, Object value) {
    Node node = find(index);
    Node newNode = new Node(value, node, node.next);
    
    if (node.next != null) {
      node.next.prev = newNode;
    }
    if (node.prev != null) {
      node.next = newNode;
    }
  }

  public Object get(int index) {
    return find(index).value;
  }

  public void remove(int index) {
    Node node = find(index);

    if (node.next != null) {
     node.next.prev = node.prev;
    }
    if (node.prev != null) {
      node.prev.next = node.next;
    }

    if (node.equals(head)) {
      head = head.prev;
    }
    if (node.equals(tail)) {
      tail = tail.next;
    }

    --size;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public void clear() {
    head = tail = null;
    size = 0;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder("{");

    boolean first = true;
    for (int i = 0; i < size; ++i) {
      if (!first) {
        builder.append(", ");
      }

      builder.append(get(i));
      first = false;
    }
    builder.append("}");

    return builder.toString();
  }

  private Node find(int index) {
    if (index < 0 || index > size - 1) {
      throw new IndexOutOfBoundsException();
    }

    Node node = null;

    if (index < size / 2) {
      node = head;
      for (int i = 0; i < index; ++i) {
        node = node.prev;
      }
    }
    else {
      node = tail;
      for (int i = 0; i < size - index - 1; ++i) {
        node = node.next;
      }
    }

    return node;
  }
}