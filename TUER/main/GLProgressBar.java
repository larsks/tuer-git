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

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeListener;

//import com.sun.opengl.util.j2d.TextRenderer;

public final class GLProgressBar {

    private boolean paintBorder;
    
    private boolean paintProgressString;
    
    private String progressString;
    
    private GLProgressBarShape shape;
    
    private BoundedRangeModel model;
    
    private int x;
    
    private int y;
    
    private int width;
    
    private int height;
    
    private TextRenderer textRenderer;
    
    public GLProgressBar(int x,int y,int width,int height){
        this(x,y,width,height,GLProgressBarShape.HORIZONTAL_STRAIGHT,0,100);
    }
    
    public GLProgressBar(int x,int y,int width,int height,BoundedRangeModel model){
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.paintBorder=true;
        this.paintProgressString=false;
        this.progressString=null;
        this.shape=GLProgressBarShape.HORIZONTAL_STRAIGHT;
        this.model=model;  
        this.textRenderer=null;
    }
    
    public GLProgressBar(int x,int y,int width,int height,int min,int max){
        this(x,y,width,height,GLProgressBarShape.HORIZONTAL_STRAIGHT,min,max);
    }
    
    public GLProgressBar(int x,int y,int width,int height,GLProgressBarShape shape,int min,int max){
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        this.paintBorder=true;
        this.paintProgressString=false;
        this.progressString=null;
        this.shape=shape;
        this.model=new DefaultBoundedRangeModel(0,0,min,max);
        this.textRenderer=null;
    }
    
    public final void addChangeListener(ChangeListener l){
        model.addChangeListener(l);
    }
    
    public final int getMaximum(){
        return(model.getMaximum());
    }
    
    public final int getMinimum(){
        return(model.getMinimum());
    }
    
    public final BoundedRangeModel getModel(){
        return(model);
    }
    
    public final GLProgressBarShape getShape(){
        return(shape);
    }
    
    public final double getPercentComplete(){
        return((getValue()-getMinimum())/(double)((double)getMaximum()-getMinimum()));
    }
    
    public final String getProgressString(){
        return(progressString);
    }
    
    public final int getValue(){
        return(model.getValue());
    }
    
    public final boolean isBorderPainted(){
        return(paintBorder);
    }
    
    public final boolean isProgressStringPainted(){
        return(paintProgressString);
    }
    
    public final void removeChangeListener(ChangeListener l){
        model.removeChangeListener(l);
    }
    
    public final void setBorderPainted(boolean paintBorder){
        this.paintBorder=paintBorder;
    }
    
    public final void setMaximum(int maximum){
        model.setMaximum(maximum);
    }

    public final void setMinimum(int minimum){
        model.setMinimum(minimum);
    }
    
    public final void setModel(BoundedRangeModel model){
        this.model=model;
    }
    
    public final void setShape(GLProgressBarShape shape){
        this.shape=shape;
    }
    
    public final void setProgressString(String progressString){
        this.progressString=progressString;
    }

    public final void setProgressStringPainted(boolean paintProgressString){
        this.paintProgressString=paintProgressString;
    }
    
    public final void setValue(int value){
        model.setValue(value);
    }
    
    public final void display(GLAutoDrawable drawable){
        GL gl=drawable.getGL();
        int progressWidth,progressHeight;
        if(paintBorder)
            {progressWidth=width-4;
             progressHeight=height-4;
             gl.glPushAttrib(GL.GL_CURRENT_BIT);
             gl.glColor3f(0.0f,0.0f,0.0f);
             gl.glBegin(GL.GL_LINES);
             gl.glVertex2i(x,y);
             gl.glVertex2i(x+width,y);
             gl.glVertex2i(x+width,y);
             gl.glVertex2i(x+width,y-height);
             gl.glVertex2i(x+width,y-height);
             gl.glVertex2i(x,y-height);
             gl.glVertex2i(x,y-height);
             gl.glVertex2i(x,y);
             gl.glEnd();
             gl.glPopAttrib();
            }
        else
            {progressWidth=width;
             progressHeight=height;
            }
        gl.glPushAttrib(GL.GL_CURRENT_BIT);
        gl.glColor3f(0.0f,0.0f,1.0f);
        gl.glBegin(GL.GL_QUADS);
        switch(shape)
            {case HORIZONTAL_STRAIGHT:
                 {int progressFullX=(int)(x+2+progressWidth*getPercentComplete());
                  gl.glVertex2i(x+2,y-2);
                  gl.glVertex2i(x+2,y-2-progressHeight);
                  gl.glVertex2i(progressFullX,y-2-progressHeight);
                  gl.glVertex2i(progressFullX,y-2);
                  break;
                 }
             case VERTICAL_STRAIGHT:
                 {int progressFullY=(int)(y-2-progressHeight*getPercentComplete());
                  gl.glVertex2i(x+2,y-2);
                  gl.glVertex2i(x+2+progressWidth,y-2);
                  gl.glVertex2i(x+2+progressWidth,progressFullY);
                  gl.glVertex2i(x+2,progressFullY);
                  break;
                 }
            }
        gl.glEnd();
        gl.glPopAttrib();      
        if(paintProgressString)
            {if(textRenderer==null)                
                 textRenderer=new TextRenderer(new Font("SansSerif",Font.PLAIN,12));
             String trueProgressString;
             if(progressString==null)
                 trueProgressString=(int)(getPercentComplete()*100)+"%";
             else
                 trueProgressString=progressString;
             Rectangle2D progressStringBounds=textRenderer.getBounds(trueProgressString);
             int progressStringx = (int)(x+2+((progressWidth-progressStringBounds.getWidth())/2));
             int progressStringy = (int)(y-2-((progressHeight-progressStringBounds.getHeight())/2)-progressStringBounds.getHeight());           
             gl.glPushAttrib(GL.GL_CURRENT_BIT);
             gl.glColor3f(0.0f,0.0f,0.0f);          
             textRenderer.beginRendering(drawable.getWidth(),drawable.getHeight());
             textRenderer.setColor(Color.BLACK);
             textRenderer.draw(trueProgressString,progressStringx,progressStringy);            
             textRenderer.endRendering();
             gl.glPopAttrib();           
            }    
    }
}
