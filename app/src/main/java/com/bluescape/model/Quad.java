package com.bluescape.model;

import android.opengl.GLES20;

import com.bluescape.AppConstants;
import com.bluescape.view.shaders.SimpleShaderProgram;
import com.bluescape.view.shaders.TextureShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUniform4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by SS_SA on 6/18/15.
 */
public class Quad {
    //region Member Variables
    private int mVertexBufferIndex = 0;
    private int mTextureBufferIndex = 0;
    private int mVertexCount = 0;
    //endregion

    //region Constructor
    public Quad(float width, float height) {
        int vboBuffers[] = new int[2];
        GLES20.glGenBuffers(2, vboBuffers, 0);
        generateVertices(width, height, vboBuffers);
        createTextureBuffer(vboBuffers);
    }
    //endregion

    //region Getters/Setters
    public int getVertexBufferIndex() {
        return mVertexBufferIndex;
    }

    public int getTextureBufferIndex() {
        return mTextureBufferIndex;
    }

    public int getVertexCount() {
        return mVertexCount;
    }
    //endregion

    //region Public Members

    //Draw Textured Quad
    public void draw(float[] mvpMatrix, TextureShaderProgram shader, int textureId) {
        glUseProgram(shader.mProgram);

        // Set up texture
        shader.setUniforms(textureId);

        // VBO code
        // Pass in the position information
        // GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubePositionsBufferIdx);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.getVertexBufferIndex());
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(shader.getPositionHandle());
        // Prepare the triangle coordinate data

        glVertexAttribPointer(shader.getPositionHandle(), /* coords per vertex*/3, GL_FLOAT, false, 0, 0);

        // Pass in the texture information

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.getTextureBufferIndex());
        // Texture coord
        GLES20.glEnableVertexAttribArray(shader.getTexCoordLoc());
        // Prepare the texturecoordinates

        glVertexAttribPointer(shader.getTexCoordLoc(), /*texture coords*/2, GL_FLOAT, false, 0, 0);

        // Clear the currently bound buffer (so future OpenGL calls do not use
        // this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glUniformMatrix4fv(shader.getMatrixHandle(), 1, false, mvpMatrix, 0);

        // Draw the triangle
        glDrawArrays(GL_TRIANGLES, 0, this.getVertexCount());

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(shader.getPositionHandle());
        GLES20.glDisableVertexAttribArray(shader.getTexCoordLoc());

    }


    //Draw Uniform Colored Quad
    public void draw(float[] mvpMatrix, SimpleShaderProgram shader, float[] normalizedColor) {
        glUseProgram(shader.mProgram);

        // Set up texture
        //shader.setUniforms(textureId);

        // VBO code
        // Pass in the position information
        // GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mCubePositionsBufferIdx);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, this.getVertexBufferIndex());
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(shader.getPositionHandle());
        // Prepare the triangle coordinate data

        glVertexAttribPointer(shader.getPositionHandle(), /* coords per vertex*/3, GL_FLOAT, false, 0, 0);


        // Set mColor for drawing the triangle
        //TODO possible optimization to use vbo for standard colors
        glUniform4fv(shader.getColorHandle(), 1, normalizedColor, 0);


        // Clear the currently bound buffer (so future OpenGL calls do not use
        // this buffer).
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        GLES20.glUniformMatrix4fv(shader.getMatrixHandle(), 1, false, mvpMatrix, 0);

        // Draw the triangle
        glDrawArrays(GL_TRIANGLES, 0, this.getVertexCount());

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(shader.getPositionHandle());

    }
    //endregion

    //region Private Methods
    private void generateVertices(float width, float height, int vboBuffers[]) {

        // We need to start at 0,0 and then move the object with the translate
        // matrix.
        float[] vertices;
        vertices = new float[] {
                // X Y Z Triangle 1
                0.0f, 0.0f, 0.0f,

                0.0f, height, 0.0f, // bottom
                // left
                width, height, 0.0f, // bottom
                // right
                // Triangle 2
                0.0f, 0.0f, 0.0f, // top
                // right
                width, height, 0.0f, // bottom
                // left
                width, 0.0f, 0.0f // bottom
                // right
        };
        mVertexCount = vertices.length/3;

        createVertexBuffer(vertices, vboBuffers);
    }

    private void createTextureBuffer(int vboBuffers[]) {
        // Texture coordinate data.

        final float[] cubeTextureCoordinateData = {
                // 1
                0, 0, 0, 1, 1, 1,
                // 2
                0, 0, 1, 1, 1, 0 };

        FloatBuffer textureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureCoordinates.put(cubeTextureCoordinateData).position(0);

        // Bind to the buffer. Future commands will affect this buffer
        // specifically.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBuffers[1]);

        // Transfer data from client memory to the buffer.
        // We can release the client memory after this call.
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, textureCoordinates.capacity() * AppConstants.BYTES_PER_FLOAT, textureCoordinates,
                GLES20.GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        mTextureBufferIndex = vboBuffers[1];

        // clear mCubeTextureCoordinates as it is now in the GPU memory
        textureCoordinates.limit(0);

    }

    private void createVertexBuffer(float[] vertices, int vboBuffers[]) {

        FloatBuffer vertexBuffer;
        if (vertices != null && vertices.length > 0) {
            // initialize vertex byte buffer for shape coordinates
            ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * AppConstants.BYTES_FLOAT); // (number
            // of
            // coordinate
            // values
            // *
            // 4
            // bytes
            // per
            // float    )

            // use the device hardware's native byte order
            bb.order(ByteOrder.nativeOrder());

            // create a floating point buffer from the ByteBuffer
            vertexBuffer = bb.asFloatBuffer();

            // add the coordinates to the FloatBuffer
            vertexBuffer.put(vertices);

            // set the buffer to read the first coordinate
            vertexBuffer.position(0);

            // Bind to the buffer. Future commands will affect this buffer
            // specifically.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboBuffers[0]);

            // Transfer data from client memory to the buffer.
            // We can release the client memory after this call.
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexBuffer.capacity() * AppConstants.BYTES_PER_FLOAT, vertexBuffer, GLES20.GL_STATIC_DRAW);

            // IMPORTANT: Unbind from the buffer when we're done with it.
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            mVertexBufferIndex = vboBuffers[0];

            vertexBuffer.limit(0);
        }
    }
    //endregion

}
