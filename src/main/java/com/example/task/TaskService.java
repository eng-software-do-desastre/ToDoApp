package com.example.task;

import org.apache.commons.mail.EmailException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Send email after creation
        emailSend(description, dueDate);
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
//            email.send();
        } catch (EmailException e) {
            System.err.println("Failed to send task creation email");
            e.printStackTrace(System.err);
        }
    }

    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

    public List<Task> all() { return taskRepository.findAll(); }
}
