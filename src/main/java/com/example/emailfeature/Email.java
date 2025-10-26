package com.example.emailfeature;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import org.apache.commons.mail.EmailException;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;


public class Email {

    private String to;
    private String subject;
    private String body;

    public Email(String to, String subject, String body) {
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public static void emailPopup(String recipient, String subject, String body) {
        String message = "📩 Email Preview\n\n" +
                "To: " + recipient + "\n" +
                "Subject: " + subject + "\n\n" +
                body;

        // If running in a Vaadin UI, show a Vaadin Dialog in the UI thread
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                try {
                    Dialog dialog = new Dialog();
                    dialog.setWidth("420px");
                    dialog.setModal(true);

                    dialog.add(new H3("Email Preview - For Testing Purposes"));
                    dialog.add(new Paragraph("To: " + recipient));
                    dialog.add(new Paragraph("Subject: " + subject));

                    Div bodyDiv = new Div();
                    bodyDiv.getStyle().set("white-space", "pre-wrap");
                    bodyDiv.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
                    bodyDiv.getStyle().set("padding", "8px");
                    bodyDiv.setText(body == null ? "" : body);
                    dialog.add(bodyDiv);

                    Button close = new Button("Close", e -> dialog.close());
                    close.getElement().getThemeList().add("primary");
                    dialog.add(close);

                    dialog.open();
                } catch (Exception ex) {
                    // If Vaadin UI rendering fails, fall back to console
                    System.out.println("[Vaadin fallback] " + message);
                }
            });
            return;
        }

        // Se o ambiente for headless (sem display), apenas escreve no STDOUT/LOG
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("[Headless] " + message);
            return;
        }

        // Executa no Event Dispatch Thread para evitar problemas com Swing
        SwingUtilities.invokeLater(() -> {
            try {
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        "Email Confirmation",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (HeadlessException he) {
                // Fallback seguro — escreve no console se algo correr mal
                System.out.println("[Headless fallback] " + message);
            } catch (Exception ex) {
                // Protege contra qualquer outro erro UI
                ex.printStackTrace(System.err);
            }
        });
    }

    public static void emailSend(String description, @Nullable LocalDate dueDate) {
        try {
            org.apache.commons.mail.SimpleEmail email = new org.apache.commons.mail.SimpleEmail();
            email.setHostName("localhost"); // Replace with your SMTP server
            email.setSmtpPort(25);
            email.setAuthentication("system@system.com", "system123");
            email.setSSLOnConnect(true);
            email.setFrom("system@system.com");
            email.addTo("user@user.com");
            email.setSubject("A new task was created");

            StringBuilder body = new StringBuilder("A new task was created");
            if (dueDate != null) {
                body.append(" and set to expire on the ").append(dueDate);
            }
            body.append(". Description: ").append(description);

            email.setMsg(body.toString());
            // email.send(); nao vale a pena enviar mesmo
            emailPopup("user@user.com", "A new task was created", body.toString());

        } catch (EmailException e) {
            System.err.println("Failed to send task creation email");
            e.printStackTrace(System.err);
        }
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
