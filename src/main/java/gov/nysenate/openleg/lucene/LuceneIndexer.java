package gov.nysenate.openleg.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;

public interface LuceneIndexer {
	void optimize() throws IOException;
	void createIndex() throws IOException;
	void closeWriter() throws IOException;
	IndexWriter openWriter() throws IOException;
	void deleteDocuments(String otype, String oid)  throws IOException;
	boolean addDocument(LuceneObject o, LuceneSerializer serializer, IndexWriter writer)  throws InstantiationException,IllegalAccessException,IOException;
}
