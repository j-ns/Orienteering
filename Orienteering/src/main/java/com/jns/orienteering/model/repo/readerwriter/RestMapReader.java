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
package com.jns.orienteering.model.repo.readerwriter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.gluonhq.connect.provider.ObjectDataReader;
import com.gluonhq.connect.provider.RestClient;
import com.jns.orienteering.model.common.MultiValueLookup;

public class RestMapReader<T extends MultiValueLookup, E> extends AbstractRestObjectsReader<T, E> {

    public RestMapReader(RestClient client, Class<T> sourceClass, String sourceUrl, Class<E> targetClass, String targetUrl) {
        super(client, sourceClass, sourceUrl, targetClass, targetUrl);
    }

    @Override
    protected void initKeysIterator(RestClient client) throws IOException {
        ObjectDataReader<T> reader = client.createObjectDataReader(new JsonInputConverterExtended<>(sourceClass));
        T lookup = reader.readObject();

        if (lookup == null) {
            keysIterator = Collections.emptyIterator();
            return;
        }

        Map<String, Boolean> keysMap = lookup.getValues();
        if (keysMap != null) {
            keysIterator = keysMap.keySet().iterator();
        } else {
            keysIterator = Collections.emptyIterator();
        }
    }

}
