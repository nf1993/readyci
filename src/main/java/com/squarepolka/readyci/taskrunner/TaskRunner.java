package com.squarepolka.readyci.taskrunner;

import com.squarepolka.readyci.tasks.Task;
import com.squarepolka.readyci.tasks.TaskExecuteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunner.class);

    protected List<Task> tasks;
    protected BuildEnvironment buildEnvironment;

    public TaskRunner(BuildEnvironment buildEnvironment) {
        this.tasks = new ArrayList<Task>();
        this.buildEnvironment = buildEnvironment;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void runTasks() {
        try {
            LOGGER.info(String.format("EXECUTING\tBUILD\t%s\t(%s)", buildEnvironment.pipelineName, buildEnvironment.buildUUID));
            checkThatTasksExist();
            runEachTask();
            LOGGER.info(String.format("COMPLETED\tBUILD\t%s\t(%s)", buildEnvironment.pipelineName, buildEnvironment.buildUUID));
        } catch (RuntimeException e) {
            LOGGER.info(String.format("FAILED\tBUILD\t%s\t(%s)", buildEnvironment.pipelineName, buildEnvironment.buildUUID));
            throw e;
        }
    }

    private void checkThatTasksExist() {
        if (tasks.size() <= 0) {
            throw new RuntimeException("There are no tasks to run. Add some tasks and then try again.");
        }
    }

    private void runEachTask() {
        for (Task task : tasks) {
            try {
                runTask(task);
            } catch (Exception e) {
                handleTaskFailure(task, e);
            }
        }
    }

    private void runTask(Task task) throws Exception {
        LOGGER.info(String.format("RUNNING\tTASK\t%s\t%s", task.taskIdentifier(), task.description));
        task.performTask(buildEnvironment);
    }

    private void handleTaskFailure(Task task, Exception e) {
        String errorMessage = String.format("FAILED\tTASK\t%s with exception: %s", task.taskIdentifier(), e.toString());
        LOGGER.error(errorMessage);
        if (task.shouldStopOnFailure()) {
            TaskExecuteException taskExecuteException = new TaskExecuteException(errorMessage);
            taskExecuteException.setStackTrace(e.getStackTrace());
            throw taskExecuteException;
        }
    }

}
