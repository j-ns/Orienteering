package com.jns.orienteering.model.dynamic;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.MissionFBRepo;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.util.GluonObservableHelper;
import com.jns.orienteering.util.Validators;

import javafx.collections.transformation.SortedList;

public class LocalMissionCache extends LocalCache<Mission> {

    public static final LocalMissionCache INSTANCE = new LocalMissionCache();

    private MissionFBRepo                 cloudRepo;

    // private ObservableList<Task> activeTasksBacking;
    private GluonObservableList<Task>     activeTasksSorted;

    private String                        selectedMissionId;

    private LocalMissionCache() {
        cloudRepo = RepoService.INSTANCE.getCloudRepo(Mission.class);
    }

    @Override
    protected GluonObservableList<Mission> retrievePrivateList(String cityId, String userId) {
        return cloudRepo.getPrivateMissions(cityId, userId);
    }

    @Override
    protected GluonObservableList<Mission> retrievePublicList(String cityId) {
        return cloudRepo.getPublicMissions(cityId);
    }

    public GluonObservableList<Task> retrieveMissionTasksSorted(String missionId) {
        if (!missionId.equals(selectedMissionId)) {
            // activeTasksBacking = null;
            activeTasksSorted = null;
        }

        if (!Validators.isNullOrEmpty(activeTasksSorted)) {
            return activeTasksSorted;
        }
        // activeTasksBacking= new GluonObservableList<>();
        activeTasksSorted = new GluonObservableList<>();

        GluonObservableList<Task> obsTasks = cloudRepo.retrieveOrderedTasksAsync(missionId);
        AsyncResultReceiver.create(obsTasks)
                           .onSuccess(result ->
                           {
                               // activeTasksBacking.setAll(result);
                               SortedList<Task> sortedList = new SortedList<>(result, (Task t, Task t1) -> Integer.compare(t
                                                                                                                            .getOrderNumber(),
                                                                                                                           t1.getOrderNumber()));
                               activeTasksSorted.setAll(sortedList);
                               GluonObservableHelper.setInitialized(activeTasksSorted, true);

                           })
                           .propagateException(activeTasksSorted)
                           .start();

        selectedMissionId = missionId;
        return activeTasksSorted;
    }

    public GluonObservableList<Task> getActiveTasksSorted() {
        return activeTasksSorted;
    }

    @Override
    public void removeItem(Mission item) {
        super.removeItem(item);
        if (item.getId().equals(selectedMissionId)) {
            activeTasksSorted = null;
        }
    }
}
