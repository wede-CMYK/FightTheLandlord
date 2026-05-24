import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class PokerFrame extends JFrame {
    private PokerGame game;
    private JPanel playerPanel; // 陈思思（你）的手牌区域
    private JPanel playArea;    // 出牌区域
    private JLabel tipLabel;    // 提示文字
    private JLabel ai1Label;    // 丁兜兜 剩余牌数
    private JLabel ai2Label;    // 谭小小 剩余牌数

    public PokerFrame() {
        game = new PokerGame();
        setTitle("Java斗地主 - 陈思思 VS 丁兜兜 VS 谭小小");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 100, 0));

        // 顶部玩家信息
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        topPanel.setOpaque(false);
        ai1Label = new JLabel("丁兜兜：17张牌", SwingConstants.CENTER);
        ai2Label = new JLabel("谭小小：17张牌", SwingConstants.CENTER);
        JLabel playerLabel = new JLabel("陈思思（你）：17张牌", SwingConstants.CENTER);
        ai1Label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        ai2Label.setFont(new Font("微软雅黑", Font.BOLD, 16));
        playerLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        ai1Label.setForeground(Color.WHITE);
        ai2Label.setForeground(Color.WHITE);
        playerLabel.setForeground(Color.WHITE);
        topPanel.add(ai1Label);
        topPanel.add(new JLabel(""));
        topPanel.add(ai2Label);
        add(topPanel, BorderLayout.NORTH);

        // 中间提示+出牌区域
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        tipLabel = new JLabel("是否叫地主？", SwingConstants.CENTER);
        tipLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        tipLabel.setForeground(Color.WHITE);
        centerPanel.add(tipLabel, BorderLayout.NORTH);

        playArea = new JPanel();
        playArea.setOpaque(false);
        centerPanel.add(playArea, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 底部玩家手牌
        playerPanel = new JPanel();
        playerPanel.setOpaque(false);
        add(playerPanel, BorderLayout.SOUTH);

        // 右侧按钮区域
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        btnPanel.setOpaque(false);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JButton callBtn = new JButton("叫地主");
        JButton noCallBtn = new JButton("不叫");
        JButton playBtn = new JButton("出牌");
        JButton passBtn = new JButton("不出");

        callBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        noCallBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        playBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        passBtn.setFont(new Font("微软雅黑", Font.PLAIN, 16));

        btnPanel.add(callBtn);
        btnPanel.add(noCallBtn);
        btnPanel.add(playBtn);
        btnPanel.add(passBtn);
        playBtn.setVisible(false);
        passBtn.setVisible(false);
        add(btnPanel, BorderLayout.EAST);

        // 叫地主事件
        callBtn.addActionListener(e -> {
            game.callLord(0); // 0=陈思思（你）当地主
            startGame();
            callBtn.setVisible(false);
            noCallBtn.setVisible(false);
            playBtn.setVisible(true);
            passBtn.setVisible(true);
        });
        noCallBtn.addActionListener(e -> {
            // AI随机叫地主：1=丁兜兜，2=谭小小
            int lord = (int)(Math.random()*2)+1;
            game.callLord(lord);
            startGame();
            callBtn.setVisible(false);
            noCallBtn.setVisible(false);
            playBtn.setVisible(true);
            passBtn.setVisible(true);
        });

        // 出牌事件
        playBtn.addActionListener(e -> playerPlay());
        passBtn.addActionListener(e -> {
            tipLabel.setText("陈思思选择不出，轮到下一家");
            aiTurn();
        });

        setVisible(true);
    }

    // 开始游戏，显示手牌
    private void startGame() {
        playerPanel.removeAll();
        TreeSet<Integer> cards = game.getPlayerCards();
        for (int id : cards) {
            playerPanel.add(new Card(id, game.getCardMap().get(id)));
        }
        // 更新剩余牌数
        updateCardCount();
        // 显示地主
        String lordName = game.getLord() == 0 ? "陈思思（你）" : game.getLord() == 1 ? "丁兜兜" : "谭小小";
        tipLabel.setText("地主是：" + lordName + "，地主先出牌");

        // 如果地主是AI，先出牌
        if (game.getLord() == 1) {
            tipLabel.setText("丁兜兜（地主）出牌中...");
            Timer timer = new Timer(1000, e -> aiPlay(1));
            timer.setRepeats(false);
            timer.start();
        } else if (game.getLord() == 2) {
            tipLabel.setText("谭小小（地主）出牌中...");
            Timer timer = new Timer(1000, e -> aiPlay(2));
            timer.setRepeats(false);
            timer.start();
        }
        revalidate();
        repaint();
    }

    // 玩家（陈思思）出牌
    private void playerPlay() {
        ArrayList<Integer> selectedCards = new ArrayList<>();
        for (Component c : playerPanel.getComponents()) {
            Card card = (Card) c;
            if (card.isSelected()) {
                selectedCards.add(card.getLevel());
            }
        }
        if (selectedCards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "陈思思，请选择要出的牌！");
            return;
        }
        // 判断是否能出
        if (!game.getLastCards().isEmpty() && !game.isBigger(selectedCards, game.getLastCards())) {
            JOptionPane.showMessageDialog(this, "牌型不对或太小，不能出！");
            return;
        }
        // 出牌
        game.removeCards(game.getPlayerCards(), selectedCards);
        game.setLastCards(selectedCards);
        game.setLastPlayer(0);
        // 更新界面
        playerPanel.removeAll();
        for (int id : game.getPlayerCards()) {
            playerPanel.add(new Card(id, game.getCardMap().get(id)));
        }
        playArea.removeAll();
        JLabel playTip = new JLabel("陈思思出：", SwingConstants.CENTER);
        playTip.setForeground(Color.WHITE);
        playTip.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        playArea.add(playTip);
        for (int id : selectedCards) {
            playArea.add(new Card(id, game.getCardMap().get(id)));
        }
        updateCardCount();
        // 判断游戏结束
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, "🎉 陈思思赢了！恭喜！");
            System.exit(0);
        }
        // AI回合
        tipLabel.setText("轮到下一家出牌");
        revalidate();
        repaint();
        Timer timer = new Timer(1000, e -> aiTurn());
        timer.setRepeats(false);
        timer.start();
    }

    // AI回合
    private void aiTurn() {
        int nextPlayer = (game.getLastPlayer() + 1) % 3;
        String aiName = nextPlayer == 1 ? "丁兜兜" : "谭小小";
        tipLabel.setText(aiName + "思考中...");

        Timer timer = new Timer(1000, e -> aiPlay(nextPlayer));
        timer.setRepeats(false);
        timer.start();
    }

    // AI出牌
    private void aiPlay(int aiId) {
        TreeSet<Integer> aiCards = aiId == 1 ? game.getAi1Cards() : game.getAi2Cards();
        String aiName = aiId == 1 ? "丁兜兜" : "谭小小";

        ArrayList<Integer> aiPlay = game.aiPlay(aiCards);
        if (aiPlay.isEmpty()) {
            tipLabel.setText(aiName + "选择不出，轮到下一家");
            game.setLastPlayer(aiId);
            // 如果下一家是玩家，结束AI回合
            if ((aiId + 1) % 3 != 0) {
                Timer timer = new Timer(1000, e -> aiTurn());
                timer.setRepeats(false);
                timer.start();
            }
            return;
        }
        // 出牌
        game.removeCards(aiCards, aiPlay);
        game.setLastCards(aiPlay);
        game.setLastPlayer(aiId);
        // 更新界面
        playArea.removeAll();
        JLabel playTip = new JLabel(aiName + "出：", SwingConstants.CENTER);
        playTip.setForeground(Color.WHITE);
        playTip.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        playArea.add(playTip);
        for (int id : aiPlay) {
            playArea.add(new Card(id, game.getCardMap().get(id)));
        }
        updateCardCount();
        // 判断游戏结束
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, aiName + "赢了！游戏结束");
            System.exit(0);
        }
        tipLabel.setText(aiName + "出完牌，轮到下一家");
        revalidate();
        repaint();
        // 下一家出牌
        if ((aiId + 1) % 3 != 0) {
            Timer timer = new Timer(1000, e -> aiTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }

    // 更新剩余牌数
    private void updateCardCount() {
        ai1Label.setText("丁兜兜：" + game.getAi1Cards().size() + "张牌");
        ai2Label.setText("谭小小：" + game.getAi2Cards().size() + "张牌");
    }
}