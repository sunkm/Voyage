package com.manchuan.tools.colorpicker.builder;

import com.manchuan.tools.colorpicker.ColorPickerView;
import com.manchuan.tools.colorpicker.renderer.ColorWheelRenderer;
import com.manchuan.tools.colorpicker.renderer.FlowerColorWheelRenderer;
import com.manchuan.tools.colorpicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
	public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
		switch (wheelType) {
			case CIRCLE:
				return new SimpleColorWheelRenderer();
			case FLOWER:
				return new FlowerColorWheelRenderer();
		}
		throw new IllegalArgumentException("wrong WHEEL_TYPE");
	}
}