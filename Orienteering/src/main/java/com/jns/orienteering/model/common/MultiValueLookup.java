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
package com.jns.orienteering.model.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

public class MultiValueLookup extends BaseModel {

    private Map<String, Boolean> values;

    @JsonDefaultConstructor
    public MultiValueLookup() {
    }

    public MultiValueLookup(String id, Map<String, Boolean> values) {
        this.id = id;
        this.values = values;
    }

    @Override
    @XmlTransient
    public String getId() {
        return super.getId();
    }

    public Map<String, Boolean> getValues() {
        if (values == null) {
            values = new HashMap<>();
        }
        return values;
    }

    public void setValues(Map<String, Boolean> values) {
        this.values = values;
    }

    public void addValue(String id) {
        getValues().put(id, true);
    }

    public void addValues(Set<String> targetIds) {
        for (String id : targetIds) {
            getValues().put(id, true);
        }
    }

    public void removeValue(String id) {
        if (values != null) {
            values.remove(id);
        }
    }

    public boolean containsValue(String targetId) {
        return values != null && values.containsKey(targetId);
    }
}
