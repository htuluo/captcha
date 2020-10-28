package com.anji.captcha.service.test;

import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.model.vo.PointVO;
import com.anji.captcha.service.impl.BlockPuzzleCaptchaServiceImpl;
import com.anji.captcha.util.AESUtil;
import com.anji.captcha.util.ImageUtils;
import com.anji.captcha.util.VerifyImageUtil;
import com.anji.captcha.util.dto.VerifyImage;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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
        BlockPuzzleCaptchaServiceImpl.interferenceByTemplate(originalImage, templage, 200, 0);
        File outFile = new File("D:/origin.png");
        File outFile2 = new File("D:/slide.png");
        ImageIO.write(originalImage, "PNG", outFile);
        ImageIO.write(templage, "PNG", outFile2);
    }

    /**
     * 灰度图片处理
     *
     * @throws IOException
     */
    @Test
    public void test2() throws IOException {
        ImageUtils.cacheImage(null, null);
        BufferedImage originalImage = ImageUtils.getOriginal();
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                grayImage.setRGB(i, j, originalImage.getRGB(i, j));
            }
        }
        File outFile2 = new File("D:/gray.png");
        ImageIO.write(grayImage, "PNG", outFile2);
    }

    /**
     * AES加密处理
     *
     * @throws IOException
     */
    @Test
    public void test3() throws Exception {

        PointVO pointVO = new PointVO();
        pointVO.setX(141);
        pointVO.setY(5);
        pointVO.setSecretKey("TkWAbsBuIOPHFJe4");
        System.out.println(AESUtil.aesEncrypt(JSONObject.toJSONString(pointVO), pointVO.getSecretKey()));

    }

    @Test
    public void test4() throws Exception {
        VerifyImage verifyImage = null;
//        for (int i = 0; i < 500; i++) {

        verifyImage = VerifyImageUtil.getVerifyImage("D:/images/1.png");
//        }
        BufferedImage srcImg = VerifyImageUtil.base64StringToImage(verifyImage.getSrcImage());
        BufferedImage zipImg=VerifyImageUtil.compressImg(srcImg);
        BufferedImage cutImg = VerifyImageUtil.base64StringToImage(verifyImage.getCutImage());
        //高斯模糊
//        BufferedImage srcFilterImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
//        VerifyImageUtil.simpleBlur(srcImg, srcFilterImg);

        File outFile1 = new File("D:/srcImg.png");
        File outFile2 = new File("D:/cutImg.png");
        File outFile3 = new File("D:/srcImg_filter.png");
        File outFile4 = new File("D:/srcImg_zip.png");
        ImageIO.write(srcImg, "PNG", outFile1);
        ImageIO.write(cutImg, "PNG", outFile2);
//        ImageIO.write(zipImg, "PNG", outFile4);

    }
}
