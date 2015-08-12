uniform mat4 u_Matrix;
attribute vec4 aPosition;
attribute vec2 atexCoord;
varying vec2 vtexCoord;

void main() {
  gl_Position = u_Matrix * aPosition;
  vtexCoord = atexCoord;
}