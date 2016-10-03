package com.jns.orienteering.model.dynamic;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.model.repo.TaskFBRepo;

public class LocalTaskCache extends LocalCache<Task> {

    public static final LocalTaskCache INSTANCE = new LocalTaskCache();

    private TaskFBRepo                 cloudRepo;

    private LocalTaskCache() {
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
