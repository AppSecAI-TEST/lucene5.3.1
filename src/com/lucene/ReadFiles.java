package com.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

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
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadFiles {
	
	String dataPath = "C:\\Users\\Administrator\\Desktop\\请假申请单.txt";
	String dataPath2 = "C:\\Users\\Administrator\\Desktop\\流程规划.xls";
	Analyzer ika = new IKAnalyzer5x();
	Directory directory = null;
	IndexWriterConfig config = null;
	
	public static String txt2String(File file){
		StringBuffer result = new StringBuffer("<pre>");
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "GBK");//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
            	result.append(lineTxt).append("<br/>");
            }
            result.append("</pre>");
            read.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result.toString();
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
			File file = new File(dataPath2);
			doc.add(new Field("contents", readExcel(dataPath2), TextField.TYPE_STORED));
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
	
	/**
	 * 读取excel
	 * @param filePath
	 */
	public static String readExcel(String filePath) {
		StringBuffer result = new StringBuffer("<pre>");
        boolean isE2007 = false;
        if(filePath.toLowerCase().endsWith("xlsx")) {
            isE2007 = true;
        }
        try {  
            InputStream input = new FileInputStream(filePath);  //建立输入流  
            Workbook wb  = null;
            //根据文件格式(2003或者2007)来初始化
            if(isE2007) {
                wb = new XSSFWorkbook(input);
            } else {  
                wb = new HSSFWorkbook(input);
            }
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
	            Sheet sheet = wb.getSheetAt(i);     //获得第一个表单
	            result.append("<br/>").append(sheet.getSheetName());
	            Iterator<Row> rows = sheet.rowIterator(); //获得第一个表单的迭代器  
	            while (rows.hasNext()) {
	                Row row = rows.next();  //获得行数据  
	                System.out.println("Row #" + row.getRowNum());  //获得行号从0开始
	                result.append("<br/>").append(row.getRowNum()).append(",");
	                Iterator<Cell> cells = row.cellIterator();    //获得第一行的迭代器  
	                while (cells.hasNext()) {
	                    Cell cell = cells.next();
	                    System.out.println("Cell #" + cell.getColumnIndex());
	                    switch (cell.getCellType()) {//根据cell中的类型来输出数据
		                    case HSSFCell.CELL_TYPE_NUMERIC:
		                    	if (HSSFDateUtil.isCellDateFormatted(cell)) {// 处理日期格式、时间格式  
		                            SimpleDateFormat sdf = null;  
		                            if (cell.getCellStyle().getDataFormat() == HSSFDataFormat  
		                                    .getBuiltinFormat("h:mm")) {  
		                                sdf = new SimpleDateFormat("HH:mm");  
		                            } else {// 日期  
		                                sdf = new SimpleDateFormat("yyyy-MM-dd");  
		                            }  
		                            Date date = cell.getDateCellValue();
		                            result.append(sdf.format(date)).append(",");
		                        } else if (cell.getCellStyle().getDataFormat() == 58) {  
		                            // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)  
		                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		                            double value = cell.getNumericCellValue();  
		                            Date date = org.apache.poi.ss.usermodel.DateUtil  
		                                    .getJavaDate(value);  
		                            result.append(sdf.format(date)).append(",");
		                        } else {  
		                            double value = cell.getNumericCellValue();  
		                            CellStyle style = cell.getCellStyle();  
		                            DecimalFormat format = new DecimalFormat();  
		                            String temp = style.getDataFormatString();  
		                            // 单元格设置成常规  
		                            if (temp.equals("General")) {  
		                                format.applyPattern("#");
		                            }
		                            result.append(format.format(value).replaceAll(",", "")).append(",");
		                        }  
		                        break;
		                    case HSSFCell.CELL_TYPE_BLANK:
		                        result.append(" ").append(",");
		                        break;
		                    case HSSFCell.CELL_TYPE_ERROR:
		                        result.append(cell.getErrorCellValue()).append(",");
		                        break;
		                    case HSSFCell.CELL_TYPE_STRING:
		                    	result.append(cell.getStringCellValue()).append(",");
		                        break;  
		                    case HSSFCell.CELL_TYPE_BOOLEAN:
		                        result.append(cell.getBooleanCellValue()).append(",");
		                        break;  
		                    case HSSFCell.CELL_TYPE_FORMULA:
		                        result.append(cell.getCellFormula()).append(",");
		                        break;  
		                    default:  
		                    	result.append(",");
		                    break;
		                }
	                }
	            }
            }
        } catch (IOException ex) {  
            ex.printStackTrace();  
        }
        return result.append("</pre>").toString();
    }
	
	public void readIndexes() {
		try {
			directory = FSDirectory.open(Paths.get(System.getProperty("user.dir") + "\\indexes"));
			DirectoryReader ireader = DirectoryReader.open(directory);
		    IndexSearcher isearcher = new IndexSearcher(ireader);
		    QueryParser parser = new QueryParser("contents", ika);
		    MultiFieldQueryParser mparser = new MultiFieldQueryParser(
		    		new String[]{"contents", "fileName"}, ika);
		    String keyword = "2017";
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
		    		+hitDoc.get("fullPath"));
		    }
		    ireader.close();
		    directory.close();
		    
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ReadFiles rf = new ReadFiles();
		rf.createIndexes(true);
		rf.readIndexes();
		
	}

}
