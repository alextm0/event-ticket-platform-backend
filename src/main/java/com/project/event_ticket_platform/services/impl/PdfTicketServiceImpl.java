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
					.setFontSize(26)
					.setBold()
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginBottom(5);
			document.add(header);

			// Ticket ID
			Paragraph ticketIdPara = new Paragraph("Ticket ID: " + ticket.id())
					.setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER)
					.setMarginBottom(20);
			document.add(ticketIdPara);

			// Main content table (2 columns)
			Table mainTable = new Table(new float[]{1.2f, 1});
			mainTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

			// Left column - Ticket details
			Cell leftCell = new Cell();
			leftCell.setBorder(Border.NO_BORDER);
			leftCell.setPadding(15);

			// Event details with better alignment
			addDetailRow(leftCell, "Event", ticket.eventTitle());
			addDetailRow(leftCell, "Location", ticket.eventLocation());
			addDetailRow(leftCell, "Date & Time", DATE_FORMATTER.format(ticket.eventStartTime().atZone(ZoneId.systemDefault())));
			addDetailRow(leftCell, "Ticket Type", ticket.ticketTypeName());
			addDetailRow(leftCell, "Status", ticket.status().toString());

			mainTable.addCell(leftCell);

			// Right column - QR Code
			Cell rightCell = new Cell();
			rightCell.setBorder(Border.NO_BORDER);
			rightCell.setPadding(15);
			rightCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);

			if (qrCodeImage != null && qrCodeImage.length > 0) {
				ImageData imageData = ImageDataFactory.create(qrCodeImage);
				Image qrImage = new Image(imageData);
				qrImage.setWidth(140);
				qrImage.setHeight(140);
				rightCell.add(new Paragraph("Scan to Validate")
						.setFontSize(11)
						.setBold()
						.setTextAlignment(TextAlignment.CENTER)
						.setMarginBottom(10));
				rightCell.add(qrImage);
			}

			mainTable.addCell(rightCell);
			document.add(mainTable);

			// Divider line
			document.add(new Paragraph("\n"));

			// Footer section
			Table footerTable = new Table(1);
			footerTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));

			Cell footerCell = new Cell();
			footerCell.setBorder(Border.NO_BORDER);
			footerCell.setPadding(10);
			footerCell.add(new Paragraph("✓ This ticket is valid for one entry only.")
					.setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER));
			footerCell.add(new Paragraph("✓ Please keep this ticket safe and bring it to the event.")
					.setFontSize(10)
					.setTextAlignment(TextAlignment.CENTER));
			footerTable.addCell(footerCell);
			document.add(footerTable);

			document.close();
		} catch (Exception e) {
			throw new RuntimeException("Error generating PDF ticket: " + e.getMessage(), e);
		}

		return out;
	}

	private void addDetailRow(Cell cell, String label, String value) {
		// Create a table with fixed column widths for proper alignment
		Table detailTable = new Table(new float[]{0.35f, 0.65f});
		detailTable.setWidth(com.itextpdf.layout.properties.UnitValue.createPercentValue(100));
		detailTable.setMarginBottom(8);

		// Label cell - left aligned with bold
		Cell labelCell = new Cell();
		labelCell.setBorder(Border.NO_BORDER);
		labelCell.setPadding(8);
		labelCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
		labelCell.add(new Paragraph(label)
				.setBold()
				.setFontSize(12)
				.setTextAlignment(TextAlignment.LEFT));
		detailTable.addCell(labelCell);

		// Value cell - left aligned
		Cell valueCell = new Cell();
		valueCell.setBorder(Border.NO_BORDER);
		valueCell.setPadding(8);
		valueCell.setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE);
		valueCell.add(new Paragraph(value != null ? value : "N/A")
				.setFontSize(12)
				.setTextAlignment(TextAlignment.LEFT));
		detailTable.addCell(valueCell);

		cell.add(detailTable);
	}
}

