uniform mat4 u_Matrix;

attribute vec4 aPosition;

void main() {
    gl_Position = u_Matrix * aPosition;
}