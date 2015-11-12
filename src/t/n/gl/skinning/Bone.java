package t.n.gl.skinning;


public class Bone {

	private float x;
	private float y;
	private float z;
	private float w;
	private float u0;
	private float u1;
	private float u2;
	private float u3;
	private float length;
	private Bone parent;
	private float[] animation;
	private float[] blend;
	private float[] top;
	private float[] bottom;
//	private int startIndex;
//	private int endIndex;

	public void setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = 1f;
	}

	public void setPosition(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public void setRotation(float u0, float u1, float u2, float u3) {
		this.u0 = u0;
		this.u1 = u1;
		this.u2 = u2;
		this.u3 = u3;
	}

	public void setLength(float length) {
		this.length = length;
	}

	public void setParent(Bone parentBone) {
		this.parent = parentBone;
	}
//
//	public void drawBone(Bone bone, float[] fs, float[] fs2, float[] fs3) {
//		// TODO Auto-generated method stub
//
//	}

	public void setAnimation(float[] arg) {
		this.animation = arg;
	}

	public float getLength() {
		return length;
	}

	public float[] getPosition() {
		return new float[]{x, y, z, w};
	}

	public float[] getRotation() {
		return new float[]{u0, u1, u2, u3};
	}

	public float[] getAnimation() {
		return animation;
	}

	public Bone getParent() {
		return parent;
	}

	public void setBottom(float[] bottom) {
		this.bottom = bottom;
	}

	public float[] getBottom() {
		return bottom;
	}

	public void setTop(float[] top) {
		this.top = top;
	}

	public float[] getTop() {
		return top;
	}

	public void setBlend(float[] blend) {
		this.blend = blend;
	}

	public float[] getBlend() {
		return blend;
	}
//
//	public int startIndex() {
//		return startIndex;
//	}
//
//	public int endIndex() {
//		return endIndex;
//	}
//
//	public void setStartIndex(int startIndex) {
//		this.startIndex = startIndex;
//	}
//
//	public void setEndIndex(int endIndex) {
//		this.endIndex = endIndex;
//	}

}
