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
package com.jns.orienteering.model.dynamic;

import com.jns.orienteering.model.persisted.MissionStat;

public class Ranking implements Comparable<Ranking> {

    private static int  referenceDuration;

    private MissionStat missionStat;
    private int         timeDifference;
    private int         place;

    public Ranking(MissionStat missionStat) {
        this.missionStat = missionStat;
    }

    public void setReferenceDuration() {
        referenceDuration = missionStat.getDuration();
    }

    public void calculateTimeDifference() {
        timeDifference = missionStat.getDuration() - referenceDuration;
    }

    public int getTimeDifference() {
        return timeDifference;
    }

    public String getTimeDifferenceText() {
        return "+" +  Long.toString(timeDifference);
    }

    public int getPlace() {
        return place;
    }

    public String getPlaceText() {
        return Integer.toString(place);
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public MissionStat getMissionStat() {
        return missionStat;
    }

    @Override
    public int compareTo(Ranking other) {
        int result = Integer.compare(missionStat.getDuration(), other.missionStat.getDuration());

        if (result == 0) {
            result = Double.compare(missionStat.getDistance(), other.missionStat.getDistance());
        }
        return result;
    }

}
