package t.n.glsl;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MyShaderUtil {
	public static boolean checkIfGLSLSupported(GL2 gl2) {
		String GLSLVersion = gl2.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("GLSL Version:" + GLSLVersion);
		return(GLSLVersion == null);
	}

	public static boolean checkIfGLSLSupported(GL3 gl3) {
		String GLSLVersion = gl3.glGetString(GL2.GL_SHADING_LANGUAGE_VERSION);
		System.out.println("GLSL Version:" + GLSLVersion);
		return(GLSLVersion == null);
	}

	public static void readShaderSource(GL2 gl, int vertShader2, String filename) throws IOException {
		String[] buf = readFile(filename);
		gl.glShaderSource(vertShader2, 1, buf, null);
	}

	//複数の要素を持つString配列を渡すとエラーになるので、改行で区切った、一つの要素しかない配列を渡す。
	public static String[] readFile(String filename) throws IOException {
//		List<String> buff = new ArrayList<String>();
		StringBuffer buff = new StringBuffer();
		String line;

		BufferedReader br = new BufferedReader(new FileReader(filename));
		while((line = br.readLine()) != null){
			if(!line.isEmpty()) {
				buff.append(line + '\n');
			}
		}

		br.close();

		return new String[]{buff.toString()};
	}

	public static String[] readFile(String filename, boolean addNewline) throws IOException {
		List<String> buff = new ArrayList<String>();
		String line;

		BufferedReader br = new BufferedReader(new FileReader(filename));
		while((line=br.readLine()) != null){
			if(addNewline){
			buff.add(line + "\n");
			} else {
				buff.add(line);
			}
		}

		String[] ret = buff.toArray(new String[buff.size()]);

		return ret;
	}

	private String readFile2(String filename) throws FileNotFoundException, IOException {
		StringBuffer buff = new StringBuffer();
		String line;

		BufferedReader br = new BufferedReader(new FileReader(filename));
		while((line=br.readLine()) != null){
			buff.append(line + "\n");
		}

		return buff.toString();

	}
//
//	public static void showErrorDialog(final Container container, final String title, final String msg) {
//		SwingUtilities.invokeLater(new Runnable() {
//			@Override
//			public void run(){
//				final JOptionPane pane = new JOptionPane(msg, JOptionPane.OK_OPTION);
//				JDialog dialog = pane.createDialog(container, title);
//				dialog.setVisible(true);
//			}
//		});
//	}

	public static void showErrorDialog(final Container container, final String title, final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run(){
				String msg = "";
				if(e instanceof IllegalStateException) {
					msg = "シェーダーがサポートされていません。";
				} else if(e instanceof IOException) {
					msg = "シェーダーファイルが見つかりません。:" + e.getMessage();
				} else if(e instanceof IOException) {
					msg = "シェーダーファイル読み込み中にエラーになりました。";
				}
				final JOptionPane pane = new JOptionPane(msg, JOptionPane.OK_OPTION);
				JDialog dialog = pane.createDialog(container, title);
				dialog.setVisible(true);
				//System.exit(1);
//				quit();
			}
		});
	}

	public static void printShaderInfoLog(GL2 gl2, int shader) {

		IntBuffer buf = IntBuffer.allocate(1);

		gl2.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH , buf);

		if (buf.get(0) > 1) {
			int length = 10000;
			ByteBuffer logBuffer = ByteBuffer.allocate(length);
			gl2.glGetShaderInfoLog(shader, length, buf, logBuffer);
			String log = null;
			try {
				log = new String(logBuffer.array(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				log = new String(logBuffer.array());
			}
			if(log.trim().isEmpty()) {
				System.err.println("InfoLog is empty");
			} else {
				System.err.printf("InfoLog:\n%s\n\n", log);
			}
		}
	}

	public static void printShaderInfoLog(GL3 gl3, int shader) {

		IntBuffer buf = IntBuffer.allocate(1);

		gl3.glGetShaderiv(shader, GL3.GL_INFO_LOG_LENGTH , buf);

		if (buf.get(0) > 1) {
			int length = 10000;
			ByteBuffer logBuffer = ByteBuffer.allocate(length);
			gl3.glGetShaderInfoLog(shader, length, buf, logBuffer);
			String log = null;
			try {
				log = new String(logBuffer.array(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				log = new String(logBuffer.array());
			}
			if(log.trim().isEmpty()) {
				System.err.println("InfoLog is empty");
			} else {
				System.err.printf("InfoLog:\n%s\n\n", log);
			}
		}
	}

	public static void printProgramInfoLog(GL2 gl2, int gl2Program) {
		IntBuffer buf = IntBuffer.allocate(1);

		gl2.glGetProgramiv(gl2Program, GL2.GL_INFO_LOG_LENGTH , buf);

		if (buf.get(0) > 1) {
			int length = 10000;
			ByteBuffer logBuffer = ByteBuffer.allocate(length);

			//		    infoLog = (GLchar *)malloc(bufSize);
			//		    if (infoLog != NULL) {
			//		      GLsizei length;
			IntBuffer lengthBuf = IntBuffer.allocate(1);
			//lengthBufはJavaでは必要ないはずだが、C++では呼び出し側にlogBufferの使われたバイト数を伝えるため必要。
			//互換性維持のため使われている
			gl2.glGetProgramInfoLog(gl2Program, length, lengthBuf, logBuffer);
			String log = null;
			try {
				log = new String(logBuffer.array(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				log = new String(logBuffer.array());
			}
			if(log.trim().isEmpty()) {
				System.err.println("InfoLog is empty");
			} else {
				System.err.printf("InfoLog:\n%s\n\n", log);
			}
		}
	}

	public static void printProgramInfoLog(GL3 gl3, int glProgram) {
		IntBuffer buf = IntBuffer.allocate(1);

		gl3.glGetProgramiv(glProgram, GL2.GL_INFO_LOG_LENGTH , buf);

		if (buf.get(0) > 1) {
			int length = 10000;
			ByteBuffer logBuffer = ByteBuffer.allocate(length);

			//		    infoLog = (GLchar *)malloc(bufSize);
			//		    if (infoLog != NULL) {
			//		      GLsizei length;
			IntBuffer lengthBuf = IntBuffer.allocate(1);
			//lengthBufはJavaでは必要ないはずだが、C++では呼び出し側にlogBufferの使われたバイト数を伝えるため必要。
			//互換性維持のため使われている
			gl3.glGetProgramInfoLog(glProgram, length, lengthBuf, logBuffer);
			String log = null;
			try {
				log = new String(logBuffer.array(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				log = new String(logBuffer.array());
			}
			if(log.trim().isEmpty()) {
				System.err.println("InfoLog is empty");
			} else {
				System.err.printf("InfoLog:\n%s\n\n", log);
			}
		}
	}
}
