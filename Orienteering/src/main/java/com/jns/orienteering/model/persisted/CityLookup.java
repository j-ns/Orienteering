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

import javax.xml.bind.annotation.XmlTransient;

import com.jns.orienteering.model.persisted.MultiValueLookup.MultiIdLookup;

public class CityLookup extends MultiIdLookup {

    private CityAssignable assignable;
    private AccessType     accessType;

    @JsonDefaultConstructor
    public CityLookup() {
        super();
    }

    public CityLookup(CityAssignable assignable) {
        this.assignable = assignable;
        id = assignable.getCityId();
        accessType = assignable.getAccessType();
        addValue(assignable.getId());
    }

    @XmlTransient
    public String getOwnerId() {
        return accessType == AccessType.PRIVATE ? assignable.getOwnerId() : null;
    }

    @XmlTransient
    public String getTargetId() {
        return assignable.getId();
    }

    public String getPreviousId() {
        return assignable.getPreviousCityId();
    }

    @XmlTransient
    public AccessType getAccessType() {
        return accessType;
    }

    @XmlTransient
    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public String getAccessTypeName() {
        return accessType.name().toLowerCase();
    }

    @XmlTransient
    public boolean accessTypeChanged() {
        return assignable.accessTypeChanged();
    }

    public boolean cityChanged() {
        return assignable.cityChanged();
    }

}