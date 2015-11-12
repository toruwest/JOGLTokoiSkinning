package util;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class MyMatrixUtil2 {

    public final static float[] getIdentityMatrix3() {
    	return new float[] {
            1.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
    	};
    }

    public final static float[] getIdentityMatrix4() {
    	return new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    	};
    }

    public static float[] translate(float[] m,float x,float y,float z, float w){
        float[] t = translateMatrix(x, y, z, w);
        return multiplyMatrix4(m, t);
    }

    public static float[] translate(float[] m,float x, float y,float z){
    	if(m.length == 16) {
    		float[] t = translateMatrix(x, y, z, 1f);
    		return multiplyMatrix4(m, t);
    	} else if(m.length == 9) {
    		float[] t = translateMatrix(x, y, z);
    		return multiplyMatrix3(m, t);
    	} else {
    		return null;
    	}
    }

	public static float[] translate(float[] m, float[] xyz) {
		if(m.length == 16) {
			return multiplyMatrix4(m, translateMatrix4(xyz));
		} else if(m.length == 9) {
			return multiplyMatrix3(m, translateMatrix4(xyz));
		} else {
			return null;
		}
	}

	public static float[] translateMatrix3(float[] xyz) {
		float x = 0f, y = 0f, z = 0f, w = 0f;
		if(xyz.length == 3) {
			x = xyz[0];
			y = xyz[1];
			z = xyz[2];
			return translateMatrix(x, y, z);
		}
//		if(xyzw.length == 4) {
//			w = xyzw[3];
//			return translateMatrix(x, y, z, w);
//		}
		return null;
	}

	public static float[] translateMatrix4(float[] xyzw ) {
		float x = 0f, y = 0f, z = 0f, w = 0f;
		if(xyzw.length >= 3) {
			x = xyzw[0];
			y = xyzw[1];
			z = xyzw[2];
		}
		if(xyzw.length == 3) {
			return translateMatrix(x, y, z, 1f);
		} else if(xyzw.length == 4) {
			w = xyzw[3];
			return translateMatrix(x, y, z, w);
		}
		return null;
	}

	public static float[] translateMatrix(float x, float y, float z) {
//		return translateMatrix(x, y, z, 1f);
		return new float[]{
				1.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 1.0f,
                x, y, z};
	}

	public static float[] translateMatrix(float x, float y, float z, float w) {
		return new float[]{ 1.0f, 0.0f, 0.0f, 0.0f,
                 0.0f, 1.0f, 0.0f, 0.0f,
                 0.0f, 0.0f, 1.0f, 0.0f,
                 x, y, z, w };
	}

	public static float[] rotateY(float[] m, float a) {
		float[] r = rotateYMatrix(a);
        return multiplyMatrix4(m, r);
	}

    public static float[] rotate(float[] m, double a, float x, float y, float z){
        float[] r = rotateMatrix(a, x, y, z);
        return multiplyMatrix4(m, r);
    }

	public static float[] rotate(float[] m, float[] rotation) {
		float[] r = rotateMatrix(rotation[0], rotation[1], rotation[2], rotation[3]);//rotateMatrix(a, x, y, z);
		return multiplyMatrix4(m, r);
	}

	public static float[] rotateMatrix(double a, float axisX, float axisY, float axisZ) {
		float s, c;
		float x = axisX;
		float y = axisY;
		float z = axisZ;
        s = (float)Math.sin(Math.toRadians(a));
        c = (float)Math.cos(Math.toRadians(a));
        float[] r = {
            x * x * (1.0f - c) + c,     y * x * (1.0f - c) + z * s, x * z * (1.0f - c) - y * s, 0.0f,
            x * y * (1.0f - c) - z * s, y * y * (1.0f - c) + c,     y * z * (1.0f - c) + x * s, 0.0f,
            x * z * (1.0f - c) + y * s, y * z * (1.0f - c) - x * s, z * z * (1.0f - c) + c,     0.0f,
            0.0f, 0.0f, 0.0f, 1.0f };
		return r;
	}

	public static float[] rotateYMatrix(double a) {
		float s = (float)Math.sin(Math.toRadians(a));
		float c = (float)Math.cos(Math.toRadians(a));
		float[] r =	{
				c, 0, -s, 0,
				0, 1, 0, 0,
				s,0, c, 0,
				0, 0, 0, 1};
		return r;
	}

    public static float[] scale(float[] m, float scale) {
    	if(m.length != 16) return null;
		Matrix4f mat = new Matrix4f(m);
		Matrix4f s = new Matrix4f();
		s.set(scale);
//		s.m33 = 1.0f;
		mat.mul(scale);
		return toArray(mat);
    }

	public static float[] applyScale(float[] m, float x, float y, float z, float w) {
//		return projection4(m, new float[]{x,y,z,w});
		return tokoiMultiplyMatrix4(m, getScaledMatrix(new float[]{x,y,z,w}));
	}

    public static float[] multiply(float[] a, float[] b){
    	int matDim = a.length;

        if(matDim == 9 && b.length == 9) {
        	return multiplyMatrix3(a, b);
        } else if(matDim == 9 && b.length == 3) {
        	return projection3(a, b);
        } else if(matDim == 16 && b.length == 16) {
        	return multiplyMatrix4(a, b);
        } else if(matDim == 16 && b.length == 4) {
        	return projection4(a, b);
        } else {
        	throw new IllegalArgumentException("警告：multiply()の引数が9でも16でもありません。");
        }
    }

	public static float[] multiplyMatrix4(float[] a, float[] b) {
    	Matrix4f matA = new Matrix4f(a);
    	Matrix4f matB = new Matrix4f(b);
    	matA.mul(matB);
    	return toArray(matA);
	}

	/**
	 * 行列 a と b の積を求める
	 * multiplyMatrix4()とは14番目,15番目の結果が異なる。
	 */
	public static float[] tokoiMultiplyMatrix4(final float[] a, final float[] b) {
		float[] result = new float[16];
		for (int i = 0; i < 16; i++) {
			int j = i & ~3, k = i & 3;
			result[i] =
					a[ 0 + k] * b[j + 0]
				  + a[ 4 + k] * b[j + 1]
				  + a[ 8 + k] * b[j + 2]
				  + a[12 + k] * b[j + 3];
		}
		return result;
	}

	private static float[] multiplyMatrix3(float[] a, float[] b) {
    	Matrix3f matA = new Matrix3f(a);
//    	matA.transpose();
    	Matrix3f matB = new Matrix3f(b);
//    	matB.transpose();
    	matA.mul(matB);
    	return toArray(matA);
	}

	private static void glMultMatrixf3(FloatBuffer a, FloatBuffer b, FloatBuffer d) {
	    final int aP = a.position();
        final int bP = b.position();
        final int dP = d.position();
        for (int i = 0; i < 3; i++) {
            final float ai0=a.get(aP+i+0*3),  ai1=a.get(aP+i+1*3),  ai2=a.get(aP+i+2*3);
            d.put(dP+i+0*3 , ai0 * b.get(bP+0+0*3) + ai1 * b.get(bP+1+0*3) + ai2 * b.get(bP+2+0*3) );
            d.put(dP+i+1*3 , ai0 * b.get(bP+0+1*3) + ai1 * b.get(bP+1+1*3) + ai2 * b.get(bP+2+1*3) );
            d.put(dP+i+2*3 , ai0 * b.get(bP+0+2*3) + ai1 * b.get(bP+1+2*3) + ai2 * b.get(bP+2+2*3) );
        }
	}

    private static void glMultMatrixf4(FloatBuffer a, FloatBuffer b, FloatBuffer d) {
        final int aP = a.position();
        final int bP = b.position();
        final int dP = d.position();
        for (int i = 0; i < 4; i++) {
            final float ai0=a.get(aP+i+0*4),  ai1=a.get(aP+i+1*4),  ai2=a.get(aP+i+2*4),  ai3=a.get(aP+i+3*4);
            d.put(dP+i+0*4 , ai0 * b.get(bP+0+0*4) + ai1 * b.get(bP+1+0*4) + ai2 * b.get(bP+2+0*4) + ai3 * b.get(bP+3+0*4) );
            d.put(dP+i+1*4 , ai0 * b.get(bP+0+1*4) + ai1 * b.get(bP+1+1*4) + ai2 * b.get(bP+2+1*4) + ai3 * b.get(bP+3+1*4) );
            d.put(dP+i+2*4 , ai0 * b.get(bP+0+2*4) + ai1 * b.get(bP+1+2*4) + ai2 * b.get(bP+2+2*4) + ai3 * b.get(bP+3+2*4) );
            d.put(dP+i+3*4 , ai0 * b.get(bP+0+3*4) + ai1 * b.get(bP+1+3*4) + ai2 * b.get(bP+2+3*4) + ai3 * b.get(bP+3+3*4) );
        }
    }

    /*
     *
     * http://miffysora.wikidot.com/ja:matrix
     */
    public static float[] gluFrustum(float left, float right, float bottom, float top, float nearVal, float farVal) {
    	float[] matrix = new float[16];
    	float w = right - left;
    	float h = top - bottom;
    	float depth = farVal - nearVal;
    	matrix[0] = -2f / w;
    	matrix[1] = matrix[2] = matrix[3] = 0f;
    	matrix[4] = 0;
    	matrix[5] = 2 * nearVal / h;
    	matrix[6] = matrix[7] = 0;
    	matrix[8] = (right + left) / w;
    	matrix[9] = (top + bottom) / h;
    	matrix[10] = -(farVal + nearVal) / depth;
    	matrix[11] = -1;
    	matrix[12] = matrix[13] = 0;
    	matrix[14] = -2 * (farVal * nearVal) / depth;
    	matrix[15] = 0;

    	return matrix;
    }

    public static float[] gluPerspective(float left, float right, float bottom, float top, float nearVal, float farVal) {
    	float[] matrix = new float[16];
    	return matrix;
    }

    public static float[] glOrtho(float left, float right, float bottom, float top, float near, float far) {
    	float[] matrix = new float[16];
    	return matrix;
    }

    /*
	 ** 視野変換行列を求める
	 */
	public static float[] lookAt(float ex, float ey, float ez,
			float tx, float ty, float tz,
			float ux, float uy, float uz
			) {
		float[] matrix = new float[16];
		float l;

		/* z 軸 = e - t */
		tx = ex - tx;
		ty = ey - ty;
		tz = ez - tz;
		l = (float)Math.sqrt(tx * tx + ty * ty + tz * tz);
		matrix[ 2] = tx / l;
		matrix[ 6] = ty / l;
		matrix[10] = tz / l;

		/* x 軸 = u x z 軸 */
		tx = uy * matrix[10] - uz * matrix[ 6];
		ty = uz * matrix[ 2] - ux * matrix[10];
		tz = ux * matrix[ 6] - uy * matrix[ 2];
		l = (float)Math.sqrt(tx * tx + ty * ty + tz * tz);
		matrix[ 0] = tx / l;
		matrix[ 4] = ty / l;
		matrix[ 8] = tz / l;

		/* y 軸 = z 軸 x x 軸 */
		matrix[ 1] = matrix[ 6] * matrix[ 8] - matrix[10] * matrix[ 4];
		matrix[ 5] = matrix[10] * matrix[ 0] - matrix[ 2] * matrix[ 8];
		matrix[ 9] = matrix[ 2] * matrix[ 4] - matrix[ 6] * matrix[ 0];

		/* 平行移動 */
		matrix[12] = -(ex * matrix[ 0] + ey * matrix[ 4] + ez * matrix[ 8]);
		matrix[13] = -(ex * matrix[ 1] + ey * matrix[ 5] + ez * matrix[ 9]);
		matrix[14] = -(ex * matrix[ 2] + ey * matrix[ 6] + ez * matrix[10]);

		/* 残り */
		matrix[ 3] = matrix[ 7] = matrix[11] = 0.0f;
		matrix[15] = 1.0f;

		return matrix;
	}

	/*
	 ** 画角から透視投影変換行列を求める
	 */
	public static float[] cameraMatrix(float fovy, float aspect, float near, float far) {
		float[] matrix = new float[16];
		float f = (float) (1.0f / Math.tan(fovy * 0.5f * 3.141593f / 180.0f));
		float dz = far - near;

		matrix[ 0] = f / aspect;
		matrix[ 5] = f;
		matrix[10] = -(far + near) / dz;
		matrix[11] = -1.0f;
		matrix[14] = -2.0f * far * near / dz;
		matrix[ 1] = matrix[ 2] = matrix[ 3] = matrix[ 4] =
				matrix[ 6] = matrix[ 7] = matrix[ 8] = matrix[ 9] =
				matrix[12] = matrix[13] = matrix[15] = 0.0f;
		return matrix;
	}

