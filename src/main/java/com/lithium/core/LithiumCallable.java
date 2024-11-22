package com.lithium.core;

import java.util.List;

public interface LithiumCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
