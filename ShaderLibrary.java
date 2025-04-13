package com.example.kulubecioglu_lab42;



// Bu sınıf, tüm shader kodlarını saklar.
public class ShaderLibrary {
    // Vertex Shader 6
    public static final String vertexShader6 =
            "#version 300 es\n" +
                    "in vec3 vPosition;\n" +
                    "in vec3 vNormal;\n" +
                    "uniform mat4 uModelMatrix;\n" +
                    "uniform mat4 uViewMatrix;\n" +
                    "uniform mat4 uProjMatrix;\n" +
                    "out vec3 currentPos;\n" +
                    "out vec3 currentNormal;\n" +
                    "void main() {\n" +
                    "   gl_Position = uProjMatrix * uViewMatrix * uModelMatrix * vec4(vPosition, 1.0);\n" +
                    "   currentPos = mat3(uModelMatrix) * vPosition;\n" +
                    "   currentNormal = mat3(uModelMatrix) * vNormal;\n" +
                    "}\n";

    // Vertex Shader 7: ek olarak gl_PointSize ayarlanır.
    public static final String vertexShader7 =
            "#version 300 es\n" +
                    "in vec3 vPosition;\n" +
                    "in vec3 vNormal;\n" +
                    "uniform mat4 uModelMatrix;\n" +
                    "uniform mat4 uViewMatrix;\n" +
                    "uniform mat4 uProjMatrix;\n" +
                    "out vec3 currentPos;\n" +
                    "out vec3 currentNormal;\n" +
                    "void main() {\n" +
                    "   gl_Position = uProjMatrix * uViewMatrix * uModelMatrix * vec4(vPosition, 1.0);\n" +
                    "   currentPos = mat3(uModelMatrix) * vPosition;\n" +
                    "   currentNormal = mat3(uModelMatrix) * vNormal;\n" +
                    "   gl_PointSize = 15.0;\n" +
                    "}\n";

    // Fragment Shader 5: Ambient + Diffuse
    public static final String fragmentShader5 =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "in vec3 currentPos;\n" +
                    "in vec3 currentNormal;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform vec3 vLightColor;\n" +
                    "uniform vec3 vLightPos;\n" +
                    "out vec4 resultColor;\n" +
                    "void main() {\n" +
                    "    vec3 norm = normalize(currentNormal);\n" +
                    "    vec3 lightDir = normalize(vLightPos - currentPos);\n" +
                    "    float diffuse = max(dot(norm, lightDir), 0.0);\n" +
                    "    float distance = length(vLightPos - currentPos);\n" +
                    "    float attenuation = 1.0 / (1.0 + distance * distance);\n" +
                    "    vec3 clr = 0.3 * vColor + 0.7 * diffuse * vLightColor * vColor;\n" +
                    "    resultColor = vec4(attenuation * clr, 1.0);\n" +
                    "}\n";

    // Fragment Shader 6: Ambient + Diffuse + Lamp Color
    public static final String fragmentShader6 =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "in vec3 currentPos;\n" +
                    "in vec3 currentNormal;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform vec3 vLightColor;\n" +
                    "uniform vec3 vLightPos;\n" +
                    "uniform int vColorMode;\n" +
                    "out vec4 resultColor;\n" +
                    "void main() {\n" +
                    "    if(vColorMode == 1) {\n" +
                    "        vec3 norm = normalize(currentNormal);\n" +
                    "        vec3 lightDir = normalize(vLightPos - currentPos);\n" +
                    "        float distance = length(vLightPos - currentPos);\n" +
                    "        float diffuse = max(dot(norm, lightDir), 0.0);\n" +
                    "        vec3 clr = 0.7 * vColor + 0.3 * diffuse * vLightColor * vColor;\n" +
                    "        resultColor = vec4((1.0 / (1.0 + distance * distance)) * clr, 1.0);\n" +
                    "    } else {\n" +
                    "        resultColor = vec4(vLightColor, 1.0);\n" +
                    "    }\n" +
                    "}\n";

    // Fragment Shader 7: Ambient + Specular + Lamp Color
    public static final String fragmentShader7 =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "in vec3 currentPos;\n" +
                    "in vec3 currentNormal;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform vec3 vLightColor;\n" +
                    "uniform vec3 vLightPos;\n" +
                    "uniform vec3 vEyePos;\n" +
                    "uniform int vColorMode;\n" +
                    "out vec4 resultColor;\n" +
                    "void main() {\n" +
                    "    if(vColorMode == 1) {\n" +
                    "        vec3 norm = normalize(currentNormal);\n" +
                    "        vec3 lightDir = normalize(vLightPos - currentPos);\n" +
                    "        vec3 reflectDir = normalize(reflect(-lightDir, norm));\n" +
                    "        vec3 eyeDir = normalize(vEyePos - currentPos);\n" +
                    "        float spec = max(dot(eyeDir, reflectDir), 0.0);\n" +
                    "        spec = pow(spec, 1000.0);\n" +
                    "        vec3 clr = 0.2 * vColor + 0.8 * spec * vLightColor;\n" +
                    "        resultColor = vec4(clr, 1.0);\n" +
                    "    } else {\n" +
                    "        resultColor = vec4(vLightColor, 1.0);\n" +
                    "    }\n" +
                    "}\n";

