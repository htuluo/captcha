package com.anji.captcha.util;

/**
 * @author ： luoleiming
 * @date ：Created in 2020/10/20
 * @description： //TODO
 */

import com.anji.captcha.service.impl.BlockPuzzleCaptchaServiceImpl;
import com.anji.captcha.util.dto.VerifyImage;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author WelKin
 * @ClassName VerifyImageUtil
 * @Description: TODO
 * @Date 2019/06/13 10:26
 * @Version 1.0
 **/
public class VerifyImageUtil {

    /**
     * 源文件宽度
     */
    private static int ORI_WIDTH = 300;
    /**
     * 源文件高度
     */
    private static int ORI_HEIGHT = 150;
    /**
     * 模板图宽度
     */
    private static int CUT_WIDTH = 60;
    /**
     * 模板图高度
     */
    private static int CUT_HEIGHT = 60;
    /**
     * 抠图凸起圆心
     */
    private static int circleR = 5;
    /**
     * 抠图内部矩形填充大小
     */
    private static int RECTANGLE_PADDING = 8;
    /**
     * 抠图的边框宽度
     */
    private static int SLIDER_IMG_OUT_PADDING = 1;
    private static int INTERRUPT_IMG_COUNT = 1;


    /**
     * 根据传入的路径生成指定验证码图片
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static VerifyImage getVerifyImage(String filePath) throws IOException {
        BufferedImage srcImage = ImageIO.read(new File(filePath));
        int locationX = CUT_WIDTH + new Random().nextInt(srcImage.getWidth() - CUT_WIDTH * 3);
        int locationY = CUT_HEIGHT + new Random().nextInt(srcImage.getHeight() - CUT_HEIGHT) / 2;
        BufferedImage markImage = new BufferedImage(CUT_WIDTH, CUT_HEIGHT, BufferedImage.TYPE_4BYTE_ABGR);
        int[][] data = getBlockData();
        cutImgByTemplate(srcImage, markImage, data, locationX, locationY);
        if (INTERRUPT_IMG_COUNT > 0) {
            int interruptX = 0, interruptY = 0;
            if (locationX < (srcImage.getWidth() / 2)) {
                interruptX = locationX + CUT_WIDTH + new Random().nextInt(srcImage.getWidth() - locationX - CUT_WIDTH * 2);
            } else {
                interruptX = new Random().nextInt(locationX - CUT_WIDTH * 2);
            }
//            if (locationY < srcImage.getHeight() / 2) {
//                interruptY = locationY + CUT_HEIGHT + new Random().nextInt(srcImage.getWidth() - locationY - CUT_HEIGHT);
//            } else {
//                interruptY = new Random().nextInt(locationX - CUT_HEIGHT);
//            }
            System.out.println(MessageFormat.format("width:{0},height:{1},interruptX:{2},cut_width:{3}", srcImage.getWidth(), srcImage.getHeight(), interruptX, CUT_WIDTH));
//            data = getBlockData();
            cutImgByTemplate(srcImage, null, data, interruptX, locationY);

        }
        return new VerifyImage(getImageBASE64(srcImage), getImageBASE64(markImage), locationX, locationY);
    }


    /**
     * 生成随机滑块形状
     * <p>
     * 0 透明像素
     * 1 滑块像素
     * 2 阴影像素
     *
     * @return int[][]
     */
    private static int[][] getBlockData() {
        int[][] data = new int[CUT_WIDTH][CUT_HEIGHT];
        Random random = new Random();
        //(x-a)²+(y-b)²=r²
        //x中心位置左右5像素随机
        double x1 = RECTANGLE_PADDING + (CUT_WIDTH - 2 * RECTANGLE_PADDING) / 2.0 - 5 + random.nextInt(10);
        //y 矩形上边界半径-1像素移动
        double y1_top = RECTANGLE_PADDING - random.nextInt(3);
        double y1_bottom = CUT_HEIGHT - RECTANGLE_PADDING + random.nextInt(3);
        double y1 = random.nextInt(2) == 1 ? y1_top : y1_bottom;


        double x2_right = CUT_WIDTH - RECTANGLE_PADDING - circleR + random.nextInt(2 * circleR - 4);
        double x2_left = RECTANGLE_PADDING + circleR - 2 - random.nextInt(2 * circleR - 4);
        double x2 = random.nextInt(2) == 1 ? x2_right : x2_left;
        double y2 = RECTANGLE_PADDING + (CUT_HEIGHT - 2 * RECTANGLE_PADDING) / 2.0 - 4 + random.nextInt(10);

        double po = Math.pow(circleR, 2);
        for (int i = 0; i < CUT_WIDTH; i++) {
            for (int j = 0; j < CUT_HEIGHT; j++) {
                //矩形区域
                boolean fill;
                if ((i >= RECTANGLE_PADDING && i < CUT_WIDTH - RECTANGLE_PADDING)
                        && (j >= RECTANGLE_PADDING && j < CUT_HEIGHT - RECTANGLE_PADDING)) {
                    data[i][j] = 1;
                    fill = true;
                } else {
                    data[i][j] = 0;
                    fill = false;
                }
                //凸出区域
                double d3 = Math.pow(i - x1, 2) + Math.pow(j - y1, 2);
                if (d3 < po) {
                    data[i][j] = 1;
                } else {
                    if (!fill) {
                        data[i][j] = 0;
                    }
                }
                //凹进区域
                double d4 = Math.pow(i - x2, 2) + Math.pow(j - y2, 2);
                if (d4 < po) {
                    data[i][j] = 0;
                }
            }
        }
        //边界阴影
        for (int i = 0; i < CUT_WIDTH; i++) {
            for (int j = 0; j < CUT_HEIGHT; j++) {
                //四个正方形边角处理
                for (int k = 1; k <= SLIDER_IMG_OUT_PADDING; k++) {
                    //左上、右上
                    if (i >= RECTANGLE_PADDING - k && i < RECTANGLE_PADDING
                            && ((j >= RECTANGLE_PADDING - k && j < RECTANGLE_PADDING)
                            || (j >= CUT_HEIGHT - RECTANGLE_PADDING - k && j < CUT_HEIGHT - RECTANGLE_PADDING + 1))) {
                        data[i][j] = 2;
                    }

                    //左下、右下
                    if (i >= CUT_WIDTH - RECTANGLE_PADDING + k - 1 && i < CUT_WIDTH - RECTANGLE_PADDING + 1) {
                        for (int n = 1; n <= SLIDER_IMG_OUT_PADDING; n++) {
                            if (((j >= RECTANGLE_PADDING - n && j < RECTANGLE_PADDING)
                                    || (j >= CUT_HEIGHT - RECTANGLE_PADDING - n && j <= CUT_HEIGHT - RECTANGLE_PADDING))) {
                                data[i][j] = 2;
                            }
                        }
                    }
                }

                if (data[i][j] == 1 && j - SLIDER_IMG_OUT_PADDING > 0 && data[i][j - SLIDER_IMG_OUT_PADDING] == 0) {
                    data[i][j - SLIDER_IMG_OUT_PADDING] = 2;
                }
                if (data[i][j] == 1 && j + SLIDER_IMG_OUT_PADDING > 0 && j + SLIDER_IMG_OUT_PADDING < CUT_HEIGHT && data[i][j + SLIDER_IMG_OUT_PADDING] == 0) {
                    data[i][j + SLIDER_IMG_OUT_PADDING] = 2;
                }
                if (data[i][j] == 1 && i - SLIDER_IMG_OUT_PADDING > 0 && data[i - SLIDER_IMG_OUT_PADDING][j] == 0) {
                    data[i - SLIDER_IMG_OUT_PADDING][j] = 2;
                }
                if (data[i][j] == 1 && i + SLIDER_IMG_OUT_PADDING > 0 && i + SLIDER_IMG_OUT_PADDING < CUT_WIDTH && data[i + SLIDER_IMG_OUT_PADDING][j] == 0) {
                    data[i + SLIDER_IMG_OUT_PADDING][j] = 2;
                }
            }
        }
        return data;
    }

