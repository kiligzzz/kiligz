package trie;

import com.hankcs.hanlp.collection.trie.DoubleArrayTrie;

import java.util.TreeMap;

/**
 * 简单的双数组单词查找树
 *
 * @author Ivan
 */
public class DAT {
    public static void main(String[] args) {
        // 持久化文件
        DoubleArrayTrie<String> dat = new DoubleArrayTrie<>();
        dat.build(new TreeMap<>());
        dat.serializeTo("${path}");

        // 加载文件
        DoubleArrayTrie<String> dat1 = DoubleArrayTrie.unSerialize("${path}");

        // 查询
        dat1.parseText("${query}", (begin, end, value) -> System.out.println(begin + end + value));
    }
}
