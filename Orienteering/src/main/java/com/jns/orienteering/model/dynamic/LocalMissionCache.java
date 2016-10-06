package com.jns.orienteering.model.dynamic;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.util.GluonObservables;

public class LocalMissionCache extends LocalCache<Mission> {

    public static final LocalMissionCache INSTANCE = new LocalMissionCache();

    private MissionFBRepo                 cloudRepo;

    private String                        selectedMissionId;
    private GluonObservableList<Task>     missionTasks;
    private GluonObservableList<Task>     missionTasksTemp;

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
            clearMissionTasks();
        }

        if (!isNullOrEmpty(missionTasks)) {
            return missionTasks;
        }

        missionTasksTemp = null;
        missionTasks = cloudRepo.retrieveOrderedTasksAsync(missionId);
        selectedMissionId = missionId;
        return missionTasks;
    }

    public GluonObservableList<Task> getMissionTasks() {
        return missionTasks;
    }

    public GluonObservableList<Task> getMissionTasksTemp() {
        if (isNullOrEmpty(missionTasksTemp)) {
            missionTasksTemp = GluonObservables.newListInitialized(missionTasks);
        }
        return missionTasksTemp;
    }

    public void removeMissionAndTasks(Mission mission) {
        removeItem(mission);
        missionTasks = null;
        missionTasksTemp = null;
    }

    public void udpateMissionTask(Task newTask, Task previousTask) {
        int idx = missionTasks.indexOf(previousTask);
        if (idx > -1) {
            missionTasks.set(idx, newTask);
        }

        int idxTemp = missionTasksTemp.indexOf(previousTask);
        if (idxTemp > -1) {
            missionTasksTemp.set(idxTemp, newTask);
        }
    }

    public void updateMissionTasksWithBuffer() {
        missionTasks.setAll(missionTasksTemp);
    }

    public void removeTask(Task task) {
        if (missionTasks != null) {
            missionTasks.remove(task);
        }
        if (missionTasksTemp != null) {
            missionTasksTemp.remove(task);
        }
    }

    @Override
    public void clearItems() {
        clearPrivateItems();
        clearPublicItems();
        clearMissionTasks();
    }

    @Override
    protected void clearPrivateItems() {
        super.clearPrivateItems();
        clearMissionTasks();
    }

    @Override
    protected void clearPublicItems() {
        super.clearPublicItems();
        clearMissionTasks();
    }

    public void clearMissionTasks() {
        missionTasks = new GluonObservableList<>();
        missionTasksTemp = new GluonObservableList<>();
    }
}
