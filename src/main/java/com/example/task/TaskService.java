package com.example.task;

import org.apache.commons.mail.EmailException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void createTask(String description, @Nullable LocalDate dueDate) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        var task = new Task(description, Instant.now());
        task.setDueDate(dueDate);
        taskRepository.saveAndFlush(task);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // Chama o metodo que constrói e envia (ou mostra popup)
                    emailSend(description, dueDate);
                }
            });
        } else {
            // Caso não haja sincronização (pouco provável neste contexto), chama directamente
            emailSend(description, dueDate);
        }
    }

    void emailSend(String description, @Nullable LocalDate dueDate) {
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

    void emailPopup(String recipient, String subject, String body) {
        String message = "📩 Email Preview\n\n" +
                "To: " + recipient + "\n" +
                "Subject: " + subject + "\n\n" +
                body;

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


    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

    public List<Task> all() { return taskRepository.findAll(); }
}
