uniform mat4 u_Matrix;
attribute vec4 aPosition;
attribute vec4 aColor;
attribute vec2 atexCoord;
varying vec4 vColor;
varying vec2 vtexCoord;

void main() {
  gl_Position = u_Matrix * aPosition;
  vtexCoord = atexCoord;
  vColor = aColor;
}