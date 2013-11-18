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
package jfpsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelConverterSet extends Tool{

	private static final long serialVersionUID=1L;
	
	private ArrayList<ModelConverter> modelConvertersList;
	
	public ModelConverterSet(){
		this("");
	}
	
	public ModelConverterSet(final String name){
		super(name);
		modelConvertersList=new ArrayList<>();
		markDirty();
	}
	
	public final void addModelConverter(final ModelConverter modelConverter){
		modelConvertersList.add(modelConverter);
        markDirty();
    }
    
    public final void removeModelConverter(final ModelConverter modelConverter){
    	modelConvertersList.remove(modelConverter);
        markDirty();
    }
    
    public final void removeAllModelConverters(){
    	modelConvertersList.clear();
        markDirty();
    }

    public final List<ModelConverter> getModelConvertersList(){
        return(Collections.unmodifiableList(modelConvertersList));
    }

    public final void setModelConvertersList(final ArrayList<ModelConverter> modelConvertersList){
        this.modelConvertersList=modelConvertersList;
        markDirty();
    }
}
