/**
 *
 *  Copyright (c) 2016, Jens Stroh
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL JENS STROH BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jns.orienteering.common;

import static com.jns.orienteering.locale.Localization.localize;

import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.connect.ConnectState;
import com.gluonhq.connect.GluonObservableList;
import com.gluonhq.connect.GluonObservableObject;
import com.jns.orienteering.control.Dialogs;
import com.jns.orienteering.model.dynamic.CityCache;
import com.jns.orienteering.model.dynamic.MissionCache;
import com.jns.orienteering.model.persisted.ActiveTaskList;
import com.jns.orienteering.model.persisted.City;
import com.jns.orienteering.model.persisted.Mission;
import com.jns.orienteering.model.persisted.MissionStat;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.persisted.User;
import com.jns.orienteering.model.repo.AsyncResultReceiver;
import com.jns.orienteering.model.repo.LocalRepo;
import com.jns.orienteering.model.repo.RepoService;
import com.jns.orienteering.model.repo.UserFBRepo;
import com.jns.orienteering.model.repo.image.ImageHandler;
import com.jns.orienteering.model.repo.image.StorableImage;
import com.jns.orienteering.model.repo.synchronizer.ActiveMissionSynchronizer;
import com.jns.orienteering.model.repo.synchronizer.CitySynchronizer;
import com.jns.orienteering.model.repo.synchronizer.ImageSynchronizer;
import com.jns.orienteering.model.repo.synchronizer.RepoSynchronizer;
import com.jns.orienteering.model.repo.synchronizer.SyncMetaData;
import com.jns.orienteering.platform.PlatformProvider;
import com.jns.orienteering.view.ViewRegistry;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class BaseService {

    private static final Logger             LOGGER            = LoggerFactory.getLogger(BaseService.class);

    private RepoService                     repoService;
    private ActivatorDeactivatorService     activatorDeactivatorService;

    private RepoSynchronizer                repoSynchronizer  = new RepoSynchronizer();

    private UserFBRepo                      userCloudRepo;
    private LocalRepo<User, User>           userLocalRepo;

    private LocalRepo<Task, ActiveTaskList> activeTasksLocalRepo;

    private boolean                         userListenerActive;

    private ObjectProperty<User>            user              = new SimpleObjectProperty<>();
    private StringProperty                  alias             = new SimpleStringProperty();
    private ObjectProperty<Image>           profileImage      = new SimpleObjectProperty<>();

    private CityCache                       cityCache         = CityCache.INSTANCE;
    private ObjectProperty<City>            defaultCity       = new SimpleObjectProperty<>();
    private ObjectProperty<City>            selectedCity      = new SimpleObjectProperty<>();
    private CityBuffer                      cityBuffer;

    private MissionCache                    missionCache      = MissionCache.INSTANCE;
    private ObjectProperty<Mission>         activeMission     = new SimpleObjectProperty<>();
    private StringProperty                  activeMissionName = new SimpleStringProperty();

    private ObjectProperty<Mission>         selectedMission   = new SimpleObjectProperty<>();
    private Task                            selectedTask;

    private BooleanProperty                 stopMission;
    private MissionStat                     activeMissionStats;

    private String                          previousViewName;

    private BooleanProperty                 initialized       = new SimpleBooleanProperty(false);

    public BaseService() {
        MobileApplication.getInstance().viewProperty().addListener((obsValue, v, v1) ->
        {
            if (v != null) {
                previousViewName = v.getName();
                if (ViewRegistry.HOME.equals(v1.getName())) {
                    PlatformProvider.getPlatformService().removeNodePositionAdjuster();
                    setSelectedMission(null);
                }
            }
        });

        repoSynchronizer.syncStateProperty().addListener((obsValue, st, st1) ->
        {
            LOGGER.debug("syncState: {}", st1);
            if (st1 == ConnectState.SUCCEEDED || st1 == ConnectState.FAILED) {
                initialized.set(true);
            }
        });

        activatorDeactivatorService = new ActivatorDeactivatorService();
        repoService = RepoService.INSTANCE;

        initRepos();
        initSynchronizers();
        initData();
    }

    private void initRepos() {
        userCloudRepo = repoService.getCloudRepo(User.class);
        userLocalRepo = repoService.getLocalRepo(User.class);
        activeTasksLocalRepo = repoService.getLocalRepo(Task.class);
    }

    private void initSynchronizers() {
        CitySynchronizer citySynchronizer = new CitySynchronizer(repoService.getCloudRepo(City.class), repoService.getLocalRepo(City.class));
        ActiveMissionSynchronizer missionSynchronizer = new ActiveMissionSynchronizer(this);
        ImageSynchronizer imageSynchronizer = new ImageSynchronizer();

        repoSynchronizer.addSynchronizer(citySynchronizer);
        repoSynchronizer.addSynchronizer(missionSynchronizer);
        repoSynchronizer.addSynchronizer(imageSynchronizer);
    }

    private void initData() {
        boolean userExists = userLocalRepo.fileExists();
        if (!userExists) {
            postUserInit();
            alias.set(localize("navigationdrawer.login"));
            setProfileImage(ImageHandler.AVATAR_PLACE_HOLDER);

        } else {
            GluonObservableList<Task> obsActiveTasks = activeTasksLocalRepo.retrieveListAsync("tasks");
            AsyncResultReceiver<GluonObservableList<Task>> activeTasksReceiver = AsyncResultReceiver.create(obsActiveTasks)
                                                                                                    .defaultProgressLayer()
                                                                                                    .onSuccess(result ->
                                                                                                    {
                                                                                                        if (!result.isEmpty() &&
                                                                                                                getActiveMission() != null) {
                                                                                                            missionCache.setActiveMissionTasks(obsActiveTasks,
                                                                                                                                               getActiveMission().getId());
                                                                                                        }
                                                                                                    })
                                                                                                    .finalize(this::postUserInit);

            GluonObservableObject<User> obsUser = userLocalRepo.retrieveObjectAsync();
            AsyncResultReceiver.create(obsUser)
                               .defaultProgressLayer()
                               .onSuccess(result ->
                               {
                                   User _user = result.get();
                                   setUser(_user);
                                   alias.set(_user.getAlias());
                                   setDefaultCity(_user.getDefaultCity());
                                   setActiveMission(_user.getActiveMission());

                                   StorableImage image = ImageHandler.retrieveImage(
                                                                                    _user.getImageUrl(),
                                                                                    ImageHandler.AVATAR_PLACE_HOLDER);

                                   setProfileImage(image.get());
                               })
                               .exceptionMessage(localize("baseService.error.loadUser"))
                               .next(activeTasksReceiver)
                               .start();
        }
    }

    private void postUserInit() {
        user.addListener((ov, u, u1) -> onUserChanged(u1));
        defaultCity.addListener((ov, c, c1) -> onCityChanged(c1));
        activeMission.addListener((ov, m, m1) -> onActiveMissionChanged(m1));

        if (internectConnectionEstablished()) {
            SyncMetaData syncMetaData = new SyncMetaData().userId(getUserId())
                                                          .activeMission(getActiveMission());
            repoSynchronizer.syncNow(syncMetaData);
        } else {
            initialized.set(true);
        }
    }

    private boolean internectConnectionEstablished() {
        boolean connectionEstablished = false;
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();
            if (con.getResponseCode() == 200) {
                connectionEstablished = true;
            }
        } catch (Exception ex) {
            LOGGER.error("no internet connection", ex);
        }
        return connectionEstablished;
    }

    private void onUserChanged(User user) {
        userListenerActive = true;

        if (user != null) {
            userLocalRepo.createOrUpdateAsync(user);

            setDefaultCity(user.getDefaultCity());
            if (user.getDefaultCity() == null) {
                Platform.runLater(() -> Dialogs.ok("baseService.info.selectDefaultCity")
                                               .showAndWait());
            }
            alias.set(user.getAlias());

            setActiveMission(user.getActiveMission());
            LOGGER.debug("user logged in: {}", user.getAlias());

        } else {
            userLocalRepo.deleteAsync();
            alias.set(localize("navigationdrawer.login"));
            setDefaultCity(null);
            setActiveMission(null);
        }
        CityCache.INSTANCE.setUserId(user == null ? null : user.getId());

        userListenerActive = false;
    }

    private void onCityChanged(City city) {
        if (!userListenerActive) {
            User _user = getUser();
            _user.setDefaultCity(city);

            userCloudRepo.createOrUpdateAsync(_user, _user.getId());
            userLocalRepo.createOrUpdateAsync(_user);
        }
    }

    private void onActiveMissionChanged(Mission mission) {
        if (!userListenerActive) {
            User _user = getUser();
            _user.setActiveMission(mission);
            _user.setTimeStamp(userCloudRepo.createTimeStamp());

            AsyncResultReceiver.create(userCloudRepo.createOrUpdateAsync(_user, _user.getId()))
                               .defaultProgressLayer()
                               .onSuccess(result -> userLocalRepo.createOrUpdateAsync(result.get()))
                               .start();
        }

        if (mission != null) {
            activeMissionName.set(mission.getMissionName());
            updateActiveTasks(mission);
            LOGGER.debug("activeMission set to: {}", mission.getMissionName());

        } else {
            activeMissionName.set(null);
            activeMissionStats = null;
            missionCache.clearActiveMissionTasks();
            activeTasksLocalRepo.deleteAsync();
        }
    }

    private void updateActiveTasks(Mission mission) {
        if (mission == null) {
            return;
        }
        GluonObservableList<Task> obsActiveTasks = missionCache.retrieveActiveMissionTasksOrdered(mission.getId());
        AsyncResultReceiver.create(obsActiveTasks)
                           .defaultProgressLayer()
                           .onSuccess(result ->
                           {
                               activeTasksLocalRepo.createOrUpdateListAsync(new ActiveTaskList(result));

                               for (Task task : result) {
                                   ImageHandler.cacheImageAsync(task.getImageUrl());
                               }
                           })
                           .start();
    }

    public ReadOnlyBooleanProperty initializedProperty() {
        return initialized;
    }

    public boolean isInitialized() {
        return initializedProperty().get();
    }

    public RepoService getRepoService() {
        return repoService;
    }

    public ActivatorDeactivatorService getActivatorDeactivatorService() {
        return activatorDeactivatorService;
    }

    public ObjectProperty<User> userProperty() {
        return user;
    }

    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user.set(user);
        LOGGER.debug("set user to: {}", getUserId());
    }

    public String getUserId() {
        return getUser() == null ? null : getUser().getId();
    }

    public ObjectProperty<Image> profileImageProperty() {
        return profileImage;
    }

    public Image getProfileImage() {
        return profileImage.get();
    }

    public void setProfileImage(Image image) {
        profileImage.set(image);
    }

    public ObservableValue<String> aliasProperty() {
        return alias;
    }

    public ObservableList<City> getCitiesSorted() {
        return cityCache.getCitiesSorted();
    }

    public void setDefaultCity(City city) {
        defaultCity.set(city);
        selectedCity.set(city);
        LOGGER.debug("default city set to: {}", city == null ? null : city.getCityName());
    }

    public void setSelectedCity(City city) {
        selectedCity.set(city);
    }

    public void setSelectedCityById(String cityId) {
        selectedCity.set(cityCache.get(cityId));
    }

    public City getSelectedCity() {
        return selectedCity.get();
    }

    public String getSelectedCityId() {
        return getSelectedCity() == null ? null : getSelectedCity().getId();
    }

    public Mission getSelectedMission() {
        return selectedMission.get();
    }

    public void setSelectedMission(Mission selectedMission) {
        this.selectedMission.set(selectedMission);
    }

    public Task getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Task task) {
        selectedTask = task;
    }

    public ObjectProperty<Mission> activeMissionProperty() {
        return activeMission;
    }

    public Mission getActiveMission() {
        return activeMission.get();
    }

    public void setActiveMission(Mission mission) {
        activeMission.set(mission);
        activeMissionName.set(mission == null ? null : mission.getMissionName());
    }

    public StringProperty activeMissionNameProperty() {
        return activeMissionName;
    }

    public boolean activeMissionContainsTask(Task task) {
        return missionCache.containsActiveTask(task);
    }

    public ObservableList<Task> getActiveTasks() {
        return missionCache.getActiveMissionTasks();
    }

    public MissionStat getActiveMissionStats() {
        return activeMissionStats;
    }

    public void setActiveMissionStat(MissionStat missionStats) {
        activeMissionStats = missionStats;
    }

    public BooleanProperty stopMissionProperty() {
        if (stopMission == null) {
            stopMission = new SimpleBooleanProperty(false);
        }
        return stopMission;
    }

    public String getPreviousViewName() {
        return previousViewName;
    }

    public CityBuffer getCityBuffer() {
        return cityBuffer;
    }

    public void setCityBuffer(CityBuffer buffer) {
        if (cityBuffer != null && buffer != null) {
            cityBuffer.setCurrentCityId(buffer.getCurrentCityId());
        } else {
            cityBuffer = buffer;
        }
    }

    public static class CityBuffer {

        private String currentCityId;
        private String originalCityId;

        public CityBuffer(String currentCityId, String originalCityId) {
            this.currentCityId = currentCityId;
            this.originalCityId = originalCityId;
        }

        public String getCurrentCityId() {
            return currentCityId;
        }

        public void setCurrentCityId(String id) {
            currentCityId = id;
        }

        public String getOriginalCityId() {
            return originalCityId;
        }
    }

}
