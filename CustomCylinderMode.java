package com.example.kulubecioglu_lab42;

import android.opengl.GLES32;
import android.opengl.Matrix;

// Silindir geometrisini oluşturan ve diffuse+specular hesaplamalarını yapan örnek mod.
public class CustomCylinderMode extends LampDiffuseSpecMode {


    public CustomCylinderMode() {
        super();
    }




    @Override
    protected void initScene() {
        // Kamera ayarları
        viewAngleBeta = 70f;
        cameraPosition[1] = -5f;
        cameraPosition[2] = 1.7f;

        // Nesne rengi
        objectColor[0] = 0f;
        objectColor[1] = 1f;
        objectColor[2] = 1f;

        // Işık konumu
        lightCoordinates[0] = 0.5f;
        lightCoordinates[1] = 0f;
        lightCoordinates[2] = 0.4f;


        // Geometri oluşturmak için yardımcı sınıf
        primitiveObj = new GraphicsPrimitives();

        float[] tempRef = null;
        for (int i = 0; i < 2; i++) {
            int pos = 0;  // Her turda pos sıfırlanıyor
            // Işık kaynağı sembolü ekleniyor
            pos = primitiveObj.addPointWithNormal(tempRef, pos,
                    0, 0, 0,
                    lightColor[0], lightColor[1], lightColor[2]);

            // Silindir ekleniyor (iki farklı normal yönü)
            pos = primitiveObj.addCylinderWithNormal(tempRef, pos,
                    0, 0, 0,
                    0.499f, 1f, 24,
                    0.5f, 0.6f, 0.8f,
                    -1f);
            pos = primitiveObj.addCylinderWithNormal(tempRef, pos,
                    0, 0, 0,
                    0.5f, 1f, 24,
                    0.5f, 0.6f, 0.8f,
                    1f);

            if (i == 0) {
                sceneVertices = new float[pos];  // İlk turda hesaplanan pos değeri kadar dizi oluşturuluyor
                tempRef = sceneVertices;
            }
        }
    }



    @Override
    public void clearScreenColor() {
        GLES32.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    protected void drawScene() {
        // VAO'yu bağla
        GLES32.glBindVertexArray(vaoId);


        // Silindiri (index 1 ve sonrası) çiz
        int colorHandle = GLES32.glGetUniformLocation(shaderProgram, "vColor");
        primitiveObj.drawObjects(1, primitiveObj.getObjectCount() - 1, colorHandle);

        // Işık kaynağı sembolünü çizmek için temel sınıftaki drawLampSymbol() metodu çağrılıyor
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
