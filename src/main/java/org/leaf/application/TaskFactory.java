package org.leaf.application;

/**
 * Factory class for creating Tasks.
 *
 * It takes care that each VM has a unique ID, which is required by CloudSim.
 */
public class TaskFactory {

    private static TaskFactory factory;
    private int nextTaskId;

    private TaskFactory() {
        nextTaskId = 0;
    }

    public static Task createTask(int mips) {
        if(factory == null)
            factory = new TaskFactory();
        Task task = new Task(factory.getNextTaskId(), mips, 1);
        task.setRam(0);
        task.setSize(0);
        task.setBw(0);
        return task;
    }

    public int getNextTaskId() {
        return nextTaskId++;
    }

    public static int createdEntities() {
        if(factory == null) return 0;
        return factory.getNextTaskId() -1;
    }
}
