import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

public class PokerGame {
    //准备牌
    //静态代码块：随着类的加载而加载，而且只执行一次
    static HashMap<Integer,String> hm=new HashMap<>();
    static ArrayList<Integer> list=new ArrayList<>();
    static{
        String[] color={"♠️","♥️","♣️","♦️"};
        String[] number={"3","4","5","6","7","8","9","10","J","Q","K","A","2"};
        int serialNumber=1;
        for(String num:number){
            for(String c:color){
                hm.put(serialNumber,c+num);
                list.add(serialNumber);
                serialNumber++;
            }
        }
        hm.put(serialNumber,"小王");
        list.add(serialNumber);
        serialNumber++;
        hm.put(serialNumber,"大王");
        list.add(serialNumber);
    }


    public PokerGame(){
        /*
            这是无参构造，在主程序运行时，除了运行静态区域的代码，就开始进行无参构造的代码
        */
        //第一步，洗牌
        Collections.shuffle(list);

        //发牌,还要创建四个集合，分别是三个玩家，还有底牌，然后还要进行排序
        TreeSet<Integer> lord=new TreeSet<>();
        TreeSet<Integer> player1=new TreeSet<>();
        TreeSet<Integer> player2=new TreeSet<>();
        TreeSet<Integer> player3=new TreeSet<>();

        for (int i = 0; i < list.size(); i++) {
            Integer temp = list.get(i);

            if(i<=2){
                lord.add(temp);
                continue;
            }else if(i%3==0){
                player1.add(temp);
            }else if(i%3==1){
                player2.add(temp);
            }else player3.add(temp);
        }
        lookPoker("底牌",lord);
        lookPoker("陈思思",player1);
        lookPoker("丁兜兜",player2);
        lookPoker("谭小小",player3);
    }


    public void lookPoker(String name,TreeSet<Integer> ts){
        //遍历
        System.out.print(name+":");
        for (Integer temp : ts) {
            String poker = hm.get(temp);
            System.out.print(poker+" ");
        }
        System.out.println();
    }

}
