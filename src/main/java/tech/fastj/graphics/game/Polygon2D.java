package tech.fastj.graphics.game;

import tech.fastj.math.Point;
import tech.fastj.math.Pointf;

import tech.fastj.graphics.Drawable;
import tech.fastj.graphics.util.DrawUtil;

import tech.fastj.systems.collections.Pair;
import tech.fastj.systems.control.GameHandler;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Objects;

/**
 * {@code Drawable} subclass for drawing a polygon.
 *
 * @author Andrew Dey
 * @since 1.0.0
 */
public class Polygon2D extends GameObject {

    /** {@link RenderStyle} representing the default render style as fill only, or {@link RenderStyle#Fill}. */
    public static final RenderStyle DefaultRenderStyle = RenderStyle.Fill;
    /** {@link Paint} representing the default fill paint value as the color black. */
    public static final Paint DefaultFill = Color.black;
    /** {@link Stroke} representing the default outline stroke value as a 1px outline with sharp edges. */
    public static final BasicStroke DefaultOutlineStroke = new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f);
    /** {@link Color} representing the default outline color value as the color black. */
    public static final Color DefaultOutlineColor = Color.black;

    public static final int QuadCurve = 1;
    public static final int BezierCurve = 2;
    public static final int MovePath = 3;

    private Pointf[] originalPoints;
    private Point[] alternateIndexes;

    private RenderStyle renderStyle;
    private Paint fillPaint;
    private Color outlineColor;
    private BasicStroke outlineStroke;

    /**
     * {@code Polygon2D} constructor that takes in an array of points.
     * <p>
     * If needed, an array of alternate point indexes can be supplied to model curves and other {@link Path2D} options.
     * <p>
     * This constructor defaults the fill paint to {@link #DefaultFill}, the outline stroke to
     * {@link #DefaultOutlineStroke}, the outline color to {@link #DefaultOutlineColor}, the render style to
     * {@link #DefaultRenderStyle}, and the {@code shouldRender} boolean to {@link Drawable#DefaultShouldRender}.
     *
     * @param points {@code Pointf} array that defines the points for the polygon.
     */
    protected Polygon2D(Pointf[] points, Point[] altIndexes) {
        originalPoints = points;
        alternateIndexes = altIndexes;
        setCollisionPath(DrawUtil.createPath(originalPoints, alternateIndexes));

        setFill(DefaultFill);
        setOutlineStroke(DefaultOutlineStroke);
        setOutlineColor(DefaultOutlineColor);
        setRenderStyle(DefaultRenderStyle);
    }

    /**
     * Gets a {@link Polygon2DBuilder} instance while setting the eventual {@link Polygon2D}'s {@code points} field.
     *
     * @param points {@code Pointf} array that defines the points for the {@code Polygon2D}.
     * @return A {@code Polygon2DBuilder} instance for creating a {@code Polygon2D}.
     */
    public static Polygon2DBuilder create(Pointf[] points) {
        return new Polygon2DBuilder(points, null, Drawable.DefaultShouldRender);
    }

    /**
     * Gets a {@link Polygon2DBuilder} instance while setting the eventual {@link Polygon2D}'s {@code points} and
     * {@code altIndexes} fields.
     *
     * @param points     {@code Pointf} array that defines the points for the {@code Polygon2D}.
     * @param altIndexes The {@code Point} array of alternate indexes defining where curves are in the array of points,
     *                   as well as other {@code Path2D} options.
     * @return A {@code Polygon2DBuilder} instance for creating a {@code Polygon2D}.
     */
    public static Polygon2DBuilder create(Pointf[] points, Point[] altIndexes) {
        return new Polygon2DBuilder(points, altIndexes, Drawable.DefaultShouldRender);
    }

    /**
     * Gets a {@link Polygon2DBuilder} instance while setting the eventual {@link Polygon2D}'s {@code points} and
     * {@code shouldRender} fields.
     *
     * @param points       {@code Pointf} array that defines the points for the {@code Polygon2D}.
     * @param shouldRender {@code boolean} that defines whether the {@code Polygon2D} would be rendered to the screen.
     * @return A {@code Polygon2DBuilder} instance for creating a {@code Polygon2D}.
     */
    public static Polygon2DBuilder create(Pointf[] points, boolean shouldRender) {
        return new Polygon2DBuilder(points, null, shouldRender);
    }

    /**
     * Gets a {@link Polygon2DBuilder} instance while setting the eventual {@link Polygon2D}'s {@code points},
     * {@code renderStyle}, and {@code shouldRender} fields.
     *
     * @param points       {@code Pointf} array that defines the points for the {@code Polygon2D}.
     * @param altIndexes   The {@code Point} array of alternate indexes defining where curves are in the array of
     *                     points, as well as other {@code Path2D} options.
     * @param shouldRender {@code boolean} that defines whether the {@code Polygon2D} would be rendered to the screen.
     * @return A {@code Polygon2DBuilder} instance for creating a {@code Polygon2D}.
     */
    public static Polygon2DBuilder create(Pointf[] points, Point[] altIndexes, boolean shouldRender) {
        return new Polygon2DBuilder(points, altIndexes, shouldRender);
    }

    /**
     * Creates a {@code Polygon2D} from the specified points.
     *
     * @param points {@code Pointf} array that defines the points for the {@code Polygon2D}.
     * @return The resulting {@code Polygon2D}.
     */
    public static Polygon2D fromPoints(Pointf[] points) {
        return new Polygon2DBuilder(points, null, Drawable.DefaultShouldRender).build();
    }

    /**
     * Creates a {@code Polygon2D} from the specified points and alternate indexes.
     *
     * @param points     {@code Pointf} array that defines the points for the {@code Polygon2D}.
     * @param altIndexes The {@code Point} array of alternate indexes defining where curves are in the array of points,
     *                   as well as other {@code Path2D} options.
     * @return The resulting {@code Polygon2D}.
     */
    public static Polygon2D fromPoints(Pointf[] points, Point[] altIndexes) {
        return new Polygon2DBuilder(points, altIndexes, Drawable.DefaultShouldRender).build();
    }

    /**
     * Creates a {@code Polygon2D} from the specified {@link Path2D.Float}.
     *
     * @param path {@code Path2D.Float} that defines the points from which to create a {@code Polygon2D}.
     * @return The resulting {@code Polygon2D}.
     */
    public static Polygon2D fromPath(Path2D.Float path) {
        Pair<Pointf[], Point[]> pathMesh = DrawUtil.pointsOfPathWithAlt(path);
        return new Polygon2DBuilder(pathMesh.getLeft(), pathMesh.getRight(), Drawable.DefaultShouldRender).build();
    }

    /**
     * Gets the polygon's original point set.
     *
     * @return The original set of points for this polygon, as a {@code Pointf[]}.
     */
    public Pointf[] getOriginalPoints() {
        return originalPoints;
    }

    /**
     * Gets the polygon's alternate indexes, which are associated with the
     * {@link #getOriginalPoints() original point set}.
     *
     * @return The original set of points for this polygon, as a {@code Pointf[]}.
     */
    public Point[] getAlternateIndexes() {
        return alternateIndexes;
    }

    /**
     * Gets the polygon's fill paint.
     *
     * @return The {@code Paint} set for this polygon.
     */
    public Paint getFill() {
        return fillPaint;
    }

    /**
     * Gets the polygon's outline color.
     *
     * @return The polygon's outline {@code Color}.
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Gets the polygon's outline stroke.
     *
     * @return The polygon's outline {@code BasicStroke}.
     */
    public BasicStroke getOutlineStroke() {
        return outlineStroke;
    }

    /**
     * Gets the polygon's render style.
     *
     * @return The polygon's {@code RenderStyle}.
     */
    public RenderStyle getRenderStyle() {
        return renderStyle;
    }

    /**
     * Sets the polygon's fill paint.
     *
     * @param newPaint The fill {@code Paint} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public Polygon2D setFill(Paint newPaint) {
        fillPaint = Objects.requireNonNull(newPaint);
        return this;
    }

    /**
     * Sets the polygon's outline color.
     *
     * @param newOutlineColor The outline {@code Color} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public Polygon2D setOutlineColor(Color newOutlineColor) {
        outlineColor = newOutlineColor;
        return this;
    }

    /**
     * Sets the polygon's outline stroke.
     *
     * @param newOutlineStroke The outline {@code BasicStroke} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public Polygon2D setOutlineStroke(BasicStroke newOutlineStroke) {
        outlineStroke = newOutlineStroke;
        return this;
    }

    /**
     * Sets the polygon's outline stroke and color.
     *
     * @param newOutlineStroke The outline {@code BasicStroke} to be used for the polygon.
     * @param newOutlineColor  The outline {@code Color} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public Polygon2D setOutline(BasicStroke newOutlineStroke, Color newOutlineColor) {
        outlineStroke = newOutlineStroke;
        outlineColor = newOutlineColor;
        return this;
    }

    /**
     * Sets the polygon's render style.
     *
     * @param newRenderStyle The {@code RenderStyle} to be used for the polygon.
     * @return The polygon instance, for method chaining.
     */
    public Polygon2D setRenderStyle(RenderStyle newRenderStyle) {
        renderStyle = newRenderStyle;
        return this;
    }

    /**
     * Gets the {@code Pointf} array associated with the current state of the polygon.
     *
     * @return The {@code Pointf} array associated with the current state of the polygon.
     */
    public Pointf[] getPoints() {
        return DrawUtil.pointsOfPath(transformedCollisionPath);
    }

    /**
     * Replaces the current point array with the parameter point array.
     * <p>
     * This does not reset the rotation, scale, or location of the original, unless specified with the second, third,
     * and fourth parameters.
     *
     * @param points           {@code Pointf} array that will replace the current points of the polygon.
     * @param resetTranslation Boolean to determine if the translation should be reset.
     * @param resetRotation    Boolean to determine if the rotation should be reset.
     * @param resetScale       Boolean to determine if the scale should be reset.
     */
    public void modifyPoints(Pointf[] points, boolean resetTranslation, boolean resetRotation, boolean resetScale) {
        originalPoints = points;
        alternateIndexes = null;

        resetTransform(resetTranslation, resetRotation, resetScale);
        setCollisionPath(DrawUtil.createPath(originalPoints));
    }

    /**
     * Replaces the current point array with the parameter point array and alternate indexes.
     * <p>
     * This does not reset the rotation, scale, or location of the original, unless specified with the third, fourth,
     * and fifth parameters.
     *
     * @param points           {@code Pointf} array that will replace the current points of the polygon.
     * @param altIndexes       {@code Point} array that will replace the current alternate indexes of the polygon.
     * @param resetTranslation Boolean to determine if the translation should be reset.
     * @param resetRotation    Boolean to determine if the rotation should be reset.
     * @param resetScale       Boolean to determine if the scale should be reset.
     */
    public void modifyPoints(Pointf[] points, Point[] altIndexes, boolean resetTranslation, boolean resetRotation, boolean resetScale) {
        originalPoints = points;
        alternateIndexes = altIndexes;

        resetTransform(resetTranslation, resetRotation, resetScale);
        setCollisionPath(DrawUtil.createPath(originalPoints, altIndexes));
    }

    /**
     * Resets the {@code Polygon2D}'s transform.
     *
     * @param resetTranslation Boolean to determine if the translation should be reset.
     * @param resetRotation    Boolean to determine if the rotation should be reset.
     * @param resetScale       Boolean to determine if the scale should be reset.
     */
    private void resetTransform(boolean resetTranslation, boolean resetRotation, boolean resetScale) {
        if (resetTranslation && resetRotation && resetScale) {
            transform.reset();
        } else {
            if (resetTranslation) {
                transform.resetTranslation();
            }
            if (resetRotation) {
                transform.resetRotation();
            }
            if (resetScale) {
                transform.resetScale();
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform oldTransform = (AffineTransform) g.getTransform().clone();
        Paint oldPaint = g.getPaint();
        Stroke oldStroke = g.getStroke();

        g.transform(getTransformation());

        switch (renderStyle) {
            case Fill: {
                g.setPaint(fillPaint);
                g.fill(collisionPath);
                break;
            }
            case Outline: {
                g.setStroke(outlineStroke);
                g.setPaint(outlineColor);
                g.draw(collisionPath);
                break;
            }
            case FillAndOutline: {
                g.setPaint(fillPaint);
                g.fill(collisionPath);

                g.setStroke(outlineStroke);
                g.setPaint(outlineColor);
                g.draw(collisionPath);
                break;
            }
        }

        g.setStroke(oldStroke);
        g.setPaint(oldPaint);
        g.setTransform(oldTransform);
    }

    @Override
    public void destroy(GameHandler origin) {
        originalPoints = new Pointf[]{};

        renderStyle = DefaultRenderStyle;
        fillPaint = DefaultFill;
        outlineColor = DefaultOutlineColor;
        outlineStroke = DefaultOutlineStroke;

        super.destroyTheRest(origin);
    }

    /**
     * Checks for equality between the {@code Polygon2D} and the other specified.
     *
     * @param other The {@code Polygon2D} to check for equality against.
     * @return Whether the two {@code Polygon2D}s are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Polygon2D polygon2D = (Polygon2D) other;
        return Arrays.equals(originalPoints, polygon2D.originalPoints)
                && renderStyle == polygon2D.renderStyle
                && DrawUtil.paintEquals(fillPaint, polygon2D.fillPaint)
                && outlineColor.equals(polygon2D.outlineColor)
                && outlineStroke.equals(polygon2D.outlineStroke);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(renderStyle, fillPaint, outlineColor, outlineStroke);
        result = 31 * result + Arrays.hashCode(originalPoints);
        return result;
    }

    @Override
    public String toString() {
        return "Polygon2D{" +
                "originalPoints=" + Arrays.toString(originalPoints) +
                ", renderStyle=" + renderStyle +
                ", fillPaint=" + fillPaint +
                ", outlineColor=" + outlineColor +
                ", outlineStroke=" + outlineStroke +
                '}';
    }
}
