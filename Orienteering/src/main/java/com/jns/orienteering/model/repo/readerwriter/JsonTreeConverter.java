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

import java.util.Collections;
import java.util.Iterator;

import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gluonhq.connect.converter.InputStreamIterableInputConverter;
import com.gluonhq.impl.connect.converter.JsonUtil;

public class JsonTreeConverter<T> extends InputStreamIterableInputConverter<T> implements Iterator<T> {

    private static final Logger      LOGGER = LoggerFactory.getLogger(JsonTreeConverter.class);

    private Class<T>                 targetClass;
    private JsonObject               tree;
    private Iterator<String>         iterator;
    private JsonConverterExtended<T> converter;

    public JsonTreeConverter(Class<T> targetClass) {
        this.targetClass = targetClass;
        this.converter = new JsonConverterExtended<>(targetClass);
    }

    @Override
    public Iterator<T> iterator() {
        try (JsonReader reader = JsonUtil.createJsonReader(getInputStream())) {
            tree = reader.readObject();

        } catch (JsonParsingException e) {
            LOGGER.error("Failed  to parse json for class: {}", targetClass, e);
            return Collections.emptyIterator();
        }
        iterator = tree.keySet().iterator();
        return this;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public T next() {
        JsonValue json = tree.get(iterator.next());
        return converter.readFromJson((JsonObject) json);
    }

}
