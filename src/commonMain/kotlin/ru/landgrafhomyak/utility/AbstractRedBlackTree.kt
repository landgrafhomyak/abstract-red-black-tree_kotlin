package ru.landgrafhomyak.utility

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField

@Suppress("FunctionName", "MemberVisibilityCanBePrivate")
abstract class AbstractRedBlackTree<NODE : Any> {
    protected abstract fun _getParent(node: NODE): NODE?
    protected abstract fun _setParent(node: NODE, parent: NODE?)
    protected abstract fun _getLeftChild(node: NODE): NODE?
    protected abstract fun _setLeftChild(node: NODE, child: NODE?)
    protected abstract fun _getRightChild(node: NODE): NODE?
    protected abstract fun _setRightChild(node: NODE, child: NODE?)
    protected abstract fun _getColor(node: NODE): Color
    protected abstract fun _setColor(node: NODE, color: Color)

    protected open fun _checkSame(leftNode: NODE, rightNode: NODE?) = leftNode === rightNode
    enum class Color(@JvmField val asBoolean: Boolean) {
        RED(true), BLACK(false)
    }

    var root: NODE? = null

    @OptIn(ExperimentalContracts::class)
    private inline fun ___rotateLeft(top: NODE, setTop: (NODE) -> Unit) {
        contract {
            callsInPlace(setTop, InvocationKind.EXACTLY_ONCE)
        }
        val node = this._getRightChild(top) ?: return

        val topColor = this._getColor(top)
        this._setColor(top, this._getColor(node))
        this._setColor(node, topColor)

        val nodeLeftChild = this._getLeftChild(node)
        this._setRightChild(top, nodeLeftChild)
        if (nodeLeftChild != null)
            this._setParent(nodeLeftChild, top)
        this._setParent(node, this._getParent(top))
        setTop(node)
        this._setLeftChild(node, top)
        this._setParent(top, node)
    }

    private fun __rotateLeft_Root(top: NODE) =
        this.___rotateLeft(top) { newRoot -> this.root = newRoot }

    private fun __rotateLeft_Left(top: NODE, parent: NODE) =
        this.___rotateLeft(top) { newChild -> this._setLeftChild(parent, newChild) }

    private fun __rotateLeft_Right(top: NODE, parent: NODE) =
        this.___rotateLeft(top) { newChild -> this._setRightChild(parent, newChild) }

    private fun __rotateLeft_Switch(top: NODE, parent: NODE?) = when {
        parent == null -> this.__rotateLeft_Root(top.__assertIsRoot())
        top === this._getLeftChild(parent) -> this.__rotateLeft_Left(top, parent)
        top === this._getRightChild(parent) -> this.__rotateLeft_Right(top, parent)
        else -> this.__throwTreeCorruptedNotChild(top, parent)
    }

    private fun __rotateLeft_Switch(top: NODE) =
        this.__rotateLeft_Switch(top, this._getParent(top))

    @OptIn(ExperimentalContracts::class)
    private inline fun ___rotateRight(top: NODE, setTop: (NODE) -> Unit) {
        contract {
            callsInPlace(setTop, InvocationKind.EXACTLY_ONCE)
        }
        val node = this._getLeftChild(top) ?: return

        val topColor = this._getColor(top)
        this._setColor(top, this._getColor(node))
        this._setColor(node, topColor)

        val nodeRightChild = this._getRightChild(node)
        this._setLeftChild(top, nodeRightChild)
        if (nodeRightChild != null)
            this._setParent(nodeRightChild, top)
        this._setParent(node, this._getParent(top))
        setTop(node)
        this._setRightChild(node, top)
        this._setParent(top, node)
    }

    private fun __rotateRight_Root(top: NODE) =
        this.___rotateRight(top) { newRoot -> this.root = newRoot }

    private fun __rotateRight_Left(top: NODE, parent: NODE) =
        this.___rotateRight(top) { newChild -> this._setLeftChild(parent, newChild) }

    private fun __rotateRight_Right(top: NODE, parent: NODE) =
        this.___rotateRight(top) { newChild -> this._setRightChild(parent, newChild) }


