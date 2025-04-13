package com.example.kulubecioglu_lab42;


import android.opengl.GLES32;
import android.opengl.Matrix;

// 3x3x3 = 27 küp gösteren mod. Kamera hareketini dokunma ile kontrol eder.
public class NineCubesRenderer extends BaseRendererMode {
    protected float touchXDown, touchYDown;
    private float[] objectColor = {0.5f, 0.7f, 1.0f};
    private float[] lightColor = {1f, 1f, 1f};

    private float prevAlphaAngle;
    private int numQuads = 0;

    public NineCubesRenderer() {
        super();
        angleAlpha = 0f;
        viewAngleBeta = 100f;
        cameraPosition[0] = 0f;
        cameraPosition[1] = -2f;
        cameraPosition[2] = 0.5f;
        initScene();
    }

    @Override
    protected void initScene() {
        numQuads = 0;
        int size = 27 * 6 * 24;  // Yaklaşık bir boyut
        sceneVertices = new float[size];
        primitiveObj = new GraphicsPrimitives();
        int pos = 0;

        // 3 katman, her katmanda 9 küp
        for (int z = 1; z <= 3; z++) {
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    pos = primitiveObj.addCubeWithNormal(sceneVertices, pos,
                            0.5f * x, 0.5f * y, 0.5f * z, 0.2f,
                            objectColor[0], objectColor[1], objectColor[2]);
                }
            }
        }
        numQuads = pos / 24;
    }

    @Override
    public boolean onTouchDown(float x, float y, int w, int h) {
        touchXDown = x;
        touchYDown = y;
        prevAlphaAngle = angleAlpha;

        // Dokunma konumuna göre açıları hızlıca değiştirme
        if (x <= 0.05f * w) angleAlpha += 10f;
        if (x >= 0.95f * w) angleAlpha -= 10f;
        if (y <= 0.05f * h) viewAngleBeta += 10f;
        if (y >= 0.95f * h) viewAngleBeta -= 10f;

        // Sınırlar
        if (viewAngleBeta < 0) viewAngleBeta = 0;
        if (viewAngleBeta > 180) viewAngleBeta = 180;
        return true;
    }

    @Override
    public boolean onTouchMove(float x, float y, int w, int h) {
        angleAlpha = prevAlphaAngle + 0.2f * (touchXDown - x);

        float step = 0.002f * (touchYDown - y);
        // Kamerayı dönen eksenlere göre hareket ettir
        cameraPosition[0] -= step * (float) Math.sin(Math.toRadians(angleAlpha));
        cameraPosition[1] += step * (float) Math.cos(Math.toRadians(angleAlpha));
        cameraPosition[2] -= step * (float) Math.cos(Math.toRadians(viewAngleBeta));

        touchYDown = y;
        return true;
    }

    @Override
    public String getDebugInfo() {
        return String.format("a=%d b=%d x=%.1f y=%.1f z=%.1f",
                (int) angleAlpha, (int) viewAngleBeta,
                cameraPosition[0], cameraPosition[1], cameraPosition[2]);
    }

    @Override
    public void createShaderProgram() {
        compileAndAttachShaders(
                ShaderLibrary.vertexShader6,
                ShaderLibrary.fragmentShader5);
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
        Matrix.perspectiveM(projMatrix, 0, 80f, aspect, 0.1f, 30f);

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

        int handle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        GLES32.glUniform3fv(handle, 1, objectColor, 0);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightColor");
        GLES32.glUniform3fv(handle, 1, lightColor, 0);

        // Işık konumunu kamera pozisyonuna eşitliyoruz (fener gibi)
        handle = GLES32.glGetUniformLocation(shaderProgram, "vLightPos");
        GLES32.glUniform3fv(handle, 1, cameraPosition, 0);

        handle = GLES32.glGetUniformLocation(shaderProgram, "vEyePos");
        GLES32.glUniform3fv(handle, 1, cameraPosition, 0);

        GLES32.glBindVertexArray(vaoId);
        primitiveObj.drawObjects(0, primitiveObj.getObjectCount() - 1, -1);
        GLES32.glBindVertexArray(0);
    }
}
