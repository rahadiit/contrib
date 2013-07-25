/**
 *  Copyright 2012 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.lucene.search;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.ExactSimScorer;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;

import eu.europeana.ranking.bm25f.enums.SolrFields;
import eu.europeana.ranking.bm25f.similarity.BM25FSimilarity;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Nov 25, 2012
 */
public class BM25FBooleanTermQuery extends Query {
	private final Term term;
	private final int docFreq;
	private final TermContext perReaderTermState;

	//private String defaultField = SolrFields.getInstance().getDefaultField();
	private String[] fields = SolrFields.getInstance().getFieldNames();

	final class BM25FTermWeight extends Weight {
		private final Similarity similarity;
		private final Similarity.SimWeight[] stats;
		private final TermContext termStates;
		private final TermContext[] fieldTermStates;
		float idf;
		public float k1;
		private String[] fields;

		protected float idf(long docFreq, long numDocs) {
			return (float) Math.log(1 + (numDocs - docFreq + 0.5D)
					/ (docFreq + 0.5D));
		}

		public BM25FTermWeight(IndexSearcher searcher, TermContext termStates,
				TermContext[] fieldTermStates) throws IOException {
			assert termStates != null : "TermContext must not be null";
			this.termStates = termStates;

			this.fieldTermStates = fieldTermStates;
			this.similarity = searcher.getSimilarity();

			
			if (this.similarity instanceof BM25FSimilarity) {
				if(StringUtils.equals(SolrFields.getInstance().getDefaultField(), term.field())){
					fields = ((BM25FSimilarity) similarity).getFields();
				} else {
					fields = new String[]{term.field()};
				}
				k1 = ((BM25FSimilarity) similarity).getK1();
			} else {
				fields = new String[] { term.field() };
			}

			this.stats = new Similarity.SimWeight[fields.length];
			for (int i = 0; i < fields.length; i++) {
				Term fieldTerm = new Term(fields[i], term.text());
				// getBoosts is not used
				this.stats[i] = similarity.computeWeight(0,
						searcher.collectionStatistics(fieldTerm.field()),
						searcher.termStatistics(fieldTerm, fieldTermStates[i]));
			}
			System.out.println("term field is " + term.field());
			Term fieldTerm = new Term(term.text(), term.field());
			TermStatistics termStat = searcher.termStatistics(fieldTerm,
					termStates);
			long df = termStat.docFreq();
			long numDocs = searcher.getIndexReader().numDocs();
			idf = idf(df, numDocs);

		}

		@Override
		public String toString() {
			return "weight(" + BM25FTermWeight.this + ")";
		}

		@Override
		public Query getQuery() {
			return BM25FBooleanTermQuery.this;
		}

		@Override
		public float getValueForNormalization() {
			// return stats.getValueForNormalization();
			return 0;
		}

		@Override
		public void normalize(float queryNorm, float topLevelBoost) {
			// stats.normalize(queryNorm, topLevelBoost);
		}

		@Override
		public Scorer scorer(AtomicReaderContext context,
				boolean scoreDocsInOrder, boolean topScorer, Bits acceptDocs)
				throws IOException {
			assert termStates.topReaderContext == ReaderUtil
					.getTopLevelContext(context) : "The top-reader used to create Weight ("
					+ termStates.topReaderContext
					+ ") is not the same as the current reader's top-reader ("
					+ ReaderUtil.getTopLevelContext(context);

			ExactSimScorer[] scorers = new ExactSimScorer[stats.length];
			DocsEnum[] docsEnums = new DocsEnum[stats.length];
			//TermsEnum termDocs = null;
			DocsEnum docsEnum = null;
			for (int i = 0; i < stats.length; i++) {
				// termDocs = getTermsEnum(context, fields[i],i);
				docsEnum = getDocsEnum(context, fields[i]);
				if (docsEnum != null) {
					scorers[i] = similarity.exactSimScorer(stats[i], context);
					docsEnums[i] = docsEnum;
				}
				// System.out.println("DOC ENUM "+i);
				// while (docsEnum[i].nextDoc() != Scorer.NO_MORE_DOCS){
				// System.out.println(" -> "+docsEnum[i].docID()+"("+docsEnum[i].freq()+")");
				// }

				// assert docsEnum[i] != null;

			}

			return new BM25FTermScorer(this, scorers, docsEnums);

		}

		// /**
		// * Returns a {@link TermsEnum} positioned at this weights Term or null
		// * if the term does not exist in the given context
		// */
		// private TermsEnum getTermsEnum(AtomicReaderContext context, String
		// field, int fieldPos)
		// throws IOException {
		// final TermState state = fieldTermStates[fieldPos].get(context.ord);
		// Term t = new Term(field, term.text());
		// if (state == null) { // term is not present in that reader
		// assert termNotInReader(context.reader(), field, t.bytes()) :
		// "no termstate found but term exists in reader term="
		// + t;
		// return null;
		// }
		// // System.out.println("LD=" + reader.getLiveDocs() + " set?=" +
		// // (reader.getLiveDocs() != null ? reader.getLiveDocs().get(0) :
		// // "null"));
		// final TermsEnum termsEnum = context.reader().terms(field)
		// .iterator(null);
		// termsEnum.seekExact(t.bytes(), state);
		// return termsEnum;
		// }

