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

/**
 * This class has the role of the view
 * (in the meaning of the design pattern "MVC") 
 * of a 3D object
 * @author Julien Gouesse
 */

package main;

import java.util.List;
import java.util.Vector;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;

class Object3DView{

    
    //contains the mainly used textures
    protected List<Texture> texturesList;
    
    //contains the textures for blending, etc...
    protected List<Texture> secondaryTexturesList;
    
    
    //contains the indices (inside the lists above) of textures for each frame
    protected List<Integer> texturesIndicesList;
    
    //contains a list of vertex set, a vertex set per frame
    protected List<IVertexSet> vertexSetsList;
    
    protected Object3DController controller;
    
    
    Object3DView(){
        this.texturesList=new Vector<Texture>();
        this.secondaryTexturesList=new Vector<Texture>();
        this.texturesIndicesList=new Vector<Integer>();
        this.vertexSetsList=new Vector<IVertexSet>();
        this.controller=null;
    }       

    Object3DView(Texture texture,Texture secondaryTexture,IVertexSet vertexSet){
        this.texturesList=new Vector<Texture>();
        this.texturesIndicesList=new Vector<Integer>();
        if(texture!=null)
            {this.texturesList.add(texture);
             this.texturesIndicesList.add(Integer.valueOf(0));
            }
        this.secondaryTexturesList=new Vector<Texture>();
        if(secondaryTexture!=null)
            this.secondaryTexturesList.add(secondaryTexture);
        this.vertexSetsList=new Vector<IVertexSet>();
        if(vertexSet!=null)
            this.vertexSetsList.add(vertexSet);
        this.controller=null;
    }             

    Object3DView(List<Texture> texturesList,List<Texture> secondaryTexturesList,List<Integer> texturesIndicesList,List<IVertexSet> vertexSetsList){
        this.texturesList=texturesList;
        this.secondaryTexturesList=secondaryTexturesList;
        this.texturesIndicesList=texturesIndicesList;
        this.vertexSetsList=vertexSetsList;
        this.controller=null;
    }


    List<Texture> getTexturesList(){
        return(texturesList);
    }

    void setTexturesList(List<Texture> texturesList){
        this.texturesList=texturesList;
    }

    List<Texture> getSecondaryTexturesList(){
        return(secondaryTexturesList);
    }

    void setSecondaryTexturesList(List<Texture> secondaryTexturesList){
        this.secondaryTexturesList=secondaryTexturesList;
    }

    List<Integer> getTexturesIndicesList(){
        return(texturesIndicesList);
    }

    void setTexturesIndicesList(List<Integer> texturesIndicesList){
        this.texturesIndicesList=texturesIndicesList;
    }

    List<IVertexSet> getVertexSetsList(){
        return(vertexSetsList);
    }

    IVertexSet getVertexSet(int index){
        return(vertexSetsList.get(index));
    }

    void setVertexSetsList(List<IVertexSet> vertexSetsList){
        this.vertexSetsList=vertexSetsList;
    }

    Object3DController getController(){
        return(controller);
    }

    void setController(Object3DController controller){
        this.controller=controller;
    }

    void draw(){
        final GL gl=GLContext.getCurrentGL();
        int index=this.controller.getCurrentFrameIndex();
        texturesList.get(texturesIndicesList.get(index).intValue()).bind(gl);
        gl.getGL2().glPushMatrix();
        gl.getGL2().glTranslatef(controller.getX(),controller.getY(),controller.getZ());
        gl.getGL2().glRotatef(controller.getHorizontalDirection(),0.0f,1.0f,0.0f);
        gl.getGL2().glRotatef(controller.getVerticalDirection(),1.0f,0.0f,0.0f);
        //don't add a third rotation to avoid any gimbal lock
        vertexSetsList.get(index).draw();
        gl.getGL2().glPopMatrix();
    }
}
