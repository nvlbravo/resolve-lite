package edu.clemson.resolve.proving.immutableadts;

import edu.clemson.resolve.proving.iterators.ChainingIterator;

import java.util.Iterator;

public class ImmutableListConcatenation<E> extends AbstractImmutableList<E> {

    private final ImmutableList<E> myFirstList;
    private final int myFirstListSize;

    private final ImmutableList<E> mySecondList;
    private final int mySecondListSize;

    private final int myTotalSize;

    public ImmutableListConcatenation(ImmutableList<E> firstList,
            ImmutableList<E> secondList) {

        myFirstList = firstList;
        myFirstListSize = myFirstList.size();

        mySecondList = secondList;
        mySecondListSize = mySecondList.size();

        myTotalSize = myFirstListSize + mySecondListSize;
    }

    @Override public E get(int index) {
        E retval;
        if (index < myFirstListSize) {
            retval = myFirstList.get(index);
        }
        else {
            retval = mySecondList.get(index - myFirstListSize);
        }
        return retval;
    }

    @Override public ImmutableList<E> head(int length) {
        ImmutableList<E> retval;
        if (length <= myFirstListSize) {
            retval = myFirstList.head(length);
        }
        else {
            retval =
                    new ImmutableListConcatenation<E>(myFirstList, mySecondList
                            .head(length - myFirstListSize));
        }
        return retval;
    }

    @Override public Iterator<E> iterator() {
        return new ChainingIterator<E>(myFirstList.iterator(), mySecondList
                .iterator());
    }

    @Override public int size() {
        return myTotalSize;
    }

    @Override public ImmutableList<E> subList(int startIndex, int length) {
        return tail(startIndex).head(length);
    }

    @Override public ImmutableList<E> tail(int startIndex) {
        ImmutableList<E> retval;

        if (startIndex < myFirstListSize) {
            retval =
                    new ImmutableListConcatenation<E>(myFirstList
                            .tail(startIndex), mySecondList);
        }
        else {
            retval = mySecondList.tail(startIndex - myFirstListSize);
        }
        return retval;
    }
}