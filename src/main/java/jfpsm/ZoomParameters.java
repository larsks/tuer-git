/**
 * Copyright (c) 2006-2016 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jfpsm;

final class ZoomParameters {

    private int factor;

    private int width;

    private int height;

    /**
     * center abscissa in the absolute reference
     */
    private int centerx;

    /**
     * center ordinate in the absolute reference
     */
    private int centery;

    ZoomParameters(int factor, int width, int height) {
        this.factor = factor;
        this.width = width;
        this.height = height;
        this.centerx = width / 2;
        this.centery = height / 2;
    }

    final void setFactor(int factor) {
        this.factor = factor;
    }

    final int getFactor() {
        return (factor);
    }

    final int getCenterx() {
        return (centerx);
    }

    final int getWidth() {
        return (width);
    }

    final void setWidth(final int width) {
        this.width = width;
    }

    final int getHeight() {
        return (height);
    }

    final void setHeight(final int height) {
        this.height = height;
    }

    final int getAbsoluteXFromRelativeX(int relativeX) {
        return (centerx - (width / (2 * factor)) + (relativeX / factor));
    }

    final int getAbsoluteYFromRelativeY(int relativeY) {
        return (centery - (height / (2 * factor)) + (relativeY / factor));
    }

    /**
     * Set the center abscissa in the absolute reference
     * 
     * @param centerx
     *            center abscissa in the absolute reference
     */
    final void setCenterx(int centerx) {
        this.centerx = Math.max(((width / factor) / 2), Math.min(centerx, width - ((width / factor) / 2)));
    }

    final int getCentery() {
        return (centery);
    }

    /**
     * Set the center ordinate in the absolute reference
     * 
     * @param centerx
     *            center ordinate in the absolute reference
     */
    final void setCentery(int centery) {
        this.centery = Math.max(((height / factor) / 2), Math.min(centery, height - ((height / factor) / 2)));
    }
}
