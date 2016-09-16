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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReaderFactory;

import com.gluonhq.connect.converter.InputStreamInputConverter;
import com.gluonhq.connect.converter.JsonConverter;

public class JsonInputConverterExtended<T> extends InputStreamInputConverter<T> {

    private static final JsonReaderFactory readerFactory = Json.createReaderFactory(null);
    private final JsonConverterExtended<T> converter;

    /**
     * Construct a new instance of a JsonInputConverter that is able to convert the data read from the InputStream into
     * objects of the specified <code>targetClass</code>.
     *
     * @param targetClass
     *            The class defining the objects being converted from JSON.
     */
    public JsonInputConverterExtended(Class<T> targetClass) {
        this.converter = new JsonConverterExtended<>(targetClass);
    }

    /**
     * Converts a JSON Object that is read from the InputStream into an object and returns it. If the specified
     * <code>targetClass</code> in the constructor equals to JsonObject.class, then this method will just return the
     * read JSON object directly instead of running the conversion. Otherwise, a {@link JsonConverter} will be used to
     * convert the JSON Object into the final object to return.
     *
     * @return An object converted from the JSON Object that was read from the InputStream.
     */
    @Override
    public T read() {
        Reader sourceReader;
        String string = null;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream(), "UTF-8"))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            string = stringBuilder.toString();
            if ("null".equals(string)) {
                return null;
            }
            if (string.contains("error")) {
                System.out.println("error in jsonMapInputconverter: " + string);
            }

            sourceReader = new StringReader(string);
            JsonObject jsonObject = readerFactory.createReader(sourceReader).readObject();

            if (JsonObject.class.isAssignableFrom(converter.getTargetClass())) {
                return (T) jsonObject;
            } else {
                return converter.readFromJson(jsonObject);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
