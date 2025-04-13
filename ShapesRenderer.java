package com.example.kulubecioglu_lab42;

import android.opengl.GLES32;

// Küre, silindir, torus, satranç tahtası gibi farklı şekilleri aynı sahnede gösteren örnek mod.
public class ShapesRenderer extends LampDiffuseSpecMode {

    public ShapesRenderer() {
        super();
    }

    @Override
    public void clearScreenColor() {
        GLES32.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    protected void initScene() {
        angleAlpha = 0f;
        viewAngleBeta = 75f;
        cameraPosition[0] = 0f;
        cameraPosition[1] = -6f;
        cameraPosition[2] = 2f;

        // Işık konumu
        lightCoordinates[0] = -0.8f;
        lightCoordinates[1] = -1.9f;
        lightCoordinates[2] = 1.2f;

        primitiveObj = new GraphicsPrimitives();

        float[] tempRef = null;
        for (int i = 0; i < 2; i++) {
            int pos = 0;
            // Işık kaynağı sembolü
            pos = primitiveObj.addPointWithNormal(tempRef, pos,
                    0, 0, 0,
                    lightColor[0], lightColor[1], lightColor[2]);

            // Küre (yumuşak mavi)
            pos = primitiveObj.addSphereWithNormal(tempRef, pos,
                    0, 0, 0.5f,
                    0.5f, 18, 36,
                    0.2f, 0.4f, 0.8f);

            // Silindir (hafif turkuaz)
            pos = primitiveObj.addCylinderWithNormal(tempRef, pos,
                    0, 0, 0.5f,
                    0.2f, 1.5f, 10,
                    0.1f, 0.7f, 0.8f,
                    1f);

            // Torus (sıcak turuncu)
            pos = primitiveObj.addTorusWithNormal(tempRef, pos,
                    0, 0, 0.5f,
                    1f, 0.15f,
                    18, 36,
                    0.9f, 0.6f, 0.2f);


            // Satranç tahtası
            pos = primitiveObj.addChessBoardWithNormal(tempRef, pos,
                    10, 0.2f,
                    0, 0, -0.5f,
                    0.95f, 0.87f, 0.73f,
                    0.6f, 0.4f, 0.2f);

            if (i == 0) {
                sceneVertices = new float[pos];
                tempRef = sceneVertices;
            }
        }
    }

    @Override
    protected void drawScene() {
        GLES32.glBindVertexArray(vaoId);

        // Işık kaynağı hariç tüm nesneler (index 1'den sonraki nesneler)
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
