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
package com.jns.orienteering.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javafx.beans.property.BooleanProperty;

public class MapUtils {

    private static final double LOAD_FACTOR = 0.75;

    private MapUtils() {
    }

    public static <K, V> Map<K, V> createMap(Function<Integer, Map<K, V>> mapSupplier, int ensuredCapacity) {
        return mapSupplier.apply(calculateCapacity(ensuredCapacity));
    }

    public static <K, V> Map<K, V> createMap(int ensuredCapacity) {
        return new HashMap<>(calculateCapacity(ensuredCapacity));
    }

    public static int calculateCapacity(int ensuredCapacity) {
        return (int) (ensuredCapacity / LOAD_FACTOR + 1);

    }

    public static <K, T> Map<K, T> createMap(Collection<T> source, Function<T, K> keyMapper) {
        return createMap(source, keyMapper, t -> t);
    }

    public static <K, V, T> Map<K, V> createMap(Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return createMap(HashMap::new, source, keyMapper, valueMapper);
    }

    public static <K, V, T> Map<K, V> createMap(Function<Integer, Map<K, V>> mapSupplier, Collection<T> source, Function<T, K> keyMapper,
                                                Function<T, V> valueMapper) {
        Map<K, V> map = createMap(mapSupplier, source.size());
        fillMap(map, source, keyMapper, valueMapper);
        return map;
    }

    public static <K, V, T> void fillMap(Map<K, V> target, Collection<T> source, Function<T, K> keyMapper, Function<T, V> valueMapper) {
        for (T obj : source) {
            K key = keyMapper.apply(obj);
            V value = valueMapper.apply(obj);
            target.put(key, value);
        }
    }

    public static <K, V> V getOrCreate(Map<K, V> map, K key, Function<K, V> valueMapper) {
        V value = map.get(key);
        if (value == null) {
            value = valueMapper.apply(key);
            map.put(key, value);
        }
        return value;
    }

    public static <K, V> boolean computeIfPresent(Map<K, V> map, K key, UnaryOperator<V> mappingFunction) {
        V oldVal = map.get(key);
        if (oldVal != null) {
            V newVal = mappingFunction.apply(oldVal);
            map.put(key, newVal);
            return true;
        }
        return false;
    }

    public static void setBooleans(Map<?, BooleanProperty> map, boolean value) {
        for (BooleanProperty bool : map.values()) {
            bool.set(value);
        }
    }
}
