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
package edu.emory.clir.clearnlp.collection.list;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import edu.emory.clir.clearnlp.collection.list.IntArrayList;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class IntArrayListTest
{
	@Test
	public void test() throws Exception
	{
		IntArrayList list = new IntArrayList();
		int[] items = {1, 2, 3};
		int i, size = items.length;
		
		for (int item : items)
			list.add(item);
		
		assertEquals(1, list.min());
		assertEquals(3, list.max());
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
		out.writeObject(list);
		out.close();
		
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(bout.toByteArray())));
		list = (IntArrayList)in.readObject();
		in.close();
		
		for (i=0; i<size; i++)
			assertEquals(items[i], list.get(i), 0);
		
		IntArrayList clone = list.clone();
		clone.set(1, 4);
		
		assertEquals(2, list .get(1), 0);
		assertEquals(4, clone.get(1), 0);
		
		clone.remove(0);
		assertEquals(2, clone.size());
		assertEquals(4, clone.get(0), 0);
	}
}