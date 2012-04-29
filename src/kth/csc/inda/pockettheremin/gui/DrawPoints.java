package kth.csc.inda.pockettheremin.gui;

import java.util.LinkedList;
import java.util.ListIterator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DrawPoints extends View {
	final int height = 100;

	private Paint mPaint = new Paint();
	private LinkedList<Point> mPoints;

	public DrawPoints(Context context, LinkedList<Point> points) {
		super(context);
		this.mPoints = points;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = mPaint;
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(5);

		int i = 0;
		ListIterator<Point> it = mPoints.listIterator();
		while (it.hasNext()) {

			if (i % 12 == 0)
				paint.setColor(Color.RED);
			else
				paint.setColor(Color.WHITE);

			paint.setStrokeWidth(5);
			Point point = it.next();
			canvas.drawPoint(point.x, point.y, paint);
			canvas.drawPoint(point.x, point.y + height, paint);

			paint.setStrokeWidth(1);
			float[] pts = { point.x, point.y, point.x, point.y + height };
			canvas.drawLines(pts, paint);

			i++;
		}

	}
}
