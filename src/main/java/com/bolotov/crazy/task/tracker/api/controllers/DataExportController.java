package com.bolotov.crazy.task.tracker.api.controllers;


import com.bolotov.crazy.task.tracker.api.controllers.helpers.ControllerHelper;
import com.bolotov.crazy.task.tracker.api.dto.TaskDto;
import com.bolotov.crazy.task.tracker.api.factories.TaskDtoFactory;
import com.bolotov.crazy.task.tracker.store.entities.ProjectEntity;
import com.bolotov.crazy.task.tracker.store.entities.TaskStateEntity;
import com.bolotov.crazy.task.tracker.store.repositories.TaskRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class DataExportController {

    TaskController taskController;

    public static final String EXPORT_TASKS_JSON = "/api/export/json/projects/{project_id}/task-states/{task_state_id}" +
            "/tasks/filename/{filename}";
    public static final String EXPORT_TASKS_CSV = "/api/export/csv/projects/{project_id}/task-states/{task_state_id}" +
            "/tasks/filename/{filename}";

    @GetMapping(EXPORT_TASKS_JSON)
    @Operation(summary = "Выгрузка задач в JSON")
    public String exportTasksJson(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "task_state_id") Long taskStateId,
            @PathVariable(name = "filename") String filename) {

        List<TaskDto> tasks = taskController.getTasks(projectId, taskStateId);

        return exportDataToJson(filename, tasks);
    }

    @GetMapping(EXPORT_TASKS_CSV)
    @Operation(summary = "Выгрузка задач в CSV")
    public String exportTasksCsv(
            @PathVariable(name = "project_id") Long projectId,
            @PathVariable(name = "task_state_id") Long taskStateId,
            @PathVariable(name = "filename") String filename) {

        List<TaskDto> tasks = taskController.getTasks(projectId, taskStateId);

        return exportDataToCsv(filename, tasks);
    }

    private String exportDataToJson(String filename, List<TaskDto> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(list);

        String exportFilename = filename + ".json";

        try (FileWriter fileWriter = new FileWriter(exportFilename)) {
            fileWriter.write(json);
            return "File \"" + exportFilename + "\" created and written successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "File \"" + exportFilename + "\" not created.";
        }
    }

    private String exportDataToCsv(String filename, List<TaskDto> list) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(list);

        String exportFilename = filename + ".csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(exportFilename))) {
            String[] header = {"id", "name", "description"};
            writer.writeNext(header);

            for (TaskDto task : list) {
                String[] data = {String.valueOf(task.getId()), task.getName(), task.getDescription()};
                writer.writeNext(data);
            }

            return "File \"" + exportFilename + "\" created and written successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "File \"" + exportFilename + "\" not created.";
        }
    }
}