    /**
     * 裁剪区块
     * 根据生成的滑块形状，对原图和裁剪块进行变色处理
     *
     * @param oriImage   原图
     * @param cutImage   裁剪图
     * @param blockImage 滑块
     * @param x          裁剪点x
     * @param y          裁剪点y
     */
    private static void cutImgByTemplate(BufferedImage oriImage, BufferedImage cutImage, int[][] blockImage, int x, int y) {
        //临时数组遍历用于高斯模糊存周边像素值
        int[][] martrix = new int[3][3];
        int[] values = new int[9];
        for (int i = 0; i < CUT_WIDTH; i++) {
            for (int j = 0; j < CUT_HEIGHT; j++) {
                int _x = x + i;
                int _y = y + j;
                int rgbFlg = blockImage[i][j];
                int rgb_ori = oriImage.getRGB(_x, _y);
                // 原图中对应位置变色处理
                if (rgbFlg == 1) {
                    //抠图上复制对应颜色值
                    if (cutImage != null) {
                        cutImage.setRGB(i, j, rgb_ori);
                    }
                    //原图对应位置颜色变化
                    readPixel(oriImage, x + i, y + j, values);
                    fillMatrix(martrix, values);
                    oriImage.setRGB(x + i, y + j, avgMatrix(martrix));
//                    oriImage.setRGB(_x, _y, Color.LIGHT_GRAY.getRGB());
                } else if (rgbFlg == 2) {
                    if (cutImage != null) {
                        cutImage.setRGB(i, j, Color.WHITE.getRGB());
                    }
                    oriImage.setRGB(_x, _y, Color.GRAY.getRGB());
                } else if (rgbFlg == 0) {
                    //int alpha = 0;
                    if (cutImage != null) {
                        cutImage.setRGB(i, j, rgb_ori & 0x00ffffff);
                    }
                }
            }

        }
    }


