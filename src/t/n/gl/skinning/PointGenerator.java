package t.n.gl.skinning;

public class PointGenerator {

	public static float[] generate(int pointCount) {
		float[] points = new float[pointCount * 3];

		  // 頂点の位置
		  for (int i = 0; i < pointCount * 3; ) {
			  //float r = (float)(Math.sqrt(2.0f * Math.random()));
			  //上は円党内にランダムに分布、下は表面だけに分布
			  float r = (float)(Math.sqrt(2.0f));
			  float t = (float)(Math.PI * 2 * Math.random());
			  points[i++] = (float)(r * Math.cos(t));
			  points[i++] = (float)(r * Math.sin(t));
			  points[i++] = (float)(Math.random());
		  }
		  return points;
	}

	//円筒の表面を覆うようなポリゴン座標を作る。
	//円筒の長さは1,直径は2に固定し、長さ方向はstacksに分割する。
	//ポリゴンの大きさは円周をslicesに分割する。
	//ある円周上の頂点と、その隣の円周上の点はポリゴンの大きさの半分だけずれている。
	public static float[] generateWireTriangle(int stacks, int slices) {
		float[] points = new float[slices * stacks * 3];
		int count = 0;
		float r = (float)(Math.sqrt(2.0f));
		float h = 1f / stacks;
		float x1 = (float) (Math.PI / slices);
		float x2 = x1 * 2f;

		  // 頂点の位置
		for (int i = 0; i < stacks * 1; i++) {
			//上は円党内にランダムに分布、下は表面だけに分布
			for(int j = 0; j < slices; j++) {
				float t = j * x2 + (i % 2 == 0 ? 0 : x1);
				points[count++] = (float)(r * Math.cos(t));
				points[count++] = (float)(r * Math.sin(t));
				points[count++] = h * i;
			}
		}
		//System.out.println("count:" + count + " , point size:" + points.length);

		return points;
	}

}
