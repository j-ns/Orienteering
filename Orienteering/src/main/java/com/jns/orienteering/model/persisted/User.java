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
package com.jns.orienteering.model.persisted;

import static com.jns.orienteering.util.DateTimeFormatters.createTimeStamp;

import java.util.Objects;

public class User extends BaseSynchronizable {

    private String  alias;
    private String  emailAdress;
    private String  password;
    private String  imageId;
    private Mission activeMission;
    private City    defaultCity;

    @JsonDefaultConstructor
    public User() {
    }

    public User(String userName, String alias, String emailAdress, City defaultCity, String password) {
        id = userName;
        this.alias = alias;
        this.emailAdress = emailAdress;
        this.password = password;
        imageId = createImageId();
        this.defaultCity = defaultCity;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public void setEmailAdress(String emailAdress) {
        this.emailAdress = emailAdress;
    }

    public City getDefaultCity() {
        return defaultCity;
    }

    public void setDefaultCity(City city) {
        defaultCity = city;
    }

    private String createImageId() {
        return id + "_" + createTimeStamp() + ".jpg";
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageId == null ? null : "profiles/" + imageId;
    }

    public Mission getActiveMission() {
        return activeMission;
    }

    public void setActiveMission(Mission activeMission) {
        this.activeMission = activeMission;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        User other = (User) obj;

        return alias.equals(other.alias) &&
                equalsSafe(emailAdress, other.emailAdress) &&
                equalsSafe(password, other.password) &&
                equalsSafe(imageId, other.imageId) &&
                equalsSafe(defaultCity, other.defaultCity) &&
                equalsSafe(activeMission, other.activeMission);
    }

    private boolean equalsSafe(Object o, Object o2) {
        return Objects.equals(o, o2);
    }

}
