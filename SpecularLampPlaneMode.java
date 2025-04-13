package com.example.kulubecioglu_lab42;


import android.opengl.GLES32;

// Düzlem üzerinde specular (yansıma) aydınlatma örneği.
// DiffuseLampPlaneMode’dan türeyerek ek olarak göz pozisyonunu (vEyePos) shader’a aktarıyor.
public class SpecularLampPlaneMode extends DiffuseLampPlaneMode {

    public SpecularLampPlaneMode() {
        super();
    }

    @Override
    public void createShaderProgram() {
        compileAndAttachShaders(
                ShaderLibrary.vertexShader7,
                ShaderLibrary.fragmentShader7);
        bindVertexArrayDouble(sceneVertices, 6,
                "vPosition", 0,
                "vNormal", 3 * 4);
    }

    @Override
    public void useProgramForDrawing(int width, int height) {
        setupProjection(width, height);

        int handle = GLES32.glGetUniformLocation(shaderProgram, "vEyePos");
        GLES32.glUniform3fv(handle, 1, cameraPosition, 0);

        drawScene();
    }
}

