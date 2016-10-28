package com.jns.orienteering.common;

import java.util.function.Supplier;

import com.jns.orienteering.util.Validators;

public class MandatoryInputValidator<T> extends SingleValidator<T> {

    private Supplier<T> inputSupplier;

    public MandatoryInputValidator(Supplier<T> inputSupplier, String message) {
        super(t -> !Validators.isObjectNullOrEmpty(t), message);
        this.inputSupplier = inputSupplier;
    }

    public boolean check() {
        return check(inputSupplier.get());
    }

}