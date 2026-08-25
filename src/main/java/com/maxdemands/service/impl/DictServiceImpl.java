package com.maxdemands.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maxdemands.entity.Dict;
import com.maxdemands.mapper.DictMapper;
import com.maxdemands.service.DictService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * 24 色固定调色板（ECharts 默认配色衍生，saturated 中度色，el-tag dark 白字可读）
     */
    private static final String[] PALETTE = {
            "#5470c6", "#91cc75", "#fac858", "#ee6666", "#73c0de",
            "#3ba272", "#fc8452", "#9a60b4", "#ea7ccc", "#41b1e9",
            "#5b8ff9", "#5ad8a6", "#5d7092", "#f6bd16", "#e86452",
            "#6dc8ec", "#945fb9", "#ff9845", "#1e9493", "#ff99c3",
            "#3f4f5f", "#a1a0fc", "#2ec7c9", "#96dee8"
    };

    @Override
    public Map<String, List<Dict>> getAllDicts() {
        List<Dict> list = list(Wrappers.<Dict>lambdaQuery().orderByAsc(Dict::getSortOrder));
        return list.stream()
                .sorted(Comparator.comparingInt(d -> d.getSortOrder() != null ? d.getSortOrder() : 0))
                .collect(Collectors.groupingBy(Dict::getDictType, LinkedHashMap::new, Collectors.toList()));
    }

    @Override
    public String getDictName(String dictType, String dictCode) {
        Dict dict = getOne(Wrappers.<Dict>lambdaQuery()
                .eq(Dict::getDictType, dictType)
                .eq(Dict::getDictCode, dictCode));
        return dict != null ? dict.getDictName() : dictCode;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int randomColorsByType(String dictType) {
        List<Dict> items = list(Wrappers.<Dict>lambdaQuery()
                .eq(Dict::getDictType, dictType)
                .orderByAsc(Dict::getSortOrder, Dict::getId));
        if (items.isEmpty()) {
            return 0;
        }
        applyShuffledColors(items);
        updateBatchById(items);
        return items.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> randomColorsAll() {
        // 按 dictType 分组，每组独立洗牌（保证每个 dictType 内无重复，跨组不要求）
        Map<String, List<Dict>> grouped = list()
                .stream()
                .collect(Collectors.groupingBy(
                        Dict::getDictType,
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Dict>> entry : grouped.entrySet()) {
            String type = entry.getKey();
            List<Dict> items = entry.getValue();
            if (items.isEmpty()) {
                result.put(type, 0);
                continue;
            }
            applyShuffledColors(items);
            updateBatchById(items);
            result.put(type, items.size());
        }
        return result;
    }

    /**
     * 给同一 dictType 下的子项分配唯一颜色：
     * - 子项数 N ≤ 24：直接从 24 色调色板洗牌后取前 N 个，零冲突
     * - 子项数 N > 24：超出部分用 HSL 黄金角分布（hue = i × 137.508° mod 360）补充，保证扩展后仍唯一
     */
    private void applyShuffledColors(List<Dict> items) {
        int size = items.size();
        List<String> pool = new ArrayList<>(size);
        List<String> shuffledPalette = new ArrayList<>(List.of(PALETTE));
        Collections.shuffle(shuffledPalette);
        for (int i = 0; i < Math.min(size, PALETTE.length); i++) {
            pool.add(shuffledPalette.get(i));
        }
        if (size > PALETTE.length) {
            for (int i = PALETTE.length; i < size; i++) {
                double hue = (i * 137.508d) % 360d;
                pool.add(hslToHex(hue, 0.65d, 0.55d));
            }
        }
        Collections.shuffle(pool);
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setColor(pool.get(i));
        }
    }

    /**
     * HSL → #RRGGBB（H:0-360, S/L:0-1）
     */
    private static String hslToHex(double h, double s, double l) {
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double x = c * (1 - Math.abs((h / 60d) % 2 - 1));
        double m = l - c / 2d;
        double r1, g1, b1;
        if (h < 60) { r1 = c; g1 = x; b1 = 0; }
        else if (h < 120) { r1 = x; g1 = c; b1 = 0; }
        else if (h < 180) { r1 = 0; g1 = c; b1 = x; }
        else if (h < 240) { r1 = 0; g1 = x; b1 = c; }
        else if (h < 300) { r1 = x; g1 = 0; b1 = c; }
        else { r1 = c; g1 = 0; b1 = x; }
        int r = (int) Math.round((r1 + m) * 255);
        int g = (int) Math.round((g1 + m) * 255);
        int b = (int) Math.round((b1 + m) * 255);
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        return String.format("#%02x%02x%02x", r, g, b);
    }
}
