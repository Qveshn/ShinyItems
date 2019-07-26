/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Qveshn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.sipsi133.engine;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Interval<T extends Comparable<T>> {

    public final T first;
    public final T second;

    public Interval(Interval<T> value) {
        this(value.first, value.second);
    }

    public Interval(T value) {
        this(value, value);
    }

    public Interval(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T min() {
        return first.compareTo(second) < 0 ? first : second;
    }

    public T max() {
        return first.compareTo(second) < 0 ? second : first;
    }

    public Interval<T> getSorted() {
        return new Interval<>(min(), max());
    }

    public boolean isInside(T value) {
        return first.compareTo(second) < 0
                ? first.compareTo(value) <= 0 && second.compareTo(value) >= 0
                : second.compareTo(value) <= 0 && first.compareTo(value) >= 0;
    }

    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^
                (second == null ? 0 : second.hashCode());
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return true;
        if (!(obj instanceof Interval)) return false;
        Interval<?> other = (Interval<?>) obj;
        return first.equals(other.first) && second.equals(other.second);
    }
}
