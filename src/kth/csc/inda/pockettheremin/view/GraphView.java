package kth.csc.inda.pockettheremin.view;

import kth.csc.inda.pockettheremin.synth.Send;
import kth.csc.inda.pockettheremin.utils.Global;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.view.View;

public class GraphView extends View implements Global {

	private Bitmap bitmap;
	private Paint paint;
	private Canvas canvas;
	private float width, height;
	private float[] points; // Array of points to draw [x0 y0 x1 y1 x2 y2 ...]
	private Path path;

	public GraphView(Context context) {
		super(context);
		canvas = new Canvas();

		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.parseColor("#FFFFFFFF"));
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(3);
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
				points[j] = (-1 * samples[i] / height) + (height / 2);

			/*
			 * Clear previous canvas.
			 */
			this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

			/*
			 * Draw path from points.
			 */
			path.moveTo(points[0], points[1]);
			for (int i = 2; i < points.length; i += 2)
				path.lineTo(points[i], points[i + 1]);
			this.canvas.drawPath(path, paint);
			path.reset();

			if (DEBUG)
				this.canvas.drawPoints(points, paint);

			/*
			 * Render result.
			 */
			canvas.drawBitmap(bitmap, 0, 0, null);
		}
	}
}
