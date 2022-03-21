package collections.observable;

import collections.enumflags.EnumFlagsEvent;

import java.util.ArrayList;

public class ObserverManager<T> extends ArrayList<Observer<T>> {
    public void send(T event) {
        for (var observer : this) {
            observer.onChange(event);
        }
    }
}
