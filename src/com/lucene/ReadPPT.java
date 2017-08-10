package com.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;

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
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;

public class ReadPPT {
	
	String dataPath = "C:\\Users\\Administrator\\Desktop\\ADP快速开发平台系统介绍V1.0.ppt";
	Analyzer ika = new IKAnalyzer5x();
	Directory directory = null;
	IndexWriterConfig config = null;
	
	private String ppt2String() {
		StringBuffer content = new StringBuffer("");
		try {
			File file = new File(dataPath);
			if (file.getName().toLowerCase().trim().endsWith("ppt")) {
				InputStream is = new FileInputStream(file);
				HSLFSlideShow ss = new HSLFSlideShow(is);
				List<HSLFSlide> slides = ss.getSlides();
				for (HSLFSlide s : slides) {
					List<List<HSLFTextParagraph>> list0 = s.getTextParagraphs();
					for (List<HSLFTextParagraph> list1 : list0) {
						for (HSLFTextParagraph htp : list1) {
							content.append(htp.toString());
						}
					}
				}
			} else {
				content.append(
					new XSLFPowerPointExtractor(POIXMLDocument.openPackage(dataPath)).getText());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "<pre>" + content.toString() + "</pre>";
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
			doc.add(new Field("contents", ppt2String(), TextField.TYPE_STORED));
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
		    String keyword = "请假申请单";
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
		ReadPPT rf = new ReadPPT();
		rf.createIndexes(true);
		rf.readIndexes();
		
	}

}
