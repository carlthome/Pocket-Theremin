package kth.csc.inda.pockettheremin.view;

import kth.csc.inda.pockettheremin.synth.Send;
import kth.csc.inda.pockettheremin.utils.Global;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.view.View;

public class GraphView extends View implements Global {

	private Bitmap bitmap;
	private Paint paintLine, paintGlow;
	private Canvas canvas;
	private float width, height;
	private float[] points; // Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
	private Path path;
	private final int colorBackground = Color.argb(255, 0, 0, 0);
	private final int colorForeground = Color.argb(255, 100, 255, 175);

	public GraphView(Context context) {
		super(context);
		canvas = new Canvas();
		canvas.drawColor(colorBackground);

		paintLine = new Paint();
		paintLine.setAntiAlias(true);
		paintLine.setDither(true);
		paintLine.setColor(colorForeground);
		paintLine.setStrokeWidth(3);
		paintLine.setStyle(Paint.Style.STROKE);

		paintGlow = new Paint();
		paintGlow.set(paintLine); // Inherit settings.
		paintGlow.setStrokeWidth(paintGlow.getStrokeWidth() * 4);
		paintGlow.setMaskFilter(new BlurMaskFilter(paintGlow.getStrokeWidth(), BlurMaskFilter.Blur.NORMAL));

		path = new Path();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		/*
		 * Store new size.
		 */
		width = w;
		height = h;

		/*
		 * Setup bitmap.
		 */
		bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
		canvas.setBitmap(bitmap);

		/*
		 * Setup points array expecting interleaved X and Y values.
		 */
		points = new float[(int) (width * 2)];

		/*
		 * Fill points array with X values.
		 */
		for (int i = 0, j = 0; i < points.length; i += 2, j++)
			points[i] = j;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		synchronized (this) {

			/*
			 * Get data.
			 */
			short[] samples = Send.getSamples();

			/*
			 * Fill points array with Y values.
			 */
			for (int i = 0, j = 1; j < points.length; i++, j += 2)
				points[j] = ((samples[i] / (2 * (float) Short.MAX_VALUE)) * height)
						+ (height / 2);

			/*
			 * Clear previous canvas.
			 */
			this.canvas.drawColor(colorBackground);

			/*
			 * Create path from points.
			 */
			path.moveTo(points[0], points[1]);
			for (int i = 2; i < points.length; i += 2)
				path.lineTo(points[i], points[i + 1]);
			
			/*
			 * Draw path.
			 */
			this.canvas.drawPath(path, paintLine);
			this.canvas.drawPath(path, paintGlow);
			
			/*
			 * Clear path.
			 */
			path.reset();

			/*
			 * Render result.
			 */
			canvas.drawBitmap(bitmap, 0, 0, null);
		}
	}
}
