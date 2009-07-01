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
package jme;

import java.io.Serializable;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

public final class EntityParameters implements Serializable{

    
    private static final long serialVersionUID = 1L;

    static{Utils.forceHandlingOfTransientModifiersForXMLSerialization(EntityParameters.class);}
    
    private Quaternion rotation;
    
    private Vector3f scale;
    
    private Vector3f translation;
    
    private String alternativeTexturePath;
    
    private String artificialIntelligenceClassName;
    
    
    public EntityParameters(){}


    public final Quaternion getRotation(){
        return(rotation);
    }

    public final void setRotation(Quaternion rotation){
        this.rotation=rotation;
    }

    public final Vector3f getScale(){
        return(scale);
    }

    public final void setScale(Vector3f scale){
        this.scale=scale;
    }

    public final Vector3f getTranslation(){
        return(translation);
    }

    public final void setTranslation(Vector3f translation){
        this.translation=translation;
    }

    public final String getAlternativeTexturePath(){
        return(alternativeTexturePath);
    }

    public final void setAlternativeTexturePath(String alternativeTexturePath){
        this.alternativeTexturePath=alternativeTexturePath;
    }

    public final String getArtificialIntelligenceClassName(){
        return(artificialIntelligenceClassName);
    }

    public final void setArtificialIntelligenceClassName(String artificialIntelligenceClassName){
        this.artificialIntelligenceClassName=artificialIntelligenceClassName;
    }  
}
