/**
 * Copyright 2014, Emory University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.emory.clir.clearnlp.dependency;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import edu.emory.clir.clearnlp.reader.TSVReader;
import edu.emory.clir.clearnlp.srl.SRLTree;
import edu.emory.clir.clearnlp.util.DSUtils;
import edu.emory.clir.clearnlp.util.Joiner;
import edu.emory.clir.clearnlp.util.arc.SRLArc;
import edu.emory.clir.clearnlp.util.constant.StringConst;


/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class DEPTreeTest
{
	@Test
	public void testTree() throws Exception
	{
		DEPTree tree = new DEPTree(DSUtils.toArrayList(new DEPNode(1, "A")));
		assertEquals(2, tree.size());
		
		tree.add(new DEPNode(2, "B"));
		tree.add(new DEPNode(3, "C"));
		assertEquals(4, tree.size());
		
		tree.insert(1, new DEPNode(0, "a"));
		assertEquals(5, tree.size());
		
		tree.insert(3, new DEPNode(0, "b"));
		assertEquals(6, tree.size());
		
		tree.insert(6, new DEPNode(0, "c"));
		assertEquals(7, tree.size());
		
		tree.remove(1);
		assertEquals(6, tree.size());
		
		tree.remove(5);
		assertEquals(5, tree.size());

		tree.remove(2);
		assertEquals(4, tree.size());
	}
	
	@Test
	@Ignore
	public void test() throws Exception
	{
		TSVReader reader = new TSVReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(new FileInputStream("src/test/resources/dependency/dependency.cnlp"));
		DEPTree tree = reader.next();
		DEPTree copy = new DEPTree(tree);
		String str = tree.toStringSRL();
		DEPNode pred = tree.get(0);
		SRLTree sTree;
		
		
		
		System.out.println(tree.toStringDEP()+"\n");
		System.out.println(tree.getDepthFirstNodeList().stream().map(DEPNode::getWordForm).collect(Collectors.joining(StringConst.SPACE)));
		
		
		
		// srl-tree
		String[] arr = {"buy.01 1:A0 2:AM-TMP 5:A1 6:AM-TMP","be.01 5:A1 7:R-A1 9:A2"};
		int i = 0;
		
		while ((pred = tree.getNextSemanticHead(pred.getID())) != null)
		{
			sTree = tree.getSRLTree(pred);
			assertEquals(arr[i++], sTree.toString());
		}
		
		// insert
		tree.remove(2);
		tree.remove(5);
		assertEquals(reader.next().toStringSRL(), tree.toStringSRL());
		
		// semantic heads
		DEPNode node = new DEPNode(0, "tomorrow", "tomorrow", "NN", null, new DEPFeat());
		node.setHead(tree.get(2), "npadvmod");
		node.initSemanticHeads();
		node.addSemanticHead(new SRLArc(tree.get(2), "AM-TMP"));
		tree.insert(5, node);
		assertEquals(reader.next().toStringSRL(), tree.toStringSRL());
		
		// projectivize
		tree.projectivize("<",">");
		assertEquals(reader.next().toStringSRL(), tree.toStringSRL());
		
		// roots
		tree.get(7).setHead(tree.get(0), "root");
		List<DEPNode> roots = tree.getRoots();
		
		assertEquals(tree.get(2), tree.getFirstRoot());
		assertEquals(tree.get(2), roots.get(0));
		assertEquals(tree.get(7), roots.get(1));

		// clone
		assertEquals(str, copy.toStringSRL());
		
		// argument list
		List<List<SRLArc>> args = copy.getArgumentList();
		
		assertEquals("1:A0 2:AM-TMP 5:A1 6:AM-TMP", Joiner.join(args.get(3), " "));
		assertEquals("5:A1 7:R-A1 9:A2", Joiner.join(args.get(8), " "));
	}
}