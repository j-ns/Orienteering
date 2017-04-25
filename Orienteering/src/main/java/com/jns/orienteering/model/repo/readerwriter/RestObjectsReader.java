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
package com.jns.orienteering.model.repo.readerwriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.gluonhq.connect.provider.RestClient;
import com.jns.orienteering.model.persisted.Lookup;

public class RestObjectsReader<T extends Lookup, E> extends AbstractRestObjectsReader<T, E> {

    public RestObjectsReader(RestClient client, Class<T> sourceClass, String sourceUrl, Class<E> targetClass, String targetUrl) {
        super(client, sourceClass, sourceUrl, targetClass, targetUrl);
    }

    public RestObjectsReader(RestClient clientSource, RestClient clientTarget, Class<T> sourceClass, String sourceUrl, Class<E> targetClass,
                             String targetUrl) {
        super(clientSource, clientTarget, sourceClass, sourceUrl, targetClass, targetUrl);
    }

    @Override
    protected void initKeysIterator(RestClient client) throws IOException {
        Set<String> keys = new HashSet<>();

        Iterator<T> it = client.createListDataReader(new JsonTreeConverter<>(sourceClass)).iterator();
        while (it.hasNext()) {
            T next = it.next();
            keys.add(next.getLookupId());
        }
        keysIterator = keys.iterator();
    }

}
