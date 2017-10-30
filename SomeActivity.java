....
/**
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */
public class CallActivity extends ActionBarActivity implements OnSipRequestListener{

    public static final String TAG = "CallActivity: ";

    private boolean mIsActivityCanceled = false;
    public static final int TARIFFING_QUANTUM = 5;
    public static final int TIMER_HOT_FINISH_SEC = 4;

....
    // Sip
    private SipStack sipStack;

    private void breakCallByUser() {
        vibrator.vibrate(AppConf.VIBRO_SHORT);
        mTimerHotFinish.startExt();
        if (AppConf.isOnline(SigmaApp.getContext())){
            stopSipCall();
        } else {
            stopTimerTask();
            finish();
        }
    }

    private void stopSipCall() {
        sipStack.drop_call();
    }

    private void startSipCall() {
        String usr = conf.getPhone();
        String psw = conf.getSipPassword();

        SipProfile sipProfile = new SipProfile();
        sipProfile.setUsr(usr);
        sipProfile.setPsw(psw);

        sipStack = new SipStack(this, sipProfile);
        // Listen to requests like BYE, RINGING, PROCESS
        sipStack.setmOnSipRequestListener(this);

        // Make call
        if (sipStack.getState() != SipStack.SipState.ERROR_INIT) {
            String targetNum = formatter.clearNum(tvPhone.getText().toString());
            sipStack.invite(targetNum);
        } else finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mIsActivityCanceled) return;

        mTimerHotFinish.cancel();
        stopPreTimer();

        if (secondsCount > 0) {
            int secCountRounded = roundToQuantum(secondsCount);
            float callCost = callPrice * secCountRounded / 60;
            ds.updateHistory(hId, secCountRounded, callCost);
            conf.setOldBalance(preCallBalance);
            conf.setLastHid(hId);
            MainActivity.sendBalanceUpdate();
        } else {
            // Register zero-length call: "callZero" proxy method
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new ZeroCallEvent(SigmaApp.getContext());
                }
            });
        }

        secondsCount = 0;

        MainActivity.sendHistoryUpdate();

        if(wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) wakeUp();

        sipStack.clear();
        sipStack = null;
    }

    private int roundToQuantum(int seconds) {
        float multiply = (float) seconds / (float) TARIFFING_QUANTUM;
        int ceil = (int) Math.ceil(multiply);
        int quantumized = ceil * TARIFFING_QUANTUM;
        return quantumized;
    }

    private void wakeUp() {
        SigmaApp.log("JellyBean Wake Up", TAG);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "WakeUp");
        wl.acquire();
        wl.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

