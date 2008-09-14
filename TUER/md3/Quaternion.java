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

package md3;

/*
 * Author: Thomas Hourdel
 * E-mail: thomas.hourdel@libertysurf.fr
 */

/** A quaternion class.
 */

public final class Quaternion{

    // This stores the 4D values for the quaternion
    private float x;
    
    private float y;
    
    private float z;
    
    private float w;


    // This is our default constructor, which initializes everything to an identity
    // quaternion.  An identity quaternion has x, y, z as 0 and w as 1.
    Quaternion(){ 
	x=0;
	y=0;
	z=0;   
	w=1;
    }

    // Creates a constructor that will allow us to initialize the quaternion when creating it
    Quaternion(float x,float y,float z,float w){ 
	this.x=x;		
	this.y=y;		
	this.z=z;		
	this.w=w;
    }


	/** Convert a quaternion to a rotation matrix.
	 *  @param pMatrix A 3D matrix.
	 */
	
    public final void createMatrix(float pMatrix[]){
	      // Make sure the matrix has allocated memory to store the rotation data.
	    if(pMatrix == null)
		    return;

	      // Fill in the rows of the 4x4 matrix, according to the quaternion to matrix equations.
	    pMatrix[ 0] = 1.0f - 2.0f * ( y * y + z * z );  
	    pMatrix[ 1] = 2.0f * ( x * y - w * z );  
	    pMatrix[ 2] = 2.0f * ( x * z + w * y );  
	    pMatrix[ 3] = 0.0f;  

	    pMatrix[ 4] = 2.0f * ( x * y + w * z );  
	    pMatrix[ 5] = 1.0f - 2.0f * ( x * x + z * z );  
	    pMatrix[ 6] = 2.0f * ( y * z - w * x );  
	    pMatrix[ 7] = 0.0f;  

	    pMatrix[ 8] = 2.0f * ( x * z - w * y );  
	    pMatrix[ 9] = 2.0f * ( y * z + w * x );  
	    pMatrix[10] = 1.0f - 2.0f * ( x * x + y * y );  
	    pMatrix[11] = 0.0f;  

	    pMatrix[12] = 0;  
	    pMatrix[13] = 0;  
	    pMatrix[14] = 0;  
	    pMatrix[15] = 1.0f;
    }


	
	/** Create a quaternion from a 3x3 or a 4x4 matrix, depending on rowColumnCount.
	 *  @param pTheMatrix A 3x3 or 4x4 matrix.
	 *  @param rowColumnCount The number of rows & columns.
	 */

	public final void createFromMatrix(float[] pTheMatrix, int rowColumnCount)
	{
		  // Make sure the matrix has valid memory and it's not expected that we allocate it.
		  // Also, we do a check to make sure the matrix is a 3x3 or a 4x4 (must be 3 or 4).
		if((pTheMatrix == null) || ((rowColumnCount != 3) && (rowColumnCount != 4)))
			return;

		  // Point the matrix pointer to the matrix passed in, assuming it's a 4x4 matrix
		float pMatrix[] = pTheMatrix;

		  // Create a 4x4 matrix to convert a 3x3 matrix to a 4x4 matrix (If rowColumnCount == 3)
		float m4x4[] = new float[16];

		  // If the matrix is a 3x3 matrix, then convert it to a 4x4
		if(rowColumnCount == 3)
		{
			  // Set the 9 top left indices of the 4x4 matrix to the 9 indices in the 3x3 matrix.
			  // It would be a good idea to actually draw this out so you can visualize it.
			m4x4[0]  = pTheMatrix[0];	m4x4[1]  = pTheMatrix[1];	m4x4[2]  = pTheMatrix[2];	m4x4[ 3] = 0f;
			m4x4[4]  = pTheMatrix[3];	m4x4[5]  = pTheMatrix[4];	m4x4[6]  = pTheMatrix[5];	m4x4[ 7] = 0f;
			m4x4[8]  = pTheMatrix[6];	m4x4[9]  = pTheMatrix[7];	m4x4[10] = pTheMatrix[8];	m4x4[11] = 0f;

			  // Since the bottom and far right indices are zero, set the bottom right corner to 1.
			  // This is so that it follows the standard diagonal line of 1's in the identity matrix.
			m4x4[12] = 0f;	m4x4[13] = 0f;	m4x4[14] = 0f;	m4x4[15] = 1;

			  // Set the matrix pointer to the first index in the newly converted matrix
			pMatrix = m4x4;
		}

		  // Find the diagonal of the matrix by adding up it's diagonal indices.
		  // This is also known as the "trace", but I will call the variable diagonal.
		float diagonal = pMatrix[0] + pMatrix[5] + pMatrix[10] + 1;
		float scale = 0.0f;

		  // If the diagonal is greater than zero
		if(diagonal > 0.00000001f)
		{
			  // Calculate the scale of the diagonal
			scale = (float)Math.sqrt(diagonal) * 2f;

			  // Calculate the x, y, z and w of the quaternion through the respective equation
			x = (pMatrix[9] - pMatrix[6]) / scale;
			y = (pMatrix[2] - pMatrix[8]) / scale;
			z = (pMatrix[4] - pMatrix[1]) / scale;
			w = 0.25f * scale;
		}
		else 
		{
			  // If the first element of the diagonal is the greatest value
			if(pMatrix[0] > pMatrix[5] && pMatrix[0] > pMatrix[10])  
			{	
				  // Find the scale according to the first element, and double that value
				scale  = (float)Math.sqrt(1.0f + pMatrix[0] - pMatrix[5] - pMatrix[10]) * 2.0f;

				  // Calculate the x, y, z and w of the quaternion through the respective equation
				x = 0.25f * scale;
				y = (pMatrix[4] + pMatrix[1]) / scale;
				z = (pMatrix[2] + pMatrix[8]) / scale;
				w = (pMatrix[9] - pMatrix[6]) / scale;	
			} 
			  // Else if the second element of the diagonal is the greatest value
			else if(pMatrix[5] > pMatrix[10]) 
			{
				  // Find the scale according to the second element, and double that value
				scale  = (float)Math.sqrt(1.0f + pMatrix[5] - pMatrix[0] - pMatrix[10]) * 2.0f;
					
				  // Calculate the x, y, z and w of the quaternion through the respective equation
				x = (pMatrix[4] + pMatrix[1]) / scale;
				y = 0.25f * scale;
				z = (pMatrix[9] + pMatrix[6]) / scale;
				w = (pMatrix[2] - pMatrix[8]) / scale;
			} 
			  // Else the third element of the diagonal is the greatest value
			else 
			{	
				  // Find the scale according to the third element, and double that value
				scale  = (float)Math.sqrt(1.0f + pMatrix[10] - pMatrix[0] - pMatrix[5]) * 2.0f;

				  // Calculate the x, y, z and w of the quaternion through the respective equation
				x = (pMatrix[2] + pMatrix[8]) / scale;
				y = (pMatrix[9] + pMatrix[6]) / scale;
				z = 0.25f * scale;
				w = (pMatrix[4] - pMatrix[1]) / scale;
			}
		}
	}


	
	/** Return a spherical linear interpolated quaternion between q1 and q2, with respect to t.
	 *  @param q1 First quaternion.
	 *  @param q2 Second quaternion.
	 *  @param t
	 */

