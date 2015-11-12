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
 
void main()
{
  gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
}
