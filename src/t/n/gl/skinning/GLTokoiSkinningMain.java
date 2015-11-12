/**
 * http://marina.sys.wakayama-u.ac.jp/~tokoi/?date=20091231をJOGLに移植
 */

package t.n.gl.skinning;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.swing.JFrame;

import t.n.glsl.MyShaderUtil;
import util.MyMatrixUtil2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

public class GLTokoiSkinningMain implements GLEventListener, MouseWheelListener, KeyListener {
	private final JFrame frame;
	private final GLCanvas canvas;////GLJPanelだとだめ
	protected GLCapabilities caps;

	// 光源
	// 光源の位置
	private static FloatBuffer lightpos;
	// 直接光強度
	private static FloatBuffer lightcol;
	// 影内の拡散反射強度
	private static float lightdim[] = { 0.2f, 0.2f, 0.2f, 1.0f };
	// 影内の鏡面反射強度
	private static float lightblk[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	// 環境光強度
	private static FloatBuffer lightamb;// = { 0.1f, 0.1f, 0.1f, 1.0f };

	private float aspect;
//	private final int[] pointBuffer = new int[1];

	private static final int WIN_HEIGHT = 500;
	private static final int WIN_WIDTH = 500;

	// アニメーションのサイクル
//	private final static int FRAMES = 600;
	private int bProgram;
	private int pProgram;
	private boolean isInitDone = false;

	private float viewScale = 2f;
	private int prevMouseX = -1;
	//別のシェーダーなので同じ番号を使っても大丈夫。
	private final int attribPosition = 0;
//	private final int attribBone = 0;
	private final int attribIndices = 1;
//	private final int attribPoints = 2;

	private final boolean isDrawPoints = true;
//	private final boolean isDrawPoints = false;
	//以下はfalseにすると動かない。falseだとVAOを使うようにしたかった？
	//"com.jogamp.opengl.GLException: array vertex_buffer_object must be unbound to call this method"という例外が起こる。
	private final boolean isUseVBO = true;
//	private final boolean isUseVBO = false;
	private int[] boneBuffers;
	private int[] pointBuffers;
	private FloatBuffer boneVertexBuf;
	private IntBuffer indicesBuffer;

	private static float[] projectionMatrix;
	private static float[] viewMatrix;
	private static int modelViewProjectionMatrixLocation;
	private static int modelViewMatrixLocation;
	private static int projectionMatrixLocation;

	//shader
	private static final String BONE_VERT_SRC = "shaders/bone.vert";
	private static final String BONE_FRAG_SRC = "shaders/bone.frag";
	private static final String BLEND_VERT_SRC = "shaders/vertexblend.vert";
	private static final String SIMPLE_FRAG_SRC = "shaders/simple.frag";

	// ボーン
	private static final int BONES_COUNT = 2;
	private static final int POINTS_COUNT = 5000;
	private static Bone bone[];
	private static int numberOfBonesLocation;
	private static int boneBottomLocation;
	private static int boneTopLocation;
	private static int blendMatrixLocation;
	private final Animator animator;
	protected boolean isBoneAngleChanded = true;

	public static void main(String[] args) {
		System.out.println("根元のパーツを操作するにはコントロールキーを押しながらドラッグしてください。");
		new GLTokoiSkinningMain();
	}

	public GLTokoiSkinningMain() {
		GLProfile prof = GLProfile.get(GLProfile.GL2);
		caps = new GLCapabilities(prof);
		setupData();

		initLight();

		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);

		frame = new JFrame(getClass().getSimpleName());

		frame.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
		frame.getContentPane().add(canvas);
		frame.addKeyListener(this);
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				prevMouseX = -1;
			}
		});

		canvas.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				int x = evt.getX() ;
				float rotDelta = 0;
				if(prevMouseX != -1) {
					rotDelta = (x - prevMouseX);
					isBoneAngleChanded = true;
					if(evt.isControlDown()) {
						boneAngle[0] -= rotDelta;
						System.out.println("0:" + boneAngle[0]);
					} else {
						boneAngle[1] -= rotDelta;
						System.out.println("1:" + boneAngle[1]);
					}
				}
				frame.repaint();

				// 現在のマウスの位置を保存
				prevMouseX = x;
			}

			@Override
			public void mouseMoved(MouseEvent e) {}
		});
		frame.addMouseWheelListener(this);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						quit();
					}
				}).start();
			}
		});

		frame.pack();
		frame.setVisible(true);
		frame.requestFocus();
		animator = new Animator(canvas);
		animator.start();
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl2 = drawable.getGL().getGL2();
		showGLInfo(drawable, gl2);
        gl2.glClearColor(1f, 1f, 1f, 1f);
        gl2.glPointSize(2.0f);
        gl2.glClearDepth(1.0f);
        gl2.glEnable(GL.GL_DEPTH_TEST);
        gl2.glDepthFunc(GL.GL_LEQUAL);

        // シェーダプログラムのコンパイル／リンク結果を得る変数
		IntBuffer compiled0 = Buffers.newDirectIntBuffer(1);
		IntBuffer linked0 = Buffers.newDirectIntBuffer(1);
		IntBuffer compiled1 = Buffers.newDirectIntBuffer(1);
		IntBuffer linked1 = Buffers.newDirectIntBuffer(1);

		  // シェーダプログラムの読み込み
		try {
			String[] boneVertFile = MyShaderUtil.readFile(BONE_VERT_SRC);
			String[] boneFragFile = MyShaderUtil.readFile(BONE_FRAG_SRC);
			String[] vertexBlendVertFile = MyShaderUtil.readFile(BLEND_VERT_SRC);
			String[] simpleFragFile = MyShaderUtil.readFile(SIMPLE_FRAG_SRC);

//			bProgram = initShaders(gl2, boneVert, boneFrag, compiled0, linked0);
			bProgram = initShaders(gl2, boneVertFile, boneFragFile, compiled0, linked0);
			if(isDrawPoints) {
//				pProgram = initShaders(gl2, vertexBlendVert, simpleFrag, compiled1, linked1);
				pProgram = initShaders(gl2, vertexBlendVertFile, simpleFragFile, compiled1, linked1);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		/* uniform 変数 modelViewProjectionMatrix の場所を得る */
//		gl2.glUseProgram(bProgram);不要

		modelViewProjectionMatrixLocation = gl2.glGetUniformLocation(bProgram, "modelViewProjectionMatrix");

		if(isDrawPoints) {
			modelViewMatrixLocation = gl2.glGetUniformLocation(pProgram, "modelViewMatrix");
			projectionMatrixLocation = gl2.glGetUniformLocation(pProgram, "projectionMatrix");

			/* バーテックスブレンディング用の uniform 変数の場所を得る */
			numberOfBonesLocation = gl2.glGetUniformLocation(pProgram, "numberOfBones");
			boneBottomLocation = gl2.glGetUniformLocation(pProgram, "boneBottom");
			boneTopLocation = gl2.glGetUniformLocation(pProgram, "boneTop");
			blendMatrixLocation = gl2.glGetUniformLocation(pProgram, "blendMatrix");
		}

		/* 視野変換行列を求める */
		//viewMatrix.loadIdentity();
		//viewMatrix.lookat(0.0f, -7.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
		//		  viewMatrix = new Matrix4f();
		//		  viewMatrix.setRotation(new Matrix3f(0.0f, -7.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f));
		//		  viewMatrix = MyMatrixUtil2.identityMatrix4;
		viewMatrix = MyMatrixUtil2.lookAt(0.0f, -7.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

		/* 一つ目のボーンの設定 */
		bone[0].setPosition(0.0f, 0.0f, -1.5f);
		bone[0].setRotation(0.0f, 0.0f, 1.0f, 0.0f);
		bone[0].setLength(1.5f);
		bone[0].setParent(null);         // 根元のボーン

		/* 二つ目のボーンの設定 */
		bone[1].setPosition(0.0f, 0.0f, 1.5f);
		bone[1].setRotation(0.0f, 0.0f, 1.0f, 0.0f);
		bone[1].setLength(1.5f);
		bone[1].setParent(bone[0]);  // bone[0] を親とする

//		vaos = new int[isDrawPoints?2:1];
//		gl2.glGenVertexArrays(vaos.length, vaos, 0);

//		gl2.glBindVertexArray(vaos[0]);
		if(isUseVBO ) {
//			gl2.glUseProgram(bProgram);
			initBoneVBO(gl2);
		}
		if(isDrawPoints) {
//			gl2.glBindVertexArray(vaos[1]);
//			gl2.glUseProgram(pProgram);
			disseminate(gl2, POINTS_COUNT);
		}
//		gl2.glUseProgram(0);

//		gl2.glBindVertexArray(0);
		isInitDone = true;
	}

	//gl2.glUseProgram(...)について、useVBO=trueとしている状態においては
	//init()から削除して、display()の中でだけ行うようにしても大丈夫だった。
	//VAOを使う場合はinit()でやっておかないと駄目かもしれない。

	@Override
	public void display(GLAutoDrawable drawable) {
		if(!isInitDone) return;
		GL2 gl2 = drawable.getGL().getGL2();
		gl2.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl2.glEnable(GL2.GL_DEPTH_TEST);
		// ボーンのアニメーション
		gl2.glUseProgram(bProgram);
		for (int i = 0; i < BONES_COUNT; i++) {
			float[] animationMatrix = MyMatrixUtil2.getIdentityMatrix4();
			//y軸周りに回転
			//animationMatrix.loadRotate(0.0f, 1.0f, 0.0f, boneAngle[i]);
			animationMatrix = MyMatrixUtil2.rotateY(animationMatrix, boneAngle[i]);
			if(isBoneAngleChanded) {
				System.out.println("index:" + i + ", angle:" + boneAngle[i]);
				MyMatrixUtil2.dump("animationMatrix", animationMatrix);
			}
			bone[i].setAnimation(animationMatrix);
			//Cではfloat[]を呼び出し先で上書きできるが、Javaでは不可。（配列の個々の要素ならできる）
			//そこでboneにfieldとgetter/setterを追加し、呼び出し先でセットする。
			drawBone(gl2, bone[i]);
		}
		isBoneAngleChanded = false;
		if(isDrawPoints) {
			gl2.glUseProgram(pProgram);
			drawPoints(gl2);
		}
		gl2.glUseProgram(0);
		gl2.glDisable(GL2.GL_DEPTH_TEST);
	}

	/**
	 ** ボーンの描画
	 */
	private void drawBone(GL2 gl2, Bone bone) {
		float[] bottom;
		float[] top;
		float[] blend;
		// ボーンの長さに合わせてスケーリングする変換行列
		float[] scale = MyMatrixUtil2.getScaledMatrix(bone.getLength());

		//boneの根元の位置
		float[] initial = MyMatrixUtil2.getIdentityMatrix4();
		//boneの先端の位置
		float[] animated = MyMatrixUtil2.getIdentityMatrix4();

		// 現在の視野変換行列をかけておく
		//オリジナルではtargetではなくboneだったが、while()の行でnullになると
		//後のほうでboneを使えなくて困るのでtargetに代入することにした。
		//後で見返したけど、何をやっているかわからない。おそらく、boneを先端から根元まで
		//たどっていって、それぞれのboneの根元と先端の位置を計算しているのだと思う。
		Bone target = bone;
		do {
			//3つのfloat配列を平行移動のためのパラメータと見なし、4x4の行列tempを生成する。
			float[] temp = MyMatrixUtil2.translateMatrix4(target.getPosition());
			temp = MyMatrixUtil2.rotate(temp, target.getRotation());
			initial = MyMatrixUtil2.tokoiMultiplyMatrix4(temp, initial);
			temp = MyMatrixUtil2.tokoiMultiplyMatrix4(temp, target.getAnimation());
			//C言語版:animated = temp.multiply(animated);
			animated = MyMatrixUtil2.tokoiMultiplyMatrix4(temp, animated);
		} while ((target = target.getParent()) != null);

		// ボーンの初期位置における根元と先端の位置を求める
		initial = MyMatrixUtil2.tokoiMultiplyMatrix4(viewMatrix, initial);
		animated =  MyMatrixUtil2.tokoiMultiplyMatrix4(viewMatrix, animated);

		//Ｃ言語版:initial.projection(bottom, boneVertex[0]);//行列とベクトルの積
		//上のC言語のメソッドではインスタンスinitialの値とboneVertex[0]の積が計算され、
		//結果はメソッドの引数bottomに格納される。Javaへの移植にあたり、引数は変えずに戻り値として
		//新しい行列を返すようにした。
		bottom = MyMatrixUtil2.projection4(initial, boneVertex[0]);
		bone.setBottom(bottom);
		//initialは変化してはいけないので、別の一時変数を使う。
		float[] initial2 = MyMatrixUtil2.tokoiMultiplyMatrix4(initial, scale);
		//行列とベクトルの積
		top = MyMatrixUtil2.projection4(initial2, boneVertex[5]);
		bone.setTop(top);
		// バーテックスブレンディング用の変換行列
		//C言語:memcpy(blend, (animated.multiply(initial.invertMatrix())).get(), sizeof blend[0] * 16);//第2引数から第1引数へ第3引数のbyte数だけcopy
		float[] invInitial = MyMatrixUtil2.inverse(initial);
		//逆行列と元の行列を掛けたら単位行列になるはずなので確認する。
//		float[] check = MyMatrixUtil2.multiplyMatrix4(initial, invInitial);
//		float[] check = MyMatrixUtil2.tokoiMultiplyMatrix4(initial, invInitial);
		blend = MyMatrixUtil2.tokoiMultiplyMatrix4(animated, invInitial);
		bone.setBlend(blend);

		// ボーンを描画する

		if(isUseVBO) {
			//bufferをinit()であらかじめ転送する場合。

			// 頂点データの場所を指定する
			gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, boneBuffers[0]);
			gl2.glEnableVertexAttribArray(attribPosition);
			gl2.glVertexAttribPointer(attribPosition, 4, GL2.GL_FLOAT, false, 0, 0);
			// 頂点のインデックスの場所を指定
			gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, boneBuffers[1]);
			gl2.glEnableVertexAttribArray(attribIndices);
			gl2.glVertexAttribPointer(attribIndices, 4, GL2.GL_FLOAT, false, 0, 0);

			//ボーンの角度を設定するところがなかったので、追加してみた。
			float[] mat = MyMatrixUtil2.tokoiMultiplyMatrix4(projectionMatrix, animated);
			mat = MyMatrixUtil2.tokoiMultiplyMatrix4(mat, scale);

			//以下の第２引数は16だと思うけど、1にしないと動かない。
			gl2.glUniformMatrix4fv(modelViewProjectionMatrixLocation, 1, false, mat, 0);
			if(isBoneAngleChanded) {
				MyMatrixUtil2.dump("modelViewProjectionMatrix", mat);
			}

			gl2.glDrawElements(GL2.GL_LINE_LOOP, indicesBuffer.limit(), GL2.GL_UNSIGNED_INT, 0);

			gl2.glDisableVertexAttribArray(attribPosition);
		} else {
			//オリジナルはdisplay()の中で転送しているので、まずそれにあわせる
			gl2.glEnableVertexAttribArray(0);
			gl2.glVertexAttribPointer(0, 4, GL2.GL_FLOAT, false, 0, boneVertexBuf);
//			MyMatrixUtil2.dump("projectionMatrix", projectionMatrix);
//			MyMatrixUtil2.dump("animated", animated);
			float[] mat = MyMatrixUtil2.multiply(projectionMatrix, animated);
			mat = MyMatrixUtil2.multiply(mat, scale);
//			mat = MyMatrixUtil2.getIdentityMatrix4();
//			MyMatrixUtil2.dump("modelViewProjectionMatrix", mat);
			gl2.glUniformMatrix4fv(modelViewProjectionMatrixLocation, 1, false, mat, 0);
			gl2.glDrawElements(GL2.GL_LINE_LOOP, indicesBuffer.limit(), GL2.GL_UNSIGNED_INT, indicesBuffer);
			gl2.glDisableVertexAttribArray(0);
		}
	}

	private void drawPoints(GL2 gl2) {
		// 点を描く
		// 点のモデリング変換／視野変換／投影変換
		float[] modelViewMatrix = MyMatrixUtil2.copy(viewMatrix);//TODO コピーでいいのか不明。代入に変えたけど変わりない。
		modelViewMatrix = MyMatrixUtil2.translate(modelViewMatrix, 0.0f, -1.5f, -1.5f);
		//オリジナル:modelViewMatrix.scale(0.3f, 0.3f, 3.0f);
		modelViewMatrix = MyMatrixUtil2.applyScale(modelViewMatrix, 0.3f, 0.3f, 3f, 1f);
		gl2.glUniformMatrix4fv(modelViewMatrixLocation, 1, false, modelViewMatrix, 0);
		gl2.glUniformMatrix4fv(projectionMatrixLocation, 1, false, projectionMatrix, 0);

		/* バーテックスブレンディング用の uniform 変数の設定 */
		gl2.glUniform1i(numberOfBonesLocation, BONES_COUNT);
		FloatBuffer bottom = genBottomBuffer();
		gl2.glUniform4fv(boneBottomLocation, bottom.limit(), bottom);
		FloatBuffer top = genTopBuffer();
		gl2.glUniform4fv(boneTopLocation, top.limit(), top);
		FloatBuffer blend = genBlendBuffer();
		gl2.glUniformMatrix4fv(blendMatrixLocation, blend.limit(), false, blend);

		/* attribute 変数 position に頂点情報を対応付けて図形を描画する */
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, pointBuffers[0]);
		gl2.glEnableVertexAttribArray(attribPosition);
		gl2.glVertexAttribPointer(attribPosition, 3, GL2.GL_FLOAT, false, 0, 0);
		gl2.glDrawArrays(GL2.GL_POINTS, 0, POINTS_COUNT);
		gl2.glDisableVertexAttribArray(attribPosition);
	}

	private void initBoneVBO(GL2 gl2) {
//		vaos = new int[1];
//		gl2.glGenVertexArrays(vaos.length, vaos, 0);
//		gl2.glBindVertexArray(vaos[0]);
//
	    boneBuffers = new int[2];
	    gl2.glGenBuffers(boneBuffers.length, boneBuffers, 0);

	    gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, boneBuffers[0]);
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, boneVertexBuf.limit() * Buffers.SIZEOF_FLOAT, boneVertexBuf, GL2.GL_STATIC_DRAW);
//	    gl2.glEnableVertexAttribArray(attribPosition);
//	    gl2.glVertexAttribPointer(attribPosition, 3, GL2.GL_FLOAT, false, 0, 0);

	    gl2.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, boneBuffers[1]);
        gl2.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Buffers.SIZEOF_INT * indicesBuffer.limit(), indicesBuffer, GL2.GL_STREAM_DRAW);
