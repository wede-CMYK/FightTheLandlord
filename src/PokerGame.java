import java.util.*;

public class PokerGame {
    private HashMap<Integer, String> cardMap = new HashMap<>();
    private ArrayList<Integer> allCards = new ArrayList<>();
    private TreeSet<Integer> playerCards = new TreeSet<>(); // 玩家手牌
    private TreeSet<Integer> ai1Cards = new TreeSet<>();    // AI1手牌
    private TreeSet<Integer> ai2Cards = new TreeSet<>();    // AI2手牌
    private TreeSet<Integer> lordCards = new TreeSet<>();   // 底牌
    private int lord; // 地主：0=玩家，1=AI1，2=AI2
    private ArrayList<Integer> lastCards = new ArrayList<>(); // 上一家出的牌
    private int lastPlayer; // 上一家出牌的人

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

    // 叫地主（简单逻辑：玩家先叫，叫了就是地主）
    public void callLord(boolean isCall) {
        if (isCall) {
            lord = 0;
            playerCards.addAll(lordCards);
        } else {
            // AI随机叫地主
            Random r = new Random();
            lord = r.nextInt(2) + 1;
            if (lord == 1) ai1Cards.addAll(lordCards);
            else ai2Cards.addAll(lordCards);
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
        return 0; // 其他牌型暂未实现
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

    // AI出牌（简单逻辑：出最小的能压的牌，不能就不出）
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

    // getter方法
    public TreeSet<Integer> getPlayerCards() { return playerCards; }
    public TreeSet<Integer> getAi1Cards() { return ai1Cards; }
    public TreeSet<Integer> getAi2Cards() { return ai2Cards; }
    public HashMap<Integer, String> getCardMap() { return cardMap; }
    public int getLord() { return lord; }
    public void setLastCards(ArrayList<Integer> cards) { lastCards = cards; }
    public void removeCards(TreeSet<Integer> set, ArrayList<Integer> cards) {
        set.removeAll(cards);
    }
    public boolean isGameOver() {
        return playerCards.isEmpty() || ai1Cards.isEmpty() || ai2Cards.isEmpty();
    }
    public String getWinner() {
        if (playerCards.isEmpty()) return "你赢了！";
        if (ai1Cards.isEmpty() || ai2Cards.isEmpty()) return "电脑赢了！";
        return "";
    }
}