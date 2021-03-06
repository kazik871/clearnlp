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
package edu.emory.clir.clearnlp.dictionary.universal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import edu.emory.clir.clearnlp.dictionary.AbstractDTTokenizer;
import edu.emory.clir.clearnlp.dictionary.PathTokenizer;
import edu.emory.clir.clearnlp.util.IOUtils;
import edu.emory.clir.clearnlp.util.Splitter;
import edu.emory.clir.clearnlp.util.StringUtils;
import edu.emory.clir.clearnlp.util.lang.TLanguage;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public class DTCompound extends AbstractDTTokenizer
{
	private Map<String,int[]> m_compound;
	
	public DTCompound(TLanguage language)
	{
		switch (language)
		{
		case ENGLISH: init(IOUtils.getInputStreamsFromClasspath(PathTokenizer.EN_COMPOUNDS)); break;
		default: throw new IllegalArgumentException(language.toString());
		}
	}
	
	public DTCompound(InputStream in)
	{
		init(in);
	}
	
	public void init(InputStream in)
	{
		BufferedReader reader = IOUtils.createBufferedReader(in);
		m_compound = new HashMap<>();
		StringBuilder build;
		String line, token;
		String[] tokens;
		int i, size;
		int[] tmp;
		
		try
		{
			while ((line = reader.readLine()) != null)
			{
				tokens = Splitter.splitSpace(line.trim());
				build  = new StringBuilder();
				size   = tokens.length - 1;
				tmp    = new int[size];
				
				for (i=0; i<size; i++)
				{
					token  = tokens[i];
					tmp[i] = build.length() + token.length();
					build.append(token);
				}
				
				build.append(tokens[size]);
				m_compound.put(StringUtils.toLowerCase(build.toString()), tmp);
			}
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	@Override
	public String[] tokenize(String original, String lower, char[] lcs)
	{
		int[] indices = m_compound.get(lower);
		return (indices != null) ? Splitter.split(original, indices) : null;
	}
}
