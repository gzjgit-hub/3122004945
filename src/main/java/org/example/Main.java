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

import java.io.FileNotFoundException;
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
    private static final int ARGS_COUNT = 3;
    private static final int HASH_BIT = 128;
    public static final int SHORT_WORD_LENGTH = 3;

    public static void main(String[] args) {
        if (args.length != ARGS_COUNT) {
            throw new IllegalArgumentException("参数个数不正确");
        }
        String originStr = null;
        String compareStr = null;
        try {
            originStr = readFile(args[0]);
            compareStr = readFile(args[1]);
        } catch (IORuntimeException | NotExistFileException e) {
            e.printStackTrace();
        }
        Map<String, Integer> originMap = null;
        Map<String, Integer> compareMap = null;
        try {
            originMap = analyseText(originStr);
            compareMap = analyseText(compareStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String originSimHash = calculateSimHash(originMap);
        String compareSimHash = calculateSimHash(compareMap);

        int hamingDistance = 0;
        int same = 0;
        for (int i = 0; i < originSimHash.length(); i++) {
            if (originSimHash.charAt(i) != compareSimHash.charAt(i)) {
                hamingDistance++;
            }
            if (originSimHash.charAt(i) == '1' && compareSimHash.charAt(i) == '1') {
                same++;
            }
        }
        double result = (double) same / (hamingDistance + same);
        String format = String.format("两者相似度为：%.2f", result);
        System.out.println(format);
        String writeFileContent = "---------------------------------------" + "\n" +
                "原文件：" + args[0] + "\n" +
                "对比文件：" + args[1] + "\n" +
                format + "\n" +
                "比较时间为：" + DateUtil.now() + "\n";
        try {
            writeFile(writeFileContent, args[2]);
        } catch (NotExistFileException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String content, String filePath) throws NotExistFileException {
        try {
            FileUtil.appendString(content, filePath, "utf-8");
        } catch (IORuntimeException e) {
            throw new NotExistFileException("该路径的文件不存在");
        }
    }

    public static String readFile(String filePath) throws NotExistFileException {
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            throw new NotExistFileException("该路径的文件不存在");
        }
    }

    public static Map<String, Integer> analyseText(String text) throws FileAnalyseException {
        if (text == null || StrUtil.isBlank(text) || StrUtil.isEmpty(text)) {
            throw new FileAnalyseException("文件解析异常，解析内容为空");
        }
        List<String> keyList = HanLP.extractKeyword(text, text.length());
        if (keyList.size() <= SHORT_WORD_LENGTH) {
            throw new FileAnalyseException("文件解析异常，关键词太少");
        }
        List<Term> termList = HanLP.segment(text);
        List<String> allWords = termList.stream().map(term -> term.word).collect(Collectors.toList());
        Map<String, Integer> wordCountMap = new HashMap<>(keyList.size());
        for (String s : keyList) {
            wordCountMap.put(s, Collections.frequency(allWords, s));
        }
        return wordCountMap;
    }

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