package com.example.pdf;

import com.example.task.Task;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.util.List;

public class Writer {
    private List<Task> tasks;

    public Writer(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void write() {
        Font f = FontFactory.getFont(FontFactory.HELVETICA, 16, BaseColor.BLACK);
        Document doc = new Document();

        try {
            PdfWriter.getInstance(doc, new FileOutputStream("export.pdf"));

            doc.open();
        } catch (Exception e) {
            System.out.println("Error opening export.pdf");
            return;
        }

        System.out.println("Numero de tarefas");
        System.out.println(this.tasks.size());
        try {
            doc.add(new Paragraph("HEADER\n", f));
        } catch (DocumentException e) {
            System.out.println("Error adding header");
        }

        for(Task task : this.tasks) {
            try {
                if(!doc.add(new Paragraph(task.getDescription(), f))) break;
            } catch (DocumentException e) {
                System.out.println("Error writing export.pdf");
            }
        }

        doc.close();
    }
}
