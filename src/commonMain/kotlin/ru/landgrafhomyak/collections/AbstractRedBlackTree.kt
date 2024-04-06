package ru.landgrafhomyak.collections

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

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
        RED(true), BLACK(false);

        companion object {
            @JvmStatic
            fun fromBoolean(value: Boolean) = if (value) Color.RED else Color.BLACK
        }
    }

    private fun __setChildSwitch(parent: NODE?, oldChild: NODE, newChild: NODE?) {
        when {
            parent == null -> {
                oldChild.__assertIsRoot()
                this.root = newChild
            }

            this._checkSame(oldChild, this._getLeftChild(parent)) -> this._setLeftChild(parent, newChild)
            this._checkSame(oldChild, this._getRightChild(parent)) -> this._setRightChild(parent, newChild)
            else -> this.__throwTreeCorruptedNotChild(oldChild, parent)
        }
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

    /*
        private fun __rotateLeft_Root(top: NODE) =
            this.___rotateLeft(top) { newRoot -> this.root = newRoot }

        private fun __rotateLeft_Right(top: NODE, parent: NODE) =
            this.___rotateLeft(top) { newChild -> this._setRightChild(parent, newChild) }
    */

    private fun __rotateLeft_Left(top: NODE, parent: NODE) =
        this.___rotateLeft(top) { newChild -> this._setLeftChild(parent, newChild) }

    private fun __rotateLeft_Switch(top: NODE, parent: NODE?) =
        this.___rotateLeft(top) { newChild -> this.__setChildSwitch(parent, top, newChild) }

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

    /*
        private fun __rotateRight_Root(top: NODE) =
            this.___rotateRight(top) { newRoot -> this.root = newRoot }

        private fun __rotateRight_Left(top: NODE, parent: NODE) =
            this.___rotateRight(top) { newChild -> this._setLeftChild(parent, newChild) }
    */

    private fun __rotateRight_Right(top: NODE, parent: NODE) =
        this.___rotateRight(top) { newChild -> this._setRightChild(parent, newChild) }

    private fun __rotateRight_Switch(top: NODE, parent: NODE?) =
        this.___rotateRight(top) { newChild -> this.__setChildSwitch(parent, top, newChild) }

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


    private fun NODE.__assertIsRoot(): NODE {
        val root = this@AbstractRedBlackTree.root
        if (root != null && this@AbstractRedBlackTree._checkSame(root, this@__assertIsRoot)) return this@__assertIsRoot
        else this@AbstractRedBlackTree.__throwTreeCorruptedNotRoot(this@__assertIsRoot)
    }

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
                this._checkSame(parent, this._getLeftChild(grandParent)) -> {
                    val uncle = this._getRightChild(grandParent)
                    if (uncle != null && this._getColor(uncle) == Color.RED) {
                        this._setColor(parent, Color.BLACK)
                        this._setColor(uncle, Color.BLACK)
                        this._setColor(grandParent, Color.RED)
                        current = grandParent
                        continue
                    }

                    when {
                        this._checkSame(current, this._getLeftChild(parent)) -> {
                            this.__rotateRight_Switch(grandParent)
                            continue
                        }

                        this._checkSame(current, this._getRightChild(parent)) -> {
                            this.__rotateLeft_Left(parent, grandParent)
                            current = parent
                            continue
                        }

                        else -> this.__throwTreeCorruptedNotChild(current, parent)
                    }
                }

                this._checkSame(parent, this._getRightChild(grandParent)) -> {
                    val uncle = this._getLeftChild(grandParent)
                    if (uncle != null && this._getColor(uncle) == Color.RED) {
                        this._setColor(parent, Color.BLACK)
                        this._setColor(uncle, Color.BLACK)
                        this._setColor(grandParent, Color.RED)
                        current = grandParent
                        continue
                    }

                    when {
                        this._checkSame(current, this._getRightChild(parent)) -> {
                            this.__rotateLeft_Switch(grandParent)
                            continue
                        }

                        this._checkSame(current, this._getLeftChild(parent)) -> {
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

    private fun ___swapColors(nod1: NODE, node2: NODE) {
        val color = this._getColor(nod1)
        this._setColor(nod1, this._getColor(node2))
        this._setColor(node2, color)
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun ___swapWithKeyNeighbour(
        node: NODE,
        nodeParent: NODE?,
        nodeForwardChild: NODE,
        setParent2Top: (newTop: NODE) -> Unit,
        getForwardChild: (node: NODE) -> NODE?,
        setForwardChild: (parent: NODE, child: NODE?) -> Unit,
        getOppositeChild: (node: NODE) -> NODE?,
        setOppositeChild: (parent: NODE, child: NODE?) -> Unit
    ) {
        contract {
            callsInPlace(setParent2Top, InvocationKind.EXACTLY_ONCE)
            callsInPlace(getForwardChild, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(setForwardChild, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(getOppositeChild, InvocationKind.AT_LEAST_ONCE)
            callsInPlace(setOppositeChild, InvocationKind.AT_LEAST_ONCE)
        }

        var repl = getOppositeChild(nodeForwardChild)
        if (repl == null) {
            BinaryTreeUtilities.Swap.swapNeighbours(
                parent = node,
                child = nodeForwardChild,
                grandparent = nodeParent,
                setGrandparent2Parent = setParent2Top,
                setParent = this::_setParent,
                getForwardChild = getForwardChild,
                setForwardChild = setForwardChild,
                getOppositeChild = getOppositeChild,
                setOppositeChild = setOppositeChild
            )
            repl = nodeForwardChild
        } else {
            var replParent = nodeForwardChild
            var replPtr: NODE = repl

            while (true) {
                val newRepl = getOppositeChild(replPtr) ?: break
                replParent = replPtr
                replPtr = newRepl
            }
            repl = replPtr

            BinaryTreeUtilities.Swap.swapDistant(
                node1 = node,
                node2 = repl,
                node1parent = nodeParent,
                node2parent = replParent,
                setParent2Node1 = setParent2Top,
                setParent2Node2 = { newChild -> setOppositeChild(replParent, newChild) },
                setParent = this::_setParent,
                getLeftChild = getForwardChild,
                setLeftChild = setForwardChild,
                getRightChild = getOppositeChild,
                setRightChild = setOppositeChild
            )
        }
        this.___swapColors(node, repl)
    }

    private fun __swapWithPrevKey(
        node: NODE,
        parent: NODE?,
        leftChild: NODE
    ) {
        this.___swapWithKeyNeighbour(
            node = node,
            nodeParent = parent,
            nodeForwardChild = leftChild,
            setParent2Top = { newTop -> this.__setChildSwitch(parent, node, newTop) },
            getForwardChild = this::_getLeftChild,
            setForwardChild = this::_setLeftChild,
            getOppositeChild = this::_getRightChild,
            setOppositeChild = this::_setRightChild
        )
    }

    /*
        private fun __swapWithNextKey(
            node: NODE,
            parent: NODE?,
            rightChild: NODE
        ) {
            this.___swapWithKeyNeighbour(
                node = node,
                nodeParent = parent,
                nodeForwardChild = rightChild,
                setParent2Top = { newTop -> this.__setChildSwitch(parent, node, newTop) },
                getForwardChild = this::_getRightChild,
                setForwardChild = this::_setRightChild,
                getOppositeChild = this::_getLeftChild,
                setOppositeChild = this::_setLeftChild
            )
        }
    */

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

                    if (this._getColor(node) != Color.RED)
                        this.__balanceAfterUnlinking(node, parent)
                    this.__setChildSwitch(parent, node, null)
                    return
                } else {
                    this.__setChildSwitch(parent, node, nodeRightChild)
                    this._setParent(nodeRightChild, parent)
                    this._setColor(nodeRightChild, Color.BLACK)
                    return
                }
            } else {
                if (nodeRightChild == null) {
                    this.__setChildSwitch(parent, node, nodeLeftChild)
                    this._setParent(nodeLeftChild, parent)
                    this._setColor(nodeLeftChild, Color.BLACK)
                    return
                } else {
                    this.__swapWithPrevKey(node, parent, nodeLeftChild)
                    continue
                }
            }
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
                this._checkSame(node, this._getLeftChild(parent)) -> continueBalancing = this.___balanceAfterUnlinking_Directed(
                    node = node,
                    parent = parent,
                    getForwardChild = this::_getLeftChild,
                    getOppositeChild = this::_getRightChild,
                    rotateForward_Switch = this::__rotateLeft_Switch,
                    rotateOpposite_Opposite = this::__rotateRight_Right,
                    updateState = { newNode, newParent -> node = newNode; parent = newParent }
                )

                this._checkSame(node, this._getRightChild(parent)) -> continueBalancing = this.___balanceAfterUnlinking_Directed(
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
        if (this._getColor(sibling).asBoolean) /* if RED */ {
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
        } else /* if BLACK */ {
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
        return this.subtreeMax(this.root ?: return null)
    }

    fun minOrThrow(): NODE = this.minOrNull() ?: throw NoSuchElementException("Red-black tree is empty")

    fun maxOrThrow(): NODE = this.maxOrNull() ?: throw NoSuchElementException("Red-black tree is empty")
}