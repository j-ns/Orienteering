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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gluonhq.charm.glisten.application.MobileApplication;
import com.gluonhq.connect.MultiValuedMap;
import com.jns.orienteering.control.ActivatableDeactivatable;
import com.jns.orienteering.util.Trigger;
import com.jns.orienteering.view.ViewRegistry;

/**
 * Service to activate {@link ActivatableDeactivatable} targets, when the corresponding view is shown.
 * When the current showing view changes to {@link ViewRegistry#HOME}, all active targets will be deactivated.
 */
public class ActivatorDeactivatorService {

    private MultiValuedMap<String, ActivatorDeactivatorConsumer> activatorDeactivatorConsumers = new MultiValuedMap<>();
    private Set<String>                                          activeViewNames               = new HashSet<>();

    public ActivatorDeactivatorService() {
        MobileApplication.getInstance().viewProperty().addListener((obsValue, v, v1) -> onViewChanged(v1.getName()));
    }

    public void add(String viewName, ActivatableDeactivatable target) {
        add(viewName, target::activate, target::deactivate);
    }

    public void addActivator(String viewName, Trigger activator) {
        add(viewName, activator, Trigger.NullObject.INSTANCE);
    }

    public void addDeactivator(String viewName, Trigger deactivator) {
        add(viewName, Trigger.NullObject.INSTANCE, deactivator);
    }

    public void add(String viewName, Trigger activator, Trigger deactivator) {
        activatorDeactivatorConsumers.putSingle(viewName, new ActivatorDeactivatorConsumer(activator, deactivator));
    }

    private void onViewChanged(String viewName) {
        if (ViewRegistry.HOME.equals(viewName)) {
            if (!activeViewNames.isEmpty()) {
                deactivateAll();
            }
        } else {
            if (!activeViewNames.contains(viewName)) {
                startActivators(viewName);
            }
        }
    }

    private void startActivators(String viewName) {
        List<ActivatorDeactivatorConsumer> activatorsDeactivators = activatorDeactivatorConsumers.get(viewName);

        if (activatorsDeactivators != null) {
            activeViewNames.add(viewName);

            for (ActivatorDeactivatorConsumer candidate : activatorsDeactivators) {
                candidate.activator.start();
            }
        }
    }

    private void deactivateAll() {
        for (String activeViewName : activeViewNames) {
            List<ActivatorDeactivatorConsumer> consumers = activatorDeactivatorConsumers.get(activeViewName);

            for (ActivatorDeactivatorConsumer candidate : consumers) {
                candidate.deactivator.start();
            }
        }
        activeViewNames.clear();
    }

    private static class ActivatorDeactivatorConsumer {

        private Trigger activator;
        private Trigger deactivator;

        private ActivatorDeactivatorConsumer(Trigger activator, Trigger deactivator) {
            this.activator = activator;
            this.deactivator = deactivator;
        }
    }
}
