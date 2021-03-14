/*
基本方針．
初期状態は1,1の正方形を各要求場所に配置する．
近傍は1行追加/消去，1列追加/消去もしくは上下左右の1マスシフト
これでビームサーチをする？1列消してから足したほうが良いことが多いので，多分ビームサーチしたほうが良い．
*/
import java.lang.Math;
import java.util.*;

class Advertisement implements Cloneable{
    public double[] want = new double[2];
    public int r,s;
    public int[] pos = new int[4]; // a,b,c,d
    public double satisf,diff;
    Advertisement(int x,int y,int r){
        this.want[0] = (double)x+0.5;
        this.want[1] = (double)y+0.5;
        this.r = r;
        this.pos[0] = x;
        this.pos[2] = x+1;
        this.pos[1] = y;
        this.pos[3] = y+1;
        this.s = 1;
        this.satisf = this.get_satisf();
    }

    public int[][] get_corners(){
        int [][] corners = new int[4][2];
        corners[0][0] = pos[0];
        corners[0][1] = pos[1];
        corners[1][0] = pos[2];
        corners[1][1] = pos[3];
        corners[2][0] = pos[0];
        corners[2][1] = pos[3];
        corners[3][0] = pos[2];
        corners[3][1] = pos[1];
        return corners;
    }

    public int calc_s(int[] p){
        return (p[2]-p[0])*(p[3]-p[1]);
    }

    public void calc_s(){
        this.s = this.calc_s(pos);
    }

    public static boolean is_point_in(int[] p,int[] corner){
        if (corner[0] < p[0] && p[0] < corner[2] && corner[1] < p[1] && p[1] < corner[3]){
            return true;
        }else {
            return false;
        }
    }

    public static boolean is_point_in(double[] p,int[] corner){
        if (corner[0] < p[0] && p[0] < corner[2] && corner[1] < p[1] && p[1] < corner[3]){
            return true;
        }else {
            return false;
        }
    }

    public boolean is_want_in(int[] p){
        return is_point_in(want, p);
    }
    
    public boolean is_want_in(){
        return is_point_in(want, pos);
    }

    public double diff_satisf(NextState ns){
        int[] p = new int[4];
        p[0] = pos[0];
        p[1] = pos[1];
        p[2] = pos[2];
        p[3] = pos[3];
        
        if (ns.vert_ind < 4){
            p[ns.vert_ind] += ns.shift;
            }else if(ns.vert_ind == 4){
                p[0] += ns.shift;
                p[2] += ns.shift;
            }else if (ns.vert_ind == 5){
                p[1] += ns.shift;
                p[3] += ns.shift;
            }
        double n_statis = this.get_satisf(p);
        diff = n_statis-this.satisf;
        return diff;

    }

    public double get_satisf(int[] p){
        if (this.is_want_in(p)){
            double ts = this.calc_s(p);
            double tp = 1.0-(double)Math.min(r,ts)/(double)Math.max(r,ts);
            return 1.0-tp*tp;
        }else{
            return 0.0;
        }
    }

    public double get_satisf(){
        return get_satisf(pos);
    }

    public double apply_ns(NextState ns){
        diff_satisf(ns);
        if (ns.vert_ind < 4){
            pos[ns.vert_ind] += ns.shift;
            }else if(ns.vert_ind == 4){
                pos[0] += ns.shift;
                pos[2] += ns.shift;
            }else if (ns.vert_ind == 5){
                pos[1] += ns.shift;
                pos[3] += ns.shift;
            }
        satisf = get_satisf();
        return diff;
    }

    @Override
    public Advertisement clone(){
        Advertisement clone = null;
        try{
            clone = (Advertisement) super.clone();
            clone.pos = this.pos.clone();
            return clone;
        }catch(Exception e){
            e.printStackTrace();
        }
        return clone;
    }
}

class NextState implements Cloneable{
    public int ad_ind;
    public int vert_ind;
    public int shift;

