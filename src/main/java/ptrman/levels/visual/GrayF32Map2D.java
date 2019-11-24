package ptrman.levels.visual;

import boofcv.struct.image.GrayF32;
import ptrman.Datastructures.IMap2d;

/** adapter to boofcv image */
public class GrayF32Map2D implements IMap2d<Float> {

	final GrayF32 img;

	public GrayF32Map2D(GrayF32 img) {
		this.img = img;
	}

	@Override
	public Float readAt(int x, int y) {
		return img.get(x, y);
	}

	@Override
	public void setAt(int x, int y, Float value) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getWidth() {
		return img.getWidth();
	}

	@Override
	public int getLength() {
		return img.getHeight();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public IMap2d<Float> copy() {
		throw new UnsupportedOperationException("TODO");
	}
}