    private fun __rotateRight_Switch(top: NODE, parent: NODE?) = when {
        parent == null -> this.__rotateRight_Root(top.__assertIsRoot())
        top === this._getLeftChild(parent) -> this.__rotateRight_Left(top, parent)
        top === this._getRightChild(parent) -> this.__rotateRight_Right(top, parent)
        else -> this.__throwTreeCorruptedNotChild(top, parent)
    }

    private fun __rotateRight_Switch(top: NODE) =
        this.__rotateRight_Switch(top, this._getParent(top))


    sealed class TreeCorruptedException(
        msg: String,
        val tree: AbstractRedBlackTree<*>,
        val node: Any
    ) : RuntimeException(msg) {
        internal class TreeCorruptedNotChildException(
            tree: AbstractRedBlackTree<*>,
            node: Any,
            val parent: Any
        ) : TreeCorruptedException(
            "$MESSAGE_PREFIX node isn't child of it parent",
            tree, node
        )

        internal class TreeCorruptedNotRootException(
            tree: AbstractRedBlackTree<*>,
            node: Any,
        ) : TreeCorruptedException(
            "$MESSAGE_PREFIX node isn't child of it parent",
            tree, node
        )

        internal class TreeCorruptedNotBalancedException(
            tree: AbstractRedBlackTree<*>,
            node: Any,
            reason: String
        ) : TreeCorruptedException(
            "$MESSAGE_PREFIX $reason",
            tree, node
        )

        companion object {
            private const val MESSAGE_PREFIX = "Red-black tree corrupted:"
        }
    }


    private fun __throwTreeCorruptedNotChild(node: NODE, parent: NODE): Nothing =
        throw TreeCorruptedException.TreeCorruptedNotChildException(this, node, parent)


    private fun __throwTreeCorruptedNotRoot(node: NODE): Nothing =
        throw TreeCorruptedException.TreeCorruptedNotRootException(this, node)

    private fun __throwTreeCorruptedNotBalanced(node: NODE, reason: String): Nothing =
        throw TreeCorruptedException.TreeCorruptedNotBalancedException(this, node, reason)


    private fun NODE.__assertIsRoot(): NODE =
        if (this@AbstractRedBlackTree.root === this@__assertIsRoot) this@__assertIsRoot
        else this@AbstractRedBlackTree.__throwTreeCorruptedNotRoot(this@__assertIsRoot)


