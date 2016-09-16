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
package com.jns.orienteering.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

public class CollectionUtils {

    private CollectionUtils() {
    }

    public static <T> ObservableList<T> createObsList(int initialCapacity) {
        return FXCollections.observableArrayList(new ArrayList<>(initialCapacity));
    }

    public static boolean sizeIsGreaterThan(Collection<?> collection, int sizeToCompare) {
        return !Validators.isNullOrEmpty(collection) && collection.size() > sizeToCompare;
    }

    public static <T> Predicate<T> notContainedIn(Collection<T> col) {
        return t -> !col.contains(t);
    }

    public static <T> Predicate<T> containedIn(Collection<T> col) {
        return t -> col.contains(t);
    }

    public static <T, R extends Collection<T>> R collect(Iterable<T> source, Supplier<R> supplier) {
        return collect(source, supplier, (t, list) -> list.add(t));
    }

    public static <T, R> List<R> collectToList(Iterable<T> source, Function<T, R> mappingFunction) {
        return collect(source, ArrayList::new, (t, list) -> list.add(mappingFunction.apply(t)));
    }

    public static <T, R> ObservableList<R> collectToObsList(Iterable<T> source, Function<T, R> mappingFunction) {
        return collect(source, FXCollections::observableArrayList, (t, list) -> list.add(mappingFunction.apply(t)));
    }

    public static <T, R> ObservableList<R> collectFilter(Iterable<T> source, Predicate<T> filter, Function<T, R> mappingFunction) {
        ObservableList<R> result = FXCollections.observableArrayList();
        for (T obj : source) {
            if (filter.test(obj)) {
                result.add(mappingFunction.apply(obj));
            }
        }
        return result;
    }

    public static <T, R extends Collection<U>, U> R collect(Iterable<T> source, Supplier<R> target, BiConsumer<T, R> accumulator) {
        R items = target.get();

        for (T item : source) {
            accumulator.accept(item, items);
        }
        return items;
    }

    public static <T> void sort(List<T> objects) {
        sort(objects, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> void sort(List<T> objects, Comparator<? super T> comparator) {
        if (objects == null || objects.isEmpty()) {
            return;
        }

        T[] a = (T[]) objects.toArray();
        Arrays.sort(a, comparator);
        ListIterator<T> it = objects.listIterator();

        for (Object obj : a) {
            it.next();
            it.set((T) obj);
        }
    }

    public static <T> void forEach(Iterable<T> items, Consumer<T> action) {
        if (items == null) {
            return;
        }
        for (T item : items) {
            action.accept(item);
        }
    }

    public static <T> Optional<T> filter(Collection<T> items, Predicate<T> filter) {
        return filter(items, filter, t -> t);
    }

    public static <T, R> Optional<R> filter(Collection<T> items, Predicate<T> filter, Function<T, R> mappingFunction) {
        for (T item : items) {
            if (filter.test(item)) {
                return Optional.ofNullable(mappingFunction.apply(item));
            }
        }
        return Optional.empty();
    }

    public static <T> void filterForEach(Collection<T> items, Predicate<T> filter, Consumer<T> action) {
        if (Validators.isNullOrEmpty(items)) {
            return;
        }
        for (T item : items) {
            if (filter.test(item)) {
                action.accept(item);
            }
        }
    }

    public static <T, R> void removeFrom(Collection<T> items, Function<T, R> mappingFunction, Predicate<R> filter) {
        for (Iterator<T> it = items.iterator(); it.hasNext();) {
            R item = mappingFunction.apply(it.next());
            if (filter.test(item)) {
                it.remove();
            }
        }
    }

    public static <T> void removeFromAndConsume(Collection<T> items, Predicate<T> filter, Consumer<T> action) {
        for (Iterator<T> it = items.iterator(); it.hasNext();) {
            T next = it.next();
            if (filter.test(next)) {
                action.accept(next);
                it.remove();
            }
        }
    }

    public static <T, R> int indexOf(Collection<T> items, Predicate<T> filter) {
        int idx = 0;
        for (T obj : items) {
            if (filter.test(obj)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    public static <T, R> R getFirstOf(Collection<T> items, Function<T, R> mappingFunction) {
        Iterator<T> it = items.iterator();
        if (it.hasNext()) {
            return mappingFunction.apply(it.next());
        }
        return null;
    }

    public static void addSave(Node nodeToAdd, ObservableList<Node> list) {
        if (!list.contains(nodeToAdd)) {
            list.add(nodeToAdd);
        }
    }

    public static void removeSave(Node nodeToRemove, ObservableList<Node> list) {
        if (list.contains(nodeToRemove)) {
            list.remove(nodeToRemove);
        }
    }

    public static <T> void consumeIfPresentOrElse(Optional<T> optional, Consumer<T> presentConsumer, Trigger missingAction) {
        if (optional.isPresent()) {
            presentConsumer.accept(optional.get());
        } else {
            missingAction.start();
        }
    }

    public static <T, R> R mapIfPresentOrElse(Optional<T> optional, Function<T, R> mappingFunction, R other) {
        if (optional.isPresent()) {
            return mappingFunction.apply(optional.get());
        } else {
            return other;
        }
    }

    public static List<Integer> toList(int number) {
        if (number == 0) {
            return new ArrayList<>(Arrays.asList(0));
        }

        List<Integer> numbers = new ArrayList<>();

        while (number > 0) {
            numbers.add(number % 10);
            number /= 10;
        }

        Collections.reverse(numbers);
        return numbers;
    }

    public static int replaceDigitAt(List<Integer> digits, int idx, int newDigit) {
        digits.set(idx, newDigit);

        int numberUpdated = 0;
        int multiplicator = (int) Math.pow(10, digits.size() - 1);

        for (int digit : digits) {
            numberUpdated += digit * multiplicator;
            multiplicator = multiplicator / 10;
        }

        return numberUpdated;
    }

}
