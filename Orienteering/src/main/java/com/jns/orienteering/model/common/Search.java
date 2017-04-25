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
