package extlib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.domino.services.HttpServiceConstants;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.http.io.output.ByteArrayOutputStream;
import com.ibm.xsp.webapp.XspHttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.Session;
import lotus.domino.Stream;

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

	public static void exportSelectedContacts() {
		try {
			Map<String, Object> sessionScope = ExtLibUtil.getSessionScope();
			HSSFWorkbook wb = createExcel();

			HSSFSheet sheet = wb.getSheetAt(0);
			String[] ids = (String[]) sessionScope.get("ids");
			Database db = ExtLibUtil.getCurrentDatabase();
			int rownum = 1;
			for (String id : ids) {
				Document doc = db.getDocumentByID(id);
				writeContact(sheet, doc, rownum);
				rownum += rownum;
			}

			finaliseWorkBook(sheet);

			// Write out to servletResponse
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			HttpServletResponse pageResponse = (HttpServletResponse) ec.getResponse();
			ServletOutputStream pageOutput = pageResponse.getOutputStream();
			pageResponse.setContentType("application/x-ms-excel");
			pageResponse.setHeader("Cache-Control", "no-cache");
			pageResponse.setHeader("Content-Disposition", "inline; filename=monthlyReport.xls");
			wb.write(pageOutput);
			pageOutput.flush();
			pageOutput.close();

			//  Terminate the request processing lifecycle.
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void emailSelectedContacts() {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ext = ctx.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) ext.getRequest();
		XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
		try {

			if (request.getMethod() != HttpServiceConstants.HTTP_POST) {
				response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			} else if (null == request.getHeader("secretKey")) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if (!"d1p243eafde5272".equals(request.getHeader("secretKey"))) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if (null == request.getHeader("sendTo")) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else {
				InputStreamReader isR = new InputStreamReader(request.getInputStream());
				JsonJavaObject jsonObj = (JsonJavaObject) JsonParser.fromJson(JsonJavaFactory.instanceEx, isR);
				JsonJavaArray ids = jsonObj.getAsArray("data");

				response.setContentType("application/json");
				response.setHeader("Cache-Control", "no-cache");

				HSSFWorkbook wb = createExcel();

				HSSFSheet sheet = wb.getSheetAt(0);
				Database db = ExtLibUtil.getCurrentDatabase();
				int rownum = 1;
				for (Object id : ids) {
					Document doc = db.getDocumentByUNID((String) id);
					writeContact(sheet, doc, rownum);
					rownum += rownum;
				}

				finaliseWorkBook(sheet);

				ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
				wb.write(os);

				Session session = ExtLibUtil.getCurrentSessionAsSigner();
				session.setConvertMime(false);
				Database mailBox = session.getDatabase(session.getServerName(), "mail.box");
				Document memo = mailBox.createDocument();
				memo.replaceItemValue("RecNoOutOfOffice", "1"); //no replies from out of office agents
				String mimeBoundary = memo.getUniversalID().toLowerCase();

				MIMEEntity mimeRoot = memo.createMIMEEntity("Body");

				MIMEHeader mimeHeader;
				mimeHeader = mimeRoot.createHeader("To");
				mimeHeader.setHeaderVal(request.getHeader("sendTo"));

				//set subject
				mimeHeader = mimeRoot.createHeader("Subject");
				mimeHeader.addValText("Contacts Export", "UTF-8");

				MIMEEntity emailRootChild = mimeRoot.createChildEntity();
				mimeHeader = emailRootChild.createHeader("Content-Type");

				mimeHeader = emailRootChild.createHeader("Content-Disposition");
				mimeHeader.setHeaderVal("attachment; filename=\"Contacts.xls\"");
				InputStream is = new ByteArrayInputStream(os.toByteArray());
				Stream stream = session.createStream();
				stream.setContents(is);
				emailRootChild.setContentFromBytes(stream, "application/vnd.ms-excel", MIMEEntity.ENC_IDENTITY_BINARY);
				memo.closeMIMEEntities(true);
				memo.send();

				session.setConvertMime(true);

				response.setStatus(HttpServletResponse.SC_OK);
			}

			//  Terminate the request processing lifecycle.
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Throwable t) {
			t.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			FacesContext.getCurrentInstance().responseComplete();
		}
	}

}
