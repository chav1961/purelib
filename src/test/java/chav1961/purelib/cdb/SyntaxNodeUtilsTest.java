package chav1961.purelib.cdb;

import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

@Tag("OrdinalTestCategory")
public class SyntaxNodeUtilsTest {
	static enum NodeType {
		OR, AND, NOT, TERM
	}
	
	@Test
	public void buildDNFTest() {
		final int[]		count = new int[1];
		
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	firstOperand = new SyntaxNode(0, 0, NodeType.TERM, 0, null);
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	secondOperand = new SyntaxNode(0, 0, NodeType.TERM, 1, null);
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	thirdOperand = new SyntaxNode(0, 0, NodeType.TERM, 2, null);
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	forthOperand = new SyntaxNode(0, 0, NodeType.TERM, 3, null);
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	fivthOperand = new SyntaxNode(0, 0, NodeType.TERM, 4, null);
		
		count[0] = 0;	// term1
		SyntaxNodeUtils.buildDNF((SyntaxNode)firstOperand, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				Assert.assertEquals(NodeType.AND, node.getType());
				Assert.assertEquals(1, node.children.length);
				Assert.assertEquals(NodeType.TERM, node.children[0].getType());
				count[0]++;
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(1, count[0]);

		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	andNode1 = new SyntaxNode(0, 0, NodeType.AND, 0, null, firstOperand, secondOperand);
		
		count[0] = 0;	// term1 and term2
		SyntaxNodeUtils.buildDNF((SyntaxNode)andNode1, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				Assert.assertEquals(NodeType.AND, node.getType());
				Assert.assertEquals(2, node.children.length);
				Assert.assertEquals(NodeType.TERM, node.children[0].getType());
				Assert.assertEquals(NodeType.TERM, node.children[1].getType());
				count[0]++;
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(1, count[0]);

		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	orNode1 = new SyntaxNode(0, 0, NodeType.OR, 0, null, firstOperand, secondOperand);
		
		count[0] = 0;	// term1 or term2
		SyntaxNodeUtils.buildDNF((SyntaxNode)orNode1, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				Assert.assertEquals(NodeType.AND, node.getType());
				Assert.assertEquals(1, node.children.length);
				Assert.assertEquals(NodeType.TERM, node.children[0].getType());
				count[0]++;
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(2, count[0]);
		
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	andNode2 = new SyntaxNode(0, 0, NodeType.AND, 0, null, thirdOperand, forthOperand);
		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	orNode2 = new SyntaxNode(0, 0, NodeType.OR, 0, null, andNode1, andNode2);
		
		count[0] = 0;	// term1 and term2 or term3 and term 4
		SyntaxNodeUtils.buildDNF((SyntaxNode)orNode2, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				Assert.assertEquals(NodeType.AND, node.getType());
				Assert.assertEquals(2, node.children.length);
				Assert.assertEquals(NodeType.TERM, node.children[0].getType());
				Assert.assertEquals(NodeType.TERM, node.children[1].getType());
				count[0]++;
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(2, count[0]);

		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	orNode3 = new SyntaxNode(0, 0, NodeType.OR, 0, null, forthOperand, fivthOperand);
		andNode2 = new SyntaxNode(0, 0, NodeType.AND, 0, null, thirdOperand, orNode3);
		orNode2 = new SyntaxNode(0, 0, NodeType.OR, 0, null, andNode1, andNode2);
	
		count[0] = 0;	// term1 and term2 or term3 and (term 4 or term5)
		SyntaxNodeUtils.buildDNF((SyntaxNode)orNode2, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				Assert.assertEquals(NodeType.AND, node.getType());
				Assert.assertEquals(2, node.children.length);
				Assert.assertEquals(NodeType.TERM, node.children[0].getType());
				Assert.assertEquals(NodeType.TERM, node.children[1].getType());
				count[0]++;
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(3, count[0]);

		SyntaxNode<NodeType, SyntaxNode<NodeType, SyntaxNode>>	notNode = new SyntaxNode(0, 0, NodeType.NOT, 0, orNode3);
		andNode2 = new SyntaxNode(0, 0, NodeType.AND, 0, null, thirdOperand, notNode);
		orNode2 = new SyntaxNode(0, 0, NodeType.OR, 0, null, andNode1, andNode2);

		count[0] = 0;	// term1 and term2 or term3 and not (term 4 or term5)
		SyntaxNodeUtils.buildDNF((SyntaxNode)orNode2, NodeType.OR, NodeType.AND, NodeType.NOT, (mode, node)->{
			if (mode == NodeEnterMode.ENTER) {
				Assert.assertEquals(NodeType.AND, node.getType());
				if (node.children.length == 2) {
					Assert.assertEquals(NodeType.TERM, node.children[0].getType());
					Assert.assertEquals(NodeType.TERM, node.children[1].getType());
				}
				else if (node.children.length == 3) {
					Assert.assertEquals(NodeType.TERM, node.children[0].getType());
					Assert.assertEquals(NodeType.NOT, node.children[1].getType());
					Assert.assertEquals(NodeType.NOT, node.children[2].getType());
				}
				else {
					Assert.fail("Error: neither 2 nor 3 for children length");
				}
				count[0]++;
			}
			return ContinueMode.CONTINUE;
		});
		Assert.assertEquals(2, count[0]);
	}
}
