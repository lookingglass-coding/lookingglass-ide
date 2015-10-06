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
package edu.wustl.lookingglass.ide.program.models;

import java.util.ArrayList;
import java.util.List;

import org.lgna.croquet.BoundedIntegerState;
import org.lgna.croquet.event.ValueEvent;
import org.lgna.project.ast.AbstractStatementWithBody;
import org.lgna.project.ast.ExpressionStatement;
import org.lgna.project.ast.Statement;

import edu.cmu.cs.dennisc.math.EpsilonUtilities;
import edu.wustl.lookingglass.ide.program.ProgramState;
import edu.wustl.lookingglass.ide.program.components.ProgramTimeSlider;
import edu.wustl.lookingglass.ide.program.event.ProgramExecutionEvent;
import edu.wustl.lookingglass.ide.program.event.ProgramStateEvent;
import edu.wustl.lookingglass.ide.program.event.StatementChangeEvent;
import edu.wustl.lookingglass.ide.program.event.StatementChangeListener;
import edu.wustl.lookingglass.ide.program.event.TimeScrubProgramListener;
import edu.wustl.lookingglass.virtualmachine.eventtracing.AbstractEventNode;
import edu.wustl.lookingglass.virtualmachine.eventtracing.ContainerEventNode;

/**
 * @author Michael Pogran
 */
public class ProgramStatementManager extends BoundedIntegerState implements TimeScrubProgramListener {
	private edu.wustl.lookingglass.ide.program.TimeScrubProgramImp program;
	private ArrayList<ArrayList<AbstractEventNode<?>>> eventNodes = new ArrayList<ArrayList<AbstractEventNode<?>>>();
	private java.util.HashSet<StatementChangeListener> changeListeners = new java.util.HashSet<StatementChangeListener>();
	private boolean isUpdating = false;
	public final int STEP_SIZE = 4;
	private int curIndex;

	public ProgramStatementManager( edu.wustl.lookingglass.ide.program.TimeScrubProgramImp program ) {
		super( new Details( edu.wustl.lookingglass.remix.models.ReuseGroup.REUSE_GROUP,
				java.util.UUID.fromString( "569328d2-def4-4c15-93b2-3a2af04e3aa4" )
				).initialValue( 0 ).minimum( 0 ).maximum( 0 ) );
		this.program = program;
		this.program.addTimeScrubProgramListener( this );

		this.addAndInvokeNewSchoolValueListener( new org.lgna.croquet.event.ValueListener<Integer>() {
			@Override
			public void valueChanged( ValueEvent<Integer> e ) {

				ProgramState state = ProgramStatementManager.this.program.getProgramState();
				if( ( state == ProgramState.PAUSED_LIVE ) || ( state == ProgramState.PAUSED_REPLAY ) || ( state == ProgramState.PLAYING_REPLAY ) ) {
					int nextIndex = ( e.getNextValue() - ( e.getNextValue() % STEP_SIZE ) ) / STEP_SIZE;
					int prevIndex = e.getNextValue() > e.getPreviousValue() ? nextIndex - 1 : nextIndex + 1;

					if( nextIndex != curIndex ) {
						List<AbstractEventNode<?>> nextNodes = ProgramStatementManager.this.getNodesForValue( e.getNextValue() );
						List<AbstractEventNode<?>> prevNodes = ProgramStatementManager.this.getNodesForValue( e.getPreviousValue() );

						notifyStatementChange( new StatementChangeEvent( nextNodes, prevNodes, nextIndex, prevIndex ) );
						curIndex = nextIndex;
					}
				}

				if( ( state == ProgramState.PAUSED_LIVE ) || ( state == ProgramState.PAUSED_REPLAY ) ) {
					double time = ProgramStatementManager.this.getTimeForValue( e.getNextValue() );
					ProgramStatementManager.this.program.setCurrentTime( time );
				}
			}
		} );
	}

	public void shutDown() {
		synchronized( this.eventNodes ) {
			this.eventNodes.clear();
		}

		synchronized( this.changeListeners ) {
			this.changeListeners.clear();
		}
		this.setValueTransactionlessly( 0 );
		this.setMaximum( 0 );
		this.program = null;
	}

	public void addStatementChangeListener( StatementChangeListener listener ) {
		synchronized( this.changeListeners ) {
			this.changeListeners.add( listener );
		}
	}

	public void removeStatementChangeListener( StatementChangeListener listener ) {
		synchronized( this.changeListeners ) {
			this.changeListeners.remove( listener );
		}
	}

	@Override
	public void programStateChange( ProgramStateEvent programStateEvent ) {
		if( ( programStateEvent.getNextState() == ProgramState.PLAYING_REPLAY ) ) {
			int nextValue = this.getValue() + STEP_SIZE;
			double nextTime = getTimeForValue( nextValue );
			if( programStateEvent.getTime() >= nextTime ) {
				this.setValueTransactionlessly( nextValue );
			}
		}
	}