    NextState(int ad_ind,int vert_ind,int shift){
        this.ad_ind = ad_ind;
        this.vert_ind = vert_ind;
        this.shift = shift;
    }
    @Override
    public NextState clone(){
        NextState clone = null;
        try{
            clone = (NextState) super.clone();
            return clone;
        }catch (Exception e){
            e.printStackTrace();
        }
        return clone;
    }
}

class ScoreToken{
    public int index;
    public double score;
    ScoreToken(int index,double score){
        this.index = index;
        this.score = score;
    }
}

class SCComparator implements Comparator<ScoreToken>{

    @Override
	public int compare(ScoreToken p1, ScoreToken p2) {
        if (p1 == null && p2 == null){
            return 0;
        }
        if (p1==null){
            return 1;
        }else if(p2 == null){
            return -1;
        }
        if (p1.score == p2.score){
            return 0;
        }else{
            return p1.score > p2.score ? -1 : 1;
        }
		
	}
}

class Space{
    public ArrayList<Advertisement> ads = new ArrayList<Advertisement>();
    public ArrayList<ArrayList<Advertisement>> next_ads = new ArrayList<ArrayList<Advertisement>>();
    public ArrayList<Double> bef_score = new ArrayList<Double>();
    public double best_score = 0.0;
    public ArrayList<Advertisement> best_ad;
    public double score = 0.0;
    private int num = 0;
    private Random rand = new Random();

    Space(int[][] ins){
        num = ins.length;
        for (int[] line : ins){
            ads.add(new Advertisement(line[0],line[1],line[2]));
            score += ads.get(ads.size()-1).get_satisf();
        }
        next_ads.add(ads);
        bef_score.add(0.0);
    }

    public NextState pick_next(int shift_width_max, int shift_width_min){
        int ad_ind = rand.nextInt(num);
        int vert_ind = rand.nextInt(6);
        int shift = rand.nextInt(shift_width_max-shift_width_min+1)+shift_width_min;
        if (rand.nextFloat() < 0.5){
            shift *= -1;
        }
        return new NextState(ad_ind,vert_ind,shift);
    }

    /*
    衝突の判定が甘い．片方がもう片方に完全に含まれるときを考慮できてないので．それを加えないと行けない．
    maxよりもminが右側にあればよい？
    */
    public boolean is_ads_collision_next(int[] shifted_pos,Advertisement ad2){
        int l_max = Math.max(shifted_pos[0],ad2.pos[0]);
        int r_min = Math.min(shifted_pos[2],ad2.pos[2]);
        int b_max = Math.max(shifted_pos[1],ad2.pos[1]);
        int u_min = Math.min(shifted_pos[3],ad2.pos[3]);

        if (l_max < r_min && b_max < u_min){
            return true;
        }else{
            return false;
        }
    }

    public boolean is_corner_valid(int[] corner){
        if (corner[0] > -1 && corner[2] < 10000 && corner[1] > -1 && corner[3] < 10000 &&
            corner[2]-corner[0] > 0 && corner[3]-corner[1] > 0){
                return true;
            }else{
                return false;
            }
    }

    public boolean is_ns_possible(NextState ns,ArrayList<Advertisement> ad){
        Advertisement ad1 = ad.get(ns.ad_ind);

        int[] shifted_pos = ad1.pos.clone();
        if (ns.vert_ind < 4){
        shifted_pos[ns.vert_ind] += ns.shift;
        }else if(ns.vert_ind == 4){
            shifted_pos[0] += ns.shift;
            shifted_pos[2] += ns.shift;
        }else if (ns.vert_ind == 5){
            shifted_pos[1] += ns.shift;
            shifted_pos[3] += ns.shift;
        }

        if (is_corner_valid(shifted_pos)){
            for (int i=0; i < num; ++i){
                if (i == ns.ad_ind){
                    continue;
                }else{
                    if (is_ads_collision_next(shifted_pos, ad.get(i))){
                        return false;
                    }
                }
            }
            return true;
        }else{
            return false;
        }
    }

