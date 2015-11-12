#if __VERSION__ >= 130
  #define varying in
  out vec4 mgl_FragColor;
#else
  #define mgl_FragColor gl_FragColor   
#endif
 
void main()
{
  gl_FragColor = vec4(0,0,1,1.0);
}