    fun balanceAfterLinking(node: NODE) {
        this._setColor(node, Color.RED)
        var current: NODE = node

        while (true) {
            val parent = this._getParent(current)
            if (parent == null) {
                current.__assertIsRoot()
                this._setColor(current, Color.BLACK)
                break
            }
            if (this._getColor(parent) == Color.BLACK) {
                break
            }

            val grandParent = this._getParent(parent)
            if (grandParent == null) {
                parent.__assertIsRoot()
                this._setColor(parent, Color.BLACK)
                break
            }

            when {
                parent === this._getLeftChild(grandParent) -> {
                    val uncle = this._getRightChild(grandParent)
                    if (uncle != null && this._getColor(uncle) == Color.RED) {
                        this._setColor(parent, Color.BLACK)
                        this._setColor(uncle, Color.BLACK)
                        this._setColor(grandParent, Color.RED)
                        current = grandParent
                        continue
                    }

                    when {
                        current === this._getLeftChild(parent) -> {
                            this.__rotateRight_Switch(grandParent)
                            continue
                        }

                        current === this._getRightChild(parent) -> {
                            this.__rotateLeft_Left(parent, grandParent)
                            current = parent
                            continue
                        }

                        else -> this.__throwTreeCorruptedNotChild(current, parent)
                    }
                }

                parent === this._getRightChild(grandParent) -> {
                    val uncle = this._getLeftChild(grandParent)
                    if (uncle != null && this._getColor(uncle) == Color.RED) {
                        this._setColor(parent, Color.BLACK)
                        this._setColor(uncle, Color.BLACK)
                        this._setColor(grandParent, Color.RED)
                        current = grandParent
                        continue
                    }

                    when {
                        current === this._getRightChild(parent) -> {
                            this.__rotateLeft_Switch(grandParent)
                            continue
                        }

                        current === this._getLeftChild(parent) -> {
                            this.__rotateRight_Right(parent, grandParent)
                            current = parent
                            continue
                        }

                        else -> this.__throwTreeCorruptedNotChild(current, parent)
                    }
                }

                else -> this.__throwTreeCorruptedNotChild(parent, grandParent)
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun ___swapNeighbours(
        parent: NODE, child: NODE,
        grandparent: NODE?,
        setGrandparent2Parent: (NODE) -> Unit,
        getForwardChild: (NODE) -> NODE?,
        setForwardChild: (parent: NODE, child: NODE?) -> Unit,
        getOppositeChild: (NODE) -> NODE?,
        setOppositeChild: (parent: NODE, child: NODE?) -> Unit
    ) {
        contract {
            callsInPlace(setGrandparent2Parent, InvocationKind.EXACTLY_ONCE)
            callsInPlace(getForwardChild, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(setForwardChild, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(getOppositeChild, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(setOppositeChild, InvocationKind.AT_LEAST_ONCE)
        }
        this._setParent(child, grandparent)
        setGrandparent2Parent(child)
        setForwardChild(parent, getForwardChild(child))
        this._setParent(parent, getOppositeChild(parent))
        setOppositeChild(parent, getOppositeChild(child))
        setOppositeChild(child, this._getParent(parent))
        getOppositeChild(child)?.also { oC -> this._setParent(oC, child) }
        getOppositeChild(parent)?.also { oC -> this._setParent(oC, parent) }
        getForwardChild(child)?.also { fC -> this._setParent(fC, parent) }
        setForwardChild(child, parent)
        this._setParent(parent, child)
        val parentColor = this._getColor(parent)
        this._setColor(parent, this._getColor(child))
        this._setColor(child, parentColor)
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun __swapNeighboursLeft(
        parent: NODE, child: NODE,
        grandparent: NODE?,
        setGrandparent2Parent: (NODE) -> Unit
    ) {
        contract {
            callsInPlace(setGrandparent2Parent, InvocationKind.EXACTLY_ONCE)
        }
        this.___swapNeighbours(
            parent, child,
            grandparent,
            setGrandparent2Parent,
            getForwardChild = { p -> this._getLeftChild(p) },
            setForwardChild = { p, c -> this._setLeftChild(p, c) },
            getOppositeChild = { p -> this._getRightChild(p) },
            setOppositeChild = { p, c -> this._setRightChild(p, c) }
        )
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun __swapNeighboursRight(
        parent: NODE, child: NODE,
        grandparent: NODE?,
        setGrandparent2Parent: (NODE) -> Unit
    ) {
        contract {
            callsInPlace(setGrandparent2Parent, InvocationKind.EXACTLY_ONCE)
        }
        this.___swapNeighbours(
            parent, child,
            grandparent,
            setGrandparent2Parent,
            getForwardChild = { p -> this._getRightChild(p) },
            setForwardChild = { p, c -> this._setRightChild(p, c) },
            getOppositeChild = { p -> this._getLeftChild(p) },
            setOppositeChild = { p, c -> this._setLeftChild(p, c) }
        )
    }

    private fun __swapRandom(node1: NODE, parent1: NODE?, left1: NODE?, right1: NODE?, node2: NODE, parent2: NODE?, left2: NODE?, right2: NODE?) {
        val color1 = this._getColor(node1)
        val color2 = this._getColor(node2)

        when {
            parent1 == null -> {
                node1.__assertIsRoot()
                this.root = node2
            }

            node1 === this._getLeftChild(parent1) -> this._setLeftChild(parent1, node2)
            node1 === this._getRightChild(parent1) -> this._setRightChild(parent1, node2)
            else -> this.__throwTreeCorruptedNotChild(node1, parent1)
        }
        when {
            parent2 == null -> {
                node2.__assertIsRoot()
                this.root = node1
            }

            node2 === this._getLeftChild(parent2) -> this._setLeftChild(parent2, node1)
            node2 === this._getRightChild(parent2) -> this._setRightChild(parent2, node1)
            else -> this.__throwTreeCorruptedNotChild(node2, parent2)
        }
        this._setParent(node2, parent1)
        this._setParent(node1, parent2)

        this._setLeftChild(node1, left2)
        this._setLeftChild(node2, left1)
        if (left2 != null) this._setParent(left2, node1)
        if (left1 != null) this._setParent(left1, node2)

        this._setRightChild(node1, right2)
        this._setRightChild(node2, right1)
        if (right2 != null) this._setParent(right2, node1)
        if (right1 != null) this._setParent(right1, node2)

        this._setColor(node1, color2)
        this._setColor(node2, color1)
    }

    fun swapNodes(node1: NODE, node2: NODE) {
        val left1 = this._getLeftChild(node1)
        val right1 = this._getRightChild(node1)
        val parent1 = this._getParent(node1)

        val left2 = this._getLeftChild(node2)
        val right2 = this._getRightChild(node2)
        val parent2 = this._getParent(node2)

        when {
            node1 === left2 ->
                when {
                    parent2 == null -> {
                        node2.__assertIsRoot()
                        this.__swapNeighboursLeft(node2, node1, null) { c -> this.root = c }
                    }

                    node2 === this._getLeftChild(parent2) -> this.__swapNeighboursLeft(node2, node1, parent2) { c -> this._setLeftChild(parent2, c) }
                    node2 === this._getRightChild(parent2) -> this.__swapNeighboursLeft(node2, node1, parent2) { c -> this._setRightChild(parent2, c) }
                    else -> this.__throwTreeCorruptedNotChild(node2, parent2)
                }

            node1 === right2 ->
                when {
                    parent2 == null -> {
                        node2.__assertIsRoot()
                        this.__swapNeighboursRight(node2, node1, null) { c -> this.root = c }
                    }

                    node2 === this._getLeftChild(parent2) -> this.__swapNeighboursRight(node2, node1, parent2) { c -> this._setLeftChild(parent2, c) }
                    node2 === this._getRightChild(parent2) -> this.__swapNeighboursRight(node2, node1, parent2) { c -> this._setRightChild(parent2, c) }
                    else -> this.__throwTreeCorruptedNotChild(node2, parent2)
                }

            node2 === left1 ->
                when {
                    parent1 == null -> {
                        node1.__assertIsRoot()
                        this.__swapNeighboursLeft(node1, node2, null) { c -> this.root = c }
                    }

                    node1 === this._getLeftChild(parent1) -> this.__swapNeighboursLeft(node1, node2, parent1) { c -> this._setLeftChild(parent1, c) }
                    node1 === this._getRightChild(parent1) -> this.__swapNeighboursLeft(node1, node2, parent1) { c -> this._setRightChild(parent1, c) }
                    else -> this.__throwTreeCorruptedNotChild(node1, parent1)
                }

            node2 === right1 ->
                when {
                    parent1 == null -> {
                        node1.__assertIsRoot()
                        this.__swapNeighboursRight(node1, node2, null) { c -> this.root = c }
                    }

                    node1 === this._getLeftChild(parent1) -> this.__swapNeighboursRight(node1, node2, parent1) { c -> this._setLeftChild(parent1, c) }
                    node1 === this._getRightChild(parent1) -> this.__swapNeighboursRight(node1, node2, parent1) { c -> this._setRightChild(parent1, c) }
                    else -> this.__throwTreeCorruptedNotChild(node1, parent1)
                }

            else -> this.__swapRandom(node1, parent1, left1, right1, node2, parent2, left2, right2)
        }


    }

    fun unlink(node: NODE) {
        while (true) {
            val nodeLeftChild = this._getLeftChild(node)
            val nodeRightChild = this._getRightChild(node)

            val parent = this._getParent(node)
            if (nodeLeftChild == null) {
                if (nodeRightChild == null) {
                    if (parent == null) {
                        node.__assertIsRoot()
                        this.root = null
                        return
                    }

                    if (this._getColor(node) == Color.RED) {
                        when {
                            node === this._getLeftChild(parent) -> this._setLeftChild(parent, null)
                            node === this._getRightChild(parent) -> this._setRightChild(parent, null)
                            else -> this.__throwTreeCorruptedNotChild(node, parent)
                        }
                        return
                    }
                    this.__balanceAfterUnlinking(node, parent)
                    break
                } else {
                    if (parent == null) {
                        node.__assertIsRoot()
                        this.root = nodeRightChild
                        this._setParent(nodeRightChild, null)
                        this._setColor(nodeRightChild, Color.BLACK)
                        return
                    }

                    val repl = this.subtreeMin(nodeRightChild)
                    this.swapNodes(node, repl)
                    continue
                }
            } else {
                if (nodeRightChild == null) {
                    if (parent == null) {
                        node.__assertIsRoot()
                        this.root = nodeLeftChild
                        this._setParent(nodeLeftChild, null)
                        this._setColor(nodeLeftChild, Color.BLACK)
                        return
                    }
                }
                val repl = this.subtreeMax(nodeLeftChild)
                this.swapNodes(node, repl)
                continue
            }
        }

        val newParent = this._getParent(node)
        when {
            newParent == null -> {
                node.__assertIsRoot()
                this.root = null
            }

            node === this._getLeftChild(newParent) -> {
                var child = this._getLeftChild(node)
                if (child == null)
                    child = this._getRightChild(node)
                else
                    this._getRightChild(node)?.also { TODO() }
                this._setLeftChild(newParent, child)
                if (child != null)
                    this._setParent(child, newParent)
            }

            node === this._getRightChild(newParent) -> {
                var child = this._getLeftChild(node)
                if (child == null)
                    child = this._getRightChild(node)
                else
                    this._getRightChild(node)?.also { TODO() }
                this._setRightChild(newParent, child)
                if (child != null)
                    this._setParent(child, newParent)
            }

            else -> this.__throwTreeCorruptedNotChild(node, newParent)
        }
    }

    /**
     * @param node expected to be [BLACK][AbstractRedBlackTree.Color.BLACK].
     */
    private fun __balanceAfterUnlinking(nodeForDeleting: NODE, parent: NODE) {
        var node = nodeForDeleting
        @Suppress("NAME_SHADOWING") var parent = parent
        var continueBalancing: Boolean
        do {
            when {
                node === this._getLeftChild(parent) -> continueBalancing = this.___balanceAfterUnlinking_Directed(
                    node = node,
                    parent = parent,
                    getForwardChild = this::_getLeftChild,
                    getOppositeChild = this::_getRightChild,
                    rotateForward_Switch = this::__rotateLeft_Switch,
                    rotateOpposite_Opposite = this::__rotateRight_Right,
                    updateState = { newNode, newParent -> node = newNode; parent = newParent }
                )

                node === this._getRightChild(parent) -> continueBalancing = this.___balanceAfterUnlinking_Directed(
                    node = node,
                    parent = parent,
                    getForwardChild = this::_getRightChild,
                    getOppositeChild = this::_getLeftChild,
                    rotateForward_Switch = this::__rotateRight_Switch,
                    rotateOpposite_Opposite = this::__rotateLeft_Left,
                    updateState = { newNode, newParent -> node = newNode; parent = newParent }
                )

                else -> this.__throwTreeCorruptedNotChild(node, parent)
            }
        } while (continueBalancing)
    }

    /**
     * Symmetric part of [`__balanceAfterUnlinking`][AbstractRedBlackTree.__balanceAfterUnlinking] function.
     *
     * @return `true` if balancing loop should be continued, otherwise `false`.
     *
     * @param node node to be deleted or value from [updateState] call.
     * Expected to be [black][AbstractRedBlackTree.Color.BLACK] (kept by [updateState] call).
     *
     * @param parent parent node of [node] param.
     * @param getForwardChild operation to get child from a same direction as [parent]->[node].
     * @param getOppositeChild operation to get child from an opposite direction to [parent]->[node].
     * @param rotateForward_Switch same naming as [getForwardChild] and [getOppositeChild].
     * @param rotateOpposite_Opposite same naming as [getForwardChild] and [getOppositeChild].
     * @param updateState callback to update [node] and [parent] in caller function.
     */
    @OptIn(ExperimentalContracts::class)
    @Suppress("LiftReturnOrAssignment")
    private inline fun ___balanceAfterUnlinking_Directed(
        node: NODE,
        parent: NODE,
        getForwardChild: (NODE) -> NODE?,
        getOppositeChild: (NODE) -> NODE?,
        @Suppress("LocalVariableName") rotateForward_Switch: (NODE) -> Unit,
        @Suppress("LocalVariableName") rotateOpposite_Opposite: (NODE, NODE) -> Unit,
        updateState: (node: NODE, parent: NODE) -> Unit
    ): Boolean {
        contract {
            callsInPlace(getForwardChild, InvocationKind.UNKNOWN)
            callsInPlace(getOppositeChild, InvocationKind.UNKNOWN)
            callsInPlace(rotateForward_Switch, InvocationKind.UNKNOWN)
            callsInPlace(rotateOpposite_Opposite, InvocationKind.UNKNOWN)
            callsInPlace(updateState, InvocationKind.AT_MOST_ONCE)
        }
        var sibling = getOppositeChild(parent) ?: this.__throwTreeCorruptedNotBalanced(node, "black node doesn't have sibling")
        when (this._getColor(sibling)) {
            Color.RED -> {
                // case 2
                rotateForward_Switch(parent)
                this._setColor(sibling, Color.BLACK)
                this._setColor(parent, Color.RED)
                sibling = getOppositeChild(parent) ?: this.__throwTreeCorruptedNotBalanced(node, "red sibling of black node doesn't have child (in current state sibling of node not exists)")
                val siblingForwardChild = getForwardChild(sibling)
                val siblingOppositeChild = getOppositeChild(sibling)
                if (siblingOppositeChild == null || this._getColor(siblingOppositeChild) == Color.BLACK) {
                    if (siblingForwardChild == null || this._getColor(siblingForwardChild) == Color.BLACK) {
                        // case 4
                        this._setColor(parent, Color.BLACK)
                        this._setColor(sibling, Color.RED)
                    }
                    return false
                } else {
                    // case 6
                    rotateForward_Switch(parent)
                    this._setColor(siblingOppositeChild, Color.BLACK)
                    return false
                }
            }

            Color.BLACK -> {
                val siblingForwardChild = getForwardChild(sibling)
                val siblingOppositeChild = getOppositeChild(sibling)
                if (siblingOppositeChild == null || this._getColor(siblingOppositeChild) == Color.BLACK) {
                    if (siblingForwardChild == null || this._getColor(siblingForwardChild) == Color.BLACK) {
                        if (this._getColor(parent) == Color.BLACK) {
                            // case 3
                            this._setColor(sibling, Color.RED)
                            val grandparent = this._getParent(parent)
                            if (grandparent == null) {
                                parent.__assertIsRoot()
                                return false
                            } else {
                                updateState(parent, grandparent)
                                return true
                            }
                        } else {
                            // case 4
                            this._setColor(parent, Color.BLACK)
                            this._setColor(sibling, Color.RED)
                            return false
                        }
                    } else {
                        // case 5
                        rotateOpposite_Opposite(sibling, parent)
                        rotateForward_Switch(parent)
                        this._setColor(sibling, Color.BLACK)
                        return false
                    }
                } else {
                    // case 6
                    rotateForward_Switch(parent)
                    this._setColor(siblingOppositeChild, Color.BLACK)
                    return false
                }
            }
        }
    }

    fun subtreeMin(node: NODE): NODE {
        var current = node
        while (true)
            current = this._getLeftChild(current) ?: return current
    }

    fun subtreeMax(node: NODE): NODE {
        var current = node
        while (true)
            current = this._getRightChild(current) ?: return current
    }

    fun minOrNull(): NODE? {
        return this.subtreeMin(this.root ?: return null)
    }

    fun maxOrNull(): NODE? {
        return this.subtreeMin(this.root ?: return null)
    }

    fun minOrThrow(): NODE = this.minOrNull() ?: throw NoSuchElementException("Red-black tree is empty")

    fun maxOrThrow(): NODE = this.maxOrNull() ?: throw NoSuchElementException("Red-black tree is empty")
}
