/*
 * Copyright (c) 2020 John Mayfield
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.simolecule.centres;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class Digraph<A, B> {

  /**
   * Upper limit on the size of the digraph, stops out of memory error with a
   * more graceful failure. 0=Infinite
   */
  private static final int MAX_NODE_COUNT = 100000;

  /**
   * Used for debugging only, 0=Infinite
   */
  private static final int MAX_NODE_DIST = 0;

  private final BaseMol<A, B> mol;
  private       Node<A, B>    root;
  private       Node<A, B>    tmproot;
  private int numNodes = 0;
  private A rule6Ref;

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
                               new char[mol.getNumAtoms()],
                               atom,
                               mol.getAtomicNum(atom),
                               1,
                               (char) 1,
                               0);
    int atomIdx = mol.getAtomIdx(atom);
    this.root.visit[atomIdx] = 1;
    numNodes++;
    return this.root;
  }

  public int getNumNodes()
  {
    return numNodes;
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
    return tmproot == null ? root : tmproot;
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

  public void expandAll()
  {
    Deque<Node<A, B>> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      Node<A, B> node = queue.poll();
      for (Edge<A, B> e : node.getEdges()) {
        if (!e.isBeg(node)) continue;
        if (!e.getEnd().isTerminal())
          queue.add(e.getEnd());
      }
    }
  }

  /**
   * Sets the root node of this digraph by flipping all the
   * 'up' edges to be 'down'.
   *
   * @param newroot the new root
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
    tmproot = newroot;
  }

  void expand(Node<A, B> beg)
  {
    final A                atom  = beg.getAtom();
    final List<Edge<A, B>> edges = beg.getEdges();
    final B                prev  = edges.size() > 0 && !edges.get(0).isBeg(beg) ? edges.get(0).getBond() : null;

    if (MAX_NODE_DIST > 0 && beg.getDistance() > MAX_NODE_DIST)
      return;
    if (MAX_NODE_COUNT > 0 && numNodes >= MAX_NODE_COUNT)
      throw new TooManyNodesException(MAX_NODE_COUNT);

    // create 'explicit' nodes
    for (final B bond : mol.getBonds(atom)) {
      final A   nbr    = mol.getOther(bond, atom);
      final int nbrIdx = mol.getAtomIdx(nbr);
      final int bord   = mol.getBondOrder(bond);

      if (beg.visit[nbrIdx] == 0) {

        Node<A, B> end = beg.newChild(nbrIdx, nbr);
        numNodes++;
        addEdge(beg, bond, end);

        // duplicate nodes for bond orders (except for root atoms...)
        // for example >S=O
        if (!root.equals(beg)) {
          if (mol.getCharge(atom) < 0 && mol.getFractionalAtomicNum(atom).getDen() > 1) {
            end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATE);
            numNodes++;
            addEdge(beg, bond, end);
          } else {
            for (int i = 1; i < bord; i++) {
              end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATE);
              numNodes++;
              addEdge(beg, bond, end);
            }
          }
        }
      }
      // bond order expansion (backwards)
      else if (bond.equals(prev)) {
        if (!root.getAtom().equals(nbr)) {
          for (int i = 1; i < bord; i++) {
            Node<A, B> end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATE);
            numNodes++;
            addEdge(beg, bond, end);
          }
        }
      }
      // ring closures
      else {
        Node<A, B> end = beg.newTerminalChild(nbrIdx, nbr, Node.RING_DUPLICATE);
        numNodes++;
        addEdge(beg, bond, end);

        if (mol.getCharge(atom) < 0 && mol.getFractionalAtomicNum(atom).getDen() > 1) {
          end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATE);
          numNodes++;
          addEdge(beg, bond, end);
        } else {
          for (int i = 1; i < bord; i++) {
            end = beg.newTerminalChild(nbrIdx, nbr, Node.BOND_DUPLICATE);
            numNodes++;
            addEdge(beg, bond, end);
          }
        }
      }
    }

    // Create implicit hydrogen nodes
    final int hcnt = mol.getNumHydrogens(atom);
    for (int i = 0; i < hcnt; i++) {
      Node<A, B> end = beg.newTerminalChild(-1, null, Node.IMPL_HYDROGEN);
      numNodes++;
      addEdge(beg, null, end);
    }
  }

  private void addEdge(Node<A, B> beg, B bond, Node<A, B> end)
  {
    Edge<A, B> e = new Edge<>(beg, end, bond);
    beg.add(e);
    end.add(e);
  }

  /**
   * Used exclusively for Rule 6, we set one atom as the reference.
   * @param ref reference atom
   */
  public void setRule6Ref(A ref) {
    this.rule6Ref = ref;
  }

  /**
   * Access the reference atom for Rule 6 (if one is set).
   */
  public A getRule6Ref() {
    return rule6Ref;
  }
}
