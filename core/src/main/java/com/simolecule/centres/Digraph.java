package com.simolecule.centres;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class Digraph<A, B> {

  private static final int MAX_NODE_COUNT = 20_000;

  private final BaseMol<A, B> mol;
  private       Node<A, B>    root;
  private       Node<A, B>    currroot;
  private int numNodes = 0;

  public Digraph(BaseMol<A, B> mol)
  {
    this.mol = mol;
  }

  public Digraph(BaseMol<A, B> mol, A atom)
  {
    this.mol = mol;
    init(atom);
  }

  public Node<A, B> init(A atom)
  {
    this.root = new Node<A, B>(this,
                               new short[mol.getNumAtoms()],
                               atom,
                               1,
                               0);
    int atomIdx = mol.getAtomIdx(atom);
    this.root.visit[atomIdx] = 1;
    numNodes++;
    return this.root;
  }

  public BaseMol<A, B> getMol()
  {
    return mol;
  }

  public Node<A, B> getRoot()
  {
    return root;
  }

  public Node<A, B> getCurrRoot()
  {
    return currroot == null ? root : currroot;
  }

  public List<Node<A, B>> getNodes(A atom)
  {
    List<Node<A, B>>  result = new ArrayList<>();
    Deque<Node<A, B>> queue  = new ArrayDeque<>();
    queue.add(getCurrRoot());
    while (!queue.isEmpty()) {
      Node<A, B> node = queue.poll();
      if (atom.equals(node.getAtom())) {
        result.add(node);
      }
      for (Edge<A, B> e : node.getEdges()) {
        if (!e.isBeg(node))
          continue;
        queue.add(e.getEnd());
      }
    }
    return result;
  }

  public void build()
  {
    Deque<Node<A, B>> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      Node<A, B> node = queue.poll();
      for (Edge<A, B> e : node.getEdges()) {
        if (!e.isBeg(node)) continue;
        queue.add(e.getEnd());
      }
    }
  }

  /**
   * Sets the root node of this digraph by flipping all the
   * 'up' edges to be 'down'.
   *
   * @param newroot the new root
   * @return the old root of the digraph
   */
  public void changeRoot(Node<A, B> newroot)
  {
    Deque<Node<A, B>> queue = new ArrayDeque<>();
    queue.add(newroot);
    List<Edge<A, B>> toflip = new ArrayList<>();
    while (!queue.isEmpty()) {
      Node<A, B> node = queue.poll();
      for (Edge<A, B> e : node.getEdges()) {
        if (e.isEnd(node)) {
          toflip.add(e);
          queue.add(e.getBeg());
        }
      }
    }
    for (Edge<A, B> e : toflip)
      e.flip();
    currroot = newroot;
  }

  void expand(Node<A, B> beg)
  {
    final A                atom  = beg.getAtom();
    final List<Edge<A, B>> edges = beg.getEdges();
    final B                prev  = edges.size() > 0 && !edges.get(0).isBeg(beg) ? edges.get(0).getBond() : null;

    // create 'explicit' nodes
    for (final B bond : mol.getBonds(atom)) {
      final A   nbr    = mol.getOther(bond, atom);
      final int nbrIdx = mol.getAtomIdx(nbr);
      final int bord   = mol.getBondOrder(bond);

      if (beg.visit[nbrIdx] == 0) {

        Node<A, B> end = beg.newChild(nbrIdx, nbr);
        if (++numNodes >= MAX_NODE_COUNT)
          throw new TooManyNodesException();
        addEdge(beg, bond, end);

        // duplicate nodes for bond orders (except for root atoms...)
        // for example >S=O
        if (!root.equals(beg)) {
          for (int i = 1; i < bord; i++) {
            end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATED);
            if (++numNodes >= MAX_NODE_COUNT)
              throw new TooManyNodesException();
            addEdge(beg, bond, end);
          }
        }
      }
      // bond order expansion (backwards)
      else if (bond.equals(prev)) {
        if (!root.getAtom().equals(nbr)) {
          for (int i = 1; i < bord; i++) {
            Node<A, B> end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATED);
            if (++numNodes >= MAX_NODE_COUNT)
              throw new TooManyNodesException();
            addEdge(beg, bond, end);
          }
        }
      }
      // ring closures
      else {
        Node<A, B> end = beg.newTerminalChild(nbrIdx, nbr, Node.RING_DUPLICATE);
        if (++numNodes >= MAX_NODE_COUNT)
          throw new TooManyNodesException();
        addEdge(beg, bond, end);

        for (int i = 1; i < bord; i++) {
          end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATED);
          if (++numNodes >= MAX_NODE_COUNT)
            throw new TooManyNodesException();
          addEdge(beg, bond, end);
        }
      }
    }

    // Create implicit hydrogen nodes
    final int hcnt = mol.getNumHydrogens(atom);
    for (int i = 0; i < hcnt; i++) {
      Node<A, B> end = beg.newTerminalChild(-1, null, Node.IMPL_HYDROGEN);
      if (++numNodes >= MAX_NODE_COUNT)
        throw new TooManyNodesException();
      addEdge(beg, null, end);
    }
  }

  private void addEdge(Node<A, B> beg, B bond, Node<A, B> end)
  {
    Edge<A, B> e = new Edge<>(beg, end, bond);
    beg.add(e);
    end.add(e);
  }
}