....
    // Call tones generator
    private ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100);
    Handler mToneHandler = new Handler();
    Runnable mToneTask = new Runnable() {
        @Override
        public void run() {
            mToneGenerator.startTone(ToneGenerator.TONE_CDMA_DIAL_TONE_LITE, 1000);
            mToneHandler.postDelayed(mToneTask, 3000);
        }
    };
    private void startToneTask(){
        SigmaApp.log("Start tone", TAG);
        mToneHandler.post(mToneTask);
    }
    private void stopToneTask() {
        SigmaApp.log("Stop tone", TAG);
        mToneHandler.removeCallbacks(mToneTask);
    }
    private void playBusyTone() {
        SigmaApp.log("Busy tone", TAG);
        vibrator.vibrate(AppConf.VIBRO_SHORT);
        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_REORDER, 2500);
    }

    private void offlineStopCallByTimer() {
        SigmaApp.log("Offline stop call by timer", TAG);
        stopTimerTask();
        stopToneTask();
        playBusyTone();
        finish();
    }

    private abstract class CDTimer extends CountDownTimer {
        public boolean isRunning;

        public CDTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            isRunning = false;
        }

        public void startExt() {
            if (!isRunning) {
                this.start();
                isRunning = true;
            }
        }
    }
    private CDTimer mTimerHotFinish = new CDTimer(TIMER_HOT_FINISH_SEC * 1000, 1000) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            SigmaApp.log("Hot timer finished! Kill CallActivity", TAG);
            offlineStopCallByTimer();
        }
    };

    private Handler mPreTimerHandler = new Handler();
    private Runnable mPreTimerRoutine = new Runnable() {
        @Override
        public void run() {
            if (!AppConf.isOnline(SigmaApp.getContext())) {
                offlineStopCallByTimer();
            }
            mPreTimerHandler.postDelayed(mPreTimerRoutine, 1000);
        }
    };
    private void startPreTimer() {
        SigmaApp.log("Start PreTimer", TAG);
        mPreTimerHandler.post(mPreTimerRoutine);
    }
    private void stopPreTimer() {
        SigmaApp.log("Stop PreTimer", TAG);
        mPreTimerHandler.removeCallbacks(mPreTimerRoutine);
    }


    private void showRateDialog() {
        AppConf.alertDialog(CallActivity.this, getString(R.string.ui_information),
                getString(R.string.ui_rate_app), R.drawable.ic_contacts, null, 12,
                getString(R.string.ui_rate), new BtnCallBack() {
                    @Override
                    public void onBtnClick() {
                        conf.setNoMoreRateApp(true);//Больше не показывать - уже оценивают
                        conf.setBeginDateRateApp(0); //Сброс даты оценки
                        MainActivity.getInstance().rateApp();
                        inCheck = false;
                    }
                }, getString(R.string.ui_remind), new BtnCallBack() {
                    @Override
                    public void onBtnClick() {
                        //Напомнить через 5 дней
                        conf.setNoMoreRateApp(false);//сбрасываем флаг не показывать
                        Calendar today = Calendar.getInstance();
                        conf.setBeginDateRateApp(today.getTimeInMillis()); //устанавливаем дату отсчета - сейчас
                        inCheck = false;
                    }
                }, getString(R.string.ui_no_more), new BtnCallBack() {
                    @Override
                    public void onBtnClick() {
                        conf.setNoMoreRateApp(true);
                        inCheck = false;
                    }
                });
    }

    // PROXY COMMUNICATIONS
    long callId;
    int maxDuration;
    float preCallBalance = 0;

    public static final int timerInterval = 1000; // Always seconds *000
    private int secondsCount = 0;

    // Conversation stage timer task (main)
    private Handler timerHandler = new Handler();

    static boolean inCheck = false;

    Runnable mTimerTarget = new Runnable() {
        @Override
        public void run() {
            if (!AppConf.isOnline(SigmaApp.getContext())) {
                offlineStopCallByTimer();
                return;
            }

            if (maxDuration <= secondsCount) {
                finishing = false;
                sipStack.drop_call();

                AppConf.alertDialog(CallActivity.this,
                        getString(R.string.ui_warning),
                        getString(R.string.ui_error_empty_balance),
                        R.drawable.ic_info, null, 0,
                        getString(R.string.ui_topup), new BtnCallBack() {
                            @Override
                            public void onBtnClick() {
                                finishing = true;
                                CallActivity.this.finish();
                                Intent intent = new Intent(CallActivity.this, TopUpActivity.class);
                                startActivity(intent);
                            }
                        }, getString(R.string.ui_cancel), new BtnCallBack() {
                            @Override
                            public void onBtnClick() {
                                finishing = true;
                                CallActivity.this.finish();
                            }
                        }, null, null);

                return;
            }
            AppConf.log("Tick! Sec: " + secondsCount);

            secondsCount += timerInterval / 1000;
            tvCallTimer.setText(NumFormat.timerString(secondsCount));
            int tickInterval = 5;
            if (secondsCount % tickInterval == 0) new tickEvent(SigmaApp.getContext(), false);
            timerHandler.postDelayed(mTimerTarget, timerInterval);
        }
    };
    void startTimerTask() {
        AppConf.log("Timer start!");
        stopPreTimer();
        tvCallTimer.setText(NumFormat.timerString(secondsCount));
        secondsCount = 1;
        timerHandler.postDelayed(mTimerTarget, timerInterval);
    }
    int stopTimerTask() {
        int tmpSec = secondsCount;
        timerHandler.removeCallbacks(mTimerTarget);
        return tmpSec;
    }

    private void askTariff(String cleanPhone) {
        Uri.Builder b = AppConf.getBuilder();
        b.appendPath("getTariff")
                .appendQueryParameter("sessionId", conf.getSessId())
                .appendQueryParameter("login", conf.getPhone())
                .appendQueryParameter("phoneNum", cleanPhone);
        String url = b.build().toString();
        TariffRequest tariffRequest = new TariffRequest(this, false);
        tariffRequest.execute(url);
    }

    // Sip requests listener

    private enum CallState {
        TRYING, RINGING, TALKING, IDLE
    }

    CallState mCallState = CallState.IDLE;

    public void setCallState(CallState mCallState) {
        this.mCallState = mCallState;
    }

    public CallState getCallState() {
        return mCallState;
    }

    @Override
    public void OnBye() {
        SigmaApp.log("BUY received set stopCallEvent", TAG);
        playBusyTone();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new stopCallEvent(SigmaApp.getContext(), false);
            }
        });
    }

    @Override
    public void OnRinging() {
        if (getCallState() != CallState.RINGING) {
            setCallState(CallState.RINGING);
            startToneTask();
        }
    }

    @Override
    public void OnBusy() {
        SigmaApp.log("OnBusy()", TAG);
        stopToneTask();
        playBusyTone();
        finish();
    }

    @Override
    public void OnAnswer() {
        stopToneTask();
        setCallState(CallState.TALKING);
        // Tell proxy about call
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new startCallEvent(SigmaApp.getContext(), false);
            }
        });
    }


    boolean finishing = true;

    @Override
    public void OnFinish() {
        SigmaApp.log("Call finished by user", TAG);
        stopToneTask();
        if (mCallState == CallState.TALKING) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new stopCallEvent(SigmaApp.getContext(), false);
                }
            });
        } else { // CANCEL
            playBusyTone();
            if (finishing) {
                finish();
            }
        }
    }

    private class TariffRequest extends HttpTask {

        public TariffRequest(Context c, boolean showDialog) {
            super(c, showDialog);
        }

        @Override
        public void processRespond(SigmaRespond r) {
            if (r.ok()){
                try {
                    JSONObject json = new JSONObject(r.text);

                    if (json.has(AppConf.JSON_RATE)) {
                        callPrice = Float.parseFloat(json.getString(AppConf.JSON_RATE));
                    } else {
                        callPrice = 0.f;
                    }

                    if (json.has(AppConf.JSON_MAXDUR)) {
                        maxDuration = json.getInt(AppConf.JSON_MAXDUR);
                    } else {
                        maxDuration = 0;
                    }

                    if (json.has(AppConf.JSON_BALANCE)) {
                        preCallBalance = Float.parseFloat(json.getString(AppConf.JSON_BALANCE));
                    } else {
                        preCallBalance = 0.f;
                    }

                    SigmaApp.log("MaxDuration: " + maxDuration + " PreCallBal: " + preCallBalance, TAG);

                    if (maxDuration < 5) {
                        finishing = false;
                        sipStack.drop_call();

                        AppConf.alertDialog(CallActivity.this,
                                getString(R.string.ui_warning),
                                getString(R.string.ui_error_empty_balance),
                                R.drawable.ic_info, null, 0,
                                getString(R.string.ui_topup), new BtnCallBack() {
                                    @Override
                                    public void onBtnClick() {
                                        finishing = true;
                                        CallActivity.this.finish();
                                        Intent intent = new Intent(CallActivity.this, TopUpActivity.class);
                                        startActivity(intent);
                                    }
                                }, getString(R.string.ui_cancel), new BtnCallBack() {
                                    @Override
                                    public void onBtnClick() {
                                        finishing = true;
                                        CallActivity.this.finish();
                                    }
                                }, null, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ZeroCallEvent extends HttpTask {
        public ZeroCallEvent(Context c) {
            super(c, false);

            String sessId = conf.getSessId();
            String log = conf.getPhone();
            String psw = conf.getSipPassword();
            String targetNum = formatter.clearNum(tvPhone.getText().toString());

            if (targetNum.length() <= 0) {
                MainActivity.showToast(c, getString(R.string.ui_error_phone_number));
                return;
            }

            Uri.Builder b = AppConf.getBuilder();
            b.appendPath(AppConf.METH_CALL_ZERO)
                    .appendQueryParameter(AppConf.REQ_SESS_ID, sessId)
                    .appendQueryParameter(AppConf.REQ_LOGIN, log)
                    .appendQueryParameter(AppConf.REQ_PSW, psw)
                    .appendQueryParameter(AppConf.REQ_PHONENUM, targetNum);

            String url = b.build().toString();
            this.execute(url);
        }

        @Override
        public void processRespond(SigmaRespond r) {}
    }

    private class startCallEvent extends HttpTask {

        public startCallEvent(Context c, boolean showDialog) {
            super(c, showDialog);

            String sessId = conf.getSessId();
            String log = conf.getPhone();
            String psw = conf.getSipPassword();
            String targetNum = formatter.clearNum(tvPhone.getText().toString());

            if (targetNum.length() <= 0) {
                MainActivity.showToast(c, getString(R.string.ui_error_phone_number));
                return;
            }

            Uri.Builder b = AppConf.getBuilder();
            b.appendPath("callStart")
                    .appendQueryParameter("sessionId", sessId)
                    .appendQueryParameter("login", log)
                    .appendQueryParameter("password", psw)
                    .appendQueryParameter("phonenum", targetNum);

            String url = b.build().toString();

            this.execute(url);
        }

        @Override
        public void processRespond(SigmaRespond r) {
            try {
                JSONObject json = new JSONObject(r.text);
                callId = json.getLong(AppConf.JSON_CALLID);
                System.out.println("");
                startTimerTask();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class stopCallEvent extends HttpTask {

        public stopCallEvent(Context c, boolean showDialog) {
            super(c, showDialog);

            String sessId = conf.getSessId();
            String log = conf.getPhone();
            String psw = conf.getSipPassword();

            int dur = stopTimerTask();

            Uri.Builder b = AppConf.getBuilder();
            b.appendPath("callFinish2")
                    .appendQueryParameter("sessionId", sessId)
                    .appendQueryParameter("login", log)
                    .appendQueryParameter("password", psw)
                    .appendQueryParameter("callId", String.valueOf(callId))
                    .appendQueryParameter("duration", String.valueOf(roundToQuantum(dur)));

            String url = b.build().toString();
            this.execute(url);
        }

        @Override
        public void processRespond(SigmaRespond r) {
            maxDuration = 0;
            callId = 0;
            finish();
        }
    }

    private class tickEvent extends HttpTask {

        public tickEvent(Context c, boolean showDialog) {
            super(c, showDialog);

            String sessId = conf.getSessId();
            String log = conf.getPhone();


            Uri.Builder b = AppConf.getBuilder();
            b.appendPath("callTick")
                    .appendQueryParameter("sessionId", sessId)
                    .appendQueryParameter("login", log)
                    .appendQueryParameter("callId", String.valueOf(callId))
                    .appendQueryParameter("duration", String.valueOf(secondsCount));

            String url = b.build().toString();
            this.execute(url);
        }

        @Override
        public void processRespond(SigmaRespond r) {

        }
    }
}