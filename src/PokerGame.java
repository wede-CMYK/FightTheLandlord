import java.util.*;

public class PokerGame {
    private HashMap<Integer, String> cardMap = new HashMap<>();
    private ArrayList<Integer> allCards = new ArrayList<>();
    private TreeSet<Integer> playerCards = new TreeSet<>(); // 陈思思（你）的手牌
    private TreeSet<Integer> ai1Cards = new TreeSet<>();    // 丁兜兜的手牌
    private TreeSet<Integer> ai2Cards = new TreeSet<>();    // 谭小小的手牌
    private TreeSet<Integer> lordCards = new TreeSet<>();   // 底牌
    private int lord; // 地主：0=陈思思，1=丁兜兜，2=谭小小
    private ArrayList<Integer> lastCards = new ArrayList<>(); // 上一家出的牌
    private int lastPlayer; // 上一家出牌的人：0=陈思思，1=丁兜兜，2=谭小小

    // 初始化牌
    public PokerGame() {
        String[] color = {"♠️", "♥️", "♣️", "♦️"};
        String[] number = {"3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2"};
        int serialNumber = 1;
        for (String num : number) {
            for (String c : color) {
                cardMap.put(serialNumber, c + num);
                allCards.add(serialNumber);
                serialNumber++;
            }
        }
        cardMap.put(serialNumber, "小王");
        allCards.add(serialNumber);
        serialNumber++;
        cardMap.put(serialNumber, "大王");
        allCards.add(serialNumber);

        // 洗牌发牌
        Collections.shuffle(allCards);
        for (int i = 0; i < allCards.size(); i++) {
            Integer temp = allCards.get(i);
            if (i <= 2) {
                lordCards.add(temp);
            } else if (i % 3 == 0) {
                playerCards.add(temp);
            } else if (i % 3 == 1) {
                ai1Cards.add(temp);
            } else {
                ai2Cards.add(temp);
            }
        }
    }

    // 叫地主
    public void callLord(int lordId) {
        this.lord = lordId;
        if (lordId == 0) {
            playerCards.addAll(lordCards); // 陈思思当地主，拿底牌
        } else if (lordId == 1) {
            ai1Cards.addAll(lordCards); // 丁兜兜当地主，拿底牌
        } else {
            ai2Cards.addAll(lordCards); // 谭小小当地主，拿底牌
        }
    }

    // 判断牌型
    public int getCardType(ArrayList<Integer> cards) {
        if (cards.size() == 1) return 1; // 单张
        if (cards.size() == 2) {
            if (cards.contains(53) && cards.contains(54)) return 10; // 王炸
            if (getPoint(cards.get(0)) == getPoint(cards.get(1))) return 2; // 对子
        }
        if (cards.size() == 3 && getPoint(cards.get(0)) == getPoint(cards.get(1))
                && getPoint(cards.get(1)) == getPoint(cards.get(2))) return 3; // 三张
        if (cards.size() == 4) {
            if (getPoint(cards.get(0)) == getPoint(cards.get(1))
                    && getPoint(cards.get(1)) == getPoint(cards.get(2))
                    && getPoint(cards.get(2)) == getPoint(cards.get(3))) return 9; // 炸弹
        }
        return 0; // 其他牌型
    }

    // 获取牌的点数
    private int getPoint(int id) {
        if (id == 53) return 16;
        if (id == 54) return 17;
        return (id - 1) / 4 + 3;
    }

    // 判断出牌是否比上一家大
    public boolean isBigger(ArrayList<Integer> newCards, ArrayList<Integer> oldCards) {
        int type1 = getCardType(newCards);
        int type2 = getCardType(oldCards);
        if (type1 == 10) return true; // 王炸最大
        if (type1 == 9 && type2 != 9 && type2 != 10) return true; // 炸弹比非炸弹大
        if (type1 != type2) return false; // 牌型不同不能压
        return Collections.max(newCards) > Collections.max(oldCards);
    }

    // AI出牌逻辑
    public ArrayList<Integer> aiPlay(TreeSet<Integer> aiCards) {
        ArrayList<Integer> result = new ArrayList<>();
        if (lastCards.isEmpty()) {
            // 先手出最小单张
            result.add(aiCards.first());
            return result;
        }
        // 找能压的牌
        for (int card : aiCards) {
            ArrayList<Integer> temp = new ArrayList<>();
            temp.add(card);
            if (isBigger(temp, lastCards)) {
                result.add(card);
                return result;
            }
        }
        return result; // 空表示不出
    }

    // getter/setter
    public TreeSet<Integer> getPlayerCards() { return playerCards; }
    public TreeSet<Integer> getAi1Cards() { return ai1Cards; }
    public TreeSet<Integer> getAi2Cards() { return ai2Cards; }
    public HashMap<Integer, String> getCardMap() { return cardMap; }
    public int getLord() { return lord; }
    public ArrayList<Integer> getLastCards() { return lastCards; }
    public void setLastCards(ArrayList<Integer> cards) { lastCards = cards; }
    public int getLastPlayer() { return lastPlayer; }
    public void setLastPlayer(int player) { lastPlayer = player; }
    public void removeCards(TreeSet<Integer> set, ArrayList<Integer> cards) {
        set.removeAll(cards);
    }
    public boolean isGameOver() {
        return playerCards.isEmpty() || ai1Cards.isEmpty() || ai2Cards.isEmpty();
    }
    public String getWinner() {
        if (playerCards.isEmpty()) return "陈思思赢了！";
        if (ai1Cards.isEmpty()) return "丁兜兜赢了！";
        if (ai2Cards.isEmpty()) return "谭小小赢了！";
        return "";
    }
}