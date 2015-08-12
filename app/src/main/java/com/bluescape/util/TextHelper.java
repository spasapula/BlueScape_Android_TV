package com.bluescape.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.widget.EditText;

import com.bluescape.AppConstants;
import com.bluescape.model.WorkSpaceState;
import com.bluescape.model.util.Rect;
import com.bluescape.model.widget.BaseWidgetModel;
import com.bluescape.model.widget.BrowserModel;
import com.bluescape.model.widget.NoteModel;
import com.bluescape.model.widget.PDFModel;

public class TextHelper {

    /**
     * Converts the string from an edittext to a bitmap.
     *
     * @param editText
     * @param model
     * @return
     */
    public static Bitmap convertEditTextToBitmap(EditText editText, NoteModel model) {
        editText.setCursorVisible(false);
        editText.buildDrawingCache();
        return Bitmap.createBitmap(editText.getDrawingCache());
    }

    /**
     * Converts the supplied text to bitmap with the supplied style.
     *
     * @return
     */

    public static Bitmap ConvertTextToBitmap(BaseWidgetModel model) {
        float textSize = 0, width = 0, height = 0;
        String text = "";
        if (model instanceof NoteModel) {
            // Grab everything from the model we need to build the bitmap.
            textSize = TextHelper.parseFontSize(((NoteModel) model).getTextStyle().getFontSize());
            text = ((NoteModel) model).getText();
            width = ((NoteModel) model).getNoteTemplate().getWidth();
            height = ((NoteModel) model).getNoteTemplate().getHeight();
        } else if (model instanceof PDFModel) {
            textSize = TextHelper.parseFontSize(((PDFModel) model).getTextStyle().getFontSize());
            text = ((PDFModel) model).getTitle();
            width = ((PDFModel) model).getWidth();
            height = ((PDFModel) model).getHeight();
        } else if (model instanceof BrowserModel) {
            textSize = TextHelper.parseFontSize(((BrowserModel) model).getTextStyle().getFontSize());
            text = (((BrowserModel) model).getDisplayURL());
            width = ((BrowserModel) model).getWidth();
            height = ((BrowserModel) model).getHeight();
        }

        Rect rect = new Rect(0, 0, width, height);

        // Build the paint to draw the text
        Paint paint = new Paint();
        if (model instanceof NoteModel) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(60);
        } else
            paint.setColor(Color.BLACK);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAntiAlias(true);
        Typeface tf = Typeface.create("Dosis-Regular", Typeface.NORMAL);
        paint.setTypeface(tf);

        // Build the bitmap and canvas.
        Bitmap bitmap = Bitmap.createBitmap(Math.abs((int) rect.getWidth()), Math.abs((int) rect.getHeight()), Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);
        drawClearBackground(canvas);

        // Get the Y value of the text, so we can draw at 0,0
        float baseline = (int) (-paint.ascent() + 0.5f);

        // Draw the darn text with our helper function.

        if (model instanceof NoteModel)
            if (((NoteModel) model).getTextStyle().getTextTranform().equals(AppConstants.UPPER_CASE_STR))
                TextHelper.drawMultilineText(AppConstants.UPPER_CASE, text, AppConstants.CARD_MARGIN_X, AppConstants.CARD_MARGIN_Y, paint, canvas, textSize, rect);
            else
                TextHelper.drawMultilineText(AppConstants.LOWER_CASE, text, AppConstants.CARD_MARGIN_X, AppConstants.CARD_MARGIN_Y, paint, canvas, textSize, rect);
        else {
            paint.setTextAlign(Paint.Align.CENTER);
            TextHelper.drawMultilineText(AppConstants.LOWER_CASE, text, (canvas.getWidth() / 2)-5, (rect.getHeight() * 0.6f), paint, canvas, textSize, rect);
        }
        // Return the bitmap with the text drawn to it.
        return bitmap;
    }



    public static Bitmap ConvertInitialsToBitmap(BaseWidgetModel model) {

        String mText = "";
        mText = model.getActivityInitials();

        Paint mTextPaint;

        int mViewWidth;
        int mViewHeight;
        int mTextBaseline;



        //init()
        mTextPaint = new TextPaint();
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);

        //onSurfaceChange
//        mViewWidth = (int)((ImageModel) model).getRect().getWidth()/AppConstants.ACTIVITY_INDICATOR_INITIALS_FACTOR;
//        mViewHeight = (int)((ImageModel) model).getRect().getHeight()/AppConstants.ACTIVITY_INDICATOR_INITIALS_FACTOR;

//        mViewWidth = 540;
//        mViewHeight =360;

        //last ok with min zoom
//        mViewWidth = 160;
//        mViewHeight =224;

        mViewWidth = 224;
        mViewHeight = 224;

//        mViewWidth = (int)WorkSpaceState.getInstance().getCurrentInitialsWidth();
//        mViewHeight =(int)WorkSpaceState.getInstance().getCurrentInitialsWidth();

        // Build the bitmap and canvas.
        Bitmap bitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.rgb((int) model.getActivityCollaboratorColor()[0]  * 255 , (int) model.getActivityCollaboratorColor()[1] * 255, (int) model.getActivityCollaboratorColor()[2] * 255));

        //adjustTextSize();
        // using .isEmpty() isn't backward compatible with older API versions
