/*
 * $Id$
 * Copyright (C) 2009 Klaus Reimer <k@ailis.de>
 * See LICENSE.TXT for licensing information
 */

package de.ailis.xadrian.data;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import de.ailis.xadrian.support.I18N;


/**
 * A race
 * 
 * @author Klaus Reimer (k@ailis.de)
 * @version $Revision$
 */

public class Race implements Serializable, Comparable<Race>
{
    /** Serial version UID */
    private static final long serialVersionUID = 1477337332848671379L;

    /** The race id */
    private final String id;
    
    /** The message id */
    private final String messageId;


    /**
     * Constructor
     * 
     * @param id
     *            The race id
     */

    public Race(final String id)
    {
        this.id = id;
        this.messageId = "race." + id;
    }


    /**
     * Return the id.
     * 
     * @return The id
     */

    public String getId()
    {
        return this.id;
    }


    /**
     * Returns the name.
     * 
     * @return The name
     */

    public String getName()
    {
        return I18N.getString(this.messageId);
    }


    /**
     * @see java.lang.Object#hashCode()
     */

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.id).toHashCode();
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        final Race other = (Race) obj;
        return new EqualsBuilder().append(this.id, other.id).isEquals();
    }

    
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    
    @Override
    public int compareTo(final Race o)
    {
        return getName().compareTo(o.getName());
    }
    
    
    /**
     * @see java.lang.Object#toString()
     */
    
    @Override
    public String toString()
    {
        return getName();
    }
}
