package org.example;

import com.hankcs.hanlp.HanLP;
import org.example.exceptions.FileAnalyseException;
import org.example.exceptions.HashException;
import org.example.exceptions.NotExistFileException;
import org.junit.jupiter.api.Assertions;

import java.io.FileNotFoundException;

@SuppressWarnings("all")

public class Test {
    //读取文件后得到的文本
    static String analyseStr;
    //两个示例句子
    static String originSentence = "今天是星期天，天气晴，今天晚上我要去看电影。";
    static String compareSentence = "今天是周天，天气晴朗，我晚上要去看电影。";
    //比对结果写入的文件
    static String writeFilePath ="C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig_ans.txt";
    //原文件
    static String OrigFilePath = "C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig.txt";
    //5个比对文件
    static String CopyFilePath1 = "C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig_0.8_add.txt";
    static String CopyFilePath2 = "C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig_0.8_del.txt";
    static String CopyFilePath3 = "C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig_0.8_dis_1.txt";
    static String CopyFilePath4 = "C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig_0.8_dis_10.txt";
    static String CopyFilePath5 = "C:\\Users\\86136\\Desktop\\3122004945\\src\\test\\java\\org\\example\\orig_0.8_dis_15.txt";

    /**
     * 测试写入文件
     */
    @org.junit.jupiter.api.Test
    void testWriteReadFile() {
        String s = null;
        try {
            Main.writeFile("------successfully content entry------", writeFilePath);
            s = Main.readFile(writeFilePath);
            Assertions.assertTrue(s.contains("------successfully content entry------"), "写入文件失败");
        } catch (NotExistFileException e) {
            System.out.println("读写文件失败");
        }
    }

    /**
     * 测试读取不存在的文件
     */
    @org.junit.jupiter.api.Test
    void testReadFileNotExist() {
        try {
            Main.readFile("C:\\not existing.txt");
        } catch (FileNotFoundException e) {
            System.out.println("文件不存在");
        }
    }

    /**
     * 测试文件解析异常(为null,为“”,为“ ”)
     */
    @org.junit.jupiter.api.Test
    void testFileAnalyseException() {
        try {
            Main.analyseText(null);
        } catch (FileAnalyseException e) {
            System.out.println("文件为 null");
        }
        try {
            Main.analyseText("");
        } catch (FileAnalyseException e) {
            System.out.println("文件为空");
        }
        try {
            Main.analyseText(" ");
        } catch (FileAnalyseException e) {
            System.out.println("文件为空");
        }
    }

    /**
     * 测试读取文件并查看分词结果
     */
    @org.junit.jupiter.api.Test
    void testReadFile() {
        try {
            //测试句子分词
            System.out.println("分词结果为：" + Main.analyseText(originSentence));
            //测试文本分词
            analyseStr = Main.readFile(OrigFilePath);
            System.out.println("分词结果为：" + Main.analyseText(analyseStr));
        } catch (Exception e) {
            System.out.println("分词结果有误");
        }
    }

    /**
     * 测试MD5算法hash计算hash，检查所得到hash值是否为128位
     */
    @org.junit.jupiter.api.Test
    void testWordHash() {
        HanLP.extractKeyword(originSentence, originSentence.length()).forEach(
                word -> {
                    String hash = null;
                    try {
                        hash = Main.wordHash(word);
                    } catch (HashException e) {
                        System.out.println(e.getMessage());
                    }
                    System.out.println(word + " : " + hash);
                }
        );
    }

    /**
     * 测试哈希异常（得到hash值为空）
     */
    @org.junit.jupiter.api.Test
    void testHashException() {
        try {
            Main.wordHash("");
        } catch (HashException e) {
            System.out.println(e.getMessage());
        }
        try {
            Main.wordHash(null);
        } catch (HashException e) {
            System.out.println(e.getMessage());
        }
        try {
            Main.wordHash("    ");
        } catch (HashException e) {
            System.out.println(e.getMessage());
        }
    }



    /**
     * 测试计算simHash
     */
    @org.junit.jupiter.api.Test
    void testCalculateSimHash() {
        try {
            String hash1 = Main.calculateSimHash(Main.analyseText(originSentence));
            System.out.println("原句子\"" + originSentence + "\"的simHash值为：" + hash1);
            Assertions.assertEquals(hash1.length(), 128, "hash值长度不是128");
            String hash2 = Main.calculateSimHash(Main.analyseText((Main.readFile(OrigFilePath))));
            System.out.println("原文本的simHash值为：" + hash2);
            Assertions.assertEquals(hash2.length(), 128, "hash值长度不是128");
        } catch (FileAnalyseException | NotExistFileException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试主函数
     */
    @org.junit.jupiter.api.Test
    void testMain() {
        String[] args = new String[3];
        args[0] = OrigFilePath;
        args[1] = CopyFilePath1;
        args[2] = writeFilePath;
        Main.main(args);

        args[1] = CopyFilePath2;
        Main.main(args);
        args[1] = CopyFilePath3;
        Main.main(args);
        args[1] = CopyFilePath4;
        Main.main(args);
        args[1] = CopyFilePath5;
        Main.main(args);
        args[0] = CopyFilePath3;
    }
}
