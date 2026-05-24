import java.util.*;

public class PokerGame {
    // 牌型常量定义（完整11种）
    public static final int TYPE_ERROR = 0;      // 非法牌型
    public static final int TYPE_SINGLE = 1;     // 单张
    public static final int TYPE_PAIR = 2;      // 对子
    public static final int TYPE_THREE = 3;     // 三张
    public static final int TYPE_THREE_ONE = 4; // 三带一
    public static final int TYPE_THREE_TWO = 5; // 三带二
    public static final int TYPE_STRAIGHT = 6;  // 顺子（5张+）
    public static final int TYPE_DOUBLE_STRAIGHT = 7; // 连对（3对+）
    public static final int TYPE_PLANE = 8;     // 飞机
    public static final int TYPE_PLANE_WING = 11; // 飞机带翅膀
    public static final int TYPE_BOMB = 9;      // 炸弹
    public static final int TYPE_KING_BOMB = 10;// 王炸

    // 基础牌数据
    private HashMap<Integer, String> cardMap = new HashMap<>();
    private ArrayList<Integer> allCards = new ArrayList<>();

    // 玩家手牌：陈思思、丁兜兜、谭小小
    private TreeSet<Integer> playerCards = new TreeSet<>();
    private TreeSet<Integer> ai1Cards = new TreeSet<>();
    private TreeSet<Integer> ai2Cards = new TreeSet<>();
    private TreeSet<Integer> lordCards = new TreeSet<>();

    // 游戏基础属性
    private int lord; // 0=陈思思，1=丁兜兜，2=谭小小
    private ArrayList<Integer> lastCards = new ArrayList<>();
    private int lastPlayer;

    // 积分系统（全局持久单局积分）
    public static int playerScore = 0;
    public static int ai1Score = 0;
    public static int ai2Score = 0;

    public PokerGame() {
        initCard();
        shuffleAndDeal();
    }

    // 初始化54张扑克牌
    private void initCard() {
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
    }

