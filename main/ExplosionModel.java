/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package main;

public final class ExplosionModel extends Object3DModel {

    
    private boolean finished;
    
    
    ExplosionModel(float x,float y,float z,float horizontalDirection,
            float verticalDirection,Clock internalClock){
        super(x,y,z,ExplosionModelFactory.getInstance().getCoordinatesBuffersList(),
                ExplosionModelFactory.getInstance().getAnimationList(),
                horizontalDirection,verticalDirection,internalClock);       
        this.finished=false;
    }
    
    @Override
    protected final void updateFrameIndex(){
        super.updateFrameIndex();
        if(getElapsedTime()-lastStartOfAnimationTime > 
            ExplosionModelFactory.getInstance().getAnimationList().get(0).getFrameDuration())
            this.finished=true;
    }
    
    final boolean isFinished(){
        return(this.finished);
    }
}
