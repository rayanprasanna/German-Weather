package com.universl.kalagune.germanweather;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.universl.kalagune.germanweather.utils.CommonUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class NowFragment extends Fragment {
    public TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    TextView todayPreText;
    static  String temp;
    ImageView statusImage;
    TextView todayIcon;
    View mview; Typeface weatherFont;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private BroadcastReceiver broadcastReceiver;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public NowFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NowFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NowFragment newInstance(String param1, String param2) {
        NowFragment fragment = new NowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public static String des,w,p,h,sr,ss,ti;
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        if (broadcastReceiver == null) {
//            broadcastReceiver=new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    temp=intent.getExtras().get("temp").toString();
//                    des=intent.getExtras().get("des").toString();
//                    w=intent.getExtras().get("w").toString();
//                    p=intent.getExtras().get("p").toString();
//                    h=intent.getExtras().get("h").toString();
//                    sr=intent.getExtras().get("sr").toString();
//                    ss=intent.getExtras().get("ss").toString();
//                    ti=intent.getExtras().get("ti").toString();
//
//                }
//            };
//        }
//        if(temp!=null){
////            Toast.makeText(getActivity(),"Not Null",Toast.LENGTH_SHORT).show();
//            setFinformation(temp,des,w,p,h,sr,ss,ti);
//        }else {
////            Toast.makeText(getActivity(),"Null Data",Toast.LENGTH_SHORT).show();
//        }
//
//       // todayTemperature.setText(temp);
//       // todayDescription.setText(des);
//       // todayHumidity.setText(h);
//       // todayWind.setText(w);
//       // todayPressure.setText(p);
//       // todaySunrise.setText(sr);
//      //  todaySunset.setText(ss);
//       // todayIcon.setText(ti);
//        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("weather updates"));
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mview= inflater.inflate(R.layout.fragment_now, container, false);
        todayTemperature = mview.findViewById(R.id.todayTemperature);
        todayDescription = mview.findViewById(R.id.todayDescription);
        todayWind = mview.findViewById(R.id.todayWind);
        todayPressure =mview.findViewById(R.id.todayPressure);
        todayHumidity = mview.findViewById(R.id.todayHumidity);
        todaySunrise = mview.findViewById(R.id.todaySunrise);
        todaySunset = mview.findViewById(R.id.todaySunset);
        lastUpdate = mview.findViewById(R.id.lastUpdate);
        todayIcon = mview.findViewById(R.id.todayIcon);
        statusImage = mview.findViewById(R.id.statusImage);
        todayPreText= mview.findViewById(R.id.toDayPer);
        weatherFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);
        return mview;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize textboxes

    }

    @Override
    public void onStart() {
        super.onStart();
        setInformation();
    }

    public void setInformation() {
        SharedPreferences sharedpref=getActivity().getSharedPreferences("weather", Context.MODE_PRIVATE);
        String temp1=sharedpref.getString("temp","");
        String des1=sharedpref.getString("des","");
        // Toast.makeText(getActivity(),temp1+""+des1,Toast.LENGTH_SHORT).show();

        String w1=sharedpref.getString("w","");
        String p1=sharedpref.getString("p","");
        String h1=sharedpref.getString("h","");
        String sr1=sharedpref.getString("sr","");
        String ss1=sharedpref.getString("ss","");
        String tl1=sharedpref.getString("tl","");
        String pre=sharedpref.getString("pre","");

        setFinformation(temp1,des1,w1,p1,h1,sr1,ss1,tl1,pre);
    }

    private void setFinformation(String temp1, String des1, String w1, String p1, String h1, String sr1, String ss1, String tl1,String todayPre) {
        todayTemperature.setText(temp1);
        //todayTemperature.setTextColor(Color.RED);
        todayDescription.setText(des1);
        todayHumidity.setText(h1);
        todayWind.setText(w1);
        todayPressure.setText(p1);
        todaySunrise.setText(sr1);
        todaySunset.setText(ss1);
        todayPreText.setText(todayPre);
        //todayIcon.setText(tl1);
        statusImage.setImageDrawable(getResources().getDrawable(CommonUtils.getStatusImage(getContext(),tl1)));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    String tmp;
    public static   void setWeather(String t, String d, String w, String p, String h, String sr, String ss, String ti) {
        //todayTemperature.setText(t);
        //ola();
    }

    private  void ola() {
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
