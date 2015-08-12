// Color value of the lines ( 0xa2 / 0xff )
#define a2 0.6352941176470588

// At the closest zoom level a worldspace coord point is equal to 0.96 pixels
//#define MAGIC_SCALE_FACTOR 0.96
#define  MAGIC_SCALE_FACTOR 1.

// The grid line width in viewspace
#define LINE_WIDTH 2.

precision mediump float;

// Background texture
uniform sampler2D uSampler;

// Points describing viewport position in worldspace
// xy == topLeft
// zw == bottomRight
uniform vec4 uWorldSpacePos;

// The width of the viewport in pixels
uniform float uViewPortWidth;

// The separation of grid lines in worldspace
uniform float uLineSeparation;

vec4 lineColor = vec4(a2, a2, a2, 1.);
vec4 dotColor = vec4(a2 - 0.2, a2 - 0.2, a2 - 0.2, 5.);
vec4 bgColor = vec4(0., 0., 0., 0.);
vec4 originLineColor = vec4(a2, a2, a2, 1.);

float computeSeparation(vec2 topLeft, vec2 bottomRight) {
  return (uViewPortWidth / (bottomRight.x - topLeft.x) * uLineSeparation * MAGIC_SCALE_FACTOR);
}

float computeYOffset(float rightY, float leftX, float rightX) {
  return -rightY * (uViewPortWidth / (rightX - leftX));
}

float computeXOffset(float leftX, float rightX) {
  return leftX * (uViewPortWidth / (rightX - leftX));
}

vec4 getGridLinesColor(float xOffset, float yOffset, float separation) {
  float val = step( LINE_WIDTH, abs( mod( gl_FragCoord.x + xOffset, separation )));
  float val2 = step( LINE_WIDTH, abs( mod( gl_FragCoord.y + yOffset, separation )));
  vec4 hColor = mix( lineColor, bgColor, val );
  return mix( lineColor, hColor, val2 );
}

float getScaleFactor() {
  return uViewPortWidth / (uWorldSpacePos.z - uWorldSpacePos.x);
}

vec4 getTexColor(float xOffset, float yOffset, float separation) {
  float scale = separation / 2.0;
  vec2 uv = vec2( gl_FragCoord.x + xOffset, gl_FragCoord.y + yOffset );
  vec4 tex = texture2D( uSampler, uv / scale );

  float scaleFactor = getScaleFactor();
  float width = scaleFactor * LINE_WIDTH + 1.;
  float excludeDotSep = 1000.0 * MAGIC_SCALE_FACTOR;
  float dotSep = excludeDotSep / 10.0;

  // TODO: Remove this if and fix this
  if ( scaleFactor >= 0.3 &&
      abs( mod(uv.x / scaleFactor + width / 2., excludeDotSep ) ) >= width &&
      abs( mod(uv.y / scaleFactor + width / 2., excludeDotSep ) ) >= width &&
      abs( mod(uv.x / scaleFactor + width / 2., dotSep ) ) < width &&
      abs( mod(uv.y / scaleFactor + width / 2., dotSep ) ) < width ) {
    return mix(dotColor, tex, 0.6);
  }

  // The origin lines are done here
  return mix(mix(mix( originLineColor, tex, step(1., uv.y) ),tex,step(uv.y, -1.))
    ,mix(mix( originLineColor, tex, step(1., uv.x) ),tex,step(uv.x, -1.)),0.5);

}

void main(void) {
  float separation = computeSeparation(uWorldSpacePos.xy, uWorldSpacePos.zw);
  float xOffset = computeXOffset(uWorldSpacePos.x, uWorldSpacePos.z);
  float yOffset = computeYOffset(uWorldSpacePos.w, uWorldSpacePos.x, uWorldSpacePos.z);
  gl_FragColor = mix(getGridLinesColor(xOffset, yOffset, separation)
        ,getTexColor(xOffset, yOffset, separation)
        ,0.92);
}
