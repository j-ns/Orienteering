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
package com.jns.orienteering.model.common;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jns.orienteering.util.BiFilter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

public class Search<S, C> {

    private static final Logger               LOGGER = LoggerFactory.getLogger(Search.class);

    private final BiFilter<S, C>              filter;
    private final Consumer<ObservableList<S>> resultConsumer;
    private BiFilter<C, C>                    refineSearchPredicate;

    private Task<Void>                        searchTask;

    private C                                 previousCriterion;
    private ObservableList<S>                 previousResult;

    public Search(BiFilter<S, C> filter, Consumer<ObservableList<S>> resultConsumer) {
        this.filter = filter;
        this.resultConsumer = resultConsumer;
    }

    public Search(BiFilter<S, C> filter, Consumer<ObservableList<S>> resultConsumer, BiFilter<C, C> refineSearchPredicate) {
        this.filter = filter;
        this.resultConsumer = resultConsumer;
        this.refineSearchPredicate = refineSearchPredicate;
    }

    public static <T> Search<T, String> textInstance(Function<T, String> stringConverter, Consumer<ObservableList<T>> resultConsumer) {
        return new Search<>((t, searchText) ->
        {
            String searchTextLowerCase = searchText.toLowerCase();
            return stringConverter.apply(t).toLowerCase().startsWith(searchTextLowerCase);
        },
                            resultConsumer, String::startsWith);
    }

    public void find(C criterion, ObservableList<S> suggestions) {
        if (isRunning()) {
            searchTask.cancel();
            LOGGER.debug("search cancelled");
        }

        searchTask = newSearchTask(() -> doFind(criterion, checkToUsePreviousResult(criterion, suggestions)));

        Thread thread = new Thread(searchTask);
        thread.setDaemon(true);
        thread.start();
    }

    private ObservableList<S> checkToUsePreviousResult(C criterion, ObservableList<S> suggestions) {
        // boolean predicate = previousResult == null ? false : getRefineSearchPredicate().test(criterion, previousSearch);
        // LOGGER.debug("usePreviousSearchResult: {}", predicate);
        //
        return refineSearchPredicate == null || previousResult == null || !refineSearchPredicate.test(criterion, previousCriterion) ? suggestions
                : previousResult;
    }

    private ObservableList<S> doFind(C criterion, ObservableList<S> suggestions) {
        ObservableList<S> result = FXCollections.observableArrayList();

        for (S candidate : suggestions) {
            if (filter.test(candidate, criterion)) {
                result.add(candidate);
            }
        }
        previousCriterion = criterion;
        previousResult = result;
        return result;
    }

    public void reset() {
        if (isRunning()) {
            searchTask.cancel();
        }
        searchTask = null;
        previousCriterion = null;
        previousResult = null;
    }

    public boolean isRunning() {
        return searchTask != null && searchTask.isRunning();
    }

    private Task<Void> newSearchTask(Supplier<ObservableList<S>> resultSupplier) {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                // Thread.sleep(2000);
                try {
                    ObservableList<S> result = resultSupplier.get();
                    if (!isCancelled()) {
                        resultConsumer.accept(result);
                    }
                } catch (Exception ex) {
                    LOGGER.error("searchTaskException", ex);
                }
                return null;
            }
        };
    }

}
