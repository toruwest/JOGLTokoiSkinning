#if __VERSION__ >= 130
  #define varying in
  #define attribute out
  out vec4 mgl_FragColor;
#else
  #define mgl_FragColor gl_FragColor   
#endif

attribute vec3 position;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

const int MAXBONES = 8;
uniform int numberOfBones;
uniform vec4 boneBottom[MAXBONES], boneTop[MAXBONES];
uniform mat4 blendMatrix[MAXBONES];

const float exponent = -16.0;

void main()
{
    vec4 p = modelViewMatrix * vec4(position, 1.0);
    vec4 q = vec4(0.0);
    for (int i = 0; i < numberOfBones; ++i) {
        vec4 v1 = boneTop[i] - boneBottom[i];
        vec4 v2 = p - boneBottom[i];
        float l = dot(v1, v1);
        if (l > 0.0) v2 -= v1 * clamp(dot(v1, v2) / l, 0.0, 1.0);
        q += blendMatrix[i] * p * pow(length(v2) + 1.0, exponent);
    }
    gl_Position = projectionMatrix * q;
}
