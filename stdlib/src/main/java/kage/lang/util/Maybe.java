package kage.lang.util;

public abstract class Maybe<T> {
    protected abstract T getValue();

    protected abstract boolean isSet();

    protected abstract T orElse(T other);

    public static class None<T> extends Maybe<T> {
        public None() {
        }

        @Override
        protected T getValue() {
            return null;
        }

        @Override
        protected boolean isSet() {
            return false;
        }

        @Override
        protected T orElse(T other) {
            return other;
        }

        @Override
        public String toString() {
            return "None()";
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public static class Some<T> extends Maybe<T> {
        private T value;

        public Some(T value) {
            this.value = value;
        }

        @Override
        protected T getValue() {
            return this.value;
        }

        @Override
        protected boolean isSet() {
            return true;
        }

        @Override
        protected T orElse(T other) {
            return this.value;
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Some<?> some = (Some<?>) o;

            return value.equals(some.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}
