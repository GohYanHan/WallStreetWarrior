import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class Report {
    Database database = new Database();

    public void generateReport() {
        User user = database.getUser();
        if (user != null) {
            String username = user.getUsername();
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = System.getProperty("user.home") + "/Downloads/" + username + "_" + currentTime + ".pdf";

            Document document = new Document();
            try {
                PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
                document.open();

                // Set font styles
                BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                Font titleFont = new Font(baseFont, 14, Font.BOLD | Font.UNDERLINE);
                Font boldFont = new Font(baseFont, 12, Font.BOLD);
                Font tableHeaderFont = new Font(baseFont, 12, Font.BOLD);
                Font tableContentFont = new Font(baseFont, 12);

                // Create title paragraph
                Paragraph title = new Paragraph("User Report", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);

                document.add(new Paragraph(" "));

                // Create user information section with border
                PdfPTable userInfoTable = new PdfPTable(2);
                userInfoTable.setWidthPercentage(100);
                userInfoTable.getDefaultCell().setPadding(10);
                userInfoTable.getDefaultCell().setBorderColor(BaseColor.BLACK);
                userInfoTable.getDefaultCell().setBorderWidth(1);

                userInfoTable.addCell(new Phrase("Username", boldFont));
                userInfoTable.addCell(new Phrase(username, tableContentFont));
                userInfoTable.addCell(new Phrase("Email", boldFont));
                userInfoTable.addCell(new Phrase(user.getEmail(), tableContentFont));
                userInfoTable.addCell(new Phrase("Status", boldFont));
                userInfoTable.addCell(new Phrase(user.getStatus(), tableContentFont));
                userInfoTable.addCell(new Phrase("Balance", boldFont));
                userInfoTable.addCell(new Phrase(String.valueOf(user.getBalance()), tableContentFont));
                userInfoTable.addCell(new Phrase("PL Points", boldFont));
                userInfoTable.addCell(new Phrase(String.valueOf(user.getPL_Points()), tableContentFont));

                document.add(userInfoTable);

                document.add(new Paragraph("\n"));

                // Create holdings section as a table
                Map<Order, Integer> holdings = database.loadHolding(user.getKey());
                PdfPTable holdingsTable = new PdfPTable(2);
                holdingsTable.setWidthPercentage(100);
                holdingsTable.setWidths(new float[]{2, 2});

                PdfPCell holdingsHeaderCell = new PdfPCell(new Phrase("Holdings", boldFont));
                holdingsHeaderCell.setColspan(2);
                holdingsHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                holdingsHeaderCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                holdingsTable.addCell(holdingsHeaderCell);

                PdfPCell stockHeaderCell = new PdfPCell(new Phrase("Stock", tableHeaderFont));
                stockHeaderCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                stockHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                holdingsTable.addCell(stockHeaderCell);

                PdfPCell sharesHeaderCell = new PdfPCell(new Phrase("Shares", tableHeaderFont));
                sharesHeaderCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                sharesHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                holdingsTable.addCell(sharesHeaderCell);

                if (!holdings.isEmpty()) {
                    for (Map.Entry<Order, Integer> entry : holdings.entrySet()) {
                        Order order = entry.getKey();
                        int shares = entry.getValue();

                        PdfPCell stockCell = new PdfPCell(new Phrase(order.getStock().getSymbol(), tableContentFont));
                        stockCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        PdfPCell sharesCell = new PdfPCell(new Phrase(String.valueOf(shares), tableContentFont));
                        sharesCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                        holdingsTable.addCell(stockCell);
                        holdingsTable.addCell(sharesCell);
                    }
                } else {
                    PdfPCell noHoldingsCell = new PdfPCell(new Phrase("No holdings", tableContentFont));
                    noHoldingsCell.setColspan(2);
                    noHoldingsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    holdingsTable.addCell(noHoldingsCell);
                }

                document.add(holdingsTable);


                document.add(new Paragraph("\n"));

                // Create trade history section as a table
                List<Order> tradeHistory = database.loadTransactionHistory(user.getKey());
                PdfPTable tradeHistoryTable = new PdfPTable(5);
                tradeHistoryTable.setWidthPercentage(100);
                float[] columnWidths = {3, 2, 2, 2, 2};
                tradeHistoryTable.setWidths(columnWidths);

                PdfPCell tradeHistoryHeaderCell = new PdfPCell(new Phrase("Trade History", boldFont));
                tradeHistoryHeaderCell.setColspan(5);
                tradeHistoryHeaderCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                tradeHistoryHeaderCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                tradeHistoryTable.addCell(tradeHistoryHeaderCell);

// Set cell alignment for table content
                tradeHistoryTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

                if (!tradeHistory.isEmpty()) {
                    PdfPCell timestampCell = new PdfPCell(new Phrase("Timestamp", tableHeaderFont));
                    PdfPCell stockCell = new PdfPCell(new Phrase("Stock", tableHeaderFont));
                    PdfPCell typeCell = new PdfPCell(new Phrase("Type", tableHeaderFont));
                    PdfPCell sharesCell = new PdfPCell(new Phrase("Shares", tableHeaderFont));
                    PdfPCell priceCell = new PdfPCell(new Phrase("Price", tableHeaderFont));

                    // Set cell alignment for header cells
                    timestampCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    stockCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    typeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    sharesCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    priceCell.setHorizontalAlignment(Element.ALIGN_CENTER);

                    tradeHistoryTable.addCell(timestampCell);
                    tradeHistoryTable.addCell(stockCell);
                    tradeHistoryTable.addCell(typeCell);
                    tradeHistoryTable.addCell(sharesCell);
                    tradeHistoryTable.addCell(priceCell);

                    // Set cell alignment for content cells
                    tradeHistoryTable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);

                    for (Order order : tradeHistory) {
                        tradeHistoryTable.addCell(new Phrase(String.valueOf(order.getTimestamp()), tableContentFont));
                        tradeHistoryTable.addCell(new Phrase(order.getStock().getSymbol(), tableContentFont));
                        tradeHistoryTable.addCell(new Phrase(order.getType().toString(), tableContentFont));
                        tradeHistoryTable.addCell(new Phrase(String.valueOf(order.getShares()), tableContentFont));
                        tradeHistoryTable.addCell(new Phrase(String.valueOf(order.getType() == Order.Type.BUY ? order.getExpectedBuyingPrice() : order.getExpectedSellingPrice()), tableContentFont));
                    }
                } else {
                    PdfPCell noTradeHistoryCell = new PdfPCell(new Phrase("No trade history", tableContentFont));
                    noTradeHistoryCell.setColspan(5);
                    noTradeHistoryCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tradeHistoryTable.addCell(noTradeHistoryCell);
                }

                document.add(tradeHistoryTable);


                document.close();
                writer.close();

                System.out.println("User report generated successfully.\n");
            } catch (DocumentException | IOException e) {
                System.out.println("An error occurred while generating the user report.");
                e.printStackTrace();
            }
        } else {
            System.out.println("User data not found in the database. Please try again later.\n");
        }
    }
}
