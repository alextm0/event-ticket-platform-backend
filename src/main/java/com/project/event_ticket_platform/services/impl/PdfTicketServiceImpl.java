package com.project.event_ticket_platform.services.impl;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.project.event_ticket_platform.dtos.TicketResponse;
import com.project.event_ticket_platform.services.PdfTicketService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class PdfTicketServiceImpl implements PdfTicketService {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

	@Override
	public ByteArrayOutputStream generateTicketPdf(TicketResponse ticket, byte[] qrCodeImage) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			PdfWriter writer = new PdfWriter(out);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);

			// Set margins
			document.setMargins(20, 20, 20, 20);

			// Header - Event Name
			Paragraph header = new Paragraph(ticket.eventTitle())
					.setFontSize(24)
					.setBold()
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginBottom(10);
			document.add(header);

			// Ticket ID
			Paragraph ticketId = new Paragraph("Ticket ID: " + ticket.id())
					.setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginBottom(20);
			document.add(ticketId);

			// Main content table (2 columns)
			Table mainTable = new Table(2);
			mainTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

			// Left column - Ticket details
			Cell leftCell = new Cell();
			leftCell.setBorder(Border.NO_BORDER);
			leftCell.setPadding(10);

			// Event details
			addDetailRow(leftCell, "Event:", ticket.eventTitle());
			addDetailRow(leftCell, "Location:", ticket.eventLocation());
			addDetailRow(leftCell, "Date & Time:", DATE_FORMATTER.format(ticket.eventStartTime().atZone(ZoneId.systemDefault())));
			addDetailRow(leftCell, "Ticket Type:", ticket.ticketTypeName());
			addDetailRow(leftCell, "Status:", ticket.status().toString());

			mainTable.addCell(leftCell);

			// Right column - QR Code
			Cell rightCell = new Cell();
			rightCell.setBorder(Border.NO_BORDER);
			rightCell.setPadding(10);
			rightCell.setTextAlignment(TextAlignment.CENTER);
			rightCell.setHorizontalAlignment(HorizontalAlignment.CENTER);

			if (qrCodeImage != null && qrCodeImage.length > 0) {
				ImageData imageData = ImageDataFactory.create(qrCodeImage);
				Image qrImage = new Image(imageData);
				qrImage.setWidth(150);
				qrImage.setHeight(150);
				rightCell.add(new Paragraph("Scan to Validate").setFontSize(10).setTextAlignment(TextAlignment.CENTER));
				rightCell.add(qrImage);
			}

			mainTable.addCell(rightCell);
			document.add(mainTable);

			// Footer section
			document.add(new Paragraph("\n"));
			Table footerTable = new Table(1);
			footerTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

			Cell footerCell = new Cell();
			footerCell.setBorder(Border.NO_BORDER);
			footerCell.add(new Paragraph("This ticket is valid for one entry only.")
					.setFontSize(9)
					.setTextAlignment(TextAlignment.CENTER)
					.setItalic());
			footerCell.add(new Paragraph("Please keep this ticket safe and bring it to the event.")
					.setFontSize(9)
					.setTextAlignment(TextAlignment.CENTER)
					.setItalic());
			footerTable.addCell(footerCell);
			document.add(footerTable);

			document.close();
		} catch (Exception e) {
			throw new RuntimeException("Error generating PDF ticket: " + e.getMessage(), e);
		}

		return out;
	}

	private void addDetailRow(Cell cell, String label, String value) {
		Table detailTable = new Table(2);
		detailTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

		// Label cell
		Cell labelCell = new Cell();
		labelCell.setBorder(Border.NO_BORDER);
		labelCell.setPadding(5);
		labelCell.add(new Paragraph(label).setBold().setFontSize(11));
		detailTable.addCell(labelCell);

		// Value cell
		Cell valueCell = new Cell();
		valueCell.setBorder(Border.NO_BORDER);
		valueCell.setPadding(5);
		valueCell.add(new Paragraph(value != null ? value : "N/A").setFontSize(11));
		detailTable.addCell(valueCell);

		cell.add(detailTable);
	}
}

