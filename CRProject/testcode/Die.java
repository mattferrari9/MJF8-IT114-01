package CRProject.testcode;

public class Die {

	private int faceValue;
	
	//default constructor
	public Die() {
		faceValue = 6;
	}
	
	//non-default constructor
	public Die(int face) {
		faceValue = face;
	}
	
	//roll method
	public int roll() {
		faceValue = (int)(Math.random()*6) + 1;
		return faceValue;
	}
	
	//getter method
	public int getFaceValue() {
		return faceValue;
	}
	
	//setter method
	public void setFaceValue(int newFace) {
		faceValue = newFace;
	}
	
	//toString method
	public String toString() {
		return "Die with face: " + faceValue;
	}
	
}

