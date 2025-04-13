package com.example.kulubecioglu_lab42;


import android.opengl.GLES32;
import android.opengl.Matrix;
import android.os.SystemClock;

// Dönen bir piramit ve satranç tahtası sahnesi.
// Piramit sürekli dönüyor, ışık kaynağı da dokunmatik ile hareket ettirilebiliyor.
public class RotatingPyramidMode extends BaseRendererMode {
    private float[] whiteColor = {1f, 1f, 1f};
    private float[] orangeColor = {1f, 0.4f, 0.7f};
    private float[] lightCoordinates = {0.5f, 0f, 0.4f};

    private int pyramidStart, pyramidEnd;
    private int boardStart, boardEnd;

    private float touchXDown, touchYDown, prevLightX, prevLightY;

    public RotatingPyramidMode() {
        super();
        cameraPosition[0] = 1.2f;
        cameraPosition[1] = -3.5f;
        cameraPosition[2] = 1.5f;
        angleAlpha = 20f;
        viewAngleBeta = 70f;
        initScene();
    }

    @Override
    protected void initScene() {
        // Piramit + satranç tahtası + lamba için yeterli büyüklükte dizi
        int size = 9 * 9 * 24 + 4 * 18 + 24;
        sceneVertices = new float[size];
        primitiveObj = new GraphicsPrimitives();
        int pos = 0;

        // Lamba sembolü
        pos = primitiveObj.addPointWithNormal(sceneVertices, pos,
                0, 0, 0,
                whiteColor[0], whiteColor[1], whiteColor[2]);

        // Piramit
        pyramidStart = primitiveObj.getObjectCount();
        pos = primitiveObj.addPyramidWithNormal(sceneVertices, pos,
                0, 0, 0.1f, 0.5f,
                orangeColor[0], orangeColor[1], orangeColor[2]);
        pyramidEnd = primitiveObj.getObjectCount() - 1;

        // Satranç tahtası
        boardStart = primitiveObj.getObjectCount();
        primitiveObj.addChessBoardWithNormal(sceneVertices, pos,
                9, 0.2f,
                0, 0, 0,
                0.92f, 0.89f, 0.84f,   // Açık kareler (yumuşak krem)
                0.41f, 0.35f, 0.31f);  // Koyu kareler (koyu ahşap tonu)
        boardEnd = primitiveObj.getObjectCount() - 1;
    }

    @Override
    public void createShaderProgram() {
        compileAndAttachShaders(
                ShaderLibrary.vertexShader7,
                ShaderLibrary.fragmentShader9);
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
    public void useProgramForDrawing(int width, int height) {
        setupProjection(width, height);

        int handle = GLES32.glGetUniformLocation(shaderProgram, "vColorMode");
        GLES32.glUniform1i(handle, 1);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightColor");
        GLES32.glUniform3fv(handle, 1, whiteColor, 0);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightPos");
        GLES32.glUniform3fv(handle, 1, lightCoordinates, 0);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vEyePos");
        GLES32.glUniform3fv(handle, 1, cameraPosition, 0);

        GLES32.glBindVertexArray(vaoId);

        // Satranç tahtası çiz
        handle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        primitiveObj.drawObjects(boardStart, boardEnd, handle);

        // Piramit: sürekli dönmesi için zaman bazlı açı hesabı
        long time = SystemClock.uptimeMillis() % 3600L;
        float rotAngle = 0.1f * (float) time;

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, rotAngle, 0, 0, -1);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uModelMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, modelMatrix, 0);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        GLES32.glUniform3fv(handle, 1, orangeColor, 0);
        primitiveObj.drawObjects(pyramidStart, pyramidEnd, -1);

        // Işık sembolü
        handle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        GLES32.glUniform3fv(handle, 1, whiteColor, 0);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vColorMode");
        GLES32.glUniform1i(handle, 0);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0,
                lightCoordinates[0], lightCoordinates[1], lightCoordinates[2]);
        handle = GLES32.glGetUniformLocation(shaderProgram, "uModelMatrix");
        GLES32.glUniformMatrix4fv(handle, 1, false, modelMatrix, 0);

        primitiveObj.drawObjects(0, 0, -1);

        GLES32.glBindVertexArray(0);
    }

    @Override
    public boolean onTouchDown(float x, float y, int w, int h) {
        touchXDown = x;
        touchYDown = y;
        prevLightX = lightCoordinates[0];
        prevLightY = lightCoordinates[1];

        if (y < 0.1f * h) {
            lightCoordinates[2] += 0.1f;
            return true;
        }
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
