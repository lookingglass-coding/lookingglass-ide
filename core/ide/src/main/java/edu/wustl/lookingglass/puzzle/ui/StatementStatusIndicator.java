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
package edu.wustl.lookingglass.puzzle.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.lgna.project.ast.Statement;

import com.sun.javafx.css.converters.EnumConverter;
import com.sun.javafx.css.converters.PaintConverter;

import edu.wustl.lookingglass.puzzle.PuzzleStatus;
import edu.wustl.lookingglass.puzzle.PuzzleStatus.State;

/**
 * @author Kyle J. Harms
 */
public class StatementStatusIndicator extends javafx.scene.layout.Region {

	private static final javafx.css.PseudoClass UNKNOWN_PSEUDOCLASS_STATE = javafx.css.PseudoClass.getPseudoClass( "unknown" ); //$NON-NLS-1$
	private static final javafx.css.PseudoClass CORRECT_PSEUDOCLASS_STATE = javafx.css.PseudoClass.getPseudoClass( "correct" ); //$NON-NLS-1$
	private static final javafx.css.PseudoClass INCORRECT_PSEUDOCLASS_STATE = javafx.css.PseudoClass.getPseudoClass( "incorrect" ); //$NON-NLS-1$

	public static enum ExecutionStatus {
		NOT_EXECUTING,
		EXECUTING
	}

	public StatementStatusIndicator( Statement statement, PuzzleStatus.State state ) {
		super();
		getStyleClass().add( "statement-status" );

		this.setPrefSize( 32.0, 32.0 );
		javafx.scene.layout.HBox.setHgrow( this, javafx.scene.layout.Priority.SOMETIMES );

		this.statement = new ReadOnlyObjectWrapper<Statement>( this, "statementProperty", statement );
		this.status.set( state );
		this.executionPulse = new SimpleDoubleProperty( this, "executionPulseProperty", 0.0 );

		this.executionPulse.addListener( new ChangeListener<Number>() {
			@Override
			public void changed( ObservableValue<? extends Number> observable, Number oldValue, Number newValue ) {
				StatementStatusIndicator.this.updateColor( (Double)newValue );
			}
		} );
		this.executionStatus.addListener( new ChangeListener<ExecutionStatus>() {
			@Override
			public void changed( ObservableValue<? extends ExecutionStatus> observable, ExecutionStatus oldValue, ExecutionStatus newValue ) {
				switch( newValue ) {
				case NOT_EXECUTING:
					executionPulse.unbind();
					break;
				case EXECUTING:
					if( StatementStatusIndicator.this.syncExecutionPulse != null ) {
						executionPulse.bind( StatementStatusIndicator.this.syncExecutionPulse );
					}
				}
				StatementStatusIndicator.this.updateColor( newValue );
			}
		} );

		// Make sure that when the color changes the control is updated
		this.styleProperty().bind( Bindings.createStringBinding(
				( ) -> {
					// Well... this seems wrong... but it's what they do in ProgressIndicatorSkin$IndeterminateSpinner.class... so I guess this is considered acceptable for now!?
					String style = null;
					Color c = colorProperty().get();
					if( c != null ) {
						style = "-fx-background-color: rgba(" + ( (int)( 255 * c.getRed() ) ) + "," + ( (int)( 255 * c.getGreen() ) ) + "," + ( (int)( 255 * c.getBlue() ) ) + "," + c.getOpacity() + ");";
					}
					return style;
				},
				this.colorProperty() ) );
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		// HACK: Okay. So I wrote this custom control not knowing a lot of things about JavaFX. I don't have the time
		// to rewrite it now. So this is a work around. Basically, all of the CSS attributes are not known until
		// the layout method is called (which calls layoutChildren). That means some stuff in the constructor is
		// actually initialized to null, because the CSS properties where not yet available. So to fix this, we just
		// call updateColor now to make sure it's initialized.
		// I'm sorry, it's lame... but I got things to do - kyle
		this.updateColor();
	}

	/*
	 * statement property
	 */

	private final ReadOnlyObjectWrapper<Statement> statement;

	public final Statement getStatement() {
		return this.statement.get();
	}

	public final ReadOnlyObjectProperty<Statement> statementProperty() {
		return this.statement.getReadOnlyProperty();
	}

	/*
	 * execution status property
	 */

	private final ObjectProperty<StatementStatusIndicator.ExecutionStatus> executionStatus = new SimpleObjectProperty<StatementStatusIndicator.ExecutionStatus>( this, "executionStatusProperty", ExecutionStatus.NOT_EXECUTING );

