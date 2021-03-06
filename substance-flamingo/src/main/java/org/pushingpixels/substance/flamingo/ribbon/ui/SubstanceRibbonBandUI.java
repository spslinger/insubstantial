/*
 * Copyright (c) 2005-2010 Flamingo / Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Flamingo Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.pushingpixels.substance.flamingo.ribbon.ui;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.*;

import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;
import org.pushingpixels.flamingo.internal.ui.ribbon.BasicRibbonBandUI;
import org.pushingpixels.lafwidget.LafWidgetUtilities;
import org.pushingpixels.substance.api.*;
import org.pushingpixels.substance.api.painter.fill.MatteFillPainter;
import org.pushingpixels.substance.api.painter.fill.SubstanceFillPainter;
import org.pushingpixels.substance.api.watermark.SubstanceWatermark;
import org.pushingpixels.substance.flamingo.common.TransitionAwareResizableIcon;
import org.pushingpixels.substance.flamingo.common.ui.ActionPopupTransitionAwareUI;
import org.pushingpixels.substance.internal.animation.StateTransitionTracker;
import org.pushingpixels.substance.internal.painter.DecorationPainterUtils;
import org.pushingpixels.substance.internal.painter.SeparatorPainterUtils;
import org.pushingpixels.substance.internal.utils.*;

/**
 * UI for ribbon bands in <b>Substance</b> look and feel.
 * 
 * @author Kirill Grouchnikov
 */
