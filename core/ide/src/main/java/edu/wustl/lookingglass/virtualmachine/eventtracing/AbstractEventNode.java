/*******************************************************************************
 * Copyright (c) 2008, 2015, Washington University in St. Louis.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Products derived from the software may not be called "Looking Glass", nor
 *    may "Looking Glass" appear in their name, without prior written permission
 *    of Washington University in St. Louis.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Washington University in St. Louis"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.  ANY AND ALL
 * EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE,
 * TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS,
 * COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package edu.wustl.lookingglass.virtualmachine.eventtracing;

import org.lgna.common.ComponentThread;
import org.lgna.project.ast.AbstractNode;

import edu.wustl.lookingglass.utilities.intervaltree.Interval;

/**
 * An object capturing the execution of a {@link AbstractNode} in the Virtual
 * Machine. An <code>AbstractEventNode</code> provides the necessary
 * functionality for replaying and capturing execution for use with the Dinah
 * interface.
 *
 * @author Michael Pogran
 */
public abstract class AbstractEventNode<T extends AbstractNode> implements Interval<Double>, Comparable<AbstractEventNode<?>> {
	private ComponentThread thread;
	private double startTime;
	private double endTime;
	protected T astNode;
	private java.util.UUID id = java.util.UUID.randomUUID();
	protected AbstractEventNode<?> parentNode;

	protected abstract void handleChildAdded( AbstractEventNode<?> eventNode );

	protected AbstractEventNode( T astNode, ComponentThread thread, double startTime, double endTime, AbstractEventNode<?> parent ) {
		this.astNode = astNode;
		this.thread = thread;
		this.startTime = startTime;
		this.endTime = endTime;
		this.parentNode = parent;

		if( this.parentNode != null ) {
			this.parentNode.handleChildAdded( this );
		}
	}

	/*package private*/void setEventNodeId( java.util.UUID id ) {
		this.id = id;
	}

	public java.util.UUID getEventNodeId() {
		return this.id;
	}

	public double getStartTime() {
		return this.startTime;
	}

	public double getEndTime() {
		return this.endTime;
	}

	public ComponentThread getThread() {
		return this.thread;
	}

	public T getAstNode() {
		return this.astNode;
	}

	public AbstractEventNode<?> getParent() {
		return this.parentNode;
	}

	public java.util.UUID getAstUUID() {
		return this.astNode.getId();
	}

	protected void setParent( AbstractEventNode<?> parent ) {
		this.parentNode = parent;
	}

	public synchronized void setStartTime( double time ) {
		this.startTime = time;
	}

	public synchronized void setEndTime( double time ) {
		this.endTime = time;
	}

	public boolean isExecutingAtTime( double time ) {
		return ( getStartTime() <= time ) && ( getEndTime() >= time );
	}

	public boolean isAfter( AbstractEventNode<?> node ) {
		return ( node != null ) && ( this.getStartTime() >= node.getStartTime() );
	}

	public boolean isBefore( AbstractEventNode<?> node ) {
		return ( node != null ) && ( this.getEndTime() <= node.getEndTime() );
	}

	@Override
	public int compareTo( AbstractEventNode<?> other ) {
		double diff;

		if( this == other ) {
			return 0;
		}

		if( this.getStartTime() == other.getStartTime() ) {
			diff = ( this.getEndTime() - other.getEndTime() );
		} else {
			diff = ( this.getStartTime() - other.getStartTime() );
		}

		if( diff <= 0 ) {
			return -1;
		} else {
			return 1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits( this.startTime );
		result = ( prime * result ) + (int)( temp ^ ( temp >>> 32 ) );
		result = ( prime * result ) + ( ( this.astNode == null ) ? 0 : this.astNode.hashCode() );
		result = ( prime * result ) + ( ( this.thread == null ) ? 0 : this.thread.hashCode() );
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}
		AbstractEventNode<?> other = (AbstractEventNode<?>)obj;
		if( Double.doubleToLongBits( this.startTime ) != Double.doubleToLongBits( other.startTime ) ) {
			return false;
		}
		if( this.astNode == null ) {
			if( other.astNode != null ) {
				return false;
			}
		} else if( !this.astNode.equals( other.astNode ) ) {
			return false;
		}
		if( this.thread == null ) {
			if( other.thread != null ) {
				return false;
			}
		} else if( !this.thread.equals( other.thread ) ) {
			return false;
		}
		return true;
	}

	@Override
	public Double getMinValue() {
		return this.startTime;
	}

	@Override
	public Double getMaxValue() {
		return this.endTime;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( this.getClass().getSimpleName() );
		sb.append( "[" );
		sb.append( this.astNode.getClass().getSimpleName() );
		sb.append( " " );
		sb.append( this.astNode.getRepr() );
		sb.append( "] " );
		sb.append( "(" );
		sb.append( String.format( "%.2f", this.startTime ) );
		sb.append( " - " );
		sb.append( String.format( "%.2f", this.endTime ) );
		sb.append( ") " );

		return sb.toString();
	}
}