    public ArrayList<NextState> generate_next(int shift_width_max,int shift_width_min,int num_states,ArrayList<Advertisement> ad){
        int count = 0;
        ArrayList<NextState> ret = new ArrayList<NextState>();
        for (int i=0; i < num_states*10; ++i){
            NextState ns = pick_next(shift_width_max,shift_width_min);
            if (is_ns_possible(ns,ad)){
                ret.add(ns.clone());
                count += 1;
            }
            if (count == num_states){
                return ret;
            }
        }
        return ret;
    }

    public void print_best(){
        ArrayList<Advertisement>adv = best_ad;
        // double tot_score = 0;
        for (Advertisement tp_ad:adv){
            System.out.println(String.valueOf(tp_ad.pos[0]) + " "+String.valueOf(tp_ad.pos[1]) + " "+String.valueOf(tp_ad.pos[2]) + " "+String.valueOf(tp_ad.pos[3]));
            // tot_score += tp_ad.get_satisf();
        }
        // System.out.println(tot_score);
    }

    public double calc_score(){
        ArrayList<Advertisement>adv = best_ad;
        double tot_score = 0;
        for (Advertisement tp_ad:adv){
            tot_score += tp_ad.get_satisf();
        }
        return tot_score/num;
    }

    public boolean is_improv(NextState ns,ArrayList<Advertisement> adv){
        if (is_ns_possible(ns, adv)){
            double diff = adv.get(ns.ad_ind).diff_satisf(ns);
            if (diff > 0){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public void greedy_step(){
        ArrayList<Advertisement> adv = best_ad;
        for (int i = 0; i < num; ++i){
            Advertisement tp_ad = adv.get(i);
            int vert_ind = 0;
            for (int j = 0; j < 100; ++j){
                NextState ns1 = new NextState(i,vert_ind,1);
                NextState ns2 = new NextState(i,vert_ind,-1);
                if (is_improv(ns1, adv)){
                    adv.get(ns1.ad_ind).apply_ns(ns1);
                }else if (is_improv(ns2, adv)){
                    adv.get(ns2.ad_ind).apply_ns(ns2);
                }else {
                    vert_ind += 1;
                }
                if (vert_ind == 4){
                    break;
                }
            }
        }
    }

    public boolean step(int ns_num, int state_num,int shift_width_max,int shift_width_min,double minus_th,boolean update_best){
        ArrayList<ArrayList<Advertisement>> ads_buf = new ArrayList<ArrayList<Advertisement>>();
        ArrayList<ScoreToken> score_buf = new ArrayList<ScoreToken>();


        for (int i = 0; i < next_ads.size(); ++i){
            ArrayList<Advertisement> adv = next_ads.get(i);
            double bef_s = bef_score.get(i);
            ArrayList<NextState> nses = generate_next(shift_width_max,shift_width_min,ns_num, adv);
            for (NextState ns:nses){
                double diff = adv.get(ns.ad_ind).diff_satisf(ns);
                if (diff >= -1e-12){
                    ads_buf.add(new ArrayList<Advertisement>());
                    ArrayList<Advertisement> tp = ads_buf.get(ads_buf.size()-1);
                    for (Advertisement tp_ad :adv){
                        tp.add(tp_ad);
                    }
                    Advertisement tadv = tp.get(ns.ad_ind).clone();
                    diff = tadv.apply_ns(ns);
                    tp.set(ns.ad_ind, tadv);
                    score_buf.add(new ScoreToken(score_buf.size(),diff+bef_s));
                }else{
                    if (rand.nextDouble() < minus_th){
                        ads_buf.add(new ArrayList<Advertisement>());
                        ArrayList<Advertisement> tp = ads_buf.get(ads_buf.size()-1);
                        for (Advertisement tp_ad :adv){
                            tp.add(tp_ad);
                        }
                        Advertisement tadv = tp.get(ns.ad_ind).clone();
                        diff = tadv.apply_ns(ns);
                        tp.set(ns.ad_ind, tadv);
                        score_buf.add(new ScoreToken(score_buf.size(),diff+bef_s));
                    }
                }
                
                
            }
        } 
        

        if (ads_buf.size()==0){
            return true;
        }

        score_buf.sort(new SCComparator());

        best_ad = ads_buf.get(score_buf.get(0).index);

        next_ads = new ArrayList<ArrayList<Advertisement>>();
        bef_score = new ArrayList<Double>();

        // if (ads_buf.size() < ){
        //     int ind = score_buf.get(0).index;
        //     next_ads.add(ads_buf.get(ind));
        //     bef_score.add(score_buf.get(ind).score);
        //     return true;
        // }

        for (int i = 0; i < Math.min(state_num,ads_buf.size()) ; ++i){
            int ind = score_buf.get(i).index;
            next_ads.add(ads_buf.get(ind));
            bef_score.add(score_buf.get(i).score);
        }
        return false;

    }
    
}

public class Main{
    public static void main(String args[]){
        long startTime = System.currentTimeMillis();
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int [][] adparm = new int[n][3];
        for (int i = 0; i < n; ++i){
            adparm[i][0] = sc.nextInt();
            adparm[i][1] = sc.nextInt();
            adparm[i][2] = sc.nextInt();
        }
        sc.close();
        ArrayList<Advertisement> best_ad = new ArrayList<Advertisement>();
        double best_score = 0.0;
        Space sp = new Space(adparm);

        // long anchor = System.currentTimeMillis();

        // for (int j = 0; j < 100000000; ++j){
            
        //     if (System.currentTimeMillis()-anchor > 4500){
        //         break;
        //     }else if (System.currentTimeMillis()-anchor > 500){
        //         // if(sp.step(20,3,100,0.1,false)){break;}
        //         // sp.greedy_step();
        //         // if(sp.step(20,10,100,10,1.0,false)){break;}
        //         break;
        //         // if(sp.step(10,3,30,10,1.0,false)){break;}
        //     }else{
        //         if(sp.step(20,3,150,100,0.0,false)){break;}
        //     }
        //     if (j % 100 == 0){
        //         System.out.println(String.valueOf(j)+" "+String.valueOf(sp.calc_score()));
        //     }
        // }
        // for (int i = 0; i < 10; ++i){sp.greedy_step();}
        
        // // System.out.println(String.valueOf(sp.calc_score()));
        // sp.print_best();

        for (int i = 0; i < 7; ++i){
            long anchor = System.currentTimeMillis();
            if (i > 0){sp = new Space(adparm);}
            
            for (int j = 0; j < 1000000; ++j){
                if (System.currentTimeMillis()-anchor > 501){
                    break;
                }else{
                    if(sp.step(20,3,150,100,0.0,false)){break;}
                }
            }
            double now_score = sp.calc_score();
            if (now_score > best_score){
                best_score = now_score;
                best_ad = new ArrayList<Advertisement>();
                for (Advertisement adddv : sp.best_ad){
                    best_ad.add(adddv.clone());
                }
            }
        }

        int loops = 0;

        sp.next_ads = new ArrayList<ArrayList<Advertisement>>();
        sp.next_ads.add(best_ad);

        for (int j = 0; j < 1000000; ++j){
            if (System.currentTimeMillis()-startTime > 4750){
                loops = j;
                break;
            }else if (System.currentTimeMillis()-startTime > 4500){
                sp.greedy_step();
            }else{
                if(sp.step(25,5,50,1,0.5,false)){break;}
            }
        }
        
        sp.print_best();
        // System.out.println(System.currentTimeMillis()-startTime);
        // System.out.println(loops);

        
    }
}