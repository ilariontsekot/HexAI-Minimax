# HexorcistaID: AI Player for Hex with Minimax and Alpha-Beta Pruning

**HexorcistaID** is an artificial intelligence engine for the board game **Hex**, developed in Java and ready to compile in NetBeans.
It implements classic AI strategies such as **Minimax**, **Alpha-Beta Pruning**, **Iterative Deepening Search**, and **Zobrist Hashing** to offer a competitive agent.

---

## 2. Alpha-Beta Pruning
To optimize the computation, **Alpha-Beta Pruning** is applied, reducing branches that cannot improve the expected result.
This significantly reduces the number of nodes evaluated, allowing for greater search depth in the same amount of time.

Simplified pseudocode example:

```python
def minimax(node, depth, alpha, beta, maximizing): 
if depth == 0 or node.is_terminal(): 
return evaluate(node) 

if maximizing: 
value = -float("inf") 
for child in node.children(): 
value = max(value, minimax(child, depth-1, alpha, beta, False)) 
alpha = max(alpha, value) 
if alpha >= beta: 
break # pruning 
return value 
else: 
value = float("inf") 
for child in node.children(): 
value = min(value, minimax(child, depth-1, alpha, beta, True)) 
beta = min(beta, value) 
if beta <= alpha: 
break # pruning 
return value
```

<p align="center"> <img src="operation.png" alt="Engine operation" /> </p>
---

## 3. Iterative Deepening Search (IDS)
The engine employs **Iterative Deepening Search (IDS)**: it first explores small depths and progressively increases until the available time is exhausted.
This ensures:

- A fast, valid move in critical situations.
- Greater accuracy if computation time allows.

---

## 4. Transposition Tables with Zobrist Hashing
To avoid recalculating repeated positions, each board state is represented with a **unique hash** using **Zobrist Hashing**.
This allows intermediate evaluations to be stored in a **transposition table**, reducing redundancy and speeding up searching on large boards.

---

## 5. Heuristics: Dijkstra and Strategic Bridges
Board evaluation combines two main ideas:

- **Dijkstra**: estimates the minimum distance between edges that the player must connect.
- **Bridges**: reinforce one's own connectivity and block opponent paths.

Approximate heuristic function:

$$
h(s) = d_{opponent}(s) - d_{player}(s)
$$

- A **negative** value indicates an advantage for the player.
- A **positive** value indicates an advantage for the opponent.

---

## 6. Decision Strategy
The agent selects moves prioritizing:

1. Reduce its own distance to victory.

2. Reinforce connectivity through **bridges**.

3. Block critical opponent paths.

4. Adjust the computation depth according to the **available time**.

---

## 7. Complexity and Efficiency
- **MiniMax without pruning**:

$$
O(b^d), \quad \text{with } b = \text{branching factor}, \; d = \text{depth}
$$

- **With Alpha-Beta**:

$$
O(b^{d/2})
$$

- **With transposition tables**: redundant calculations are further reduced, improving overall efficiency.
