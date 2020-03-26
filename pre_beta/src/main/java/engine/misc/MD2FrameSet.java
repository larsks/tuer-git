/**
 * Copyright (c) 2006-2020 Julien Gouesse
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
package engine.misc;

/**
 * set regrouping the first frame, the last frame and the frame rate of a
 * typical MD2 animation reference: http://tfc.duke.free.fr/old/models/md2.htm
 * (special thanks to David Henry)
 * 
 * N.B: Blender does not show the frame whose index is zero, you have to modify
 * its import script to show all frames (with a shift)
 * 
 * @author Julien Gouesse
 *
 */
public enum MD2FrameSet {

    STAND(0, 39, 9), RUN(40, 45, 10), ATTACK(46, 53, 10), PAIN_A(54, 57, 7), PAIN_B(58, 61, 7), PAIN_C(62, 65,
            7), JUMP(66, 71, 7), FLIP(72, 83, 7), SALUTE(84, 94, 7), FALLBACK(95, 111, 10), // taunt?
            WAVE(112, 122, 7), POINT(123, 134, 6), CROUCH_STAND(135, 153, 10), CROUCH_WALK(154, 159, 7), CROUCH_ATTACK(
                    160, 168, 10), CROUCH_PAIN(166, 172, 7), CROUCH_DEATH(173, 177, 5), DEATH_FALLBACK(178, 183,
                            7), DEATH_FALLFORWARD(184, 189, 7), DEATH_FALLBACKSLOW(190, 197, 7), BOOM(198, 198, 5);

    private final int firstFrameIndex;

    private final int lastFrameIndex;

    private final int framesPerSecond;

    private MD2FrameSet(final int firstFrameIndex, final int lastFrameIndex, final int framesPerSecond) {
        this.firstFrameIndex = firstFrameIndex;
        this.lastFrameIndex = lastFrameIndex;
        this.framesPerSecond = framesPerSecond;
    }

    public int getFirstFrameIndex() {
        return (firstFrameIndex);
    }

    public int getLastFrameIndex() {
        return (lastFrameIndex);
    }

    public int getFramesPerSecond() {
        return (framesPerSecond);
    }
}
