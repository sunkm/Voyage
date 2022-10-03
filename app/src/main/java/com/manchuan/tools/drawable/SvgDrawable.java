package com.manchuan.tools.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RawRes;
import androidx.core.graphics.ColorUtils;

import com.manchuan.tools.utils.PathParser;
import com.manchuan.tools.utils.SystemUiUtil;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import timber.log.Timber;

public class SvgDrawable {

    private static final String TAG = SvgDrawable.class.getSimpleName();
    private static final boolean DEBUG = false;

    private static final boolean ENABLE_IMAGES = true;

    private final List<SvgObject> objects;
    private final List<String> ids;
    private float offsetX;
    private float offsetY;
    private float scale;
    private float zoom;
    private final float pixelUnit;
    private float svgWidth, svgHeight;
    private final Paint paint, paintIntersection, paintDebug;
    private int backgroundColor;
    private final RectF rectF;
    private PointF pointF;
    private final Random random;
    private final Path pathTransformed, pathIntersected;
    private final Matrix matrixCanvas;
    private final Matrix matrixPathTransformation;

    public SvgDrawable(Context context, @RawRes int resId) {
        pixelUnit = getPixelUnit(context);

        objects = new ArrayList<>();
        ids = new ArrayList<>();

        try {
            parse(context.getResources().openRawResource(resId));
        } catch (IOException e) {
            Log.e(TAG, "Could not open SVG resource", e);
        }

        scale = 1;

        paint = new Paint();
        paintIntersection = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectF = new RectF();
        random = new Random();

        pathTransformed = new Path();
        pathIntersected = new Path();

        matrixCanvas = new Matrix();
        matrixPathTransformation = new Matrix();

        paintDebug = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintDebug.setStrokeWidth(SystemUiUtil.dpToPx(context, 4));
        paintDebug.setStyle(Style.STROKE);
        paintDebug.setStrokeCap(Cap.ROUND);
        paintDebug.setColor(Color.CYAN);
    }

    private static float getPixelUnit(Context context) {
        return SystemUiUtil.dpToPx(context, 1) * 0.33f;
    }

    @Nullable
    public SvgObject findObjectById(String id) {
        if (ids.contains(id)) {
            return objects.get(ids.indexOf(id));
        } else {
            return null;
        }
    }

    @NonNull
    public SvgObject requireObjectById(String id) {
        if (ids.contains(id)) {
            return objects.get(ids.indexOf(id));
        } else {
            return new SvgObject(SvgObject.TYPE_NONE);
        }
    }

    /**
     * The final offset is calculated with the elevation
     */
    public void setOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Set how much should be zoomed out. The final value is calculated with the elevation of each
     * object. An object with elevation of 1 (nearest) is zoomed out much more than an object with the
     * elevation 0.1 (almost no parallax/zoom effect).
     *
     * @param zoom value from 0-1: 0 = original size; 1 = max zoomed out (depending on the elevation)
     */
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    /**
     * Apply random elevation between 0 (no parallax/zoom) to 1 (maximal effects) to all objects
     *
     * @param min Set the minimal parallax/zoom intensity (good if nothing should be static)
     */
    public void applyRandomElevationToAll(float min) {
        for (SvgObject object : objects) {
            object.elevation = min + random.nextFloat() * (1 - min);
        }
    }

