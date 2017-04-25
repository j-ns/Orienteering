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

/**
 * Copyright (c) 2016 Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of Gluon, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;

import com.gluonhq.connect.converter.OutputConverter;
import com.gluonhq.connect.converter.OutputStreamOutputConverter;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An OutputConverter that converts an object into a JSON Object that is written to an OutputStream. The actual
 * conversion to JSON is handled by using an instance of {@link JsonConverter}.
 *
 * @param <T> the type of the object to convert to a JSON Object
 */
public class JsonOutputConverterExtended<T> extends OutputStreamOutputConverter<T> implements OutputConverter<T> {

    private static final Logger LOG = Logger.getLogger(JsonOutputConverterExtended.class.getName());

    private static final JsonWriterFactory writerFactory = Json.createWriterFactory(null);

    private final JsonConverterExtended<T> converter;

    /**
     * Construct a new instance of a JsonOutputConverter that is able to convert objects of the specified
     * <code>targetClass</code> into JSON Objects and write them into the OutputStream.
     *
     * @param targetClass The class defining the objects being converted into JSON.
     */
    public JsonOutputConverterExtended(Class<T> targetClass) {
        converter = new JsonConverterExtended<>(targetClass);
    }

    /**
     * Converts an object into a JSON Object that is written to the InputStream. If the specified
     * <code>targetClass</code> in the constructor equals to JsonObject.class, then this method will cast the provided
     * object into a JsonObject instance and write it directly to the OutputStream. Otherwise, a {@link JsonConverter}
     * will be used to convert the object into a JSON Object.
     *
     * @param t The object to convert into a JSON Object that will be written to the OutputStream.
     */
    @Override
    public void write(T t) {
        try (JsonWriter writer = writerFactory.createWriter(getOutputStream())) {
            JsonObject jsonObject;
            if (JsonObject.class.isAssignableFrom(converter.getTargetClass())) {
                jsonObject = (JsonObject) t;
            } else {
                jsonObject = converter.writeToJson(t);
            }

            if (LOG.isLoggable(Level.FINE)) {
                StringWriter stringWriter = new StringWriter();
                try (JsonWriter writer2 = writerFactory.createWriter(stringWriter)) {
                    writer2.writeObject(jsonObject);
                    LOG.log(Level.FINE, "Written JSON data: " + stringWriter.toString());
                }
            }

            writer.writeObject(jsonObject);
        }
    }
}

