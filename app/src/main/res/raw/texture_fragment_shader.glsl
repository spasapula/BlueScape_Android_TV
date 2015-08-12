precision mediump float;

uniform vec4 vColor;
varying vec2 vtexCoord;
uniform sampler2D uSampler;

void main() {
    vec4 texel = texture2D(uSampler, vtexCoord);
    if(texel.a < 0.5)
        discard;
    gl_FragColor = texture2D(uSampler, vtexCoord);
    //gl_FragColor = vec4(1.0,0.0,0.0,1.0);
}