// Credit: https://www.coding-daddy.xyz/node/16

package shaders;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.jogamp.opengl.GL3;

/**
 * Manages the shader program.
 * 
 * @author serhiy
 */
public class ShaderProgram {
	private int programId;
	private int vertexShaderId;
	private int fragmentShaderId;
	private Map<EShaderAttribute, Integer> shaderAttributeLocations = new HashMap<>();
	private boolean initialized = false;

	/**
	 * Initializes the shader program.
	 * 
	 * @param gl context.
	 * @param vertexShader file.
	 * @param fragmentShader file.
	 * @return true if initialization was successful, false otherwise.
	 */
	public boolean init(GL3 gl, File vertexShader, File fragmentShader) {
		if (initialized) {
			throw new IllegalStateException(
					"Unable to initialize the shader program! (it was already initialized)");
		}

		try {
			String vertexShaderCode = ShaderUtils.loadResource(vertexShader
					.getPath());
			String fragmentShaderCode = ShaderUtils.loadResource(fragmentShader
					.getPath());

			programId = gl.glCreateProgram();
			vertexShaderId = ShaderUtils.createShader(gl, programId,
					vertexShaderCode, GL3.GL_VERTEX_SHADER);
			fragmentShaderId = ShaderUtils.createShader(gl, programId,
					fragmentShaderCode, GL3.GL_FRAGMENT_SHADER);

			ShaderUtils.link(gl, programId);

			shaderAttributeLocations.put(EShaderAttribute.POSITION,
					gl.glGetAttribLocation(programId, EShaderAttribute.POSITION.getAttributeName()));
			shaderAttributeLocations.put(EShaderAttribute.COLOR,
					gl.glGetAttribLocation(programId, EShaderAttribute.COLOR.getAttributeName()));
			shaderAttributeLocations.put(EShaderAttribute.TRANS,
					gl.glGetUniformLocation(programId, EShaderAttribute.TRANS.getAttributeName()));
			shaderAttributeLocations.put(EShaderAttribute.ORTHO,
					gl.glGetUniformLocation(programId, EShaderAttribute.ORTHO.getAttributeName()));

			initialized = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return initialized;

	}

	/**
	 * Destroys the shader program.
	 * 
	 * @param gl context.
	 */
	public void dispose(GL3 gl) {
		initialized = false;
		gl.glDetachShader(programId, vertexShaderId);
		gl.glDetachShader(programId, fragmentShaderId);
		gl.glDeleteProgram(programId);
	}

	/**
	 * @return shader program id.
	 */
	public int getProgramId() {
		if (!initialized) {
			throw new IllegalStateException(
					"Unable to get the program id! The shader program was not initialized!");
		}
		return programId;
	}

	/**
	 * @param shaderAttribute to retrieve its location.
	 * @return location of the shader attribute.
	 */
	public int getShaderLocation(EShaderAttribute shaderAttribute) {
		if (!initialized) {
			throw new IllegalStateException(
					"Unable to get the attribute location! The shader program was not initialized!");
		}
		return shaderAttributeLocations.get(shaderAttribute);
	}

	public boolean isInitialized() {
		return initialized;
	}
}