    public final Quaternion slerp(Quaternion q1, Quaternion q2, float t){
	  // Create a local quaternion to store the interpolated quaternion.
	Quaternion qInterpolated = new Quaternion();

	  // Here we do a check to make sure the 2 quaternions aren't the same, return q1 if they are.
	if(q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) 
		return q1;

	  // Following the (b.a) part of the equation, we do a dot product between q1 and q2.
	  // We can do a dot product because the same math applied for a 3D vector as a 4D vector.
	float result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z) + (q1.w * q2.w);

	  // If the dot product is less than 0, the angle is greater than 90 degrees
	if(result < 0.0f)
	{
		  // Negate the second quaternion and the result of the dot product
		q2.x = -q2.x;
		q2.y = -q2.y;
		q2.z = -q2.z;
		q2.w = -q2.w;
		result = -result;
	}

	  // Set the first and second scale for the interpolation
	float scale0 = 1 - t;
	float scale1 = t;

	  // Check if the angle between the 2 quaternions was big enough to warrant such calculations
	if((1 - result) > 0.1f)
	{
		  // Get the angle between the 2 quaternions, and then store the sin() of that angle
		float theta = (float)Math.acos(result);
		float sinTheta = (float)Math.sin(theta);

		  // Calculate the scale for q1 and q2, according to the angle and it's sine value
		scale0 = (float)Math.sin((1 - t) * theta) / sinTheta;
		scale1 = (float)Math.sin((t * theta)) / sinTheta;
	}	

	  // Calculate the x, y, z and w values for the quaternion by using a special
	  // form of linear interpolation for quaternions.
	qInterpolated.x = (scale0 * q1.x) + (scale1 * q2.x);
	qInterpolated.y = (scale0 * q1.y) + (scale1 * q2.y);
	qInterpolated.z = (scale0 * q1.z) + (scale1 * q2.z);
	qInterpolated.w = (scale0 * q1.w) + (scale1 * q2.w);

	  // Return the interpolated quaternion
	return qInterpolated;
    }
	
    public String toString(){
        return("x="+x+" y="+y+" z="+z+" w="+w);
    }
    
    public boolean equals(Quaternion q){
        return(this.x==q.x && this.y==q.y && this.z==q.z && this.w==q.w);
    }
    
    float getX(){
        return(x);
    }
    
    float getY(){
        return(y);
    }
    
    float getZ(){
        return(z);
    }
    
    float getW(){
        return(w);
    }
    
    void setX(float x){
        this.x=x;
    }
    
    void setY(float y){
        this.y=y;
    }
    
    void setZ(float z){
        this.z=z;
    }
    
    void setW(float w){
        this.w=w;
    }
}
