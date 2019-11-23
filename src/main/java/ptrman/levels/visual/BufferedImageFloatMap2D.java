package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/** adapter to AWT BufferedImage reduced to single color plane */
abstract public class BufferedImageFloatMap2D implements IMap2d<Float> {

	public static BufferedImageFloatMap2D map(BufferedImage b) {
		switch (b.getType()) {
			case BufferedImage.TYPE_INT_RGB:
				return new RGBToMono(b);
			default:
				throw new UnsupportedOperationException("TODO");
		}
	}

	final BufferedImage b;

	private static final class RGBToMono extends BufferedImageFloatMap2D {

		private final int[] raster;
		private final int w;

		private RGBToMono(BufferedImage b) {
			super(b);
			this.raster = ((DataBufferInt)(b.getRaster().getDataBuffer())).getData();
			this.w = b.getWidth();
		}

		@Override
		public Float readAt(int x, int y) {
			int rgb = raster[y * w + x];
			float r = ((rgb >> 16) & 255)/256f;
			float g = ((rgb >> 8) & 255)/256f;
			float b = ((rgb) & 255)/256f;
			return (r + g + b)/3;
		}

	}

	private BufferedImageFloatMap2D(BufferedImage b) {
		this.b = b;
	}

	@Override
	public void setAt(int x, int y, Float value) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public int getWidth() {
		return b.getWidth();
	}

	@Override
	public int getLength() {
		return b.getHeight();
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
