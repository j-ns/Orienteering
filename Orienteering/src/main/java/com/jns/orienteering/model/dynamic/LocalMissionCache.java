package com.jns.orienteering.model.dynamic;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.util.GluonObservableHelper;

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
            missionTasksTemp = missionTasks;
            return missionTasks;
        }
        selectedMissionId = missionId;

        missionTasks = cloudRepo.retrieveOrderedTasksAsync(missionId);
        missionTasksTemp = null;
        return missionTasks;
    }

    public GluonObservableList<Task> getMissionTasks() {
        return missionTasks;
    }

    public GluonObservableList<Task> getMissionTasksTemp() {
        if (missionTasksTemp == null) {
            missionTasksTemp = GluonObservableHelper.newGluonObservableListInitialized(missionTasks);
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
        missionTasks.set(idx, newTask);
    }

    public void updateMissionTasksFromBuffer() {
        missionTasks.setAll(missionTasksTemp);
    }

    public void removeTask(Task task) {
        if (missionTasks != null) {
            missionTasks.remove(task);
            missionTasksTemp.remove(task);
        }
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
