import domain.Card;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class PokerFrame extends JFrame {
    private PokerGame game;
    private JPanel playerPanel;
    private JPanel playArea;
    private JLabel tipLabel;
    private JLabel ai1Label;
    private JLabel ai2Label;
    private JLabel scoreLabel; // 积分展示标签

    public PokerFrame() {
        game = new PokerGame();
        setTitle("Java斗地主 - 陈思思 VS 丁兜兜 VS 谭小小");
        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(0, 100, 0));

        // 顶部玩家信息+积分面板
        JPanel topPanel = new JPanel(new GridLayout(2, 3));
        topPanel.setOpaque(false);

        ai1Label = new JLabel("丁兜兜：17张牌 | 积分：0", SwingConstants.CENTER);
        ai2Label = new JLabel("谭小小：17张牌 | 积分：0", SwingConstants.CENTER);
        scoreLabel = new JLabel("陈思思（你）：17张牌 | 积分：0", SwingConstants.CENTER);

        ai1Label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        ai2Label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        scoreLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        ai1Label.setForeground(Color.WHITE);
        ai2Label.setForeground(Color.WHITE);
        scoreLabel.setForeground(Color.WHITE);

        topPanel.add(ai1Label);
        topPanel.add(new JLabel(""));
        topPanel.add(ai2Label);
        topPanel.add(new JLabel(""));
        topPanel.add(scoreLabel);
        topPanel.add(new JLabel(""));
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
            game.callLord(0);
            startGame();
            callBtn.setVisible(false);
            noCallBtn.setVisible(false);
            playBtn.setVisible(true);
            passBtn.setVisible(true);
        });

        noCallBtn.addActionListener(e -> {
            int lord = (int) (Math.random() * 2) + 1;
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
            game.setLastPlayer(0);
            aiTurn();
        });

        setVisible(true);
    }

    private void startGame() {
        playerPanel.removeAll();
        TreeSet<Integer> cards = game.getPlayerCards();
        for (int id : cards) {
            playerPanel.add(new Card(id, game.getCardMap().get(id)));
        }
        updateCardCount();
        updateScore();

        String lordName = game.getLord() == 0 ? "陈思思（你）" : game.getLord() == 1 ? "丁兜兜" : "谭小小";
        tipLabel.setText("地主是：" + lordName + "，地主先出牌");

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

    private void playerPlay() {
        ArrayList<Integer> selectedCards = new ArrayList<>();
        for (Component c : playerPanel.getComponents()) {
            Card card = (Card) c;
            if (card.isSelected()) {
                selectedCards.add(card.getLevel());
            }
        }

        // 空牌校验
        if (selectedCards.isEmpty()) {
            JOptionPane.showMessageDialog(this, "陈思思，请选择要出的牌！");
            return;
        }

        // 非法牌型校验
        int type = game.getCardType(selectedCards);
        if (type == PokerGame.TYPE_ERROR) {
            JOptionPane.showMessageDialog(this, "所选牌型不合法，请重新选择！\n支持：单张、对子、三张、三带一、三带二、顺子、连对、炸弹、王炸");
            return;
        }

        // 压牌校验
        if (!game.getLastCards().isEmpty() && !game.isBigger(selectedCards, game.getLastCards())) {
            JOptionPane.showMessageDialog(this, "牌型不对或牌力太小，无法压制！");
            return;
        }

        // 执行出牌
        game.removeCards(game.getPlayerCards(), selectedCards);
        game.setLastCards(selectedCards);
        game.setLastPlayer(0);

        // 刷新界面
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
        updateScore();

        // 游戏结束判定
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, game.getWinner());
            System.exit(0);
        }

        tipLabel.setText("轮到下一家出牌");
        revalidate();
        repaint();
        Timer timer = new Timer(1000, e -> aiTurn());
        timer.setRepeats(false);
        timer.start();
    }

    private void aiTurn() {
        int nextPlayer = (game.getLastPlayer() + 1) % 3;
        String aiName = nextPlayer == 1 ? "丁兜兜" : "谭小小";
        tipLabel.setText(aiName + "思考中...");
        Timer timer = new Timer(1000, e -> aiPlay(nextPlayer));
        timer.setRepeats(false);
        timer.start();
    }

    private void aiPlay(int aiId) {
        TreeSet<Integer> aiCards = aiId == 1 ? game.getAi1Cards() : game.getAi2Cards();
        String aiName = aiId == 1 ? "丁兜兜" : "谭小小";
        ArrayList<Integer> aiPlayCards = game.aiPlay(aiCards);

        // 不出牌逻辑
        if (aiPlayCards.isEmpty()) {
            tipLabel.setText(aiName + "选择不出，轮到下一家");
            game.setLastPlayer(aiId);
            if ((aiId + 1) % 3 != 0) {
                Timer timer = new Timer(1000, e -> aiTurn());
                timer.setRepeats(false);
                timer.start();
            } else {
                game.setLastCards(new ArrayList<>());
                tipLabel.setText("所有人都不出，清空出牌轮次，重新出牌");
            }
            return;
        }

        // AI出牌逻辑
        game.removeCards(aiCards, aiPlayCards);
        game.setLastCards(aiPlayCards);
        game.setLastPlayer(aiId);

        // 刷新界面
        playArea.removeAll();
        JLabel playTip = new JLabel(aiName + "出：", SwingConstants.CENTER);
        playTip.setForeground(Color.WHITE);
        playTip.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        playArea.add(playTip);
        for (int id : aiPlayCards) {
            playArea.add(new Card(id, game.getCardMap().get(id)));
        }

        updateCardCount();
        updateScore();

        // 游戏结束判定
        if (game.isGameOver()) {
            JOptionPane.showMessageDialog(this, game.getWinner());
            System.exit(0);
        }

        tipLabel.setText(aiName + "出完牌，轮到下一家");
        revalidate();
        repaint();

        if ((aiId + 1) % 3 != 0) {
            Timer timer = new Timer(1000, e -> aiTurn());
            timer.setRepeats(false);
            timer.start();
        } else {
            game.setLastCards(new ArrayList<>());
        }
    }

    // 更新剩余牌数
    private void updateCardCount() {
        ai1Label.setText("丁兜兜：" + game.getAi1Cards().size() + "张牌");
        ai2Label.setText("谭小小：" + game.getAi2Cards().size() + "张牌");
    }

    // 更新积分展示
    private void updateScore() {
        scoreLabel.setText("陈思思（你）：" + game.getPlayerCards().size() + "张牌 | 积分：" + PokerGame.playerScore);
        ai1Label.setText("丁兜兜：" + game.getAi1Cards().size() + "张牌 | 积分：" + PokerGame.ai1Score);
        ai2Label.setText("谭小小：" + game.getAi2Cards().size() + "张牌 | 积分：" + PokerGame.ai2Score);
    }
}
