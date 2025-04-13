package com.example.kulubecioglu_lab42;

import android.opengl.GLES32;

// Torus (simidon) geometrisini oluşturan ve diffuse+specular hesaplamalarını yapan örnek mod.
public class CustomTorusMode extends LampDiffuseSpecMode {

    public CustomTorusMode() {
        super();
    }

    @Override
    protected void initScene() {
        angleAlpha = 0f;
        viewAngleBeta = 56f;
        cameraPosition[0] = 0f;
        cameraPosition[1] = -6f;
        cameraPosition[2] = 4f;

        lightCoordinates[0] = -1f;
        lightCoordinates[1] = -1f;
        lightCoordinates[2] = 1f;

        primitiveObj = new GraphicsPrimitives();

        float[] tempRef = null;
        for (int i = 0; i < 2; i++) {
            int pos = 0;
            // Işık kaynağı sembolü
            pos = primitiveObj.addPointWithNormal(tempRef, pos,
                    0, 0, 0,
                    lightColor[0], lightColor[1], lightColor[2]);

            // Torus ekle
            pos = primitiveObj.addTorusWithNormal(tempRef, pos,
                    0, 0, 0,
                    1f, 0.3f,
                    18, 36,
                    0.6f, 0.6f, 1f);

            if (i == 0) {
                sceneVertices = new float[pos];
                tempRef = sceneVertices;
            }
        }
    }

    @Override
    public void clearScreenColor() {
        GLES32.glClearColor(0f, 0f, 0f, 0f);
    }

    @Override
    protected void drawScene() {
        GLES32.glBindVertexArray(vaoId);

        // Torus (index 1 ve sonrası) çizilir
        int colorHandle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        primitiveObj.drawObjects(1, primitiveObj.getObjectCount() - 1, colorHandle);

        // Işık kaynağı sembolü çizilir
        drawLampSymbol();

        GLES32.glBindVertexArray(0);
    }

    @Override
    public void createShaderProgram() {
        compileAndAttachShaders(
                ShaderLibrary.vertexShader7,
                ShaderLibrary.fragmentShader8);
        bindVertexArrayDouble(sceneVertices, 6,
                "vPosition", 0,
                "vNormal", 3 * 4);
    }
}