	public final ExecutionStatus getExecutionStatus() {
		return this.executionStatus.get();
	}

	public final void setExecutionStatus( ExecutionStatus executionStatus ) {
		this.executionStatus.set( executionStatus );
	}

	public final ObjectProperty<StatementStatusIndicator.ExecutionStatus> executionStatusProperty() {
		return this.executionStatus;
	}

	/*
	 * status property
	 */

	private final ObjectProperty<PuzzleStatus.State> status = new StyleableObjectProperty<PuzzleStatus.State>( null ) {
		@Override
		protected void invalidated() {
			pseudoClassStateChanged( UNKNOWN_PSEUDOCLASS_STATE, get() == PuzzleStatus.State.UNKNOWN );
			pseudoClassStateChanged( CORRECT_PSEUDOCLASS_STATE, get() == PuzzleStatus.State.CORRECT );
			pseudoClassStateChanged( INCORRECT_PSEUDOCLASS_STATE, get() == PuzzleStatus.State.INCORRECT );
		}

		@Override
		public CssMetaData<? extends Styleable, State> getCssMetaData() {
			return StyleableProperties.STATUS;
		}

		@Override
		public Object getBean() {
			return StatementStatusIndicator.this;
		}

		@Override
		public String getName() {
			return "statusProperty";
		}
	};

	public final PuzzleStatus.State getStatus() {
		return this.status.get();
	}

	public final ReadOnlyObjectProperty<PuzzleStatus.State> statusProperty() {
		return this.status;
	}

	/*
	 * execution pulse property
	 */

	private DoubleProperty syncExecutionPulse = null;
	private final DoubleProperty executionPulse;

	public DoubleProperty executionPulseProperty() {
		return this.executionPulse;
	}

	public void setSyncExecutionPulse( DoubleProperty sync ) {
		this.syncExecutionPulse = sync;
	}

	/*
	 * status color property
	 */

	private final ObjectProperty<Paint> statusColor = new StyleableObjectProperty<Paint>( null ) {
		@Override
		protected void invalidated() {
			final Paint value = get();
			if( ( value != null ) && !( value instanceof Color ) ) {
				if( isBound() ) {
					unbind();
				}
				set( null );
				throw new IllegalArgumentException( "Only Color objects are supported" );
			}
		}

		@Override
		public Object getBean() {
			return StatementStatusIndicator.this;
		}

		@Override
		public String getName() {
			return "statusColorProperty";
		}

		@Override
		public CssMetaData<StatementStatusIndicator, Paint> getCssMetaData() {
			return StyleableProperties.STATUS_COLOR;
		}
	};

	/*
	 * status executing low color property
	 */

	private final ObjectProperty<Paint> statusExecutingLowColor = new StyleableObjectProperty<Paint>( null ) {
		@Override
		protected void invalidated() {
			final Paint value = get();
			if( ( value != null ) && !( value instanceof Color ) ) {
				if( isBound() ) {
					unbind();
				}
				set( null );
				throw new IllegalArgumentException( "Only Color objects are supported" );
			}
		}

		@Override
		public Object getBean() {
			return StatementStatusIndicator.this;
		}

		@Override
		public String getName() {
			return "statusExecutingLowColorProperty";
		}

		@Override
		public CssMetaData<StatementStatusIndicator, Paint> getCssMetaData() {
			return StyleableProperties.STATUS_EXECUTING_LOW_COLOR;
		}
	};

	/*
	 * status executing high color property
	 */

	private final ObjectProperty<Paint> statusExecutingHighColor = new StyleableObjectProperty<Paint>( null ) {
		@Override
		protected void invalidated() {
			final Paint value = get();
			if( ( value != null ) && !( value instanceof Color ) ) {
				if( isBound() ) {
					unbind();
				}
				set( null );
				throw new IllegalArgumentException( "Only Color objects are supported" );
			}
		}

		@Override
		public Object getBean() {
			return StatementStatusIndicator.this;
		}

		@Override
		public String getName() {
			return "statusExecutingHighColorProperty";
		}

		@Override
		public CssMetaData<StatementStatusIndicator, Paint> getCssMetaData() {
			return StyleableProperties.STATUS_EXECUTING_HIGH_COLOR;
		}
	};

	/*
	 * color property
	 */

	private final ReadOnlyObjectWrapper<Color> color = new ReadOnlyObjectWrapper<Color>( this, "colorProperty", (Color)statusColor.get() );;

