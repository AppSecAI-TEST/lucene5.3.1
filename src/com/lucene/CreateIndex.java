package com.lucene;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 创建索引
 * @author yuxiao
 */
public class CreateIndex { 
	
	String data = "张明,31,1985-5-11 18:30:05,31.12345,117.98765,370304198505111011;"
				+ "刘明,29,1986-6-2 9:10:12,31.23456,117.87654,370109198606021889;"
				+ "于勇,1,2015-2-7 16:5:00,31.34567,117.765432,370101201502071239";
	Analyzer ika = new IKAnalyzer5x();
	
	public void createIndexes(boolean deleted) {
	    IndexWriterConfig config = new IndexWriterConfig(ika);
		try {
			Directory directory = FSDirectory.open(Paths.get(System.getProperty("user.dir") + "\\indexes"));
			IndexWriter iwriter = new IndexWriter(directory, config);
			if (deleted) {
				iwriter.deleteAll();
			}
			
		    //Directory directory = new RAMDirectory();
			String[] datas = data.split(";");
			List<Document> documentList = new ArrayList<Document>();
		    for (String str : datas) {
			    Document doc = new Document();
			    String[] zds = str.split(",");
			    doc.add(new Field("name", zds[0], TextField.TYPE_STORED));
			    doc.add(new LongField("age", Long.valueOf(zds[1]), LongField.TYPE_STORED));
			    doc.add(new Field("date", dateFormat(zds[2]), TextField.TYPE_STORED));
			    doc.add(new DoubleField("zbx", Double.valueOf(zds[3]), DoubleField.TYPE_STORED));
			    doc.add(new DoubleField("zby", Double.valueOf(zds[4]), DoubleField.TYPE_STORED));
			    doc.add(new Field("sfzhm", zds[5], TextField.TYPE_STORED));
			    documentList.add(doc);
		    }
		    iwriter.addDocuments(documentList);
		    iwriter.forceMerge(1);
		    iwriter.close();
		    directory.close();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String dateFormat(String date) {
		try {
			DateFormat df1 = new SimpleDateFormat("yyyyMMddHHmmss");
			return df1.format(df1.parse(date));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		CreateIndex ci = new CreateIndex();
		ci.createIndexes(true);
	}
	
}
