#if __VERSION__ >= 130
  #define varying in
  #define attribute out 
  out vec4 mgl_FragColor;
#else
  #define mgl_FragColor gl_FragColor   
#endif

attribute vec3 position;
uniform mat4 modelViewProjectionMatrix;
 
void main()
{
  gl_Position = modelViewProjectionMatrix * vec4(position, 1.0);
}