	@Override
	public void startingExecution( ProgramExecutionEvent programExecutionEvent ) {
		Statement statement = programExecutionEvent.getStatement();
		AbstractEventNode<?> eventNode = programExecutionEvent.getEventNode();

		if( this.program.getProgramState() == ProgramState.PLAYING_LIVE ) {
			if( ( statement instanceof ExpressionStatement ) || ( statement instanceof AbstractStatementWithBody ) ) {

				int newValue = 0;
				synchronized( this.eventNodes ) {
					if( this.eventNodes.isEmpty() ) {
						ArrayList<AbstractEventNode<?>> newList = new ArrayList<AbstractEventNode<?>>();
						newList.add( eventNode );
						this.eventNodes.add( newList );
					} else {
						double lastTime = this.eventNodes.get( this.eventNodes.size() - 1 ).get( 0 ).getStartTime();
						boolean isWithinTime = EpsilonUtilities.isWithinReasonableEpsilon( eventNode.getStartTime(), lastTime );
						boolean isContained = ( eventNode.getParent() instanceof ContainerEventNode ) && this.eventNodes.get( this.eventNodes.size() - 1 ).contains( eventNode.getParent() );

						if( isWithinTime || isContained ) {
							this.eventNodes.get( this.eventNodes.size() - 1 ).add( eventNode );
						} else {
							ArrayList<AbstractEventNode<?>> newList = new ArrayList<AbstractEventNode<?>>();
							newList.add( eventNode );
							this.eventNodes.add( newList );
						}
					}

					newValue = new Integer( this.eventNodes.size() * STEP_SIZE );
				}
				if( newValue > this.getMaximum() ) {
					this.isUpdating = true && ( newValue > STEP_SIZE );
					this.setMaximum( newValue );
					this.setValueTransactionlessly( newValue );
					this.curIndex = ( newValue - ( newValue % STEP_SIZE ) ) / STEP_SIZE;
					this.isUpdating = false;
				}
			}
		}
	}

	@Override
	public void endingExecution( ProgramExecutionEvent programExecutionEvent ) {
	}

	public void stepForward() {
		if( this.getValue() < this.getMaximum() ) {
			this.setValueTransactionlessly( this.getValue() + STEP_SIZE );
		}
	}

	public void stepBackward() {
		if( this.getValue() > 0 ) {
			this.setValueTransactionlessly( this.getValue() - STEP_SIZE );
		}
	}

	private void notifyStatementChange( StatementChangeEvent statementChangeEvent ) {
		synchronized( this.changeListeners ) {
			for( StatementChangeListener listener : this.changeListeners ) {
				listener.statementChange( statementChangeEvent );
			}
		}
	}

	public List<AbstractEventNode<?>> getCurrentEventNodes() {
		if( this.getValue() == this.getMaximum() ) {
			synchronized( this.eventNodes ) {
				if( this.eventNodes.size() != 0 ) {
					return this.eventNodes.get( this.eventNodes.size() - 1 );
				} else {
					return java.util.Collections.emptyList();
				}
			}
		} else {
			return this.getNodesForValue( this.getValue() );
		}
	}

	private List<AbstractEventNode<?>> getNodesAtIndex( int index ) {
		synchronized( this.eventNodes ) {
			if( ( index < this.eventNodes.size() ) && ( index > -1 ) ) {
				return this.eventNodes.get( index );
			} else {
				return java.util.Collections.emptyList();
			}
		}
	}

	private int getIndexForNode( AbstractEventNode<?> node ) {
		synchronized( this.eventNodes ) {
			for( ArrayList<AbstractEventNode<?>> item : this.eventNodes ) {
				if( item.contains( node ) ) {
					return this.eventNodes.indexOf( item );
				}
			}
		}
		return -1;
	}

	public int getValueForTime( double time ) {
		synchronized( this.eventNodes ) {
			for( ArrayList<AbstractEventNode<?>> item : this.eventNodes ) {
				if( item.get( 0 ).getStartTime() == time ) {
					return this.eventNodes.indexOf( item ) * STEP_SIZE;
				}
			}
			return this.eventNodes.size() * STEP_SIZE;
		}
	}

	private double getTimeForIndex( int index ) {
		List<AbstractEventNode<?>> nodes = this.getNodesAtIndex( index );
		if( nodes.isEmpty() ) {
			return Double.NaN;
		} else {
			return nodes.get( 0 ).getStartTime();
		}
	}

	public List<AbstractEventNode<?>> getNodesForValue( int value ) {
		int index;
		if( ( value % STEP_SIZE ) == 0 ) {
			index = value / STEP_SIZE;
		} else {
			index = ( value - ( value % STEP_SIZE ) ) / STEP_SIZE;
		}
		return getNodesAtIndex( index );
	}

	public double getTimeForValue( int value ) {
		if( value == this.getMaximum() ) {
			return this.program.getMaxProgramTime();
		} else if( value == this.getMinimum() ) {
			return 0.0;
		} else {
			if( ( value % STEP_SIZE ) == 0 ) {
				return getTimeForIndex( value / STEP_SIZE );
			} else {
				int newValue = value - ( value % STEP_SIZE );
				double startTime = getTimeForValue( newValue );
				double endTime = getTimeForValue( newValue + STEP_SIZE );
				double step = ( endTime - startTime ) / (double)STEP_SIZE;
				double time = startTime + ( step * ( value % STEP_SIZE ) );
				return time;
			}
		}
	}

	public boolean isUpdating() {
		return this.isUpdating;
	}

	public int getCurrentIndex() {
		return this.curIndex;
	}

	@Override
	public ProgramTimeSlider createSlider() {
		ProgramTimeSlider rv = new ProgramTimeSlider( this );

		rv.getAwtComponent().addMouseListener( new java.awt.event.MouseAdapter() {
			@Override
			public void mousePressed( java.awt.event.MouseEvent e ) {
				ProgramStatementManager.this.program.pauseProgram();
			}
		} );

		return rv;
	}
}
