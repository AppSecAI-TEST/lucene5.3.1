package com.lucene;

import java.io.StringReader;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * 查询索引
 * @author xxxx
 */
public class QueryIndex {
	
	Analyzer ika = new IKAnalyzer5x();
	
	/**
	 * 转换日期为索引
	 * @param date
	 * @return
	 */
	public String dateFormat(String date) {
		try {
			DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String d1 = df1.format(df1.parse(date));
			d1 = d1.replaceAll("\\s|[-]|[:]", "");
			return d1;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 索引转换正常日期格式
	 * @param date
	 * @return
	 */
	public String dateFormat2(String date) {
		StringBuffer sb = new StringBuffer(date.substring(0,4));
		sb.append("-");
		sb.append(date.substring(4, 6));
		sb.append("-");
		sb.append(date.substring(6, 8));
		sb.append(" ");
		sb.append(date.substring(8, 10));
		sb.append(":");
		sb.append(date.substring(10, 12));
		sb.append(":");
		sb.append(date.substring(12, 14));
		return sb.toString();
	}
	
	/**
	 * 查询索引
	 */
	public void queryIndexes() {
		try {
			Directory directory = FSDirectory.open(Paths.get(System.getProperty("user.dir") + "\\indexes"));
			DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    QueryParser parser = new QueryParser("name", ika);
		    String keyword = " 明 明 于 于 ";
		    Query query = parser.parse(keyword);
		    
		    Builder builder = new Builder();
		    builder.add(query, Occur.MUST);
		    
		    Query xyQuery = NumericRangeQuery.newDoubleRange("zbx", 31.01, 31.24, true, true);
		    //bq.add(xyQuery, Occur.MUST);
		    
		    Query ageQuery = NumericRangeQuery.newLongRange("age", 27l, 31l, true, true);
		    //bq.add(ageQuery, Occur.MUST);
		    
		    Query dateQuery = TermRangeQuery.newStringRange("date", dateFormat("1985-5-11 18:30:05"), dateFormat("2015-2-7 16:5:00"), true, true);
		    //bq.add(dateQuery, Occur.MUST);
		    
		    ScoreDoc[] hits = isearcher.search(builder.build(), 10).scoreDocs;
		    System.out.println(builder.build());
		    System.out.println(hits.length);
		    for (int i = 0; i < hits.length; i++) {
		      Document hitDoc = isearcher.doc(hits[i].doc);
		      System.out.println("---------------------");
		      String name = getLightWords("name", keyword, hitDoc.get("name"));
		      System.out.println(name+","+hitDoc.get("age")+","+dateFormat2(hitDoc.get("date"))+
		      ","+hitDoc.get("zbx")+","+hitDoc.get("zby")+","+hitDoc.get("sfzhm"));
		    }
		    ireader.close();
		    directory.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 自定义高亮器
	 * @param fieldName
	 * @param keywords
	 * @param content
	 * @return
	 */
	public String getLightWords(String fieldName, String keywords, String content) {
		try {
			TokenStream tokenStream = ika.tokenStream(fieldName, new StringReader(keywords));
			tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.reset();
			List<String> list = new ArrayList<String>();
			while (tokenStream.incrementToken()) {
				CharTermAttribute charTermAttribute = (CharTermAttribute) tokenStream.getAttribute(CharTermAttribute.class);
				String key = charTermAttribute.toString();
				if (!list.contains(key)) {
					list.add(key);
				}
			}
			tokenStream.end();
			tokenStream.close();
			
			for (String keyword : list) {
				StringBuffer sb = new StringBuffer("<span style='color:#FF0000'>");
				sb.append(keyword);
				sb.append("</span>");
				content = content.replaceAll(keyword, sb.toString());
			}
			
			return content;
			
		} catch (Exception e) {
			e.printStackTrace();
			return content;
		}
	}
	
	public static void main(String[] args) {
		QueryIndex qi = new QueryIndex();
		qi.queryIndexes();
	}
	
}