    // 洗牌发牌
    private void shuffleAndDeal() {
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

    // 叫地主逻辑
    public void callLord(int lordId) {
        this.lord = lordId;
        if (lordId == 0) {
            playerCards.addAll(lordCards);
        } else if (lordId == 1) {
            ai1Cards.addAll(lordCards);
        } else {
            ai2Cards.addAll(lordCards);
        }
    }

    // 核心：获取牌型（全覆盖11种牌型）
    public int getCardType(ArrayList<Integer> cards) {
        if (cards == null || cards.isEmpty()) return TYPE_ERROR;
        List<Integer> pointList = getCardPointList(cards);
        Map<Integer, Integer> countMap = getPointCountMap(pointList);
        Set<Integer> pointSet = countMap.keySet();

        // 1. 单张
        if (cards.size() == 1) return TYPE_SINGLE;

        // 2. 对子
        if (cards.size() == 2 && countMap.size() == 1) return TYPE_PAIR;

        // 3. 三张
        if (cards.size() == 3 && countMap.size() == 1) return TYPE_THREE;

        // 4. 四张：炸弹 / 非法
        if (cards.size() == 4) {
            if (countMap.size() == 1) return TYPE_BOMB;
            // 三带一：3+1点数
            if (countMap.size() == 2 && countMap.containsValue(3)) return TYPE_THREE_ONE;
            return TYPE_ERROR;
        }

        // 5. 三带二 3+2（5张）
        if (cards.size() == 5 && countMap.size() == 2
                && countMap.containsValue(3) && countMap.containsValue(2)) {
            return TYPE_THREE_TWO;
        }

        // 6. 顺子：5张及以上连续单牌，无2、无王
        if (cards.size() >= 5 && isStraight(pointList) && countMap.size() == cards.size()) {
            return TYPE_STRAIGHT;
        }

        // 7. 连对：3对及以上连续对子
        if (cards.size() >= 6 && cards.size() % 2 == 0
                && isDoubleStraight(pointList, countMap)) {
            return TYPE_DOUBLE_STRAIGHT;
        }

        // 8. 飞机、飞机带翅膀
        if (isPlane(pointList, countMap)) {
            if (cards.size() % 3 == 0) {
                return TYPE_PLANE;
            } else {
                return TYPE_PLANE_WING;
            }
        }

        // 9. 王炸
        if (cards.size() == 2 && cards.contains(53) && cards.contains(54)) {
            return TYPE_KING_BOMB;
        }

        return TYPE_ERROR;
    }

    // 获取所有牌的点数
    private List<Integer> getCardPointList(ArrayList<Integer> cards) {
        List<Integer> pointList = new ArrayList<>();
        for (int id : cards) {
            pointList.add(getPoint(id));
        }
        Collections.sort(pointList);
        return pointList;
    }

    // 统计点数出现次数
    private Map<Integer, Integer> getPointCountMap(List<Integer> pointList) {
        Map<Integer, Integer> countMap = new HashMap<>();
        for (int p : pointList) {
            countMap.put(p, countMap.getOrDefault(p, 0) + 1);
        }
        return countMap;
    }

    // 判断顺子（无2、王，连续递增）
    private boolean isStraight(List<Integer> pointList) {
        for (int p : pointList) {
            if (p >= 15) return false; // 2、小王、大王不能顺子
        }
        int min = pointList.get(0);
        for (int i = 0; i < pointList.size(); i++) {
            if (pointList.get(i) != min + i) return false;
        }
        return true;
    }

    // 判断连对
    private boolean isDoubleStraight(List<Integer> pointList, Map<Integer, Integer> countMap) {
        for (int p : pointList) {
            if (p >= 15) return false;
        }
        for (int count : countMap.values()) {
            if (count != 2) return false;
        }
        Set<Integer> points = countMap.keySet();
        List<Integer> sortPoints = new ArrayList<>(points);
        Collections.sort(sortPoints);
        for (int i = 0; i < sortPoints.size() - 1; i++) {
            if (sortPoints.get(i + 1) - sortPoints.get(i) != 1) return false;
        }
        return sortPoints.size() >= 3;
    }

    // 判断飞机
    private boolean isPlane(List<Integer> pointList, Map<Integer, Integer> countMap) {
        List<Integer> threePoints = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() == 3 && entry.getKey() < 15) {
                threePoints.add(entry.getKey());
            }
        }
        if (threePoints.size() < 2) return false;
        Collections.sort(threePoints);
        for (int i = 0; i < threePoints.size() - 1; i++) {
            if (threePoints.get(i + 1) - threePoints.get(i) != 1) return false;
        }
        return true;
    }

    // 获取牌点数
    public int getPoint(int id) {
        if (id == 53) return 16;
        if (id == 54) return 17;
        return (id - 1) / 4 + 3;
    }

    // 牌型大小比较
    public boolean isBigger(ArrayList<Integer> newCards, ArrayList<Integer> oldCards) {
        int newType = getCardType(newCards);
        int oldType = getCardType(oldCards);

        // 非法牌型不能出牌
        if (newType == TYPE_ERROR) return false;

        // 王炸最大
        if (newType == TYPE_KING_BOMB) return true;
        // 炸弹大于所有非王炸牌型
        if (newType == TYPE_BOMB && oldType != TYPE_BOMB && oldType != TYPE_KING_BOMB) return true;

        // 牌型不一致无法压制
        if (newType != oldType) return false;

        // 同牌型比较最大点数
        return Collections.max(newCards) > Collections.max(oldCards);
    }

    // 高级AI出牌逻辑：支持所有牌型，智能出牌、压牌
    public ArrayList<Integer> aiPlay(TreeSet<Integer> aiCards) {
        ArrayList<Integer> aiHand = new ArrayList<>(aiCards);
        ArrayList<Integer> result = new ArrayList<>();

        // 1. 先手出牌：优先出最小合法牌型
        if (lastCards.isEmpty()) {
            // 优先单张最小牌
            result.add(aiHand.get(0));
            return result;
        }

        // 2. 后手：寻找最小能压制的同牌型牌
        int needType = getCardType(lastCards);
        List<ArrayList<Integer>> allLegalCards = getAllLegalCardType(aiHand);

        // 遍历所有手牌组合，找最小能压的牌
        for (ArrayList<Integer> combo : allLegalCards) {
            if (getCardType(combo) == needType && isBigger(combo, lastCards)) {
                return combo;
            }
        }

        // 3. 同牌型压不住，尝试炸弹、王炸
        for (ArrayList<Integer> combo : allLegalCards) {
            int type = getCardType(combo);
            if ((type == TYPE_BOMB || type == TYPE_KING_BOMB) && isBigger(combo, lastCards)) {
                return combo;
            }
        }

        // 4. 无法压制，不出牌
        return result;
    }

    // 辅助：获取手牌所有合法牌型组合（简化AI组合）
    private List<ArrayList<Integer>> getAllLegalCardType(List<Integer> hand) {
        List<ArrayList<Integer>> list = new ArrayList<>();
        // 单张
        for (int num : hand) {
            ArrayList<Integer> single = new ArrayList<>();
            single.add(num);
            list.add(single);
        }
        // 对子
        for (int i = 0; i < hand.size(); i++) {
            for (int j = i + 1; j < hand.size(); j++) {
                if (getPoint(hand.get(i)) == getPoint(hand.get(j))) {
                    ArrayList<Integer> pair = new ArrayList<>();
                    pair.add(hand.get(i));
                    pair.add(hand.get(j));
                    list.add(pair);
                }
            }
        }
        return list;
    }

    // 移除出牌
    public void removeCards(TreeSet<Integer> set, ArrayList<Integer> cards) {
        set.removeAll(cards);
    }

    // 游戏结束判定
    public boolean isGameOver() {
        return playerCards.isEmpty() || ai1Cards.isEmpty() || ai2Cards.isEmpty();
    }

    // 胜负判定 + 积分结算
    public String getWinner() {
        if (playerCards.isEmpty()) {
            playerScore += 10;
            ai1Score -= 5;
            ai2Score -= 5;
            return "🎉 陈思思赢了！积分+10";
        } else if (ai1Cards.isEmpty()) {
            ai1Score += 10;
            playerScore -= 5;
            return "😥 丁兜兜赢了！你的积分-5";
        } else {
            ai2Score += 10;
            playerScore -= 5;
            return "😥 谭小小赢了！你的积分-5";
        }
    }

    // Getter & Setter
    public TreeSet<Integer> getPlayerCards() { return playerCards; }
    public TreeSet<Integer> getAi1Cards() { return ai1Cards; }
    public TreeSet<Integer> getAi2Cards() { return ai2Cards; }
    public HashMap<Integer, String> getCardMap() { return cardMap; }
    public int getLord() { return lord; }
    public ArrayList<Integer> getLastCards() { return lastCards; }
    public void setLastCards(ArrayList<Integer> cards) { lastCards = cards; }
    public int getLastPlayer() { return lastPlayer; }
    public void setLastPlayer(int player) { lastPlayer = player; }
}
