package com.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ReadPdf {
	
	String dataPath = "C:\\Users\\Administrator\\Desktop\\test.pdf";
	Analyzer ika = new IKAnalyzer5x();
	Directory directory = null;
	IndexWriterConfig config = null;
	
	private String pdf2String() {
		String result = null;
	    try {
	    	FileInputStream is = new FileInputStream(dataPath);
	        PDFParser parser = new PDFParser(new RandomAccessBuffer(is));
	        parser.parse();
	        PDDocument document = parser.getPDDocument();
	        PDFTextStripper stripper = new PDFTextStripper();
	        result = stripper.getText(document);
	        document.close();
	        is.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	public void createIndexes(boolean deleted) {
		try {
			config = new IndexWriterConfig(ika);
			directory = FSDirectory.open(Paths.get(System.getProperty("user.dir") + "\\indexes"));
			IndexWriter iwriter = new IndexWriter(directory, config);
			if (deleted) {
				iwriter.deleteAll();
			}
			
			Document doc = new Document();
			File file = new File(dataPath);
			doc.add(new Field("contents", pdf2String(), TextField.TYPE_STORED));
			doc.add(new Field("fileName", file.getName(), TextField.TYPE_STORED));
			doc.add(new Field("fullPath", file.getCanonicalPath(), TextField.TYPE_STORED));
    
		    iwriter.addDocument(doc);
		    iwriter.forceMerge(1);
		    iwriter.close();
		    directory.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void readIndexes() {
		try {
			directory = FSDirectory.open(Paths.get(System.getProperty("user.dir") + "\\indexes"));
			DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    QueryParser parser = new QueryParser("contents", ika);
		    MultiFieldQueryParser mparser = new MultiFieldQueryParser(
		    		new String[]{"contents", "fileName"}, ika);
		    String keyword = "需求";
		    Query query = mparser.parse(keyword);
		    
		    Builder builder = new Builder();
		    builder.add(query, Occur.MUST);
		    ScoreDoc[] hits = isearcher.search(builder.build(), 10).scoreDocs;
		    System.out.println(builder.build());
		    System.out.println(hits.length);
		    for (int i = 0; i < hits.length; i++) {
			    Document hitDoc = isearcher.doc(hits[i].doc);
			    System.out.println("---------------------");
			    System.out.println(hitDoc.get("contents")+"####"+hitDoc.get("fileName")+"####"
		    		+hitDoc.get("fullPath")+"####");
		    }
		    ireader.close();
		    directory.close();
		    
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ReadPdf rf = new ReadPdf();
		rf.createIndexes(true);
		rf.readIndexes();
		
	}

}
