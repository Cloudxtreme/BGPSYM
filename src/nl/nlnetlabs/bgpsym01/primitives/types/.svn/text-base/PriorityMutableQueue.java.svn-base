package nl.nlnetlabs.bgpsym01.primitives.types;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * 
 * 
 * Kolejka priorytetowa z możliwością zmieniania klucza, kasowania oraz
 * wyszukiwania elementów w czasie O(logn). Kopiec zaimplementowany przy użyciu
 * tablicy.
 * 
 * @author wojciech
 * 
 * @param <E>
 *            typ elementów przechowywanych w kolejce
 * @param <V>
 *            typ którym indeksujemy wartości w kolejce
 */
public class PriorityMutableQueue<E, V extends Comparable<V>> {

    /**
     * Klasa modelująca pojedyńczy element w kolejce.
     * 
     * @author wojciech
     * 
     * @param <E>
     * @param <V>
     */
    private static class Entry<E, V extends Comparable<V>> {
        /**
         * pozycja elementu w tablicy
         */
        int pos;
        E element;
        V value;
    }

    /**
     * autoroszerzalna tablicy będąca podstawą kolejki priortytetowej
     */
    private ArrayList<Entry<E, V>> heapList = new ArrayList<Entry<E, V>>();

    /**
     * rozmiar kolejki
     */
    private int size;

    /**
     * mapa wszystkich elementów znajdujących się w kolejce priorytetowej
     */
    private HashMap<E, Entry<E, V>> elementMap = new HashMap<E, Entry<E, V>>();

    /**
     * @return pierwszy element w kolejce
     */
    public E peek() {
        if (size == 0) {
            return null;
        }
        return heapList.get(0).element;
    }

    /**
     * @return indeks pierwszego elementu w kolejce
     */
    public V peekValue() {
        if (size == 0) {
            return null;
        }
        return heapList.get(0).value;
    }

    /**
     * Usuwa element z kolejki (w czasie O(logn)).
     * 
     * @param element
     *            element od usunięcia.
     */
    public void remove(E element) {
        Entry<E, V> entry = elementMap.get(element);
        if (entry == null) {
            // nie ma takiego elementu - ok
            return;
        }
        Entry<E, V> last = heapList.get(size - 1);

        if (last == entry) {
            heapList.remove(--size);
        } else {
            swap(entry, last);
            heapList.remove(--size);
            correct(last);
        }

        elementMap.remove(element);
    }

    /**
     * 
     * @return true jeśli kolejka jest pusta
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @return pierwszy element z kolejki (jest usuwany)
     */
    public E pop() {
        if (size == 0) {
            return null;
        }

        Entry<E, V> entry = heapList.get(0);
        Entry<E, V> last = heapList.remove(--size);

        if (size != 0) {
            last.pos = 0;
            heapList.set(0, last);
            correct(last);
        }
        elementMap.remove(entry.element);
        return entry.element;
    }

    /**
     * @return rozmiar kolejki
     */
    public int size() {
        return size;
    }

    /**
     * Dodaje element do kolejki.
     * 
     * @param element
     *            element do dodania
     * @param value
     *            indeks dodawanego elementu
     * @return true jeśli element był już w kolejce (zostaje wtedy zmieniony
     *         jego indeks)
     */
    public boolean add(E element, V value) {
        boolean result = true;
        Entry<E, V> entry = elementMap.get(element);
        if (entry == null) {
            result = false;
            entry = new Entry<E, V>();
            entry.element = element;
            elementMap.put(element, entry);
            entry.pos = size++;
            heapList.add(entry);
        }
        entry.value = value;
        correct(entry);
        return result;
    }

    /**
     * Metoda poprawiająca kolejkę - przesuwa element rekurencyjnie w dół lub w
     * górę (w zależności od potrzeb).
     * 
     * @param entry
     */
    private void correct(Entry<E, V> entry) {
        // jak ten nad nami jest od nas mniejszy to się zamieniamy i poprawiamy
        // dalej
        if (entry.pos != 0) {
            Entry<E, V> previous = heapList.get((entry.pos - 1) / 2);
            if (previous.value.compareTo(entry.value) == 1) {
                swap(previous, entry);
                correct(entry);
                return;
            }
        }

        // jak któryś z tych pod nami jest od nas większy to się z nim
        // zamienianmy
        int prevPos = entry.pos * 2 + 1;

        Entry<E, V> e = entry;

        if (prevPos < size && heapList.get(prevPos).value.compareTo(e.value) == -1) {
            e = heapList.get(prevPos);
        }
        prevPos++;

        if ((prevPos < size && heapList.get(prevPos).value.compareTo(e.value) == -1)) {
            e = heapList.get(prevPos);
        }

        if (e != entry) {
            swap(e, entry);
            correct(entry);
            return;
        }
    }

    /**
     * Zamienia dwa elementy miejscami.
     * 
     * @param e1
     * @param e2
     */
    private void swap(Entry<E, V> e1, Entry<E, V> e2) {
        int tmp = e1.pos;
        e1.pos = e2.pos;
        e2.pos = tmp;

        heapList.set(e1.pos, e1);
        heapList.set(e2.pos, e2);
    }

    /**
     * @return aktualny stan kolejki jako string
     */
    String asString() {
        StringBuilder result = new StringBuilder("\n");
        for (int i = 0; i < size; i++) {
            result.append("i=").append(i).append(", el=").append(heapList.get(i).element).append(", val=").append(heapList.get(i).value).append("\n");
        }
        return result.toString();
    }

    /**
     * Czyści kolejkę.
     */
    public void clear() {
        heapList.clear();
        elementMap.clear();
    }

}