		private DocsEnum getDocsEnum(AtomicReaderContext context, String field
				) throws IOException {

			return context.reader().termDocsEnum(
					null, field,
					new Term(field, term.text()).bytes());

		}

		
//		private boolean termNotInReader(AtomicReader reader, String field,
//				BytesRef bytes) throws IOException {
//			// only called from assert
//			// System.out.println("TQ.termNotInReader reader=" + reader +
//			// " term=" + field + ":" + bytes.utf8ToString());
//			return reader.docFreq(field, bytes) == 0;
//		}

		@Override
		public Explanation explain(AtomicReaderContext context, int doc)
				throws IOException {
			Scorer scorer = scorer(context, true, false, context.reader()
					.getLiveDocs());
			if (scorer != null) {
				int newDoc = scorer.advance(doc);
				if (newDoc == doc) {

					ExactSimScorer[] scorers = new ExactSimScorer[stats.length];
					for (int i = 0; i < stats.length; i++) {

						scorers[i] = similarity.exactSimScorer(stats[i],
								context);
					}

					ComplexExplanation result = new ComplexExplanation();
					result.setDescription("idf(t) * [field scores / (k1) + field scores]");
					result.setValue(scorer.score());
					Explanation scores = new Explanation();
					scores.setDescription("field scores, sum of:");
					float acum = 0;
					for (int i = 0; i < stats.length; i++) {

						int freq = ((BM25FTermScorer) scorer).getFieldFreq(i);
						if (freq == 0)
							continue;

						Explanation freqExplanation = new Explanation(freq,
								"tf in " + fields[i]);
						Explanation scoreExplanation = scorers[i].explain(doc,
								freqExplanation);
						acum += scoreExplanation.getValue();

						scores.addDetail(scoreExplanation);
					}
					scores.setValue(acum);
					result.addDetail(scores);
					result.addDetail(new Explanation(idf, "idf"));
					result.addDetail(new Explanation(k1, "k1"));
					return result;

					// for (int i = 0; i < stats.length; i++) {
					// Explanation scoreExplanation = scorers[i].explain(doc,
					// new Explanation(freq, "termFreq=" + freq));
					// result.addDetail(scoreExplanation);
					// result.setValue(scoreExplanation.getValue());
					// result.setMatch(true);
					// }
					// return result;

				}
			}
			return new ComplexExplanation(false, 0.0f, "no matching term");
		}
	}

	/** Constructs a query for the term <code>t</code>. */
	public BM25FBooleanTermQuery(Term t) {
		this(t, -1);
	}

	/**
	 * Expert: constructs a TermQuery that will use the provided docFreq instead
	 * of looking up the docFreq against the searcher.
	 */
	public BM25FBooleanTermQuery(Term t, int docFreq) {
		term = t;
		this.docFreq = docFreq;
		perReaderTermState = null;
	}

	/**
	 * Expert: constructs a TermQuery that will use the provided docFreq instead
	 * of looking up the docFreq against the searcher.
	 */
	public BM25FBooleanTermQuery(Term t, TermContext states) {
		assert states != null;
		term = t;
		docFreq = states.docFreq();
		perReaderTermState = states;
	}

	/** Returns the term of this query. */
	public Term getTerm() {
		return term;
	}

	@Override
	public Weight createWeight(IndexSearcher searcher) throws IOException {
		final IndexReaderContext context = searcher.getTopReaderContext();
		final TermContext termState;
		if (perReaderTermState == null
				|| perReaderTermState.topReaderContext != context) {
			// make TermQuery single-pass if we don't have a PRTS or if the
			// context differs!
			termState = TermContext.build(context, term, true); // cache term
																// lookups!
		} else {
			// PRTS was pre-build for this IS
			termState = this.perReaderTermState;
		}
		TermContext[] fieldTermContext = new TermContext[fields.length];

		for (int i = 0; i < fields.length; i++) {
			Term t = new Term(fields[i], term.text());

			fieldTermContext[i] = TermContext.build(context, t, false);
		}
		// we must not ignore the given docFreq - if set use the given value
		// (lie)
		if (docFreq != -1)
			termState.setDocFreq(docFreq);

		return new BM25FTermWeight(searcher, termState, fieldTermContext);
	}

	@Override
	public void extractTerms(Set<Term> terms) {
		terms.add(getTerm());
	}

	/** Prints a user-readable version of this query. */
	@Override
	public String toString(String field) {
		StringBuilder buffer = new StringBuilder();
		if (!term.field().equals(field)) {
			buffer.append(term.field());
			buffer.append(":");
		}
		buffer.append(term.text());
		buffer.append(ToStringUtils.boost(getBoost()));
		return buffer.toString();
	}

	/** Returns true iff <code>o</code> is equal to this. */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BM25FBooleanTermQuery))
			return false;
		BM25FBooleanTermQuery other = (BM25FBooleanTermQuery) o;
		return (this.getBoost() == other.getBoost())
				&& this.term.equals(other.term);
	}

	/** Returns a hash code value for this object. */
	@Override
	public int hashCode() {
		return Float.floatToIntBits(getBoost()) ^ term.hashCode();
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		return this;
	}

}
