package com.smartinventory.service;

import com.smartinventory.model.Product;
import com.smartinventory.repository.ProductRepository;
import com.smartinventory.repository.ReportRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportService {
    private final ProductRepository productRepository = new ProductRepository();
    private final ReportRepository reportRepository = new ReportRepository();

    public Path exportTextPdf() throws Exception {
        Files.createDirectories(Path.of("exports"));
        Path path = Path.of("exports", "inventory-report.pdf");
        writePdf(path);
        return path.toAbsolutePath();
    }

    public Path exportExcel() throws Exception {
        Files.createDirectories(Path.of("exports"));
        Path path = Path.of("exports", "inventory-report.xlsx");
        List<Product> products = productRepository.findAll("", null);
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            entry(zip, "[Content_Types].xml", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
                    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
                    <Default Extension="xml" ContentType="application/xml"/>
                    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
                    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
                    </Types>
                    """);
            entry(zip, "_rels/.rels", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
                    </Relationships>
                    """);
            entry(zip, "xl/workbook.xml", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
                    <sheets><sheet name="Inventory" sheetId="1" r:id="rId1"/></sheets></workbook>
                    """);
            entry(zip, "xl/_rels/workbook.xml.rels", """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
                    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
                    </Relationships>
                    """);
            entry(zip, "xl/worksheets/sheet1.xml", worksheet(products));
        }
        return path.toAbsolutePath();
    }

    private void writePdf(Path path) throws Exception {
        List<Product> products = productRepository.findAll("", null);
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, Files.newOutputStream(path));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);

        Paragraph title = new Paragraph("Smart Inventory System - Inventory Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph generated = new Paragraph("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), subtitleFont);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(16);
        document.add(generated);

        PdfPTable kpis = new PdfPTable(5);
        kpis.setWidthPercentage(100);
        kpis.setSpacingAfter(16);
        addKpi(kpis, "Total Products", String.valueOf(reportRepository.totalProducts()));
        addKpi(kpis, "Low Stock", String.valueOf(reportRepository.lowStockProducts()));
        addKpi(kpis, "Out of Stock", String.valueOf(reportRepository.outOfStockProducts()));
        addKpi(kpis, "Suppliers", String.valueOf(reportRepository.totalSuppliers()));
        addKpi(kpis, "Users", String.valueOf(reportRepository.totalUsers()));
        document.add(kpis);

        document.add(section("Category Statistics", headingFont));
        PdfPTable categories = new PdfPTable(2);
        categories.setWidthPercentage(45);
        categories.setHorizontalAlignment(Element.ALIGN_LEFT);
        header(categories, "Category");
        header(categories, "Products");
        for (Map.Entry<String, Integer> entry : reportRepository.byCategory().entrySet()) {
            cell(categories, entry.getKey());
            cell(categories, String.valueOf(entry.getValue()));
        }
        categories.setSpacingAfter(16);
        document.add(categories);

        document.add(section("Product Inventory", headingFont));
        PdfPTable table = new PdfPTable(new float[]{0.7f, 2.5f, 1.5f, 2.2f, 1.0f, 1.0f, 1.4f});
        table.setWidthPercentage(100);
        header(table, "ID");
        header(table, "Name");
        header(table, "Category");
        header(table, "Supplier");
        header(table, "Qty");
        header(table, "Price");
        header(table, "Status");
        for (Product product : products) {
            cell(table, String.valueOf(product.getId()));
            cell(table, product.getName());
            cell(table, product.getCategory());
            cell(table, product.getSupplier());
            cell(table, String.valueOf(product.getQuantity()));
            cell(table, product.getPrice().toPlainString());
            cell(table, product.getStatus());
        }
        document.add(table);
        document.close();
    }

    private Paragraph section(String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        paragraph.setSpacingBefore(6);
        paragraph.setSpacingAfter(8);
        return paragraph;
    }

    private void addKpi(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setPhrase(new Phrase(label + "\n" + value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void header(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private void cell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String reportText() throws Exception {
        StringBuilder text = new StringBuilder("Smart Inventory Report\\n");
        text.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\\n\\n");
        text.append("Total products: ").append(reportRepository.totalProducts()).append("\\n");
        text.append("Low stock: ").append(reportRepository.lowStockProducts()).append("\\n");
        text.append("Out of stock: ").append(reportRepository.outOfStockProducts()).append("\\n");
        text.append("Total suppliers: ").append(reportRepository.totalSuppliers()).append("\\n\\n");
        for (Map.Entry<String, Integer> entry : reportRepository.byCategory().entrySet()) {
            text.append(entry.getKey()).append(": ").append(entry.getValue()).append("\\n");
        }
        text.append("\\nProducts\\n");
        for (Product product : productRepository.findAll("", null)) {
            text.append(product.getId()).append(" | ").append(product.getName()).append(" | ").append(product.getCategory())
                    .append(" | ").append(product.getSupplier()).append(" | ").append(product.getQuantity())
                    .append(" | ").append(product.getPrice()).append(" | ").append(product.getStatus()).append("\\n");
        }
        return text.toString();
    }

    private String worksheet(List<Product> products) {
        StringBuilder rows = new StringBuilder();
        rows.append(row(1, "ID", "Name", "Category", "Supplier", "Quantity", "Price", "Status"));
        int index = 2;
        for (Product product : products) {
            rows.append(row(index++, String.valueOf(product.getId()), product.getName(), product.getCategory(), product.getSupplier(),
                    String.valueOf(product.getQuantity()), product.getPrice().toPlainString(), product.getStatus()));
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData>"
                + rows + "</sheetData></worksheet>";
    }

    private String row(int rowNumber, String... values) {
        StringBuilder row = new StringBuilder("<row r=\"").append(rowNumber).append("\">");
        for (String value : values) {
            row.append("<c t=\"inlineStr\"><is><t>").append(xml(value)).append("</t></is></c>");
        }
        return row.append("</row>").toString();
    }

    private String xml(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void entry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
