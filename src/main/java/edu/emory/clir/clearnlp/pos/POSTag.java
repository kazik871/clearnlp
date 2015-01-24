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
package edu.emory.clir.clearnlp.pos;

/**
 * @since 3.0.0
 * @author Jinho D. Choi ({@code jinho.choi@emory.edu})
 */
public interface POSTag
{
	/** The part-of-speech tag for emoticons. */
	String POS_EMOTICON = "EMO";
	/** The part-of-speech tag for final tags. */
	String POS_FINAL = POSTagEn.POS_PERIOD;
	/** The part-of-speech tag for hyperlinks (e.g., URLs, emails). */
	String POS_HYPERLINK = POSTagEn.POS_ADD;
}