    /**
     * Apply elevation between 0 (no parallax/zoom) to 1 (maximal effects) to all objects in the
     * original order
     *
     * @param min Set the minimal parallax/zoom intensity (good if nothing should be static)
     */
    public void applyRelativeElevationToAll(float min) {
        float step = (1 - min) / objects.size();
        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).elevation = Math.min(min + step * i, 1);
        }
    }

    /**
     * Apply random rotation to all objects, which is applied with the current zoom intensity
     *
     * @param min Set the minimal rotation in degrees (can be negative)
     * @param max Set the maximal rotation in degrees
     */
    public void applyRandomZoomRotationToAll(int min, int max) {
        for (SvgObject object : objects) {
            if (object.isRotatable) {
                if (DEBUG) {
                    object.zoomRotation = 260;
                } else {
                    object.zoomRotation = min == 0 && max == 0
                            ? 0
                            : random.nextInt(max - min + 1) + min;
                }
            } else {
                object.zoomRotation = 0;
            }
        }
    }

    public static float getDefaultScale(Context context) {
        try {
            int screenWidth = SystemUiUtil.getDisplayWidth(context);
            int screenHeight = SystemUiUtil.getDisplayHeight(context);
            float displayWidth = Math.min(screenWidth, screenHeight);
            float circleWidth = 300 * getPixelUnit(context);
            float currentRatio = circleWidth / displayWidth;
            float originalRatio = 0.2777f;
            float scale = (1 - (currentRatio / originalRatio)) + 1.2f;
            return BigDecimal.valueOf(scale).setScale(1, RoundingMode.HALF_DOWN).floatValue();
        } catch (Exception e) {
            return 1;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawColor(backgroundColor);

        for (SvgObject object : objects) {
            drawObject(canvas, object, null);
        }
    }

    private void parse(InputStream inputStream) throws IOException {
        try (inputStream) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, ENABLE_IMAGES);
            parser.setInput(inputStream, null);
            parser.next();
            readSvg(parser);
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "parse", e);
        }
    }

    private void readSvg(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, "svg");
        String viewBox = parser.getAttributeValue(null, "viewBox");
        if (viewBox != null) {
            String[] metrics = viewBox.split(" ");
            svgWidth = Float.parseFloat(metrics[2]) - Float.parseFloat(metrics[0]);
            svgHeight = Float.parseFloat(metrics[3]) - Float.parseFloat(metrics[1]);
        } else {
            Log.e(TAG, "readSvg: required viewBox attribute is missing");
            return;
        }

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            readObject(parser, null);
        }
    }

    private void readObject(XmlPullParser parser, SvgObject parentGroup)
            throws IOException, XmlPullParserException {
        switch (parser.getName()) {
            case SvgObject.TYPE_GROUP:
                if (parentGroup == null) {
                    readGroup(parser);
                } else {
                    Log.w(TAG, "readSvg: child groups in groups are not supported, skipping...");
                    skip(parser);
                }
                break;
            case SvgObject.TYPE_PATH:
                readPath(parser, parentGroup);
                break;
            case SvgObject.TYPE_RECT:
                readRect(parser, parentGroup);
                break;
            case SvgObject.TYPE_CIRCLE:
                readCircle(parser, parentGroup);
                break;
            case SvgObject.TYPE_ELLIPSE:
                readEllipse(parser, parentGroup);
                break;
            case SvgObject.TYPE_IMAGE:
                if (ENABLE_IMAGES) {
                    readImage(parser, parentGroup);
                }
                break;
            default:
                skip(parser);
                break;
        }
    }

    private void drawObject(Canvas canvas, SvgObject object, SvgObject parentGroup) {
        float zoomRotation = object.isRotatable ? object.zoomRotation * zoom : 0;
        boolean hasPivotOffset = object.pivotOffsetX != 0 || object.pivotOffsetY != 0;
        boolean canvasTransformed = !object.isInGroup && (object.rotation != 0 || zoomRotation != 0);
        if (canvasTransformed) {
            canvas.save();
        }

        if (!object.isInGroup && (object.rotation != 0 || zoomRotation != 0)) {
            if (!hasPivotOffset) {
                // Even for groups this rotation is required
                canvas.rotate(
                        object.rotation + zoomRotation,
                        object.cx * canvas.getWidth(),
                        object.cy * canvas.getHeight()
                );
            } else {
                if (object.rotation != 0) {
                    canvas.rotate(
                            object.rotation, object.cx * canvas.getWidth(), object.cy * canvas.getHeight()
                    );
                }
                if (zoomRotation != 0) {
                    if (DEBUG) { // draw pivot offset
                        canvas.drawPoint(
                                object.cx * canvas.getWidth() + object.pivotOffsetX * pixelUnit * scale,
                                object.cy * canvas.getHeight() + object.pivotOffsetY * pixelUnit * scale,
                                getDebugPaint(Color.YELLOW)
                        );
                    }
                    canvas.rotate(
                            zoomRotation,
                            object.cx * canvas.getWidth() + object.pivotOffsetX * pixelUnit * scale,
                            object.cy * canvas.getHeight() + object.pivotOffsetY * pixelUnit * scale
                    );
                }
            }
        }
        switch (object.type) {
            case SvgObject.TYPE_GROUP:
                drawGroup(canvas, object);
                break;
            case SvgObject.TYPE_PATH:
                drawPath(canvas, object, parentGroup);
                break;
            case SvgObject.TYPE_RECT:
                drawRect(canvas, object, parentGroup);
                break;
            case SvgObject.TYPE_CIRCLE:
            case SvgObject.TYPE_ELLIPSE:
                drawCircle(canvas, object, parentGroup);
                break;
            case SvgObject.TYPE_IMAGE:
                if (ENABLE_IMAGES) {
                    drawImage(canvas, object, parentGroup);
                }
                break;
        }
        if (canvasTransformed) {
            canvas.restore();
        }
    }

    private void readGroup(XmlPullParser parser) throws IOException, XmlPullParserException {
        SvgObject object = new SvgObject(SvgObject.TYPE_GROUP);
        object.children = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_GROUP);
        String tag = parser.getName();
        object.id = parser.getAttributeValue(null, "id");

        if (tag.equals(SvgObject.TYPE_GROUP)) {
            if (object.id == null) {
                Log.w(TAG, "readGroup: id is missing, skipping...");
                return;
            } else if (ids.contains(object.id)) {
                Log.w(TAG, "readGroup: id '" + object.id + "' already exists, skipping...");
                return;
            }

            // Save transformation value now (but don't use it, center is not calculated yet)
            // When we continue parsing, the translation value would be lost
            String transformation = parser.getAttributeValue(null, "transform");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                readObject(parser, object);
            }

            parseTransformation(transformation, object);

            // Compensate rotation of the child center positions
            for (SvgObject child : object.children) {
                PointF center = getRotatedPoint(
                        child.cx, child.cy, object.rotationX, object.rotationY, object.rotation
                );
                child.cx = center.x;
                child.cy = center.y;
            }

            // Calculate group center
            RectF centersBounds = new RectF();
            for (int i = 0; i < object.children.size(); i++) {
                SvgObject child = object.children.get(i);
                if (i == 0) {
                    centersBounds.offset(child.cx, child.cy);
                } else {
                    centersBounds.union(child.cx, child.cy);
                }
            }
            object.cx = centersBounds.centerX();
            object.cy = centersBounds.centerY();

            // Pass the distance from group center to all children
            for (SvgObject child : object.children) {
                child.xDistGroupCenter = (child.cx - object.cx) * pixelUnit;
                child.yDistGroupCenter = (child.cy - object.cy) * pixelUnit;
                // Rotate the child around the group center with the negative group rotation angle
                PointF finalDistance = getRotatedPoint(
                        child.xDistGroupCenter, child.yDistGroupCenter, 0, 0, -object.rotation
                );
                child.xDistGroupCenter = finalDistance.x;
                child.yDistGroupCenter = finalDistance.y;
            }

            // Make group center relative
            object.cx /= svgWidth;
            object.cy /= svgHeight;
        }

        objects.add(object);
        ids.add(object.id);
    }

    private void drawGroup(Canvas canvas, SvgObject object) {
        pointF = getFinalCenter(canvas, object, null);
        if (DEBUG) { // draw final group center
            float strokeWidth = paintDebug.getStrokeWidth();
            paintDebug.setStrokeWidth(strokeWidth * 2);
            canvas.drawPoint(pointF.x, pointF.y, getDebugPaint(Color.GREEN));
            paintDebug.setStrokeWidth(strokeWidth);
        }
        object.cxFinal = pointF.x;
        object.cyFinal = pointF.y;
        object.childScale = getFinalScale(object, null);
        for (SvgObject child : object.children) {
            drawObject(canvas, child, object);
        }
    }

    private void readPath(XmlPullParser parser, SvgObject parentGroup)
            throws IOException, XmlPullParserException {
        SvgObject object = new SvgObject(SvgObject.TYPE_PATH);
        object.isInGroup = parentGroup != null;

        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_PATH);
        String tag = parser.getName();
        object.id = parser.getAttributeValue(null, "id");

        if (tag.equals(SvgObject.TYPE_PATH)) {
            if (object.id == null) {
                Log.w(TAG, "readPath: id is missing, skipping...");
                return;
            } else if (ids.contains(object.id)) {
                Log.e(TAG, "readPath: id '" + object.id + "' already exists, skipping...");
                return;
            }

            String d = parser.getAttributeValue(null, "d");
            if (d != null && !d.isEmpty()) {
                try {
                    object.path = androidx.core.graphics.PathParser.createPathFromPathData(d);
                    if (object.path == null) {
                        return;
                    }
                } catch (RuntimeException e) {
                    Timber.tag(TAG).w("readPath: error with legacy parser, trying with alternative...");
                    object.path = PathParser.getPath(d);
                }
            } else {
                return;
            }

            RectF bounds = new RectF();
            object.path.computeBounds(bounds, true);
            object.width = bounds.width();
            object.height = bounds.height();
            object.cx = bounds.centerX();
            object.cy = bounds.centerY();

            readStyle(parser, object);
            parseTransformation(parser.getAttributeValue(null, "transform"), object);

            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_PATH);

        // apply display metrics
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(pixelUnit, pixelUnit, object.cx, object.cy);
        object.path.transform(scaleMatrix);
        if (!object.isInGroup) { // else keep absolute values for later calculation
            object.cx /= svgWidth;
            object.cy /= svgHeight;
        }

        if (parentGroup == null) {
            objects.add(object);
            ids.add(object.id);
        } else {
            parentGroup.children.add(object);
        }
    }

    private void drawPath(Canvas canvas, SvgObject object, SvgObject parentGroup) {
        canvas.save();
        matrixCanvas.reset();

        // TODO: find fix for different behavior of matrix transformation compared to canvas operations
        boolean isPathIntersected = object.willBeIntersected || !object.intersections.isEmpty();

        float scale = getFinalScale(object, parentGroup);
        pointF = getFinalCenter(canvas, object, parentGroup);

        if (DEBUG) { // draw final object center
            canvas.drawPoint(pointF.x, pointF.y, getDebugPaint(Color.RED));
        }

        float dx = pointF.x - object.cx * (object.isInGroup ? 1 : svgWidth);
        float dy = pointF.y - object.cy * (object.isInGroup ? 1 : svgHeight);

        float px = object.isInGroup ? parentGroup.cxFinal - dx : pointF.x - dx;
        float py = object.isInGroup ? parentGroup.cyFinal - dy : pointF.y - dy;

        if (!object.isInGroup && object.willBeIntersected) {
            // store further canvas transformations in pathTransformed for later intersection
            matrixPathTransformation.reset();
            matrixPathTransformation.postTranslate(dx, dy);
            matrixPathTransformation.postScale(scale, scale, px, py);
            object.pathTransformed.reset();
            object.pathTransformed.addPath(object.path, matrixPathTransformation);
        }

        if (object.isInGroup) {
            float elevation = parentGroup.elevation;
            float xCompensate = ((px + dx) - pointF.x) * (this.scale - 1) * (1 - zoom * elevation);
            float yCompensate = ((py + dy) - pointF.y) * (this.scale - 1) * (1 - zoom * elevation);
            if (isPathIntersected) {
                matrixCanvas.postTranslate(dx + xCompensate, dy + yCompensate);
            } else {
                canvas.translate(dx + xCompensate, dy + yCompensate);
            }
        } else {
            if (isPathIntersected) {
                matrixCanvas.postTranslate(dx, dy);
            } else {
                canvas.translate(dx, dy);
            }
        }

        if (DEBUG) { // draw scaling pivot point
            canvas.drawPoint(px, py, getDebugPaint(Color.BLUE));
        }

        if (isPathIntersected) {
            matrixCanvas.postScale(scale, scale, px, py);
        } else {
            canvas.scale(scale, scale, px, py);
        }

        if (object.isInGroup) {
            // fixes child path offset when zoomed out
            // TODO: for scale 1.4-1.7 tiny offset still occurs, find a better fix
            float elevation = parentGroup.elevation;
            float xCompensate = ((px + dx) - pointF.x) * (1 - (this.scale - 1)) * (zoom * elevation);
            float yCompensate = ((py + dy) - pointF.y) * (1 - (this.scale - 1)) * (zoom * elevation);
            if (isPathIntersected) {
                matrixCanvas.postTranslate(-xCompensate, -yCompensate);
            } else {
                canvas.translate(-xCompensate, -yCompensate);
            }
        }

        if (isPathIntersected) {
            canvas.concat(matrixCanvas);
        }

        // start with fill and repeat with stroke if both are set
        // don't apply scale to stroke width, stroke is already scaled with canvas transformation
        int runs = applyPaintStyle(object, 1, false) ? 2 : 1;
        for (int i = 0; i < runs; i++) {
            if (i == 1) {
                applyPaintStyle(object, 1, true);
            }
            canvas.drawPath(object.path, paint);
        }

        canvas.restore();

        if (!object.isInGroup && !object.intersections.isEmpty()) {
            for (PathIntersection intersection : object.intersections) {
                SvgObject other = findObjectById(intersection.id);
                if (other != null && other.willBeIntersected && other.pathTransformed != null) {
                    // transform object.path with recent canvas transformations
                    pathTransformed.reset();
                    pathTransformed.addPath(object.path, matrixCanvas);
                    // intersect transformed object.path with other.pathTransformed
                    pathIntersected.op(pathTransformed, other.pathTransformed, Path.Op.INTERSECT);
                    paintIntersection.setColor(intersection.color);
                    canvas.drawPath(pathIntersected, paintIntersection);

                    if (DEBUG) {
                        canvas.drawPath(other.pathTransformed, getDebugPaint(Color.BLUE));
                        canvas.drawPath(pathTransformed, getDebugPaint(Color.RED));
                    }
                }
            }
        }
    }

    private void readRect(XmlPullParser parser, SvgObject parentGroup)
            throws IOException, XmlPullParserException {
        SvgObject object = new SvgObject(SvgObject.TYPE_RECT);
        object.isInGroup = parentGroup != null;

        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_RECT);
        String tag = parser.getName();
        object.id = parser.getAttributeValue(null, "id");

        if (tag.equals(SvgObject.TYPE_RECT)) {
            if (object.id == null) {
                Log.w(TAG, "readRect: id is missing, skipping...");
                return;
            } else if (ids.contains(object.id)) {
                Log.w(TAG, "readRect: id '" + object.id + "' already exists, skipping...");
                return;
            }

            object.width = parseFloat(parser.getAttributeValue(null, "width"));
            object.height = parseFloat(parser.getAttributeValue(null, "height"));
            float x = parseFloat(parser.getAttributeValue(null, "x"));
            float y = parseFloat(parser.getAttributeValue(null, "y"));
            object.cx = x + object.width / 2;
            object.cy = y + object.height / 2;
            object.rx = parseFloat(parser.getAttributeValue(null, "rx"));
            object.ry = parseFloat(parser.getAttributeValue(null, "ry"));

            readStyle(parser, object);
            parseTransformation(parser.getAttributeValue(null, "transform"), object);

            // has same size as SVG? Use it as background color and don't use it as object
            if (object.width == svgWidth && object.height == svgHeight) {
                backgroundColor = object.fill;
                return;
            }

            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_RECT);

        // apply display metrics
        object.width *= pixelUnit;
        object.height *= pixelUnit;
        object.rx *= pixelUnit;
        object.ry *= pixelUnit;
        if (!object.isInGroup) { // else keep absolute values for later calculation
            object.cx /= svgWidth;
            object.cy /= svgHeight;
        }

        if (parentGroup == null) {
            objects.add(object);
            ids.add(object.id);
        } else {
            parentGroup.children.add(object);
        }
    }

    private void drawRect(Canvas canvas, SvgObject object, SvgObject parentGroup) {
        float scale = getFinalScale(object, parentGroup);
        pointF = getFinalCenter(canvas, object, parentGroup);
        rectF.set(
                pointF.x - (object.width * scale) / 2,
                pointF.y - (object.height * scale) / 2,
                pointF.x + (object.width * scale) / 2,
                pointF.y + (object.height * scale) / 2
        );

        // start with fill and repeat with stroke if both are set
        int runs = applyPaintStyle(object, scale, false) ? 2 : 1;
        for (int i = 0; i < runs; i++) {
            if (i == 1) {
                applyPaintStyle(object, scale, true);
            }
            if (object.rx == 0 && object.ry == 0) {
                canvas.drawRect(rectF, paint);
            } else {
                float rx = object.rx != 0 ? object.rx : object.ry;
                float ry = object.ry != 0 ? object.ry : object.rx;
                canvas.drawRoundRect(rectF, rx, ry, paint);
            }
        }

        if (DEBUG) { // draw final object center
            canvas.drawPoint(pointF.x, pointF.y, getDebugPaint(Color.RED));
        }
    }

    private void readCircle(XmlPullParser parser, SvgObject parentGroup)
            throws IOException, XmlPullParserException {
        SvgObject object = new SvgObject(SvgObject.TYPE_CIRCLE);
        object.isInGroup = parentGroup != null;

        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_CIRCLE);
        String tag = parser.getName();
        object.id = parser.getAttributeValue(null, "id");

        if (tag.equals(SvgObject.TYPE_CIRCLE)) {
            if (object.id == null) {
                Log.w(TAG, "readCircle: id is missing, skipping...");
                return;
            } else if (ids.contains(object.id)) {
                Log.w(TAG, "readCircle: id '" + object.id + "' already exists, skipping...");
                return;
            }

            object.cx = parseFloat(parser.getAttributeValue(null, "cx"));
            object.cy = parseFloat(parser.getAttributeValue(null, "cy"));
            object.r = parseFloat(parser.getAttributeValue(null, "r"));

            readStyle(parser, object);
            parseTransformation(parser.getAttributeValue(null, "transform"), object);

            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_CIRCLE);

        // apply display metrics
        if (!object.isInGroup) { // else keep absolute values for later calculation
            object.cx /= svgWidth;
            object.cy /= svgHeight;
        }
        object.r *= pixelUnit;

        if (parentGroup == null) {
            objects.add(object);
            ids.add(object.id);
        } else {
            parentGroup.children.add(object);
        }
    }

    private void drawCircle(Canvas canvas, SvgObject object, SvgObject parentGroup) {
        pointF = getFinalCenter(canvas, object, parentGroup);
        float scale = getFinalScale(object, parentGroup);

        // start with fill and repeat with stroke if both are set
        int runs = applyPaintStyle(object, scale, false) ? 2 : 1;
        for (int i = 0; i < runs; i++) {
            if (i == 1) {
                applyPaintStyle(object, scale, true);
            }
            if (object.type.equals(SvgObject.TYPE_CIRCLE) || object.rx == object.ry) {
                float radius = object.r > 0 ? object.r : object.rx;
                canvas.drawCircle(pointF.x, pointF.y, radius * scale, paint);
            } else if (object.type.equals(SvgObject.TYPE_ELLIPSE)) {
                canvas.drawOval(
                        pointF.x - object.rx * scale,
                        pointF.y - object.ry * scale,
                        pointF.x + object.rx * scale,
                        pointF.y + object.ry * scale,
                        paint
                );
            }
        }
    }

    private void readEllipse(XmlPullParser parser, SvgObject parentGroup)
            throws IOException, XmlPullParserException {
        SvgObject object = new SvgObject(SvgObject.TYPE_ELLIPSE);
        object.isInGroup = parentGroup != null;

        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_ELLIPSE);
        String tag = parser.getName();
        object.id = parser.getAttributeValue(null, "id");

        if (tag.equals(SvgObject.TYPE_ELLIPSE)) {
            if (object.id == null) {
                Log.w(TAG, "readEllipse: id is missing, skipping...");
                return;
            } else if (ids.contains(object.id)) {
                Log.w(TAG, "readEllipse: id '" + object.id + "' already exists, skipping...");
                return;
            }

            object.cx = parseFloat(parser.getAttributeValue(null, "cx"));
            object.cy = parseFloat(parser.getAttributeValue(null, "cy"));
            object.rx = parseFloat(parser.getAttributeValue(null, "rx"));
            object.ry = parseFloat(parser.getAttributeValue(null, "ry"));

            readStyle(parser, object);
            parseTransformation(parser.getAttributeValue(null, "transform"), object);

            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_ELLIPSE);

        // apply display metrics
        if (!object.isInGroup) { // else keep absolute values for later calculation
            object.cx /= svgWidth;
            object.cy /= svgHeight;
        }
        object.rx *= pixelUnit;
        object.ry *= pixelUnit;

        if (parentGroup == null) {
            objects.add(object);
            ids.add(object.id);
        } else {
            parentGroup.children.add(object);
        }
    }

    // drawEllipse is included in drawCircle

    private void readImage(XmlPullParser parser, SvgObject parentGroup)
            throws IOException, XmlPullParserException {
        SvgObject object = new SvgObject(SvgObject.TYPE_IMAGE);
        object.isInGroup = parentGroup != null;

        parser.require(XmlPullParser.START_TAG, null, SvgObject.TYPE_IMAGE);
        String tag = parser.getName();
        object.id = parser.getAttributeValue(null, "id");

        if (tag.equals(SvgObject.TYPE_IMAGE)) {
            if (object.id == null) {
                Log.w(TAG, "readImage: id is missing, skipping...");
                return;
            } else if (ids.contains(object.id)) {
                Log.w(TAG, "readImage: id '" + object.id + "' already exists, skipping...");
                return;
            }

            object.width = parseFloat(parser.getAttributeValue(null, "width"));
            object.height = parseFloat(parser.getAttributeValue(null, "height"));
            float x = parseFloat(parser.getAttributeValue(null, "x"));
            float y = parseFloat(parser.getAttributeValue(null, "y"));
            object.cx = x + object.width / 2;
            object.cy = y + object.height / 2;

            readStyle(parser, object);
            parseTransformation(parser.getAttributeValue(null, "transform"), object);

            String image = parser.getAttributeValue(parser.getNamespace("xlink"), "href");
            if (image != null) {
                image = image.substring(image.indexOf(",") + 1);
                byte[] decoded = Base64.decode(image, Base64.DEFAULT);
                object.bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            }

            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, null, SvgObject.TYPE_IMAGE);

        // apply display metrics
        object.width *= pixelUnit;
        object.height *= pixelUnit;
        if (!object.isInGroup) { // else keep absolute values for later calculation
            object.cx /= svgWidth;
            object.cy /= svgHeight;
        }

        if (parentGroup == null) {
            objects.add(object);
            ids.add(object.id);
        } else {
            parentGroup.children.add(object);
        }
    }

    private void drawImage(Canvas canvas, SvgObject object, SvgObject parentGroup) {
        paint.reset();
        paint.setAntiAlias(true);

        float scale = getFinalScale(object, parentGroup);
        pointF = getFinalCenter(canvas, object, parentGroup);
        rectF.set(
                pointF.x - (object.width * scale) / 2,
                pointF.y - (object.height * scale) / 2,
                pointF.x + (object.width * scale) / 2,
                pointF.y + (object.height * scale) / 2
        );

        canvas.drawBitmap(object.bitmap, null, rectF, paint);
    }

    private void parseTransformation(String transformation, SvgObject object) {
        if (transformation == null || transformation.isEmpty()) {
            return;
        }
        String[] transform = transformation.split("[ ](?=[^)]*?(?:\\(|$))");
        for (String action : transform) {
            String value = action.substring(action.indexOf("(") + 1, action.indexOf(")"));
            if (action.contains("rotate")) {
                String[] rotation = value.split("[\\n\\r\\s]+");

                object.rotation = Float.parseFloat(rotation[0]);
                if (rotation.length == 3) {
                    object.rotationX = Float.parseFloat(rotation[1]);
                    object.rotationY = Float.parseFloat(rotation[2]);
                }

                if (!object.type.equals(SvgObject.TYPE_GROUP)) {
                    pointF = getRotatedPoint(
                            object.cx, object.cy, object.rotationX, object.rotationY, object.rotation
                    );
                    object.cx = pointF.x;
                    object.cy = pointF.y;
                }
            } else if (action.contains("translate")) {
                String[] translation = value.split(",");
                if (translation.length != 2) {
                    Log.e(TAG, "parseTransformation: translation: value not supported");
                    return;
                }
                object.translationX = Float.parseFloat(translation[0]) / svgWidth;
                object.translationY = Float.parseFloat(translation[1]) / svgHeight;
            } else if (action.contains("scale")) {
                String[] scale = value.split("[\\n\\r\\s]+");
                if (scale.length > 1) {
                    Log.e(TAG, "parseTransformation: scale: multiple values are not supported");
                    return;
                }
                object.scale = Float.parseFloat(scale[0]);
        /*if (object.scale != 1) { TODO: at that time the cx and cy values in the original svg size
          object.cx *= object.scale;
          object.cy *= object.scale;
        }*/
            }
        }
    }

    private void readStyle(XmlPullParser parser, SvgObject object) {
        object.fill = parseColor(parser.getAttributeValue(null, "fill"));
        object.stroke = parseColor(parser.getAttributeValue(null, "stroke"));
        object.fillOpacity = parseOpacity(
                parser.getAttributeValue(null, "fill-opacity")
        );
        object.strokeOpacity = parseOpacity(
                parser.getAttributeValue(null, "stroke-opacity")
        );
        object.strokeWidth = parseFloat(parser.getAttributeValue(null, "stroke-width"));
        object.strokeLineCap = parser.getAttributeValue(null, "stroke-linecap");
        object.strokeLineJoin = parser.getAttributeValue(null, "stroke-linejoin");
    }

    /**
     * @return true if a second draw for a separate stroke style is needed
     */
    private boolean applyPaintStyle(SvgObject object, float scale, boolean applyStrokeIfBothSet) {
        paint.reset();
        paint.setAntiAlias(true);

        boolean hasFill = object.fill != 0;
        boolean hasStroke = object.stroke != 0 && object.strokeWidth > 0;
        boolean hasFillAndStroke = hasFill && hasStroke;

        if ((hasFillAndStroke && applyStrokeIfBothSet) || (!hasFill && hasStroke)) {
            paint.setStyle(Style.STROKE);
            paint.setShader(object.shader);
            paint.setARGB(
                    (int) (object.strokeOpacity * 255),
                    Color.red(object.stroke),
                    Color.green(object.stroke),
                    Color.blue(object.stroke)
            );
            paint.setStrokeWidth(object.strokeWidth * pixelUnit * scale);
            if (object.strokeLineCap != null) {
                switch (object.strokeLineCap) {
                    case SvgObject.LINE_CAP_BUTT:
                        paint.setStrokeCap(Cap.BUTT);
                        break;
                    case SvgObject.LINE_CAP_ROUND:
                        paint.setStrokeCap(Cap.ROUND);
                        break;
                    case SvgObject.LINE_CAP_SQUARE:
                        paint.setStrokeCap(Cap.SQUARE);
                        break;
                }
            }
            if (object.strokeLineJoin != null) {
                switch (object.strokeLineJoin) {
                    case SvgObject.LINE_JOIN_MITER:
                        paint.setStrokeJoin(Join.MITER);
                        break;
                    case SvgObject.LINE_JOIN_ROUND:
                        paint.setStrokeJoin(Join.ROUND);
                        break;
                    case SvgObject.LINE_JOIN_BEVEL:
                        paint.setStrokeJoin(Join.BEVEL);
                        break;
                }
            }
        } else if (hasFillAndStroke || hasFill) {
            paint.setStyle(Style.FILL);
            paint.setShader(object.shader);
            paint.setColor(ColorUtils.setAlphaComponent(object.fill, (int) (object.fillOpacity * 255)));
        }

        if (DEBUG) { // draw semi-translucent for point/pivot debugging
            paint.setAlpha(150);
        }

        return hasFillAndStroke;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static class SvgObject {

        public static final String TYPE_NONE = "none";
        public static final String TYPE_GROUP = "g";
        public static final String TYPE_PATH = "path";
        public static final String TYPE_RECT = "rect";
        public static final String TYPE_CIRCLE = "circle";
        public static final String TYPE_ELLIPSE = "ellipse";
        public static final String TYPE_IMAGE = "image";
        // stroke line cap
        public static final String LINE_CAP_BUTT = "butt";
        public static final String LINE_CAP_ROUND = "round";
        public static final String LINE_CAP_SQUARE = "square";
        // stroke line join
        public static final String LINE_JOIN_ROUND = "round";
        public static final String LINE_JOIN_BEVEL = "bevel";
        public static final String LINE_JOIN_MITER = "miter";

        public String id;
        public final String type;
        public boolean isInGroup;
        public float elevation;
        public int zoomRotation;
        public boolean isRotatable;
        public float pivotOffsetX, pivotOffsetY;

        // GROUP
        public List<SvgObject> children;
        public float cxFinal, cyFinal;
        public float childScale;
        // offset for each child (set on the child objects)
        public float xDistGroupCenter, yDistGroupCenter;

        // STYLE
        public int fill;
        public int stroke;
        public float fillOpacity, strokeOpacity;
        public String strokeLineCap, strokeLineJoin;
        public float strokeWidth;
        public Shader shader;

        // TRANSFORMATION
        public float rotation, rotationX, rotationY;
        public float translationX, translationY;
        public float scale;

        // PATH
        public Path path;
        public boolean willBeIntersected = false;
        public final Path pathTransformed = new Path();
        public final List<PathIntersection> intersections = new ArrayList<>();

        // RECT/IMAGE
        public float width, height;
        public float rx, ry;
        public Bitmap bitmap;

        // CIRCLE
        public float cx, cy;
        public float r;

        public SvgObject(@NonNull String type) {
            this.type = type;
        }

        public void addPathIntersection(String id, String color) {
            if (isInGroup || type.equals(TYPE_GROUP)) {
                Log.e(TAG, "addPathIntersection: operation does not support groups or children");
                return;
            }
            PathIntersection intersection = new PathIntersection(id, color);
            if (!intersections.contains(intersection)) {
                intersections.add(intersection);
            }
        }

        @NonNull
        @Override
        public String toString() {
            if (type.equals(TYPE_GROUP)) {
                return "SvgGroup{'" + id + "', children=" + children.toString() + '}';
            } else {
                return "SvgObject('" + id + "', '" + type + "')";
            }
        }
    }

    private float parseFloat(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private float parseOpacity(String value) {
        if (value != null && !value.isEmpty()) {
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 1;
        }
    }

    private static int parseColor(String value) {
        if (value != null && !value.isEmpty() && !value.equals("#00000000") && !value.equals("none")) {
            try {
                return Color.parseColor(value);
            } catch (IllegalArgumentException e) {
                if (value.matches("#[a-fA-F0-9]{3}")) {
                    String first = value.substring(1, 2);
                    String second = value.substring(2, 3);
                    String third = value.substring(3, 4);
                    String hex = "#" + first + first + second + second + third + third;
                    try {
                        return Color.parseColor(hex);
                    } catch (IllegalArgumentException exception) {
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    private PointF getRotatedPoint(float x, float y, float cx, float cy, float degrees) {
        double radians = Math.toRadians(degrees);

        float x1 = x - cx;
        float y1 = y - cy;

        float x2 = (float) (x1 * Math.cos(radians) - y1 * Math.sin(radians));
        float y2 = (float) (x1 * Math.sin(radians) + y1 * Math.cos(radians));

        return new PointF(x2 + cx, y2 + cy);
    }

    private PointF getFinalCenter(Canvas canvas, SvgObject object, SvgObject parentGroup) {
        float cx;
        float cy;
        if (object.isInGroup) {
            cx = parentGroup.cxFinal + object.xDistGroupCenter * parentGroup.childScale;
            cy = parentGroup.cyFinal + object.yDistGroupCenter * parentGroup.childScale;
        } else {
            cx = object.cx * canvas.getWidth() + object.translationX * canvas.getWidth();
            cy = object.cy * canvas.getHeight() + object.translationY * canvas.getHeight();
        }

        float cxShifted = cx - (offsetX * object.elevation);
        float cyShifted = cy - (offsetY * object.elevation);

        // We need to compensate the object rotation, else the object would shift in that direction
        // This is caused by the canvas rotation, but that's how objects can be rotated
        PointF compensated = getRotatedPoint(cxShifted, cyShifted, cx, cy, -object.rotation);
        cx = compensated.x;
        cy = compensated.y;

        float centerX = canvas.getWidth() / 2f;
        if (cx < centerX) {
            float dist = centerX - cx;
            cx += dist * object.elevation * zoom;
        } else {
            float dist = cx - centerX;
            cx -= dist * object.elevation * zoom;
        }

        float centerY = canvas.getHeight() / 2f;
        if (cy < centerY) {
            float dist = centerY - cy;
            cy += dist * object.elevation * zoom;
        } else {
            float dist = cy - centerY;
            cy -= dist * object.elevation * zoom;
        }
        return new PointF(cx, cy);
    }

    private float getFinalScale(SvgObject object, SvgObject parentGroup) {
        return object.isInGroup ? parentGroup.childScale : scale - (zoom * object.elevation);
    }

    private Paint getDebugPaint(@ColorInt int color) {
        paintDebug.setColor(color);
        return paintDebug;
    }

    private static class PathIntersection {
        final String id;
        final int color;

        public PathIntersection(@NonNull String id, @NonNull String color) {
            this.id = id;
            this.color = parseColor(color);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PathIntersection)) {
                return false;
            }
            PathIntersection that = (PathIntersection) o;
            return color == that.color && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, color);
        }
    }
}