//	/*
//	 ** 行列 m0 と m1 の積を求める
//	 */
//	public static void multiplyMatrix(final float[] m0, final float[] m1, float[] matrix) {
//		for (int i = 0; i < 16; ++i) {
//			int j = i & ~3, k = i & 3;
//
//			matrix[i] = m0[j + 0] * m1[ 0 + k]
//					+ m0[j + 1] * m1[ 4 + k]
//							+ m0[j + 2] * m1[ 8 + k]
//									+ m0[j + 3] * m1[12 + k];
//		}
//
//	}

	public static float[] copy(float[] copySrc) {
    	float[] t = new float[copySrc.length];
    	for(int i = 0; i < copySrc.length; i++) {
    		t[i] = copySrc[i];
    	}

		return t;
	}

	public static void dump(float[] m) {
		for (int i = 0; i < m.length; ++i) {
			System.out.print(m[i] + ",");
		}
		System.out.println();
	}

	public static void dump(String msg, float[] m) {
		System.out.println(msg + ":");
		int c = 0;
		int j = 0;
		if(m.length == 16) {
			c = 4;
		} else if(m.length == 9) {
			c = 3;
		}
		for (int i = 0; i < m.length; ++i) {
			System.out.print(m[i] + ", ");
			if(++j == c) {
				System.out.println();
				j = 0;
			}
		}
		System.out.println();
	}

	public static float[] inverse(float[] matrix) {
		if(matrix.length != 16) return null;
		Matrix4f mat = new Matrix4f(matrix);
		double det = mat.determinant();
		if(Math.abs(det) > DELTA) {
			mat.invert();
			return toArray(mat);
		} else { //分母がゼロになるので計算不能。
			return null;
		}
	}

	public static Matrix4f inverseV(float[] matrix) {
		if(matrix.length != 16) return null;
		Matrix4f mat = new Matrix4f(matrix);
		double det = mat.determinant();
		if(Math.abs(det) > DELTA) {
			mat.invert();
			return mat;
		} else { //分母がゼロになるので計算不能。
			return null;
		}
	}

	public static float[] toArray(Vector3f v) {
		float[] result = new float[3];
		result[0] = v.x;
		result[1] = v.y;
		result[2] = v.z;
		return result;
	}

	public static float[] toArray(Matrix3f mat) {
		float[] result = new float[9];
		float[][] tmp = new float[3][3];
		mat.getRow(0, tmp[0]);
		mat.getRow(1, tmp[1]);
		mat.getRow(2, tmp[2]);
		for(int i = 0; i < 9; i++) {
			result[i] = tmp[i/3][i % 3];
		}
		return result;
	}

	public static float[] toArray(Vector4f v) {
		float[] result = new float[4];
		result[0] = v.x;
		result[1] = v.y;
		result[2] = v.z;
		result[3] = v.w;
		return result;
	}

	public static float[] toArray(Matrix4f mat) {
		float[] result = new float[16];
		float[][] tmp = new float[4][4];
		mat.getRow(0, tmp[0]);
		mat.getRow(1, tmp[1]);
		mat.getRow(2, tmp[2]);
		mat.getRow(3, tmp[3]);
		for(int i = 0; i < 16; i++) {
			result[i] = tmp[i/4][i % 4];
		}
		return result;
	}

	private static final double DELTA = 1e-5;

	public static float[] transpose(float[] matrix) {
		if(matrix.length != 16) return null;
		Matrix4f mat = new Matrix4f(matrix);
		mat.transpose();
		return toArray(mat);
	}

	public static float[] transposeV(Matrix4f matrix) {
		if(matrix == null) return null;
//		Matrix4f mat = new Matrix4f(matrix);
		matrix.transpose();
		return toArray(matrix);
	}

	public static float[] negate(float[] matrix) {
		if(matrix.length == 0) return null;
//		if(matrix.length == 16) {
//			Matrix4f mat = new Matrix4f(matrix);
//			mat.negate();
//			return toArray(mat);
//		} else if(matrix.length == 9) {
//			Matrix3f mat = new Matrix3f(matrix);
//			mat.negate();
//			return toArray(mat);
//		} else {
			float[] result = new float[matrix.length];
			for(int i = 0; i < matrix.length; i++) {
				result[i] = -matrix[i];
			}
			return result;
//		}
	}

	public static float[] normalize(float[] matrix) {
		if(matrix.length != 16) return null;
		Matrix4f mat = new Matrix4f(matrix);
		Matrix3f rotscale = new Matrix3f();
		mat.getRotationScale(rotscale);
		rotscale.normalize();
		mat.setRotationScale(rotscale);
		return toArray(mat);
	}

	//行列mとベクトルvの積を返す。
	public static float[] projection3(float[] m, float[] v) {
		float[] result = new float[3];
		if(m.length == 9 && v.length == 3) {
			for (int i = 0; i < 3; i++) {
				result[i] =
					   m[ 0 + i] * v[0]
					 + m[ 3 + i] * v[1]
					 + m[ 6 + i] * v[2];
			}
		} else {
			throw new IllegalArgumentException("引数の配列の要素数が9個ではありません。");
		}
		return result;
	}

	//行列mとベクトルvの積をベクトルとして返す。
	public static float[] projection4(float[] m, float[] v) {
		float[] result = new float[4];
		if(m.length == 16 && v.length == 4) {
			for (int i = 0; i < 4; i++) {
				result[i] = m[ 0 + i] * v[0]
					 + m[ 4 + i] * v[1]
					 + m[ 8 + i] * v[2]
					 + m[12 + i] * v[3];
			}
		} else {
			throw new IllegalArgumentException("引数の行列の要素数が16個ではないか、ベクトルの要素数が4個ではありません。");
		}
		return result;
	}

	public static float[] getScaledMatrix(float s) {
		float[] t = {
				s, 0.0f, 0.0f, 0.0f,
                0.0f, s, 0.0f, 0.0f,
                0.0f, 0.0f, s, 0.0f,
                0, 0, 0, 1.0f };
		return t;
	}

	public static float[] getScaledMatrix(float[] v) {
		float[] t = null;
		if(v.length == 4) {
			t = new float[] {
				v[0], 0.0f, 0.0f, 0.0f,
                0.0f, v[1], 0.0f, 0.0f,
                0.0f, 0.0f, v[2], 0.0f,
                0.0f, 0.0f, 0.0f, v[3]};
		} else if(v.length == 3) {
			t = new float[] {
				v[0], 0.0f, 0.0f, 0.0f,
                0.0f, v[1], 0.0f, 0.0f,
                0.0f, 0.0f, v[2], 0.0f,
                0.0f, 0.0f, 0.0f, 1f };
		} else {
			throw new IllegalArgumentException("引数のベクトルの要素数が3個または4個ではありません。");
		}
		return t;
	}

	public static float[]  getRow(float[] m, int rowNo) {
		int l = m.length;
		float[] result = null;
		if(l == 9) {
			result = new float[3];
			for(int i = 0; i < 3; i++) {
				result[i] = m[rowNo * 3 + i];
			}
		} else if(l == 16) {
			result = new float[4];
			for(int i = 0; i < 4; i++) {
				result[i] = m[rowNo * 4 + i];
			}
		}
		return result;
	}

	public static float[] getColumn(float[] m, int colNo) {
		int l = m.length;
		float[] result = null;
		if(l == 9) {
			result = new float[3];
			for(int i = 0; i < 3; i++) {
				result[i] = m[colNo + 3*i];
			}
		} else if(l == 16) {
			result = new float[4];
			for(int i = 0; i < 4; i ++) {
				result[i] = m[colNo + 4*i];
			}
		}
		return result;
	}

}
