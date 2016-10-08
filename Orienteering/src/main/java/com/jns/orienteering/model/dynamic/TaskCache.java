package com.jns.orienteering.model.dynamic;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.model.repo.TaskFBRepo;

public class TaskCache extends ModelCache<Task> {

    public static final TaskCache INSTANCE = new TaskCache();

    private TaskFBRepo            cloudRepo;

    private TaskCache() {
        cloudRepo = RepoService.INSTANCE.getCloudRepo(Task.class);
    }

    @Override
    protected GluonObservableList<Task> retrievePrivateItems(String cityId, String userId) {
        return cloudRepo.getPrivateTasksAsync(cityId, userId);
    }

    @Override
    protected GluonObservableList<Task> retrievePublicItems(String cityId) {
        return cloudRepo.getPublicTasksAsync(cityId);
    }

}
