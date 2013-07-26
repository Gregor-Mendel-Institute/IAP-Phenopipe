/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ------------------------------
 * AbstractIntervalXYDataset.java
 * ------------------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 * Original Author: David Gilbert (for Object Refinery Limited).
 * Contributor(s): -;
 * $Id: AbstractIntervalXYDataset.java,v 1.1 2011-01-31 09:02:17 klukas Exp $
 * Changes
 * -------
 * 05-May-2004 : Version 1 (DG);
 */

package org.jfree.data;

/**
 * An base class that you can use to create new implementations of the {@link XYDataset} interface.
 */
public abstract class AbstractIntervalXYDataset extends AbstractXYDataset
																implements IntervalXYDataset {

	/**
	 * Returns the start x-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The start x-value.
	 */
	public double getStartX(int series, int item) {
		double result = Double.NaN;
		Number x = getStartXValue(series, item);
		if (x != null) {
			result = x.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the end x-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The end x-value.
	 */
	public double getEndX(int series, int item) {
		double result = Double.NaN;
		Number x = getEndXValue(series, item);
		if (x != null) {
			result = x.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the start y-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The start y-value.
	 */
	public double getStartY(int series, int item) {
		double result = Double.NaN;
		Number y = getStartYValue(series, item);
		if (y != null) {
			result = y.doubleValue();
		}
		return result;
	}

	/**
	 * Returns the end y-value (as a double primitive) for an item within a series.
	 * 
	 * @param series
	 *           the series (zero-based index).
	 * @param item
	 *           the item (zero-based index).
	 * @return The end y-value.
	 */
	public double getEndY(int series, int item) {
		double result = Double.NaN;
		Number y = getEndYValue(series, item);
		if (y != null) {
			result = y.doubleValue();
		}
		return result;
	}

}