	public ReadOnlyObjectProperty<Color> colorProperty() {
		return this.color.getReadOnlyProperty();
	}

	private void updateColor( ExecutionStatus executionStatus, Double pulse ) {
		switch( executionStatus ) {
		case NOT_EXECUTING:
			if( statusColor.get() != null ) {
				color.set( (Color)statusColor.get() );
			}
			break;
		case EXECUTING:
			if( ( statusExecutingLowColor.get() != null ) && ( statusExecutingHighColor.get() != null ) ) {
				color.set( ( (Color)statusExecutingLowColor.get() ).interpolate( (Color)statusExecutingHighColor.get(), pulse ) );
			}
			break;
		}
	}

	private void updateColor( ExecutionStatus executionStatus ) {
		updateColor( executionStatus, executionPulse.get() );
	}

	private void updateColor( Double pulse ) {
		updateColor( executionStatus.get(), pulse );
	}

	private void updateColor() {
		updateColor( executionStatus.get(), executionPulse.get() );
	}

	/*
	 * css
	 */

	@Override
	public java.util.List<javafx.css.CssMetaData<? extends javafx.css.Styleable, ?>> getCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}

	private static class StyleableProperties {
		private static final CssMetaData<StatementStatusIndicator, PuzzleStatus.State> STATUS =
				new CssMetaData<StatementStatusIndicator, PuzzleStatus.State>( "-fx-status", new EnumConverter<>( PuzzleStatus.State.class ), PuzzleStatus.State.UNKNOWN ) {

					@Override
					public PuzzleStatus.State getInitialValue( StatementStatusIndicator node ) {
						return node.getStatus();
					}

					@Override
					public boolean isSettable( StatementStatusIndicator n ) {
						return ( n.status == null ) || !n.status.isBound();
					}

					@SuppressWarnings( "unchecked" )
					@Override
					public StyleableProperty<PuzzleStatus.State> getStyleableProperty( StatementStatusIndicator n ) {
						return (StyleableProperty<PuzzleStatus.State>)n.statusProperty();
					}
				};

		private static final CssMetaData<StatementStatusIndicator, Paint> STATUS_COLOR =
				new CssMetaData<StatementStatusIndicator, Paint>( "-fx-status-color", PaintConverter.getInstance(), null ) {

					@Override
					public boolean isSettable( StatementStatusIndicator n ) {
						return ( n.statusColor == null ) || !n.statusColor.isBound();
					}

					@Override
					public StyleableProperty<Paint> getStyleableProperty( StatementStatusIndicator n ) {
						return (StyleableProperty<Paint>)(WritableValue<Paint>)n.statusColor;
					}
				};

		private static final CssMetaData<StatementStatusIndicator, Paint> STATUS_EXECUTING_LOW_COLOR =
				new CssMetaData<StatementStatusIndicator, Paint>( "-fx-status-executing-low-color", PaintConverter.getInstance(), null ) {

					@Override
					public boolean isSettable( StatementStatusIndicator n ) {
						return ( n.statusExecutingLowColor == null ) || !n.statusExecutingLowColor.isBound();
					}

					@Override
					public StyleableProperty<Paint> getStyleableProperty( StatementStatusIndicator n ) {
						return (StyleableProperty<Paint>)(WritableValue<Paint>)n.statusExecutingLowColor;
					}
				};

		private static final CssMetaData<StatementStatusIndicator, Paint> STATUS_EXECUTING_HIGH_COLOR =
				new CssMetaData<StatementStatusIndicator, Paint>( "-fx-status-executing-high-color", PaintConverter.getInstance(), null ) {

					@Override
					public boolean isSettable( StatementStatusIndicator n ) {
						return ( n.statusExecutingHighColor == null ) || !n.statusExecutingHighColor.isBound();
					}

					@Override
					public StyleableProperty<Paint> getStyleableProperty( StatementStatusIndicator n ) {
						return (StyleableProperty<Paint>)(WritableValue<Paint>)n.statusExecutingHighColor;
					}
				};

		private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
		static {
			final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>( javafx.scene.layout.Region.getClassCssMetaData() );
			styleables.add( STATUS );
			styleables.add( STATUS_COLOR );
			styleables.add( STATUS_EXECUTING_LOW_COLOR );
			styleables.add( STATUS_EXECUTING_HIGH_COLOR );
			STYLEABLES = Collections.unmodifiableList( styleables );
		}
	}

	public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
		return StyleableProperties.STYLEABLES;
	}
}
