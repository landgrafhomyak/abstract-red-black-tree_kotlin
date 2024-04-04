package ru.landgrafhomyak.utility

import kotlin.test.Test
import kotlin.test.assertFalse

@Suppress("TestFunctionName")
internal class AbstractRedBlackTreeTest {
    private class TestNode(val key: Int, var parent: TestNode? = null) /*: AbstractMap<TestNode.Key, Any?>()*/ {
        enum class Key {
            LEFT, RIGHT, COLOR
        }


        var leftChild: TestNode? = null
        var rightChild: TestNode? = null
        var color: AbstractRedBlackTree.Color = AbstractRedBlackTree.Color.RED

        private val AbstractRedBlackTree.Color.string: String
            get() = when (this@string) {
                AbstractRedBlackTree.Color.RED -> "red"
                AbstractRedBlackTree.Color.BLACK -> "black"
            }

        private fun TestNode?._fmtNode(prefix: String): String {
            if (this@_fmtNode == null)
                return ""

            return "${prefix}{${this@_fmtNode.key}, ${this@_fmtNode.color.string}}"
        }

//        private class Entry(override val key: Key, override val value: Any?) : Map.Entry<Key, Any?>

//        override val entries: Set<Map.Entry<Key, Any?>>
//            get() = linkedSetOf(Entry(Key.LEFT, this.leftChild), Entry(Key.RIGHT, this.rightChild), Entry(Key.COLOR, this.color))

        override fun toString(): String = "<node${this.leftChild._fmtNode(" left=")} ${this._fmtNode("")}${this.rightChild._fmtNode(" right=")}>"
    }

    private class RedBlackTreeTestImpl : AbstractRedBlackTree<TestNode>() {
        override fun _getParent(node: TestNode): TestNode? = node.parent

        override fun _setParent(node: TestNode, parent: TestNode?) {
            node.parent = parent
        }

        override fun _getLeftChild(node: TestNode): TestNode? = node.leftChild

        override fun _setLeftChild(node: TestNode, child: TestNode?) {
            node.leftChild = child
        }

        override fun _getRightChild(node: TestNode): TestNode? = node.rightChild

        override fun _setRightChild(node: TestNode, child: TestNode?) {
            node.rightChild = child
        }

        override fun _getColor(node: TestNode): Color = node.color

        override fun _setColor(node: TestNode, color: Color) {
            node.color = color
        }

//        override fun _compare(leftNode: TestNode, rightNode: TestNode): Int = leftNode.key.compareTo(rightNode.key)

        fun add(key: Int) {
            var parent = this.root
            if (parent == null) {
                val node = TestNode(key)
                this.root = node
                this.balanceAfterLinking(node)
                return
            }
            var child: TestNode?
            while (true) {
                parent as TestNode
                val cmp = key.compareTo(parent.key)
                if (cmp < 0) {
                    child = parent.leftChild
                    if (child != null) {
                        parent = child
                        continue
                    } else {
                        val node = TestNode(key, parent)
                        parent.leftChild = node
                        this.balanceAfterLinking(node)
                        return
                    }
                } else if (cmp > 0) {
                    child = parent.rightChild
                    if (child != null) {
                        parent = child
                        continue
                    } else {
                        val node = TestNode(key, parent)
                        parent.rightChild = node
                        this.balanceAfterLinking(node)
                        return
                    }
                } else {
                    return
                }
            }
        }

        fun remove(key: Int) {
            var parent = this.root ?: return
            while (true) {
                val cmp = key.compareTo(parent.key)
                @Suppress("LiftReturnOrAssignment")
                if (cmp < 0) {
                    parent = parent.leftChild ?: return
                } else if (cmp > 0) {
                    parent = parent.rightChild ?: return
                } else {
                    this.unlink(parent)
                    return
                }
            }
        }
    }


    private fun __checkNode(node: TestNode?): Int {
        node ?: return 0
        if (node.color == AbstractRedBlackTree.Color.RED) {
            if (node.leftChild?.color === AbstractRedBlackTree.Color.RED)
                return -1
            if (node.rightChild?.color === AbstractRedBlackTree.Color.RED)
                return -1
        }

        val left = this.__checkNode(node.leftChild)
        if (left != this.__checkNode(node.rightChild))
            return -1

        @Suppress("LiftReturnOrAssignment")
        if (node.color == AbstractRedBlackTree.Color.BLACK)
            return left + 1
        else
            return left
    }

    private fun _checkTree(tree: RedBlackTreeTestImpl) = this.__checkNode(tree.root) < 0

    @Test
    fun testNaturals() {
        val tree = RedBlackTreeTestImpl()
        (0..1000)
            .onEachIndexed { i, n ->
                tree.add(n)
                Unit
                assertFalse("Invalid tree after adding $i-th element $n") { this._checkTree(tree) }
            }
            .onEachIndexed { i, n ->
                val node = tree.root!!
                tree.unlink(node)
                Unit
                assertFalse("Invalid tree after removing element $i-th element ${node.key}") { this._checkTree(tree) }
            }
    }
}
