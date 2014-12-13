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
package edu.emory.clir.clearnlp.component.mode.dep;

import java.util.List;

import edu.emory.clir.clearnlp.classification.prediction.StringPrediction;
import edu.emory.clir.clearnlp.collection.stack.Stack;
import edu.emory.clir.clearnlp.component.AbstractState;
import edu.emory.clir.clearnlp.component.CFlag;
import edu.emory.clir.clearnlp.dependency.DEPNode;
import edu.emory.clir.clearnlp.dependency.DEPTree;
import edu.emory.clir.clearnlp.feature.AbstractFeatureToken;
import edu.emory.clir.clearnlp.util.DSUtils;
import edu.emory.clir.clearnlp.util.arc.DEPArc;
import edu.emory.clir.clearnlp.util.constant.StringConst;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class DEPState extends AbstractState<DEPArc,DEPLabel> implements DEPTransition
{
	private Stack<DEPNode> d_stack;
	private Stack<DEPNode> d_inter;
	private int i_input;

	private List<DEPArc>[] d_2ndHeads;
	private StringBuilder s_states;
	
//	====================================== Initialization ======================================
	
	public DEPState(DEPTree tree, CFlag flag)
	{
		super(tree, flag);
		init();
	}
	
	@SuppressWarnings("unchecked")
	private void init()
	{
		d_stack = new Stack<>(t_size);
		d_inter = new Stack<>();
		i_input = 0;
		shift();
		
		d_2ndHeads = (List<DEPArc>[])DSUtils.createEmptyListArray(t_size);
		s_states = new StringBuilder();
	}

//	====================================== LABEL ======================================

	@Override
	protected void initOracle()
	{
		g_oracle = d_tree.getHeads();
 		d_tree.clearHeads();
	}
	
	@Override
	public void resetOracle()
	{
		d_tree.setHeads(g_oracle);
	}
	
	@Override
	public DEPLabel getGoldLabel()
	{
		DEPNode stack = getStack();
		DEPNode input = getInput();
		DEPArc  oracle;
		String  list;
		
		oracle = getOracle(stack.getID());
		
		if (oracle.getNode() == input)
		{
			list = isGoldReduce(true) ? T_REDUCE : T_PASS;
			return new DEPLabel(T_LEFT, list, oracle.getLabel());
		}
					
		oracle = getOracle(i_input);
		
		if (oracle.getNode() == stack)
		{
			list = isGoldShift() ? T_SHIFT : T_PASS;
			return new DEPLabel(T_RIGHT, list, oracle.getLabel());
		}
		
		if      (isGoldShift())			list = T_SHIFT;
		else if (isGoldReduce(false))	list = T_REDUCE;
		else							list = T_PASS;
		
		return new DEPLabel(T_NO, list, StringConst.EMPTY);
	}
	
	/** Called by {@link #getGoldLabel()}. */
	private boolean isGoldShift()
	{
		// if head(input) < stack
		DEPNode stack = getStack();
		
		if (getOracle(i_input).getNode().getID() < stack.getID())
			return false;
		
		// if child(input) < stack
		DEPNode input = getInput();
		int i = 1;

		while ((stack = d_stack.peek(i++)) != null)
		{
			if (getOracle(stack.getID()).getNode() == input)
				return false;
		}
		
		return true;
	}
	
	/** Called by {@link #getGoldLabel()}. */
	private boolean isGoldReduce(boolean hasHead)
	{
		// if stack has no head
		DEPNode stack = getStack();
		
		if (!hasHead && !stack.hasHead())
			return false;
		
		// if child(input) > stack 
		for (int i=i_input+1; i<t_size; i++)
		{
			if (getOracle(i).getNode() == stack)
				return false;
		}
		
		return true;
	}
	
//	====================================== NODE ======================================
	
	@Override
	public DEPNode getNode(AbstractFeatureToken token)
	{
		DEPNode node = null;
		
		switch (token.getSource())
		{
		case i: node = d_stack.peek(token.getOffset());		break;
		case j: node = getNode(i_input+token.getOffset());	break;
		case k: node = d_inter.peek(token.getOffset());		break;
		}
		
		return getNodeRelation(token, node);
	}
	
	public DEPNode getStack()
	{
		return d_stack.peek();
	}
	
	public DEPNode getInput()
	{
		return getNode(i_input);
	}
	
	public int getInputID()
	{
		return i_input;
	}
	
//	====================================== TRANSITION ======================================
	
	@Override
	public void next(DEPLabel label)
	{
		DEPNode stack = getStack();
		DEPNode input = getInput();
		saveState(label);
		
		if (label.isArc(T_LEFT))
		{
			stack.setHead(input, label.getDeprel());
			if (label.isList(T_REDUCE)) reduce();
			else pass();
		}
		else if (label.isArc(T_RIGHT))
		{
			input.setHead(stack, label.getDeprel());
			if (label.isList(T_SHIFT)) shift();
			else pass();
		}
		else
		{
			if (label.isList(T_SHIFT)) shift();
			else if (label.isList(T_REDUCE)	&& stack.hasHead()) reduce();
			else pass();
		}
		
		if (d_stack.isEmpty())
			shift();
	}
	
	private void saveState(DEPLabel label)
	{
		s_states.append(label.toString());
		s_states.append(StringConst.TAB);
		s_states.append(d_stack.toString());
		s_states.append(StringConst.TAB);
		s_states.append(d_inter.toString());
		s_states.append(StringConst.TAB);
		s_states.append(i_input);
		s_states.append(StringConst.NEW_LINE);
	}
	
	public String getStateHistory()
	{
		return s_states.toString();
	}
	
	@Override
	public boolean isTerminate()
	{
		return i_input >= t_size;
	}
	
	private void shift()
	{
		if (d_inter.isEmpty())
		{
			for (int i=d_inter.size()-1; i>=0; i--)
				d_stack.push(d_inter.get(i));
			
			d_inter.clear();
		}
		
		d_stack.push(getNode(i_input++));
	}
	
	private void reduce()
	{
		d_stack.pop();
	}
	
	private void pass()
	{
		d_inter.push(d_stack.pop());
	}
	
//	====================================== HELPER ======================================

	@Override
	public boolean extractWordFormFeature(DEPNode node)
	{
		return true;
	}
	
	public int distanceBetweenStackAndInput()
	{
		int d = i_input - getStack().getID(); 
		return (d > 6) ? 6 : d;
	}
	
	public void addSecondHead(StringPrediction[] ps)
	{
		double d0 = ps[0].getScore();
		DEPNode stack = getStack();
		DEPNode input = getInput();
		int i, size = ps.length;
		StringPrediction p;
		DEPLabel label;
		
		for (i=1; i<size; i++)
		{
			p = ps[i];
			if (d0 - p.getScore() >= 1) break;
			label = new DEPLabel(p.getLabel());
			
			if (!stack.hasHead() && label.isArc(T_LEFT))
				d_2ndHeads[stack.getID()].add(new DEPArc(input, label.getDeprel()));
			
			if (!input.hasHead() && label.isArc(T_RIGHT))
				d_2ndHeads[input.getID()].add(new DEPArc(stack, label.getDeprel()));
		}
		
	}
}