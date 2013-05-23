package nl.nlnetlabs.bgpsym01.primitives.types;

public class Pair<X, T> {

    public final X key;

    public final T value;

    public Pair(X key, T value) {
        this.key = key;
        this.value = value;
    }

	public X getKey () {
		return key;
	}

	public T getValue() {
		return value;
	}

    @Override
    public int hashCode() {
        return (key == null ? -7 : key.hashCode()) * 300234 + (value == null ? -8 : value.hashCode()) * 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair p = (Pair) obj;
            return (key == null ? p.key == null : key.equals(p.key)) && (value == null ? p.value == null : value.equals(p.value));
        }
        return false;
    }

    @Override
    public String toString() {
        return "PAIR, k: " + key + ", v=" + value;
    }

}