public class SubstanceRibbonBandUI extends BasicRibbonBandUI {
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.plaf.ComponentUI#createUI(javax.swing.JComponent)
	 */
	public static ComponentUI createUI(JComponent comp) {
		SubstanceCoreUtilities.testComponentCreationThreadingViolation(comp);
		return new SubstanceRibbonBandUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#installDefaults()
	 */
	@Override
	protected void installDefaults() {
		super.installDefaults();
		SubstanceLookAndFeel.setDecorationType(this.ribbonBand,
				DecorationAreaType.GENERAL);

		Color backgr = this.ribbonBand.getBackground();
		if (backgr == null || backgr instanceof UIResource) {
			Color toSet = SubstanceColorSchemeUtilities.getColorScheme(
					this.ribbonBand, ComponentState.ENABLED)
					.getBackgroundFillColor();
			this.ribbonBand.setBackground(new ColorUIResource(toSet));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#uninstallDefaults()
	 */
	@Override
	protected void uninstallDefaults() {
		DecorationPainterUtils.clearDecorationType(this.ribbonBand);
		super.uninstallDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#installComponents()
	 */
	@Override
	protected void installComponents() {
		super.installComponents();

		SubstanceLookAndFeel.setDecorationType(this.ribbonBand,
				DecorationAreaType.GENERAL);

		if (this.expandButton != null) {
			this.expandButton.putClientProperty(
					SubstanceLookAndFeel.USE_THEMED_DEFAULT_ICONS,
					Boolean.FALSE);
			this.expandButton.setFocusable(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#paintBandTitle(java.awt
	 * .Graphics, java.awt.Rectangle, java.lang.String)
	 */
	@Override
	protected void paintBandTitle(Graphics graphics, Rectangle titleRectangle,
			String title) {
		// fix for issue 28 - empty ribbon band
		if (titleRectangle.width <= 0)
			return;

		Graphics2D g2d = (Graphics2D) graphics.create();
		g2d.setFont(UIManager.getFont("Ribbon.font"));

		FontMetrics fm = graphics.getFontMetrics();

		int currLength = (int) fm.getStringBounds(title, g2d).getWidth();
		String titleToPaint = title;
		while (currLength > titleRectangle.width) {
			title = title.substring(0, title.length() - 1);
			titleToPaint = title + "...";
			currLength = (int) fm.getStringBounds(titleToPaint, g2d).getWidth();
		}

		SubstanceSkin skin = SubstanceCoreUtilities.getSkin(this.ribbonBand);

		// compute the FG color for the ribbon band
		// SubstanceColorScheme fgColorScheme = skin.getColorScheme(
		// this.expandButton, ComponentState.ENABLED);

		// make the title color blend a little with the background
		SubstanceColorScheme bgColorScheme = skin
				.getBackgroundColorScheme(DecorationAreaType.HEADER);
		Color bgFillColor = bgColorScheme.getBackgroundFillColor();
		Color fgColor = bgColorScheme.getForegroundColor();
		fgColor = SubstanceColorUtilities.getInterpolatedColor(fgColor,
				bgFillColor, 0.8f);

		g2d.setColor(fgColor);

		// restrict the title rectangle so that it's painted centered
		int deltaX = (titleRectangle.width - currLength) / 2;
		int deltaY = (titleRectangle.height - fm.getAscent() - fm.getDescent()) / 2;
		Rectangle smallTitleRectangle = new Rectangle(
				titleRectangle.x + deltaX, titleRectangle.y + deltaY,
				titleRectangle.width - 2 * deltaX, titleRectangle.height - 2
						* deltaY);
		SubstanceTextUtilities.paintText(g2d, this.ribbonBand,
				smallTitleRectangle, titleToPaint, -1, g2d.getFont(), g2d
						.getColor(), g2d.getClipBounds());

		g2d.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#paintBandTitleBackground
	 * (java.awt.Graphics, java.awt.Rectangle, java.lang.String)
	 */
	@Override
	protected void paintBandTitleBackground(Graphics g,
			Rectangle titleRectangle, String title) {
		SubstanceFillPainter gradientPainter = new MatteFillPainter();

		Graphics2D g2d = (Graphics2D) g.create();

		SubstanceSkin skin = SubstanceCoreUtilities.getSkin(this.ribbonBand);
		SubstanceColorScheme colorScheme = skin
				.getBackgroundColorScheme(DecorationAreaType.HEADER);

		boolean isDark = colorScheme.isDark();
		float alpha = 0.85f - (isDark ? 0.15f : 0.35f) * this.rolloverAmount;
		g2d.setComposite(LafWidgetUtilities.getAlphaComposite(this.ribbonBand,
				alpha, g));

		SubstanceRibbonBandBorder border = (SubstanceRibbonBandBorder) this.ribbonBand
				.getBorder();
		float cornerRadius = border.getCornerRadius(this.ribbonBand);

		GeneralPath outline = new GeneralPath();

		// top left
		outline.moveTo(0, 0);
		// top right
		outline.lineTo(titleRectangle.width, 0);
		// bottom right
		outline.lineTo(titleRectangle.width, titleRectangle.height
				- cornerRadius - 1);
		outline.append(new Arc2D.Double(titleRectangle.width - 2 * cornerRadius
				- 1, titleRectangle.height - 1 - 2 * cornerRadius,
				2 * cornerRadius, 2 * cornerRadius, 0, -90, Arc2D.OPEN), true);
		// bottom left
		outline.lineTo(cornerRadius, titleRectangle.height - 1);
		outline.append(new Arc2D.Double(0, titleRectangle.height - 2
				* cornerRadius - 1, 2 * cornerRadius, 2 * cornerRadius, 270,
				-90, Arc2D.OPEN), true);
		// top left
		outline.lineTo(0, 0);

		g2d.translate(titleRectangle.x, titleRectangle.y);

		gradientPainter.paintContourBackground(g2d, this.ribbonBand,
				titleRectangle.width, titleRectangle.height, outline, false,
				colorScheme, false);

		// outline
		g2d.setColor(colorScheme.getMidColor());
		g2d.setStroke(new BasicStroke(1.2f));
		g2d.draw(outline);

		// top line
		SubstanceColorScheme separatorScheme = SubstanceLookAndFeel
				.getCurrentSkin(this.ribbonBand).getColorScheme(
						DecorationAreaType.HEADER,
						ColorSchemeAssociationKind.SEPARATOR,
						ComponentState.ENABLED);
		g2d.setComposite(LafWidgetUtilities.getAlphaComposite(this.ribbonBand,
				alpha * 0.7f, g));
		SeparatorPainterUtils.paintSeparator(this.ribbonBand, g2d,
				separatorScheme, titleRectangle.width, 1,
				SwingConstants.HORIZONTAL, false, 0, 0, true);
		g2d.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#paintBandBackground(java
	 * .awt.Graphics, java.awt.Rectangle)
	 */
	@Override
	protected void paintBandBackground(Graphics graphics, Rectangle toFill) {
		paintRibbonBandBackground(graphics, this.ribbonBand,
				this.rolloverAmount, 0);
	}

	public static void paintRibbonBandBackground(Graphics graphics,
			Component comp, float rolloverAmount, int dy) {

		SubstanceSkin skin = SubstanceCoreUtilities.getSkin(comp);
		SubstanceColorScheme bgScheme = skin
				.getBackgroundColorScheme(SubstanceLookAndFeel
						.getDecorationType(comp));

		int offset = 20 - dy;
		float bp = (float) offset / (float) comp.getHeight();

		Color c1 = bgScheme.getUltraLightColor();
		Color c2 = SubstanceColorUtilities.getInterpolatedColor(bgScheme
				.getUltraLightColor(), bgScheme.getExtraLightColor(),
				rolloverAmount);
		Color c3 = SubstanceColorUtilities
				.getInterpolatedColor(bgScheme.getExtraLightColor(), bgScheme
						.getLightColor(), rolloverAmount);
		Color c4 = SubstanceColorUtilities.getInterpolatedColor(bgScheme
				.getUltraLightColor(), bgScheme.getExtraLightColor(),
				rolloverAmount);

		LinearGradientPaint fillPaint = new LinearGradientPaint(0, 0, 0, comp
				.getHeight(), new float[] { 0.0f, bp - 0.00001f, bp, 1.0f },
				new Color[] { c1, c2, c3, c4 });

		Graphics2D g2d = (Graphics2D) graphics.create();
		g2d.setPaint(fillPaint);
		g2d.fillRect(0, 0, comp.getWidth(), comp.getHeight());

		// stamp watermark
		SubstanceWatermark watermark = skin.getWatermark();
		if ((watermark != null) && SubstanceCoreUtilities.toDrawWatermark(comp))
			watermark.drawWatermarkImage(g2d, comp, 0, 0, comp.getWidth(), comp
					.getHeight());
		g2d.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#getBandTitleHeight()
	 */
	@Override
	public int getBandTitleHeight() {
		return 1 + SubstanceSizeUtils.getAdjustedSize(SubstanceSizeUtils
				.getComponentFontSize(this.ribbonBand), 17, 1, 1, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI#createExpandButton()
	 */
	@Override
	protected JCommandButton createExpandButton() {
		RibbonBandExpandButton result = new RibbonBandExpandButton();
		// since paintBandTitleBackground uses HEADER, mark this button with
		// HEADER as well to sync the mark color
		SubstanceLookAndFeel.setDecorationType(result,
				DecorationAreaType.HEADER);
		result.setIcon(getExpandButtonIcon(result));
		return result;
	}

	private static ResizableIcon getExpandButtonIcon(
			final AbstractCommandButton button) {
		final int fontSize = SubstanceSizeUtils.getComponentFontSize(button);
		int arrowIconHeight = (int) SubstanceSizeUtils
				.getSmallArrowIconHeight(fontSize) + 2;
		int arrowIconWidth = (int) SubstanceSizeUtils
				.getSmallArrowIconWidth(fontSize);
		final ResizableIcon arrowIcon = new TransitionAwareResizableIcon(
				button,
				new TransitionAwareResizableIcon.StateTransitionTrackerDelegate() {
					@Override
					public StateTransitionTracker getStateTransitionTracker() {
						return ((ActionPopupTransitionAwareUI) button.getUI())
								.getActionTransitionTracker();
					}
				}, new TransitionAwareResizableIcon.Delegate() {
					@Override
					public Icon getColorSchemeIcon(SubstanceColorScheme scheme,
							int width, int height) {
						return SubstanceImageCreator
								.getDoubleArrowIcon(
										SubstanceSizeUtils
												.getComponentFontSize(button),
										width,
										height,
										SubstanceSizeUtils
												.getDoubleArrowStrokeWidth(fontSize),
										button.getComponentOrientation()
												.isLeftToRight() ? SwingConstants.EAST
												: SwingConstants.WEST, scheme);
					}
				}, new Dimension(arrowIconWidth, arrowIconHeight));
		return arrowIcon;
	}

	@Override
	protected void syncExpandButtonIcon() {
		ResizableIcon icon = getExpandButtonIcon(this.expandButton);
		this.expandButton.setIcon(icon);
	}

	private class RibbonBandExpandButton extends JCommandButton implements
			SubstanceInternalButton {
		public RibbonBandExpandButton() {
			super(null, null);
			this.setFocusable(false);

			this.setBorder(new EmptyBorder(3, 2, 3, 2));
			this.setActionKeyTip(ribbonBand.getExpandButtonKeyTip());
			this.setActionRichTooltip(ribbonBand.getExpandButtonRichTooltip());
		}
	}
}