    // Fragment Shader 8: Ambient + Diffuse + Specular + Lamp Color
    public static final String fragmentShader8 =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "in vec3 currentPos;\n" +
                    "in vec3 currentNormal;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform vec3 vLightColor;\n" +
                    "uniform vec3 vLightPos;\n" +
                    "uniform vec3 vEyePos;\n" +
                    "uniform int vColorMode;\n" +
                    "out vec4 resultColor;\n" +
                    "void main() {\n" +
                    "    if(vColorMode == 1) {\n" +
                    "        vec3 norm = normalize(currentNormal);\n" +
                    "        vec3 lightDir = normalize(vLightPos - currentPos);\n" +
                    "        float diffuse = max(dot(norm, lightDir), 0.0);\n" +
                    "        vec3 reflectDir = normalize(reflect(-lightDir, norm));\n" +
                    "        vec3 eyeDir = normalize(vEyePos - currentPos);\n" +
                    "        float spec = max(dot(eyeDir, reflectDir), 0.0);\n" +
                    "        spec = pow(spec, 100.0);\n" +
                    "        vec3 clr = 0.3 * vColor + 0.4 * diffuse * vColor * vLightColor + 0.3 * spec * vLightColor;\n" +
                    "        float distance = length(vLightPos - currentPos);\n" +
                    "        resultColor = vec4((1.0 / (1.0 + distance * distance)) * clr, 1.0);\n" +
                    "    } else {\n" +
                    "        resultColor = vec4(vLightColor, 1.0);\n" +
                    "    }\n" +
                    "}\n";

    // Fragment Shader 9: Attenuation * (Ambient + Diffuse + Specular + Lamp Color)
    public static final String fragmentShader9 =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "in vec3 currentPos;\n" +
                    "in vec3 currentNormal;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform vec3 vLightColor;\n" +
                    "uniform vec3 vLightPos;\n" +
                    "uniform vec3 vEyePos;\n" +
                    "uniform int vColorMode;\n" +
                    "out vec4 resultColor;\n" +
                    "void main() {\n" +
                    "    if(vColorMode == 1) {\n" +
                    "        vec3 norm = normalize(currentNormal);\n" +
                    "        vec3 lightDir = normalize(vLightPos - currentPos);\n" +
                    "        float distance = length(vLightPos - currentPos);\n" +
                    "        float diffuse = max(dot(norm, lightDir), 0.0);\n" +
                    "        vec3 reflectDir = normalize(reflect(-lightDir, norm));\n" +
                    "        vec3 eyeDir = normalize(vEyePos - currentPos);\n" +
                    "        float spec = max(dot(eyeDir, reflectDir), 0.0);\n" +
                    "        spec = pow(spec, 500.0);\n" +
                    "        vec3 clr = 0.3 * vColor + 0.2 * diffuse * vColor * vLightColor + 0.7 * spec * vLightColor;\n" +
                    "        resultColor = vec4((1.0 / (1.0 + distance * distance)) * clr, 1.0);\n" +
                    "    } else {\n" +
                    "        resultColor = vec4(vLightColor, 1.0);\n" +
                    "    }\n" +
                    "}\n";

    // Fragment Shader 10: Ambient + Diffuse + Specular
    public static final String fragmentShader10 =
            "#version 300 es\n" +
                    "precision mediump float;\n" +
                    "in vec3 currentPos;\n" +
                    "in vec3 currentNormal;\n" +
                    "uniform vec3 vColor;\n" +
                    "uniform vec3 vLightColor;\n" +
                    "uniform vec3 vLightPos;\n" +
                    "uniform vec3 vEyePos;\n" +
                    "out vec4 resultColor;\n" +
                    "void main() {\n" +
                    "    vec3 norm = normalize(currentNormal);\n" +
                    "    vec3 lightDir = normalize(vLightPos - currentPos);\n" +
                    "    float diffuse = max(dot(norm, lightDir), 0.0);\n" +
                    "    vec3 reflectDir = normalize(reflect(-lightDir, norm));\n" +
                    "    vec3 eyeDir = normalize(vEyePos - currentPos);\n" +
                    "    float spec = max(dot(eyeDir, reflectDir), 0.0);\n" +
                    "    spec = pow(spec, 200.0);\n" +
                    "    vec3 clr = 0.3 * vColor + 0.4 * diffuse * vColor + 0.3 * spec * vLightColor;\n" +
                    "    float distance = length(vLightPos - currentPos);\n" +
                    "    resultColor = vec4((1.0 / (1.0 + distance * distance)) * clr, 1.0);\n" +
                    "}\n";
}
