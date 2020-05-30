package ru.arjentix.gotl.list;

public class List {

  private class Node {
    int value;
    Node prev;
    Node next;

    public Node(int value, Node prev, Node next) {
      this.value = value;
      this.prev = prev;
      this.next = next;
    }
  }

  Node head;
  Node tail;
  int size;
  
  public List() {
    this.head = null;
    this.tail = null;
    this.size = 0;
  }

  public void add(int value) {
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

  public void add(int index, int value) {
    Node node = find(index);
    Node newNode = new Node(value, node, node.next);
    
    if (node.next != null) {
      node.next.prev = newNode;
    }
    if (node.prev != null) {
      node.next = newNode;
    }
  }

  public int get(int index) {
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

    --size;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
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