//        gl2.glEnableVertexAttribArray(attribIndices);
//        gl2.glVertexAttribPointer(attribIndices, 3, GL2.GL_INT, false, 0, 0);

	    gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
//	    gl2.glBindVertexArray(0);
	}

	/*
	** 点を空間に散布する
	*/
	private void disseminate(GL2 gl2, int pointCount) {
		pointBuffers = new int[1];

		FloatBuffer pointBuf = Buffers.newDirectFloatBuffer(PointGenerator.generate(pointCount));
		pointBuf.rewind();
		//		  dump(pointBuf);

		gl2.glGenBuffers(pointBuffers.length, pointBuffers, 0);
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, pointBuffers[0]);
		gl2.glBufferData(GL2.GL_ARRAY_BUFFER, Buffers.SIZEOF_FLOAT * pointCount * 3, pointBuf, GL2.GL_STATIC_DRAW);

		// 頂点バッファオブジェクトを解放する
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
	}

	private FloatBuffer genBlendBuffer() {
		FloatBuffer buf = Buffers.newDirectFloatBuffer(BONES_COUNT * 16);
		for (int i = 0; i < BONES_COUNT; i++) {
			buf.put(bone[i].getBlend());
		}
		buf.rewind();
		return buf;
	}

	private FloatBuffer genTopBuffer() {
		FloatBuffer buf = Buffers.newDirectFloatBuffer(BONES_COUNT * 4);
		for (int i = 0; i < BONES_COUNT; i++) {
			buf.put(bone[i].getTop());
		}
		buf.rewind();
		return buf;
	}

	private FloatBuffer genBottomBuffer() {
		FloatBuffer buf = Buffers.newDirectFloatBuffer(BONES_COUNT * 4);
		for (int i = 0; i < BONES_COUNT; i++) {
			buf.put(bone[i].getBottom());
		}
		buf.rewind();
		return buf;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		GL2 gl2 = drawable.getGL().getGL2();

		gl2.glViewport(0, 0, width, height);

		aspect = (float) width / height;
		projectionMatrix = MyMatrixUtil2.getIdentityMatrix4();
		projectionMatrix = MyMatrixUtil2.cameraMatrix(30f, aspect, 5f, 9f);
	}

	private void setupData(){
		bone = new Bone[BONES_COUNT];
		bone[0] = new Bone();
		bone[1] = new Bone();

		boneVertexBuf = Buffers.newDirectFloatBuffer(boneVertex.length * boneVertex[0].length);
		for(int i = 0; i < boneVertex.length; i++) {
			for(int j = 0; j < boneVertex[i].length; j++) {
				boneVertexBuf.put(boneVertex[i][j]);
			}
		}
		boneVertexBuf.rewind();
		indicesBuffer = Buffers.newDirectIntBuffer(boneEdge);

	}

	private int initShaders(GL2 gl2, String[] vertSrc, String[] fragSrc, IntBuffer compiled, IntBuffer linked) throws IOException, IllegalStateException {
		int vertShader;
		int fragShader;

//		// GLSL の初期化
//		if (MyShaderUtil.checkIfGLSLSupported(gl2)) {
//			throw new IllegalStateException("glsl init failed");// exit(1);
//		}

		// シェーダオブジェクトの作成
		vertShader = gl2.glCreateShader(GL2.GL_VERTEX_SHADER);
		fragShader = gl2.glCreateShader(GL2.GL_FRAGMENT_SHADER);

		// シェーダのソースプログラムの読み込み
//		String[] vertShaderLines = MyShaderUtil.readFile(vertSrc, false);
//		toJavaSrc(vertShaderLines);
//		String[] fragShaderLines = MyShaderUtil.readFile(fragSrc, false);
//		toJavaSrc(fragShaderLines);
//		MyShaderUtilLocal.initShaders(gl2, concat(vertShaderLines), concat(fragShaderLines));
//		MyShaderUtilLocal.readShaderSource(gl2, fragShader, fragFile);

		// バーテックスシェーダのソースプログラムのコンパイル
//		gl2.glShaderSource(vertShader, 1, vertSrc, null);
		gl2.glShaderSource(vertShader, 1, vertSrc, null);
		gl2.glCompileShader(vertShader);
		gl2.glGetShaderiv(vertShader, GL2.GL_COMPILE_STATUS, compiled);
//		MyShaderUtil.printShaderInfoLog(gl2, vertShader);
		int result = compiled.get(0);
		if (result == GL2.GL_FALSE) {
            int[] logLength = new int[1];
            gl2.glGetShaderiv(vertShader, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
            int[] length = new int[logLength[0]];
            byte[] log = new byte[logLength[0]];
            gl2.glGetShaderInfoLog(vertShader, logLength[0], length, 0, log, 0);

            System.err.println("Error compiling the vertex shader: " + new String(log));

//			MyShaderUtil.printShaderInfoLog(gl2, vertShader);
//			MyShaderUtil.printProgramInfoLog(gl2, glProgram);
//			throw new IllegalStateException("Compile error in vertex shader: " + vertSrc);
		}

		// フラグメントシェーダのソースプログラムのコンパイル
//		gl2.glShaderSource(fragShader, 1, fragSrc, null);
		gl2.glShaderSource(fragShader, 1, fragSrc, null);
		gl2.glCompileShader(fragShader);
		gl2.glGetShaderiv(fragShader, GL2.GL_COMPILE_STATUS, compiled);
//		MyShaderUtil.printShaderInfoLog(gl2, fragShader);
		result = compiled.get(0);
		if (result == GL2.GL_FALSE) {
            int[] logLength = new int[1];
            gl2.glGetShaderiv(fragShader, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
            int[] length = new int[logLength[0]];
            byte[] log = new byte[logLength[0]];
            gl2.glGetShaderInfoLog(fragShader, logLength[0], length, 0, log, 0);

            System.err.println("Error compiling the fragment shader: " + new String(log));

//			MyShaderUtil.printShaderInfoLog(gl2, fragShader);
//			MyShaderUtil.printProgramInfoLog(gl2, glProgram);
//			throw new IllegalStateException("Compile error in fragment shader: " + fragSrc);
		}

		/* プログラムオブジェクトの作成 */
		int glProgram = gl2.glCreateProgram();

		/* シェーダオブジェクトのシェーダプログラムへの登録 */
		gl2.glAttachShader(glProgram, vertShader);
		gl2.glAttachShader(glProgram, fragShader);

		/* シェーダオブジェクトの削除 */
		gl2.glDeleteShader(vertShader);
		gl2.glDeleteShader(fragShader);

		// attribute 変数 position の index を 0 に指定する
		gl2.glBindAttribLocation(glProgram, attribPosition, "position");

		/* シェーダプログラムのリンク */
		gl2.glLinkProgram(glProgram);
		gl2.glGetProgramiv(glProgram, GL2.GL_LINK_STATUS, linked);
		MyShaderUtil.printProgramInfoLog(gl2, glProgram);
		int linkResult = linked.get();
		if (linkResult == GL2.GL_FALSE) {
			throw new IllegalStateException("Link error");
		}
//
//		/* シェーダプログラムの適用 */
//		gl2.glUseProgram(glProgram);
//
//		/* テクスチャユニット０を指定する */
//		gl2.glUniform1i(gl2.glGetUniformLocation(glProgram, "texture"), 0);

		return glProgram;
	}

	private void toJavaSrc(String[] vertShaderLines) {
		for(String line : vertShaderLines) {
			System.out.println("\"" + line + "\\n\" +" );
		}
		System.out.println(";");
	}

	private String concat(String[] shaderSourceLines) {
		StringBuilder sb = new StringBuilder();
		for(String line : shaderSourceLines) {
			sb.append(line);
		}
		return sb.toString();
	}

	private void dump(FloatBuffer pointBuf) {
		for(int i = 0; i < pointBuf.limit(); i++) {
			Float f = pointBuf.get(i);
			System.out.print(f + ", ");
			if(i % 3 == 2) {
				System.out.println();
			}
		}
	}

	private void quit() {
		//animator.stop();
		System.exit(0);
	}

	@Override
	public void keyPressed(KeyEvent key) {
		switch (key.getKeyChar()) {
		case KeyEvent.VK_ESCAPE:
			quit();
			break;

		case 'q':
			quit();
			break;

		default:
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent evt) {
		int step = evt.getWheelRotation();
		if(step < 0) { //前方向への回転 = zoom in
			viewScale *= 1.05;
		} else if(0 < step) {
			viewScale *= 0.95;
		}
		System.out.println("scale:" + viewScale);
		frame.repaint();
	}

	private void showGLInfo(GLAutoDrawable drawable, GL2 gl2) {
		System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());
		System.err.println("INIT GL IS: " + gl2.getClass().getName());
		System.err.println("GL_VENDOR: " + gl2.glGetString(GL.GL_VENDOR));
		System.err.println("GL_RENDERER: " + gl2.glGetString(GL.GL_RENDERER));
		System.err.println("GL_VERSION: " + gl2.glGetString(GL.GL_VERSION));
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
	}

	private void initLight() {
		lightpos = FloatBuffer.allocate(4);
		lightpos.put(4.0f);
		lightpos.put(9.0f);
		lightpos.put(5.0f);
		lightpos.put(1.0f);

		lightcol = FloatBuffer.allocate(4);
		lightcol.put(1.0f);
		lightcol.put(1.0f);
		lightcol.put(1.0f);
		lightcol.put(1.0f);

		lightamb = FloatBuffer.allocate(4);
		lightamb.put(0.1f);
		lightamb.put(0.1f);
		lightamb.put(0.1f);
		lightamb.put(1.0f);
	}

	/* ボーンの図形データ */
	static float boneVertex[][] = {
		{  0.0f,  0.0f,  0.0f,  1.0f },
		{  0.1f,  0.0f,  0.1f,  1.0f },
		{  0.0f,  0.1f,  0.1f,  1.0f },
		{ -0.1f,  0.0f,  0.1f,  1.0f },
		{  0.0f, -0.1f,  0.1f,  1.0f },
		{  0.0f,  0.0f,  1.0f,  1.0f },
	};

	static int boneEdge[] = {
		0, 1, 5, 3, 0, 2, 5, 4, 1, 2, 3, 4,
	};

	static float boneAngle[] = { 0.0f, 0.0f};

}
