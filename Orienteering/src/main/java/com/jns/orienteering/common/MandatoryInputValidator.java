package com.jns.orienteering.common;

import static com.jns.orienteering.util.Validators.isObjectNullOrEmpty;

import java.util.function.Supplier;

public class MandatoryInputValidator<T> extends SingleValidator<T> {

    private Supplier<T> inputSupplier;

    public MandatoryInputValidator(Supplier<T> inputSupplier, String message) {
        super(t -> !isObjectNullOrEmpty(t), message);
        this.inputSupplier = inputSupplier;
    }

    @Override
    public boolean check() {
        return check(inputSupplier.get());
    }

}