    /**
     * 随机获取一张图片对象
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static BufferedImage getRandomImage(String path) throws IOException {
        File files = new File(path);
        File[] fileList = files.listFiles();
        List<String> fileNameList = new ArrayList<>();
        if (fileList != null && fileList.length != 0) {
            for (File tempFile : fileList) {
                if (tempFile.isFile() && tempFile.getName().endsWith(".jpg")) {
                    fileNameList.add(tempFile.getAbsolutePath().trim());
                }
            }
        }
        Random random = new Random();
        File imageFile = new File(fileNameList.get(random.nextInt(fileNameList.size())));
        return ImageIO.read(imageFile);
    }

    /**
     * 将IMG输出为文件
     *
     * @param image
     * @param file
     * @throws Exception
     */
    public static void writeImg(BufferedImage image, String file) throws Exception {
        byte[] imagedata = null;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bao);
        imagedata = bao.toByteArray();
        FileOutputStream out = new FileOutputStream(new File(file));
        out.write(imagedata);
        out.close();
    }

    /**
     * 将图片转换为BASE64
     *
     * @param image
     * @return
     * @throws IOException
     */
    public static String getImageBASE64(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        //转成byte数组
        byte[] bytes = out.toByteArray();
        BASE64Encoder encoder = new BASE64Encoder();
        //生成BASE64编码
        return encoder.encode(bytes);
    }

    /**
     * 将BASE64字符串转换为图片
     *
     * @param base64String
     * @return
     */
    public static BufferedImage base64StringToImage(String base64String) {
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64String);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void readPixel(BufferedImage img, int x, int y, int[] pixels) {
        int xStart = x - 1;
        int yStart = y - 1;
        int current = 0;
        for (int i = xStart; i < 3 + xStart; i++) {
            for (int j = yStart; j < 3 + yStart; j++) {
                int tx = i;
                if (tx < 0) {
                    tx = -tx;

                } else if (tx >= img.getWidth()) {
                    tx = x;
                }
                int ty = j;
                if (ty < 0) {
                    ty = -ty;
                } else if (ty >= img.getHeight()) {
                    ty = y;
                }
                pixels[current++] = img.getRGB(tx, ty);

            }
        }
    }

    private static void fillMatrix(int[][] matrix, int[] values) {
        int filled = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                x[j] = values[filled++];
            }
        }
    }

    private static int avgMatrix(int[][] matrix) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < matrix.length; i++) {
            int[] x = matrix[i];
            for (int j = 0; j < x.length; j++) {
                if (j == 1) {
                    continue;
                }
                Color c = new Color(x[j]);
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
            }
        }
        return new Color(r / 8, g / 8, b / 8).getRGB();
    }

    /**
     * 高斯模糊
     *
     * @param radius
     * @param horizontal
     * @return
     */
    public static ConvolveOp getGaussianBlurFilter(int radius,
                                                   boolean horizontal) {
        if (radius < 1) {
            throw new IllegalArgumentException("Radius must be >= 1");
        }

        int size = radius * 2 + 1;
        float[] data = new float[size];

        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;

        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }

        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }

        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }

    public static void simpleBlur(BufferedImage src, BufferedImage dest) {
        BufferedImageOp op = getGaussianBlurFilter(1, false);
        op.filter(src, dest);
    }
}
