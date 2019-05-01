package extlib;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import lotus.domino.Document;

public class ExcelUtils {

	public static HSSFWorkbook createExcel() {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet1 = wb.createSheet("ExportData");
		HSSFCellStyle style = wb.createCellStyle();
		HSSFFont headerFont = wb.createFont();
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(headerFont);
		style.setWrapText(true);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		HSSFRow row = sheet1.createRow(0);
		HSSFCell cell = row.createCell(0);
		cell.setCellStyle(style);
		cell.setCellValue("First Name");
		cell = row.createCell(1);
		cell.setCellStyle(style);
		cell.setCellValue("Last Name");
		cell = row.createCell(2);
		cell.setCellStyle(style);
		cell.setCellValue("Email");
		cell = row.createCell(3);
		cell.setCellStyle(style);
		cell.setCellValue("City");
		cell = row.createCell(4);
		cell.setCellStyle(style);
		cell.setCellValue("State");
		return wb;
	}

	public static HSSFSheet writeContact(HSSFSheet sheet, Document doc, int rownum) {
		try {
			HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
			style.setWrapText(true);
			style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
			HSSFRow row = sheet.createRow(rownum);
			HSSFCell cell = row.createCell(0);
			cell.setCellStyle(style);
			cell.setCellValue(doc.getItemValueString("FirstName"));
			cell = row.createCell(1);
			cell.setCellStyle(style);
			cell.setCellValue(doc.getItemValueString("LastName"));
			cell = row.createCell(2);
			cell.setCellStyle(style);
			cell.setCellValue(doc.getItemValueString("Email"));
			cell = row.createCell(3);
			cell.setCellStyle(style);
			cell.setCellValue(doc.getItemValueString("City"));
			cell = row.createCell(4);
			cell.setCellStyle(style);
			cell.setCellValue(doc.getItemValueString("State"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return sheet;
	}

	public static HSSFSheet finaliseWorkBook(HSSFSheet sheet) {
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);
		// Jump back to first cell
		HSSFRow row = sheet.getRow(0);
		HSSFCell hCell = row.getCell(0);
		hCell.setAsActiveCell();
		return sheet;
	}

}