//        if (mText.length() == 0) {
//            return;
//        }
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(100);
        //mTextPaint.setTextSize(60);
        //mTextPaint.setTextSize(200);


        mTextPaint.setTextScaleX(1.0f);
        android.graphics.Rect bounds = new android.graphics.Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text
        mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);

        // get the height that would have been produced
        int h = bounds.bottom - bounds.top;

        // make the text text up 70% of the height
        float target = (float) mViewHeight * .7f;

        // figure out what textSize setting would create that height
        // of text
        float size = ((target / h) * 100f);

        // and set it into the paint
        mTextPaint.setTextSize(size);


        //end adjustTextSize

        //start adjust TextScale
        // do calculation with scale of 1.0 (no scale)
        mTextPaint.setTextScaleX(1.0f);
        bounds = new android.graphics.Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text.
        mTextPaint.getTextBounds(mText, 0, mText.length(), bounds);

        // determine the width
        int w = bounds.right - bounds.left;

        // calculate the baseline to use so that the
        // entire text is visible including the descenders
        int text_h = bounds.bottom - bounds.top;
        mTextBaseline = bounds.bottom + ((mViewHeight - text_h) / 2);

        // determine how much to scale the width to fit the view
//        float xscale = ((float) (mViewWidth - getPaddingLeft() - getPaddingRight()))
//                / w;
        //TODO determine PADDING
//        float xscale = ((float) (mViewWidth ))
//                / w;

        //leave 30% border width .15 on left and .15 on right
        float xscale = ((float) (mViewWidth - (mViewWidth *0.3) ))
                / w;

        // set the scale for the text paint
        mTextPaint.setTextScaleX(xscale);

        //end adjustTextScale


        //onDraw
        // draw the text
        // position is centered on width
        // and the baseline is calculated to be positioned from the
        // view bottom
        canvas.drawText(mText, mViewWidth / 2, mViewHeight - mTextBaseline,
                mTextPaint);



        // Return the bitmap with the text drawn to it.
        return bitmap;
    }
    /**
     * Converts the fontsize format of the json data to something we can use.
     *
     * @param fontSize
     * @return
     */
    public static float parseFontSize(String fontSize) {
        // The format should be "44px"
        String[] split = fontSize.split("[a-zA-Z]+");
        return Float.parseFloat(split[0]);
    }

    /**
     * Grabs the hieght of the text.
     *
     * @param testString
     * @param currentSize
     * @return
     */
    private static int calculateHeightFromFontSize(String testString, float currentSize) {
        android.graphics.Rect bounds = new android.graphics.Rect();
        Paint paint = new Paint();
        paint.setTextSize(currentSize);
        paint.getTextBounds(testString, 0, testString.length(), bounds);

        return (int) Math.ceil(bounds.height());
    }

    /**
     * Grabs width from the text and size.
     *
     * @param testString
     * @param currentSize
     * @return
     */
    private static int calculateWidthFromFontSize(String testString, float currentSize) {
        android.graphics.Rect bounds = new android.graphics.Rect();
        Paint paint = new Paint();
        paint.setTextSize(currentSize); // adding 5% to get it to be a little
        // spacey.
        paint.getTextBounds(testString, 0, testString.length(), bounds);

        return (int) Math.ceil(bounds.width());
    }

    private static void drawClearBackground(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
    }

    /**
     * Uses the helper methods to draw the string on multiple lines. catches /n
     *
     * @param str
     * @param x
     * @param y
     * @param paint
     * @param canvas
     * @param fontSize
     * @param rect
     */
    private static void drawMultilineText(boolean isUpper, String str, int x, float y, Paint paint, Canvas canvas, float fontSize, Rect rect) {
        int lineHeight = 0;
        int yoffset = 0;
        String[] lines = str.split(" ");

        // set height of each line (height of text + 20%)
        lineHeight = (int) (calculateHeightFromFontSize(str, fontSize) * 1.2);

        // draw each line
        String line = "";
        for (String line1 : lines) {
            // Check to see if we are past the width of the rect, if we are then
            // create a new line
            if (calculateWidthFromFontSize(line + " " + line1, fontSize) <= rect.getWidth()) {
                // Check if it's a blank line, if it is don't add space,
                // otherwise add space.
                if (line.equals("")) {
                    line += line1;
                } else {
                    line = line + " " + line1;
                }
            } else {

                if (line.contains("\n")) {
                    String[] newLines = line.split("\n");
                    for (int lineNo = 0; lineNo < newLines.length; lineNo++) {
                        canvas.drawText(newLines[lineNo], x, y + yoffset, paint);
                        yoffset = yoffset + lineHeight;
                    }
                    line = line1;
                } else {
                    canvas.drawText(line, x, y + yoffset, paint);
                    yoffset = yoffset + lineHeight;
                    line = line1;
                }
            }
        }
        if (isUpper)
            canvas.drawText(line.toUpperCase(), x, y + yoffset, paint);
        else {
            if (line.contains("\n")) {
                String[] newLines = line.split("\n");
                for (int lineNo = 0; lineNo < newLines.length; lineNo++) {
                    canvas.drawText(newLines[lineNo], x, y + yoffset, paint);
                    ;
                    yoffset = yoffset + lineHeight;
                }
            } else {
                canvas.drawText(line, x, y + yoffset, paint);
            }
        }

    }
}
