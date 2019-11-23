package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/** adapter to AWT BufferedImage reduced to single color plane */
abstract public class BufferedImageMap2D<X> implements IMap2d<X> {

	public static BufferedImageMap2D<Float> mono(BufferedImage b) {
		switch (b.getType()) {
			case BufferedImage.TYPE_INT_ARGB: //TODO special alpha impl
			case BufferedImage.TYPE_INT_RGB:
				return new RGB_Mono(b);
			default:
				throw new UnsupportedOperationException("TODO");
		}
	}

	final BufferedImage b;

	public static BufferedImageMap2D<ColorRgb> rgb(BufferedImage b) {
		switch (b.getType()) {
			case BufferedImage.TYPE_INT_ARGB: //TODO special alpha impl
			case BufferedImage.TYPE_INT_RGB:
				return new RGB_RGB(b);
			default:
				throw new UnsupportedOperationException("TODO");
		}
	}

	private static final class RGB_Mono extends BufferedImageMap2D<Float> {

		private final int[] raster;
		private final int w;

		private RGB_Mono(BufferedImage b) {
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
	private static final class RGB_RGB extends BufferedImageMap2D<ColorRgb> {

		private final int[] raster;
		private final int w;

		private RGB_RGB(BufferedImage b) {
			super(b);
			this.raster = ((DataBufferInt)(b.getRaster().getDataBuffer())).getData();
			this.w = b.getWidth();
		}

		@Override
		public ColorRgb readAt(int x, int y) {
			int rgb = raster[y * w + x];
			return new ColorRgb(rgb);
		}

	}

	private BufferedImageMap2D(BufferedImage b) {
		this.b = b;
	}

	@Override
	public void setAt(int x, int y, X value) {
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
	public IMap2d<X> copy() {
		throw new UnsupportedOperationException("TODO");
	}

}
