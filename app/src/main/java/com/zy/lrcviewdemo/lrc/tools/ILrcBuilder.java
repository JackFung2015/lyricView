package com.zy.lrcviewdemo.lrc.tools;

import com.zy.lrcviewdemo.lrc.entity.LrcRow;
import java.util.List;

/**
 * @Author: fzy
 * @Date: 2022/11/3
 * @Description:解析歌词，得到LrcRow的集合
 */
public interface ILrcBuilder {
    List<LrcRow> getLrcRows(String rawLrc);
}
