/*
 * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
 * See LICENSE.txt file for licensing information.
 */
package de.ailis.xadrian.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Iterator which iterates over multiple collections
 *
 * @param <T>
 *            The collection type
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public class MultiIterator<T> implements Iterator<T>
{
    /** Iterator over all iterators */
    private final Iterator<Iterator<T>> iterators;

    /** Current iterator. Null if end of wrapped collections is reached. */
    private Iterator<T> current;

    /**
     * Creates iterator which iterates over all specified collections
     *
     * @param collections
     *            The collections to iterate
     */
    public MultiIterator(final Collection<T>[] collections)
    {
        final List<Iterator<T>> localIterators =
            new ArrayList<>(collections.length);
        for (final Collection<T> collection: collections)
        {
            localIterators.add(collection.iterator());
        }
        this.iterators = localIterators.iterator();
        if (this.iterators.hasNext())
            this.current = this.iterators.next();
        else
            this.current = null;
    }

    /**
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext()
    {
        if (this.current == null) return false;
        return this.current.hasNext();

    }

    /**
     * @see java.util.Iterator#next()
     */
    @Override
    public T next()
    {
        if (this.current == null)
            throw new NoSuchElementException("No more elements");

        final T next = this.current.next();
        while (this.current != null && !this.current.hasNext())
            if (this.iterators.hasNext())
                this.current = this.iterators.next();
            else
                this.current = null;
        return next;
    }

    /**
     * @see java.util.Iterator#remove()
     */
    @Override
    public void remove()
    {
        throw new UnsupportedOperationException("Read-only iterator");
    }
}
