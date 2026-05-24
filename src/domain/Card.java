package domain;

import javax.swing.*;
import java.awt.*;

public class Card extends JButton {
    private int id; // 牌的序号1-54，越大牌越大
    private String name; // 牌面：♠️3、大王等
    private boolean selected; // 是否被选中

    public Card(int id, String name) {
        this.id = id;
        this.name = name;
        this.selected = false;
        setText(name);
        setFont(new Font("微软雅黑", Font.PLAIN, 16));
        setPreferredSize(new Dimension(70, 100));
        setBackground(Color.WHITE);
        setFocusPainted(false);

        // 点击选中/取消选中
        addActionListener(e -> {
            selected = !selected;
            setBackground(selected ? Color.LIGHT_GRAY : Color.WHITE);
        });
    }

    // 获取牌的大小（序号越大越大）
    public int getLevel() {
        return id;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getName() {
        return name;
    }
}
