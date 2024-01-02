package dev.ericburden.aoc2023

/**
 * Represents a single plugged in component
 *
 * These are derived from each of the three-letter names from the input. The
 * idea is that comparing integers and copying them around is marginally faster
 * than comparing and copying strings. I'm not actually sure if this really
 * is faster, but it's a nice idea.
 *
 * @property id Unique integer identifier for this [Component].
 */
data class Component(val id: Int) {
    companion object {
        /**
         * Parse a [Component] from a name in the input file
         *
         * Each three-letter name is converted to an integer by bit-shifting
         * each character to the left and 'or'-ing all the bits together.
         *
         * @param str A component name from the input file.
         * @return The [Component] represented by the three-letter name.
         * @throws Exception When `str` isn't a three-letter string.
         */
        fun fromString(str: String): Component {
            require(str.length == 3) {
                throw Exception("$str is not a valid [Component] name!")
            }

            val (c1, c2, c3) = str.toList().map { it.code }
            val id = c1.shl(16).or(c2.shl(8)).or(c3)
            return Component(id)
        }
    }
}

/**
 * This class represents an edge in the undirected [ComponentGraph]
 *
 * This class is useful because we want to represent edges between components
 * in our graph of components, and since the graph is undirected we want to
 * consider (A -> B) to be equal to (B -> A).
 *
 * @property c1 One [Component] in the edge.
 * @property c2 Another [Component] in the edge.
 */
data class ComponentEdge(val c1: Component, val c2: Component) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ComponentEdge) return false

        // Check equality regardless of the order of components
        return (c1 == other.c1 && c2 == other.c2) || (c1 == other.c2 && c2 == other.c1)
    }

    override fun hashCode(): Int {
        // Ensure the same hash code is generated regardless of the order
        // of components
        return c1.hashCode() * c2.hashCode()
    }
}

/**
 * This class represents the graph of connections between [Component]s
 *
 * The graph here consists of undirected connections between components. We
 * know from the puzzle text that there are two groups of heavily interconnected
 * components separated by three nodes that can be "cut" to produce two
 * distinct networks.
 *
 * @property graph A mapping of one component to the list of components that are
 * reachable from that component.
 */
data class ComponentGraph(val graph: MutableMap<Component, MutableList<Component>>) {

    companion object {
        /**
         * Produce an empty [ComponentGraph]
         *
         * @return A mutable [ComponentGraph] with no nodes or edges.
         */
        fun empty(): ComponentGraph {
            val graph = mutableMapOf<Component, MutableList<Component>>()
            return ComponentGraph(graph)
        }

        /**
         * Parse the lines from the input file into a [ComponentGraph]
         *
         * Each line represents a set of undirected connections, such that
         * "jqt: rhn xhk nvd" can be interpreted as (jqt <--> rhn),
         * (jqt <--> xhk), (jqt <--> nvd).
         *
         * @param lines The lines from the input file.
         * @return A [ComponentGraph] parsed from the input file.
         * @throws Exception If any component name fails to parse to a [Component].
         */
        fun fromInput(lines: List<String>): ComponentGraph {
            val graph = empty()
            lines.filter { it.isNotEmpty() }.flatMap { line ->
                val (keyStr, valueStr) = line.split(": ")
                val key = Component.fromString(keyStr)
                val values = valueStr.split(" ").map(Component::fromString)
                values.map { ComponentEdge(key, it) }
            }.forEach { graph.addEdge(it) }

            return graph
        }
    }

    /**
     * Add an edge to the [ComponentGraph]
     *
     * Technically, adds both the forward and reverse edges to the underlying
     * map.
     *
     * @param edge The [ComponentEdge] to add to the graph.
     */
    fun addEdge(edge: ComponentEdge) {
        val (c1, c2) = edge

        val c1Values = graph.getOrDefault(c1, mutableListOf())
        if (c2 !in c1Values) c1Values.add(c2)
        graph[c1] = c1Values

        val c2Values = graph.getOrDefault(c2, mutableListOf())
        if (c1 !in c2Values) c2Values.add(c1)
        graph[c2] = c2Values
    }

    /**
     * Return (one of) the component(s) that lies furthest from `start`
     *
     * Performs a comprehensive breadth-first search over the component graph,
     * starting with `start`, returning the last node found. In the event that
     * more than one component lies equally distant from `start`, there is no
     * guarantee as to which of these furthest components will be returned.
     *
     * @param start The component to start searching from.
     * @return One of the components that lies furthest from `start`.
     */
    fun furthestComponentFrom(start: Component): Component {
        val queue = ArrayDeque(listOf(start))
        val seen = mutableSetOf<Component>()
        var currentComponent = start

        while (queue.isNotEmpty()) {
            currentComponent = queue.removeLast()
            if (currentComponent in seen) continue
            seen.add(currentComponent)

            val nextComponents = graph.getOrDefault(currentComponent, listOf())
            for (nextComponent in nextComponents) {
                if (nextComponent in seen) continue
                queue.addFirst(nextComponent)
            }
        }

        return currentComponent
    }

