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
package com.jns.orienteering.model.repo.synchronizer;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.GluonObservableList;
import com.jns.orienteering.common.ImageHandler;
import com.jns.orienteering.model.persisted.ActiveTaskList;
import com.jns.orienteering.model.persisted.ChangeLogEntry;
import com.jns.orienteering.model.persisted.Task;
import com.jns.orienteering.model.repo.AsyncResultReceiver;

public class ImageSynchronizer extends BaseSynchronizer<Task, Task, ActiveTaskList> {

    private static final Logger LOGGER                = LoggerFactory.getLogger(ImageSynchronizer.class);

    public static final String  NAME                  = "image_synchronizer";
    private static final String IMAGE_LIST_IDENTIFIER = "images";

    public ImageSynchronizer() {
        super(IMAGE_LIST_IDENTIFIER);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void syncNow(SyncMetaData syncMetaData) {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LOGGER.debug("firstOfMonth (epochDay): {}", firstOfMonth.toEpochDay());

        if (!syncMetaData.isLastSyncedBefore(firstOfMonth)) {
            LOGGER.debug("lastSynced was after firstOfMonth");
            return;
        }

        setRunning();
        setSyncMetaData(syncMetaData);

        AsyncResultReceiver.create(retrieveChangeLog(IMAGE_LIST_IDENTIFIER))
                           .onSuccess(result ->
                           {
                               ImageHandler.removeFromCacheAsync(result);
                               setSucceeded();
                           })
                           .onException(this::setFailed)
                           .start();
    }

    @Override
    protected void syncLocalData(GluonObservableList<ChangeLogEntry> log) {
        // noop
    }

}
