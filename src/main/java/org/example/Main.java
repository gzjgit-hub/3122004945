package org.example;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import org.example.exceptions.FileAnalyseException;
import org.example.exceptions.HashException;
import org.example.exceptions.NotExistFileException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class Main {
    private static final int ARGS_COUNT = 3; // 命令行参数的数量
    private static final int HASH_BIT = 128; // SimHash算法中使用的哈希位数
    public static final int SHORT_WORD_LENGTH = 3; // 定义短词的长度

    /**
     * 程序的主入口。
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        if (args.length != ARGS_COUNT) {
            throw new IllegalArgumentException("参数个数不正确");
        }
        String originStr = null;
        String compareStr = null;
        try {
            originStr = readFile(args[0]); // 读取第一个文件
            compareStr = readFile(args[1]); // 读取第二个文件
        } catch (IORuntimeException | NotExistFileException e) {
            e.printStackTrace();
        }
        Map<String, Integer> originMap = null;
        Map<String, Integer> compareMap = null;
        try {
            originMap = analyseText(originStr); // 分析第一个文件的文本
            compareMap = analyseText(compareStr); // 分析第二个文件的文本
        } catch (Exception e) {
            e.printStackTrace();
        }
        String originSimHash = calculateSimHash(originMap); // 计算第一个文件的SimHash值
        String compareSimHash = calculateSimHash(compareMap); // 计算第二个文件的SimHash值

        int hamingDistance = 0;
        int same = 0;
        for (int i = 0; i < originSimHash.length(); i++) {
            if (originSimHash.charAt(i) != compareSimHash.charAt(i)) {
                hamingDistance++; // 计算汉明距离
            }
            if (originSimHash.charAt(i) == '1' && compareSimHash.charAt(i) == '1') {
                same++; // 计算相同位数
            }
        }
        double result = (double) same / (hamingDistance + same); // 计算相似度
        String format = String.format("两者相似度为：%.2f", result);
        System.out.println(format);
        String writeFileContent = "---------------------------------------" + "\n" +
                "原文件：" + args[0] + "\n" +
                "对比文件：" + args[1] + "\n" +
                format + "\n" +
                "比较时间为：" + DateUtil.now() + "\n";
        try {
            writeFile(writeFileContent, args[2]); // 将结果写入第三个文件
        } catch (NotExistFileException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将结果写入文件。
     * @param content 要写入的内容
     * @param filePath 文件路径
     * @throws NotExistFileException 如果文件不存在则抛出异常
     */
    public static void writeFile(String content, String filePath) throws NotExistFileException {
        try {
            FileUtil.appendString(content, filePath, "utf-8");
        } catch (IORuntimeException e) {
            throw new NotExistFileException("该路径的文件不存在");
        }
    }

    /**
     * 从文件路径读取文件内容。
     * @param filePath 文件路径
     * @return 文件内容
     * @throws NotExistFileException 如果文件不存在则抛出异常
     */
    public static String readFile(String filePath) throws NotExistFileException {
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            throw new NotExistFileException("该路径的文件不存在");
        }
    }

    /**
     * 分析文本，提取关键词及其频率。
     * @param text 文本内容
     * @return 关键词及其频率的映射
     * @throws FileAnalyseException 如果文本分析失败则抛出异常
     */
    public static Map<String, Integer> analyseText(String text) throws FileAnalyseException {
        if (text == null || StrUtil.isBlank(text) || StrUtil.isEmpty(text)) {
            throw new FileAnalyseException("文件解析异常，解析内容为空");
        }
        List<String> keyList = HanLP.extractKeyword(text, text.length()); // 提取关键词
        if (keyList.size() <= SHORT_WORD_LENGTH) {
            throw new FileAnalyseException("文件解析异常，关键词太少");
        }
        List<Term> termList = HanLP.segment(text); // 分词
        List<String> allWords = termList.stream().map(term -> term.word).collect(Collectors.toList());
        Map<String, Integer> wordCountMap = new HashMap<>(keyList.size());
        for (String s : keyList) {
            wordCountMap.put(s, Collections.frequency(allWords, s)); // 计算词频
        }
        return wordCountMap;
    }

    /**
     * 计算文本的SimHash值。
     * @param wordCountMap 关键词及其频率的映射
     * @return SimHash值
     */
    public static String calculateSimHash(Map<String, Integer> wordCountMap) {
        int[] mergeHash = new int[HASH_BIT];
        for (int i = 0; i < HASH_BIT; i++) {
            mergeHash[i] = 0;
        }
        wordCountMap.forEach((word, count) -> {
            String hash = null;
            try {
                hash = wordHash(word);
            } catch (HashException e) {
                throw new RuntimeException(e);
            }
            int[] hashArray = new int[HASH_BIT];
            for (int i = 0; i < hash.length(); i++) {
                hashArray[i] = hash.charAt(i) == '1' ? count : -1 * count;
            }
            for (int i = 0; i < hashArray.length; i++) {
                mergeHash[i] += hashArray[i];
            }
        });
        StringBuilder simHash = new StringBuilder();
        for (int hash : mergeHash) {
            simHash.append(hash > 0 ? "1" : "0");
        }
        return simHash.toString();
    }

    /**
     * 对单个词进行哈希处理，生成固定长度的二进制字符串。
     * @param word 要哈希的词
     * @return 二进制形式的哈希值
     * @throws HashException 如果哈希生成失败则抛出异常
     */
    public static String wordHash(String word) throws HashException {
        if (word == null || StrUtil.isBlank(word) || StrUtil.isEmpty(word)) {
            throw new HashException("词语为空");
        }
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        digest.update(word.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : digest.digest()) {
            hash.append(String.format("%02x", b));
        }
        StringBuilder finalHash = new StringBuilder();
        String strTemp;
        for (int i = 0; i < hash.length(); i++) {
            strTemp = "0000" + Integer.toBinaryString(Integer.parseInt(hash.substring(i, i + 1), 16));
            finalHash.append(strTemp.substring(strTemp.length() - 4));
        }
        if (finalHash.length() != HASH_BIT) {
            throw new HashException("hash值长度不为128");
        }
        return finalHash.toString();
    }

}