    /**
     * Return the set of edges in _a_ constrained shortest path from `start` to `finish`
     *
     * Performs a breadth-first search from `start` to `finish`, returning
     * the set of edges in the first path found. In the event that there is
     * more than one equally short path, there is no guarantee as to which is
     * returned. Any edge in `excluded` will not be traversed when finding the
     * shortest path.
     *
     * @param start The first component in the path.
     * @param finish The last component in the path.
     * @param excluded A set of edges that are considered impassable for this
     * traversal.
     */
    fun pathExcludingEdges(
        start: Component,
        finish: Component,
        excluded: Set<ComponentEdge>
    ): Set<ComponentEdge>? {
        val queue = ArrayDeque(listOf(start to setOf<ComponentEdge>()))
        val seen = mutableSetOf<Component>()

        while (queue.isNotEmpty()) {
            val (component, path) = queue.removeLast()
            if (component in seen) continue
            if (component == finish) return path
            seen.add(component)

            val nextComponents = graph.getOrDefault(component, listOf())
            for (nextComponent in nextComponents) {
                if (nextComponent in seen) continue
                val edge = ComponentEdge(component, nextComponent)
                if (edge in excluded) continue
                queue.addFirst(nextComponent to path + edge)
            }
        }

        return null
    }

    /**
     * Count the number of reachable components from `start`
     *
     * Performs yet another variant of a breadth-first search, searching
     * outwards from `start` to find all connected nodes. Any edge contained
     * in `excluded` is considered impassable and nodes on the other side of
     * that edge may not be discoverable (unless they can be reached another
     * way).
     *
     * @param start The component to start searching from.
     * @param excluded The set of edges that cannot be traversed in this search.
     * @return The count of reachable components from `start`.
     */
    fun countReachableComponents(
        start: Component,
        excluded: Set<ComponentEdge>
    ): Int {
        val queue = ArrayDeque(listOf(start))
        val seen = mutableSetOf<Component>()

        while (queue.isNotEmpty()) {
            val component = queue.removeLast()
            if (component in seen) continue
            seen.add(component)

            val nextComponents = graph.getOrDefault(component, listOf())
            for (nextComponent in nextComponents) {
                if (nextComponent in seen) continue
                val edge = ComponentEdge(component, nextComponent)
                if (edge in excluded) continue
                queue.addFirst(nextComponent)
            }
        }

        return seen.size
    }
}

class Day25(input: List<String>) {

    // Turn that input file into a real graph!
    private val parsed = ComponentGraph.fromInput(input)

    // Time to cut some components! For this final puzzle, we need to count
    // the number of components on either side of a three-component bridge
    // between two clusters of highly-connected components.
    fun solvePart1(): Int {
        // There are definitely some assumptions being made here, chief among
        // them that the two sub-graphs are relatively balanced on either side
        // of the cut-off (the hint being that we need to go from 100 stars
        // required to 50, i.e. remove half the components). We start by picking
        // a node at 'random', then finding the node furthest from it to serve
        // as a starting point. Then, we find the node furthest from `start`
        // and _assume_ it's on the other side of the three nodes we need to
        // disconnect.
        val aNode = parsed.graph.keys.take(1).single()
        val start = parsed.furthestComponentFrom(aNode)
        val finish = parsed.furthestComponentFrom(start)

        // Now, we find the shortest path from `start` to `finish` three times,
        // constraining the set of edges in these three paths to be unique (no
        // taking the same path twice). We still don't know _which_ three nodes
        // need to be disconnected, but if `start` and `finish` are on opposite
        // sides, we can be reasonably certain that they're in the list of
        // excluded edges we accumulate.
        val path1 = parsed.pathExcludingEdges(start, finish, setOf())
            ?: throw Exception("Could not find one path from $start to $finish!")
        val path2 = parsed.pathExcludingEdges(start, finish, path1)
            ?: throw Exception("Could not find two paths from $start to $finish!")
        val path3 = parsed.pathExcludingEdges(start, finish, path1 + path2)
            ?: throw Exception("Could not find three paths from $start to $finish!")
        val excludedEdges = path1 + path2 + path3

        // Now, with our list of excluded edges containing the only paths from
        // one sub-graph to another, we perform one more search over each side
        // of the cut-off, counting components in each. This bit assumes that
        // the components in each sub-graph are connected _enough_ that we'll
        // still be able to reach all of them despite our overly generous set
        // of excluded edges.
        val group1Size = parsed.countReachableComponents(start, excludedEdges)
        val group2Size = parsed.countReachableComponents(finish, excludedEdges)

        // Finally, we return the product of the two group sizes.
        return group1Size * group2Size
    }

}
