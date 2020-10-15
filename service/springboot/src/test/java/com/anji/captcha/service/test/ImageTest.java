package com.anji.captcha.service.test;

import com.anji.captcha.service.impl.BlockPuzzleCaptchaServiceImpl;
import com.anji.captcha.util.ImageUtils;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author ： luoleiming
 * @date ：Created in 2020/10/15
 * @description： //TODO
 */
public class ImageTest {
    @Test
    public void test1() throws IOException {
        ImageUtils.cacheImage(null, null);
        BufferedImage originalImage = ImageUtils.getOriginal();
        BufferedImage templage = ImageUtils.getBase64StrToImage(ImageUtils.getslidingBlock());
        BlockPuzzleCaptchaServiceImpl.interferenceByTemplate(originalImage, templage, 125, 0);
        File outFile = new File("D:/origin.png");
        File outFile2 = new File("D:/slide.png");
        ImageIO.write(originalImage, "PNG", outFile);
        ImageIO.write(templage, "PNG", outFile2);
    }
}
