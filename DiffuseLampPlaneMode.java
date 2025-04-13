package com.example.kulubecioglu_lab42;

import android.opengl.GLES32;
import android.opengl.Matrix;

// Düz bir zeminde diffuse aydınlatma ve lamba sembolünü gösteren mod.
public class DiffuseLampPlaneMode extends BaseRendererMode {
    protected float[] objectColor = {0f, 1f, 1f};
    protected float[] lightColor = {1f, 1f, 0f};
    protected float[] lightCoordinates = {0f, 0f, 0.1f};

    private float touchXDown, touchYDown, prevLightX, prevLightY;

    public DiffuseLampPlaneMode() {
        super();
        angleAlpha = 0f;
        viewAngleBeta = 70f;
        initScene();
    }

    @Override
    protected void initScene() {
        sceneVertices = new float[1000];
        primitiveObj = new GraphicsPrimitives();
        int pos = 0;
        // Işık kaynağı sembolü
        pos = primitiveObj.addPointWithNormal(sceneVertices, pos,
                0, 0, 0,
                lightColor[0], lightColor[1], lightColor[2]);
        // Düzlem (quad)
        pos = primitiveObj.addQuadWithNormal(sceneVertices, pos,
                -1, -1, 0,
                -1, 1, 0,
                1, 1, 0,
                1, -1, 0,
                objectColor[0], objectColor[1], objectColor[2]);
    }

    @Override
    public void createShaderProgram() {
        compileAndAttachShaders(
                ShaderLibrary.vertexShader7,
                ShaderLibrary.fragmentShader6);
        bindVertexArrayDouble(sceneVertices, 6,
                "vPosition", 0,
                "vNormal", 3 * 4);
    }

    @Override
    protected void setupProjection(int width, int height) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.setIdentityM(projMatrix, 0);

        Matrix.rotateM(viewMatrix, 0, -viewAngleBeta, 1, 0, 0);
        Matrix.rotateM(viewMatrix, 0, -angleAlpha, 0, 0, 1);
        Matrix.translateM(viewMatrix, 0,
                -cameraPosition[0], -cameraPosition[1], -cameraPosition[2]);

        float aspect = (float) width / (float) height;
        Matrix.perspectiveM(projMatrix, 0, 45f, aspect, 0.1f, 30f);

        int handle = GLES32.glGetUniformLocation(shaderProgram, "uViewMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, viewMatrix, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uProjMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, projMatrix, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uModelMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, modelMatrix, 0);
    }

    @Override
    protected void drawScene() {
        GLES32.glBindVertexArray(vaoId);

        // Zemini diffuse aydınlatma ile çiz
        int handle = GLES32.glGetUniformLocation(shaderProgram, "vColorMode");
        GLES32.glUniform1i(handle, 1);
        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightColor");
        GLES32.glUniform3fv(handle, 1, lightColor, 0);
        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightPos");
        GLES32.glUniform3fv(handle, 1, lightCoordinates, 0);
        primitiveObj.drawObjects(1, 1, GLES32.glGetUniformLocation(shaderProgram, "vColor"));

        // Işık kaynağı sembolü
        handle = GLES32.glGetUniformLocation(shaderProgram, "vColorMode");
        GLES32.glUniform1i(handle, 0);
        Matrix.translateM(modelMatrix, 0, lightCoordinates[0], lightCoordinates[1], lightCoordinates[2]);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uModelMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, modelMatrix, 0);
        primitiveObj.drawObjects(0, 0, GLES32.glGetUniformLocation(shaderProgram, "vLightColor"));

        GLES32.glBindVertexArray(0);
    }

    @Override
    public void useProgramForDrawing(int width, int height) {
        setupProjection(width, height);
        drawScene();
    }

    @Override
    public boolean onTouchDown(float x, float y, int w, int h) {
        touchXDown = x;
        touchYDown = y;
        prevLightX = lightCoordinates[0];
        prevLightY = lightCoordinates[1];
        // Ekranın üstüne dokunulursa ışığı yükselt
        if (y < 0.1f * h) {
            lightCoordinates[2] += 0.1f;
            return true;
        }
        // Ekranın altına dokunulursa ışığı alçalt
        if (y > 0.9f * h) {
            lightCoordinates[2] -= 0.1f;
            if (lightCoordinates[2] < 0.1f) {
                lightCoordinates[2] = 0.1f;
            }
        }
        return true;
    }

    @Override
    public boolean onTouchMove(float x, float y, int w, int h) {
        lightCoordinates[0] = prevLightX + 0.005f * (x - touchXDown);
        lightCoordinates[1] = prevLightY - 0.005f * (y - touchYDown);
        return true;
    }

    @Override
    public String getDebugInfo() {
        return String.format("x=%.1f y=%.1f z=%.1f",
                lightCoordinates[0], lightCoordinates[1], lightCoordinates[2]);
    }
}
