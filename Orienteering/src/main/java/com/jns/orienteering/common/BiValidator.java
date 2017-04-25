
package com.jns.orienteering.common;

import static com.jns.orienteering.control.Dialogs.showError;
import static com.jns.orienteering.util.Validations.isNullOrEmpty;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public class BiValidator<T, U> implements Validator {

    private Supplier<T>               supplier1;
    private Supplier<U>               supplier2;
    private BiFunction<T, U, Boolean> predicateFunction;
    private String                    errorMessage;

    public BiValidator(BiFunction<T, U, Boolean> predicateFunction, String errorMessage) {
        this(null, null, predicateFunction, errorMessage);
    }

    public BiValidator(Supplier<T> supplier1, Supplier<U> supplier2, BiFunction<T, U, Boolean> predicateFunction, String errorMessage) {
        this.supplier1 = supplier1;
        this.supplier2 = supplier2;
        this.predicateFunction = predicateFunction;
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean check() {
        return check(supplier1.get(), supplier2.get());
    }

    public boolean check(T t, U u) {
        if (!predicateFunction.apply(t, u)) {
            if (!isNullOrEmpty(errorMessage)) {
                showError(errorMessage);
            }
            return false;
        }
        return true;
    }

}
