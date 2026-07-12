package com.pinetechs.orvix.ims.android.task.data.dto;

import java.util.List;

public class AppInventoryTaskSliceResponse {
    private Long userId;
    private int assignedTasks;
    private int readyToStartTasks;
    private int inProgressTasks;
    private int completedTasks;
    private TaskSlice tasks;

    public Long getUserId() {
        return userId;
    }

    public int getAssignedTasks() {
        return assignedTasks;
    }

    public int getReadyToStartTasks() {
        return readyToStartTasks;
    }

    public int getInProgressTasks() {
        return inProgressTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public TaskSlice getTasks() {
        return tasks;
    }

    public static class TaskSlice {
        private List<AppInventoryTaskResponse> content;
        private int number;
        private int size;
        private int numberOfElements;
        private boolean last;

        public List<AppInventoryTaskResponse> getContent() {
            return content;
        }

        public int getNumber() {
            return number;
        }

        public int getSize() {
            return size;
        }

        public boolean isLast() {
            return last;
        }
    }
}
