package com.jns.orienteering.model.dynamic;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.model.repo.RepoService;

public class LocalMissionCache extends LocalCache<Mission> {

    public static final LocalMissionCache INSTANCE = new LocalMissionCache();

    private MissionFBRepo                 cloudRepo;

    private String                        selectedMissionId;
    private GluonObservableList<Task>     missionTasks;

    private LocalMissionCache() {
        cloudRepo = RepoService.INSTANCE.getCloudRepo(Mission.class);
    }

    @Override
    protected GluonObservableList<Mission> retrievePrivateItems(String cityId, String userId) {
        return cloudRepo.getPrivateMissions(cityId, userId);
    }

    @Override
    protected GluonObservableList<Mission> retrievePublicItems(String cityId) {
        return cloudRepo.getPublicMissions(cityId);
    }

    public GluonObservableList<Task> retrieveMissionTasksSorted(String missionId) {
        if (!missionId.equals(selectedMissionId)) {
            missionTasks = null;
        }

        if (!isNullOrEmpty(missionTasks)) {
            return missionTasks;
        }
        selectedMissionId = missionId;
        missionTasks = new GluonObservableList<>();

        missionTasks = cloudRepo.retrieveOrderedTasksAsync(missionId);
        return missionTasks;
    }

    public GluonObservableList<Task> getMissionTasks() {
        return missionTasks;
    }

    public void udpateMissionTask(Task newTask, Task previousTask) {
        int idx = missionTasks.indexOf(previousTask);
        missionTasks.set(idx, newTask);
    }

    @Override
    public void removeItem(Mission item) {
        super.removeItem(item);
        if (item.getId().equals(selectedMissionId)) {
            missionTasks = null;
        }
    }
}
