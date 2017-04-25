/*
 *
 *  Copyright 2016 - 2017, Jens Stroh
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
package com.jns.orienteering.model.persisted;

public class ChangeLogEntry extends BaseModel {

    private RepoAction action;
    private long       timeStamp;

    @JsonDefaultConstructor
    public ChangeLogEntry() {
    }

    public ChangeLogEntry(Synchronizable synchronizable) {
        id = synchronizable.getId();
        action = synchronizable.getRepoAction();
        timeStamp = synchronizable.getTimeStamp();
    }

    public String getTargetId() {
        return id;
    }

    public RepoAction getAction() {
        return action;
    }

    public void setAction(RepoAction action) {
        this.action = action;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long epochSeconds) {
        timeStamp = epochSeconds;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("targetId: ")
                                  .append(id)
                                  .append(" action: ")
                                  .append(action)
                                  .append(" timeStamp: ")
                                  .append(timeStamp)
                                  .toString();
    }
}
